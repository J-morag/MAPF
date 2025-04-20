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
    protected static final List<TimeInterval> DEFAULT_SINGLETON_LIST_OF_INF_INTERVAL = Collections.singletonList(TimeInterval.DEFAULT_INTERVAL);

    protected Map<I_Location, List<TimeInterval>> sortedSafeIntervalsByLocation;

    public SingleAgentAStarSIPP_Solver() {
        super();
        super.name = "SIPP";
    }

    public SingleAgentAStarSIPP_Solver(Comparator<AStarState> stateComparator) {
        super(stateComparator);
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
            addToOpenList(createNewState(lastExistingMove, null, existingPlanTotalCost, 0, lastMoveInterval, visitedTarget(null, existingPlan.containsTarget()), 0));

        } else { // the existing plan is empty (no existing plan)
            I_Location sourceLocation = map.getMapLocation(this.sourceCoor);

            List<I_Location> neighborLocations = new ArrayList<>(sourceLocation.outgoingEdges());
            if (Objects.equals(sourceLocation.getCoordinate(), this.targetCoor)) {
                neighborLocations.add(sourceLocation);
            }

            for (I_Location destination : neighborLocations) {
                addInitialNodesToOpen(destination, sourceLocation);
            }
        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    protected void addInitialNodesToOpen(I_Location destination, I_Location sourceLocation) {
        Move possibleMove = new Move(agent, problemStartTime + 1, sourceLocation, destination);
        AStarState rootState = createNewState(possibleMove, null, getG(null, possibleMove), 0, null, visitedTarget(null, isMoveToTarget(possibleMove)), 0);
        moveToNeighborLocation(rootState, possibleMove, true);
    }

    protected List<TimeInterval> getIntervalsForLocation(I_Location location) {
        return sortedSafeIntervalsByLocation.getOrDefault(location, DEFAULT_SINGLETON_LIST_OF_INF_INTERVAL);
    }

    protected AStarState createNewState(Move move, AStarState prev, int g, int conflicts, TimeInterval timeInterval, boolean visitedTarget, int intervalID) {
        boolean isLastMove = move.currLocation.getCoordinate().equals(move.agent.target) && (timeInterval != null && timeInterval.end() == Integer.MAX_VALUE); // todo - this will have to go through goalCondition to be more generic and specifically to support TMAPF
        return new AStarSIPPState(move, (AStarSIPPState) prev, g, conflicts, timeInterval, visitedTarget, isLastMove);
    }

    // todo override SingleAgentAStar_Solver.generate() and use that instead of directly using AStarSIPPState::new and addToOpenList

    @Override
    protected void expand(@NotNull AStarState state) {
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
            moveToNeighborLocation(state, possibleMove, false);
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
    protected AStarState generateChildState(Move move, AStarState state, TimeInterval interval, boolean init, int intervalID) {
        // todo - I think we always have isALastMove as false in SIPP. So we don't count conflicts resulting from staying at target.
        if (init) {
            return createNewState(move, null, getG(null, move), state.conflicts + numConflicts(move, false), interval, visitedTarget(null, isMoveToTarget(move)), intervalID);
        }
        return createNewState(move, state, getG(state, move), state.conflicts + numConflicts(move, false), interval, visitedTarget(state, isMoveToTarget(move)), intervalID);
    }

    /**
     * Tries to move into the safe intervals (separate states) of a neighboring location based on the current state and a possible move.
     *
     * @param state        The current state.
     * @param possibleMove The move to be checked.
     * @param init         Flag indicating if this is an initial state or not.
     */
    protected void moveToNeighborLocation(AStarState state, Move possibleMove, boolean init) {
        I_Location prevLocation = possibleMove.prevLocation;
        I_Location neighborLocation = possibleMove.currLocation;
        TimeInterval timeInterval = ((AStarSIPPState) state).timeInterval;

        // Retrieve safe intervals for the current location
        List<TimeInterval> sortedSafeIntervalsNeighborLocation = getIntervalsForLocation(neighborLocation);
        int earliestMoveTime = possibleMove.timeNow;
        TimeInterval prevLocationRelevantInterval = init ? getIntervalsForLocation(prevLocation).get(0) : timeInterval;
        int latestMoveTime = prevLocationRelevantInterval.end() == Integer.MAX_VALUE ? Integer.MAX_VALUE // integer overflow guard
                : prevLocationRelevantInterval.end() + 1;

        int startIndex = getSafeIntervalsListIterationStartIndex(sortedSafeIntervalsNeighborLocation, earliestMoveTime);
        // Iterate through the intervals starting from the first relevant time
        for (int i = startIndex; i < sortedSafeIntervalsNeighborLocation.size(); i++) {
            TimeInterval interval = sortedSafeIntervalsNeighborLocation.get(i);
            if (earliestMoveTime <= interval.end() && latestMoveTime >= interval.start()) {
                moveIntoSafeInterval(state, possibleMove, init, prevLocation, neighborLocation, prevLocationRelevantInterval, interval, i);
            } else if (latestMoveTime < interval.start()) { // no need to check later intervals
                break;
            }
        }
    }

    protected int getSafeIntervalsListIterationStartIndex(List<TimeInterval> safeIntervalsCurrLocation, int nextMoveStartTime) {
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

    protected int getEarliestTransitionTimeWithoutBreakingHardConstraints(AStarState state, Move possibleMove, I_Location prevLocation, I_Location intervalLocation,
                                                                          TimeInterval prevLocationRelevantInterval,
                                                                          TimeInterval interval) {
        int possibleMoveTime;
        boolean afterLastConstraint;
        Move samePossibleMove = null;

        // If the move is accepted and is within the interval, return its time
        if ((constraints.accepts(possibleMove)) && (possibleMove.timeNow >= interval.start())) {
            return possibleMove.timeNow;
        }

        // Reset the move to start with waiting at the previous location
        possibleMove = new Move(possibleMove.agent, possibleMove.timeNow, prevLocation, prevLocation);

        while (possibleMove.timeNow <= prevLocationRelevantInterval.end() && possibleMove.timeNow <= interval.end()) {
            if (!constraints.accepts(possibleMove)) return -1;

            afterLastConstraint = computeAfterLastConstraintTime(state);
            possibleMoveTime = !afterLastConstraint ? possibleMove.timeNow + 1 : state.move.timeNow;

            // If the move time is within or right before the interval, check constraints
            if (possibleMove.timeNow >= interval.start() - 1) {
                possibleMove = new Move(possibleMove.agent, possibleMoveTime, prevLocation, intervalLocation);
                if (constraints.accepts(possibleMove)) {
                    return possibleMoveTime; // Found valid arrival time
                }
                if (possibleMove.equals(samePossibleMove)) return -1;
                samePossibleMove = possibleMove;
            }

            // Update the move to increment the time for the same location
            possibleMove = new Move(possibleMove.agent, possibleMoveTime, prevLocation, prevLocation);
        }

        return -1; // No valid move time found
    }

    protected void moveIntoSafeInterval(AStarState state, Move possibleMove, boolean init,
                                           I_Location prevLocation, I_Location intervalLocation,
                                           TimeInterval prevLocationRelevantInterval, TimeInterval interval,
                                           int intervalID) {
        AStarState child = state;
        int possibleMoveTime;
        boolean afterLastConstraint;
        Move samePossibleMove = null;

        // If the move is accepted and is within the current interval, create a child state and add to the open list
        if (constraints.accepts(possibleMove) && possibleMove.timeNow >= interval.start()) {
            child = generateChildState(possibleMove, state, interval, init, intervalID);
            addToOpenList(child);
            return;
        }

        // Reset the move to start by waiting at the previous location
        possibleMove = new Move(child.move.agent, possibleMove.timeNow, prevLocation, prevLocation);
        while (possibleMove.timeNow <= prevLocationRelevantInterval.end()) {
            if (!constraints.accepts(possibleMove)) return;

            // Generate child state based on the possible move
            if (init) {
                child = generateChildState(possibleMove, child, prevLocationRelevantInterval, init, intervalID);
                init = false;
            } else {
                child = generateChildState(possibleMove, child, prevLocationRelevantInterval, false, intervalID);
            }

            afterLastConstraint = computeAfterLastConstraintTime(child);
            possibleMoveTime = !afterLastConstraint ? possibleMove.timeNow + 1 : child.move.timeNow;

            // If the move time is within or right before the current interval, check constraints
            if (possibleMove.timeNow >= interval.start() - 1) {
                possibleMove = new Move(child.move.agent, possibleMoveTime, prevLocation, intervalLocation);
                if (constraints.accepts(possibleMove)) {
                    child = generateChildState(possibleMove, child, interval, false, intervalID);
                    addToOpenList(child);
                    return;
                }
                if (possibleMove.equals(samePossibleMove)) return;
                samePossibleMove = possibleMove;
            }

            // Update the move to increment the time for waiting at the same location
            possibleMove = new Move(child.move.agent, possibleMoveTime, prevLocation, prevLocation);
        }
    }

    protected boolean computeAfterLastConstraintTime(AStarState child) {
        return child.move.timeNow > constraints.getLastConstraintStartTime();
    }

    public class AStarSIPPState extends AStarState {
        protected final TimeInterval timeInterval;

        public AStarSIPPState(Move move, AStarSIPPState prev, int g, int conflicts, TimeInterval timeInterval, boolean visitedTarget, boolean isLastMove) {
            super(move, prev, g, conflicts, visitedTarget, isLastMove); 
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

