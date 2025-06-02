package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Safe Interval Path Planning (SIPP) implementation of the A* algorithm for single-agent pathfinding.
 * <p>
 *     Based on the paper:
 *     <i>Phillips, Mike, and Maxim Likhachev. "Sipp: Safe interval path planning for dynamic environments." 2011 IEEE international conference on robotics and automation. IEEE, 2011.</i>
 */
public class SingleAgentAStarSIPP_Solver extends SingleAgentAStar_Solver {
    private static final List<TimeInterval> DEFAULT_SINGLETON_LIST_OF_INF_INTERVAL = Collections.singletonList(TimeInterval.DEFAULT_INTERVAL);

    private Map<I_Location, List<TimeInterval>> sortedSafeIntervalsByLocation;

    public SingleAgentAStarSIPP_Solver() {
        super();
        super.name = "SIPP";
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        if (runParameters instanceof RunParameters_SAAStarSIPP parameters && parameters.safeIntervalsByLocation != null) {
            this.sortedSafeIntervalsByLocation = parameters.safeIntervalsByLocation;
        } else {
            this.sortedSafeIntervalsByLocation = this.constraints.vertexConstraintsToSortedSafeTimeIntervals(this.agent, this.map);
        }

        if (goalCondition instanceof VisitedTargetAStarGoalCondition) {
            throw new IllegalArgumentException(goalCondition.getClass().getSimpleName() + " not currently supported in " + this.getClass().getSimpleName());
        }
    }

    @Override
    protected @NotNull I_OpenList<AStarState> createEmptyOpenList() {
        // todo - testing shows SIPP expands significantly more nodes with the bucketing open list, so disabled until I find why.
        return new OpenListTree<>(super.stateComparator);
    }

    @Override
    protected boolean initOpen() {
        // if the existing plan isn't empty, we start() from the last move of the existing plan.
        if (existingPlan.size() > 0) {
            Move lastExistingMove = existingPlan.moveAt(existingPlan.getEndTime());
            // We assume that we cannot change the existing plan, so if it is rejected by constraints, we can't initialise OPEN.
            if (constraints.rejects(lastExistingMove)) {
                return false;
            }
            // Get the total cost of the existing plan, replace `existingPlanTotalCost` with your calculation
            int existingPlanTotalCost = existingPlan.getCost();

            // Find the time interval for the last move
            List<TimeInterval> intervals = getIntervalsForLocation(lastExistingMove.currLocation);
            TimeInterval lastMoveInterval = null;
            for (TimeInterval interval : intervals) {
                if (interval.start() <= lastExistingMove.timeNow && interval.end() >= lastExistingMove.timeNow) {
                    lastMoveInterval = interval;
                    break;
                }
            }
            addToOpenList(new AStarSIPPState(lastExistingMove, null, existingPlanTotalCost, 0, lastMoveInterval, visitedTarget(null, existingPlan.containsTarget())));

        } else { // the existing plan is empty (no existing plan)
            I_Location sourceLocation = map.getMapLocation(this.sourceCoor);

            List<I_Location> neighborLocations = new ArrayList<>(sourceLocation.outgoingEdges());
            if (Objects.equals(sourceLocation.getCoordinate(), this.targetCoor)) {
                neighborLocations.add(sourceLocation);
            }

            for (I_Location destination : neighborLocations) {
                Move possibleMove = new Move(agent, problemStartTime + 1, sourceLocation, destination);
                AStarSIPPState rootState = new AStarSIPPState(possibleMove, null, getG(null, possibleMove), 0, null, visitedTarget(null, isMoveToTarget(possibleMove)));
                moveToNeighborLocation(rootState, possibleMove, true);
            }
        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    private List<TimeInterval> getIntervalsForLocation(I_Location location) {
        return sortedSafeIntervalsByLocation.getOrDefault(location, DEFAULT_SINGLETON_LIST_OF_INF_INTERVAL);
    }

    // todo override SingleAgentAStar_Solver.generate() and use that instead of directly using AStarSIPPState::new and addToOpenList

    @Override
    public void expand(@NotNull AStarState state) {
        assert state.move != null;
        if (state.move.currLocation.getType() == Enum_MapLocationType.NO_STOP) {
            throw new RuntimeException("UnsupportedOperationException");
        }
        expandedNodes++;
        // can move to neighboring locations
        List<I_Location> neighborLocations = new ArrayList<>(state.move.currLocation.outgoingEdges());
        boolean afterLastConstraint = state.move.timeNow > constraints.getLastConstraintStartTime();

        for (I_Location destination : neighborLocations) {
            Move possibleMove = new Move(state.move.agent, !afterLastConstraint ? state.move.timeNow + 1 : state.move.timeNow,
                    state.move.currLocation, destination);
            moveToNeighborLocation((AStarSIPPState) state, possibleMove, false);
        }
    }

    /**
     * Generates a child state based on the move, current state, interval, and initialization state.
     *
     * @param move     The move to be made.
     * @param state    The current state.
     * @param interval The interval in which the move is being made.
     * @param init     Flag indicating if this is an initial state or not.
     * @return A new child AStarSIPPState based on the provided parameters.
     */
    private AStarSIPPState generateChildState(Move move, AStarSIPPState state, TimeInterval interval, boolean init) {
        // todo can we have a more accurate counting of conflicts? right now only counts the number of conflicts in the move into the interval.
        //  maybe we can add the number of conflicts in the interval itself once we know the next move? And what to do about the first state?
        if (init) {
            return new AStarSIPPState(move, null, getG(null, move), 0, interval, visitedTarget(null, isMoveToTarget(move)));
        }
        return new AStarSIPPState(move, state, getG(state, move), state.conflicts + numConflicts(move, false) // todo IsALastMove handling for TMAPF support
                , interval, visitedTarget(state, isMoveToTarget(move)));
    }

    /**
     * Tries to move into the safe intervals (separate states) of a neighboring location based on the current state and a possible move.
     *
     * @param state        The current state.
     * @param possibleMove The move to be checked.
     * @param init         Flag indicating if this is an initial state or not.
     */
    private void moveToNeighborLocation(AStarSIPPState state, Move possibleMove, boolean init) {
        I_Location prevLocation = possibleMove.prevLocation;
        I_Location neighborLocation = possibleMove.currLocation;

        // Retrieve safe intervals for the current location
        List<TimeInterval> sortedSafeIntervalsNeighborLocation = getIntervalsForLocation(neighborLocation);
        int earliestMoveTime = possibleMove.timeNow;
        TimeInterval prevLocationRelevantInterval = init ? getIntervalsForLocation(prevLocation).get(0) : state.timeInterval;
        int latestMoveTime = prevLocationRelevantInterval.end() == Integer.MAX_VALUE ? Integer.MAX_VALUE // integer overflow guard
                : prevLocationRelevantInterval.end() + 1;

        int startIndex = getSafeIntervalsListIterationStartIndex(sortedSafeIntervalsNeighborLocation, earliestMoveTime);
        // Iterate through the intervals starting from the first relevant time
        for (int i = startIndex; i < sortedSafeIntervalsNeighborLocation.size(); i++) {
            TimeInterval interval = sortedSafeIntervalsNeighborLocation.get(i);
            if (earliestMoveTime <= interval.end() && latestMoveTime >= interval.start()) {
                moveIntoSafeInterval(state, possibleMove, init, prevLocation, neighborLocation, prevLocationRelevantInterval, interval);
            } else if (latestMoveTime < interval.start()) { // no need to check later intervals
                break;
            }
        }
    }

    private int getSafeIntervalsListIterationStartIndex(List<TimeInterval> safeIntervalsCurrLocation, int nextMoveStartTime) {
        int startIndex = 0;
        // for larger lists of safe intervals, use binary search to find the first relevant interval quickly
        if (safeIntervalsCurrLocation.size() >= 10 && nextMoveStartTime > safeIntervalsCurrLocation.get(0).end()) {
            startIndex = Collections.binarySearch(safeIntervalsCurrLocation, null,
                    (interval, k) -> {
                        if (interval.start() <= nextMoveStartTime && interval.end() >= nextMoveStartTime) return 0;
                        return Integer.compare(interval.start(), nextMoveStartTime);
                    });
            if (startIndex < 0) startIndex = -startIndex - 1;
        }
        return startIndex;
    }

    /**
     * Moves into a safe interval, creating child states as necessary.
     * TODO - This method needs to not generate the "wait" states between moves states. It shouldn't be done in SIPP.
     * @param state                      The current state.
     * @param possibleMove               The move to be checked.
     * @param init                       Flag indicating if this is an initial state or not.
     * @param prevLocation               The previous location from where the move starts.
     * @param intervalLocation               The current/target location of the move.
     * @param prevLocationRelevantInterval The relevant interval of the previous location.
     * @param interval               The current interval being considered.
     */
    private void moveIntoSafeInterval(AStarSIPPState state, Move possibleMove, boolean init, I_Location prevLocation,
                                      I_Location intervalLocation, TimeInterval prevLocationRelevantInterval, TimeInterval interval) {
        AStarSIPPState child = state;
        int possibleMoveTime;
        boolean afterLastConstraint;
        Move samePossibleMove = null;

        // If the move is accepted and is within the current interval, create a child state and add to the open list
        if ((constraints.accepts(possibleMove)) && (possibleMove.timeNow >= interval.start())) {
            child = generateChildState(possibleMove, state, interval, init);
            addToOpenList(child);
            return;
        }

        // Reset the move to start with waiting at the previous location
        possibleMove = new Move(child.move.agent, possibleMove.timeNow, prevLocation, prevLocation);
        while (possibleMove.timeNow <= prevLocationRelevantInterval.end()) {
            if (!constraints.accepts(possibleMove)) return;

            // Generate child state based on the possible move
            if (init) {
                child = generateChildState(possibleMove, child, prevLocationRelevantInterval, init);
                init = false;
            } else child = generateChildState(possibleMove, child, prevLocationRelevantInterval, false);

            afterLastConstraint = child.move.timeNow > constraints.getLastConstraintStartTime();
            possibleMoveTime = !afterLastConstraint ? possibleMove.timeNow + 1 : child.move.timeNow;

            // If the move time is within or right before the current interval, create the move and check constraints
            if (possibleMove.timeNow >= interval.start() - 1) {
                possibleMove = new Move(child.move.agent, possibleMoveTime, prevLocation, intervalLocation);
                if (constraints.accepts(possibleMove)) {
                    child = generateChildState(possibleMove, child, interval, false);
                    addToOpenList(child);
                    return;
                }
                if (possibleMove.equals(samePossibleMove)) return;
                samePossibleMove = possibleMove;
            }

            // Update the move to increment the time for the same location
            possibleMove = new Move(child.move.agent, possibleMoveTime, prevLocation, prevLocation);
        }
    }

    public class AStarSIPPState extends AStarState {
        private final TimeInterval timeInterval;

        public AStarSIPPState(Move move, AStarSIPPState prev, int g, int conflicts, TimeInterval timeInterval, boolean visitedTarget) {
            super(move, prev, g, conflicts, visitedTarget, false); // todo add support for isALastMove for TMAPF
            this.timeInterval = timeInterval;
        }

        @Override
        public boolean equals(Object o) { // todo visited target to support TMAPF. Maybe just get from super and expend? Also, what about visited target in the comparator?
            if (this == o) return true;
            if (!(o instanceof AStarSIPPState that)) return false;

            if (timeInterval.start() != that.timeInterval.start()) return false;
            assert move != null;
            assert that.move != null;
            return move.currLocation.equals(that.move.currLocation);
        }

        @Override
        public int hashCode() {
            assert move != null;
            int result = move.currLocation.hashCode();
            result = 31 * result + this.timeInterval.start();
            return result;
        }

        @Override
        protected @NotNull List<Move> getOrderedMoves() {
            // todo randomize the transition times
            return super.getOrderedMoves();

        }
    }
}

