package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.ArrayMap;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.*;
import Environment.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SingleAgentAStarSIPP_Solver extends SingleAgentAStar_Solver {

    private Map<I_Location, List<Interval>> safeIntervalsByLocation;

    public SingleAgentAStarSIPP_Solver() {
        super();
        super.name = "SIPP";
    }

    public record Interval(int start, int end) {
        public static final Interval DEFAULT_INTERVAL = new Interval(0, Integer.MAX_VALUE);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        if (runParameters instanceof RunParameters_SAAStarSIPP parameters && parameters.safeIntervalsByLocation != null) {
            this.safeIntervalsByLocation = parameters.safeIntervalsByLocation;
        } else {
            this.safeIntervalsByLocation = vertexConstraintsToSafeTimeIntervals(this.constraints, this.agent, this.map);
        }

        if (goalCondition instanceof VisitedTargetAStarGoalCondition) {
            throw new IllegalArgumentException(goalCondition.getClass().getSimpleName() + " not currently supported in " + this.getClass().getSimpleName());
        }
    }

    @Override
    protected boolean initOpen() {
        // if the existing plan isn't empty, we start from the last move of the existing plan.
        if (existingPlan.size() > 0) {
            Move lastExistingMove = existingPlan.moveAt(existingPlan.getEndTime());
            // We assume that we cannot change the existing plan, so if it is rejected by constraints, we can't initialise OPEN.
            if (constraints.rejects(lastExistingMove)) {
                return false;
            }
            // Get the total cost of the existing plan, replace `existingPlanTotalCost` with your calculation
            int existingPlanTotalCost = existingPlan.getCost();

            // Find the time interval for the last move
            List<Interval> intervals = getIntervalsForLocation(lastExistingMove.currLocation);
            Interval lastMoveInterval = null;
            for (Interval interval : intervals) {
                if (interval.start <= lastExistingMove.timeNow && interval.end >= lastExistingMove.timeNow) {
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
                moveToNeighbor(rootState, possibleMove, true);
            }
        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    private List<Interval> getIntervalsForLocation(I_Location location) {
        return safeIntervalsByLocation.computeIfAbsent(location, k -> Collections.singletonList(Interval.DEFAULT_INTERVAL));
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
            moveToNeighbor((AStarSIPPState) state, possibleMove, false);
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
    private AStarSIPPState generateChildState(Move move, AStarSIPPState state, Interval interval, boolean init) {
        // todo can we have a more accurate counting of conflicts? right now only counts the number of conflicts in the move into the interval.
        //  maybe we can add the number of conflicts in the interval itself once we know the next move? And what to do about the first state?
        if (init) {
            return new AStarSIPPState(move, null, getG(null, move), 0, interval, visitedTarget(null, isMoveToTarget(move)));
        }
        return new AStarSIPPState(move, state, getG(state, move), state.conflicts + numConflicts(move, false) // todo IsALastMove handling for TMAPF support
                , interval, visitedTarget(state, isMoveToTarget(move)));
    }

    /**
     * Moves to a neighboring location based on the current state and a possible move.
     *
     * @param state        The current state.
     * @param possibleMove The move to be checked.
     * @param init         Flag indicating if this is an initial state or not.
     */
    private void moveToNeighbor(AStarSIPPState state, Move possibleMove, boolean init) {
        I_Location prevLocation = possibleMove.prevLocation;
        I_Location currLocation = possibleMove.currLocation;

        // Retrieve safe intervals for the current location
        List<Interval> safeIntervalsCurrLocation = getIntervalsForLocation(currLocation);
        int nextMoveStartTime = possibleMove.timeNow;

        Interval prevLocationRelevantInterval = init ? getIntervalsForLocation(prevLocation).get(0) : state.timeInterval;

        // Iterate through the intervals of the current location
        for (Interval currInterval : safeIntervalsCurrLocation) {
            if (currInterval.end >= nextMoveStartTime) {
                if ((currInterval.start <= nextMoveStartTime)) {
                    moveIntoSafeInterval(state, possibleMove, init, prevLocation, currLocation, prevLocationRelevantInterval, currInterval);
                    continue;
                }
                if (prevLocationRelevantInterval.end >= currInterval.start - 1) {
                    moveIntoSafeInterval(state, possibleMove, init, prevLocation, currLocation, prevLocationRelevantInterval, currInterval);
                }
            }
        }
    }

    /**
     * Moves into a safe interval, creating child states as necessary.
     *
     * @param state                      The current state.
     * @param possibleMove               The move to be checked.
     * @param init                       Flag indicating if this is an initial state or not.
     * @param prevLocation               The previous location from where the move starts.
     * @param currLocation               The current/target location of the move.
     * @param prevLocationRelevantInterval The relevant interval of the previous location.
     * @param currInterval               The current interval being considered.
     */
    private void moveIntoSafeInterval(AStarSIPPState state, Move possibleMove, boolean init, I_Location prevLocation,
                                      I_Location currLocation, Interval prevLocationRelevantInterval, Interval currInterval) {
        AStarSIPPState child = state;
        int possibleMoveTime;
        boolean afterLastConstraint;
        Move samePossibleMove = null;

        // If the move is accepted and is within the current interval, create a child state and add to the open list
        if ((constraints.accepts(possibleMove)) && (possibleMove.timeNow >= currInterval.start)) {
            child = generateChildState(possibleMove, state, currInterval, init);
            addToOpenList(child);
            return;
        }

        // Reset the move to start from the previous location
        possibleMove = new Move(child.move.agent, possibleMove.timeNow, prevLocation, prevLocation);
        while (possibleMove.timeNow <= prevLocationRelevantInterval.end) {
            if (!constraints.accepts(possibleMove)) return;

            // Generate child state based on the possible move
            if (init) {
                child = generateChildState(possibleMove, child, prevLocationRelevantInterval, init);
                init = false;
            } else child = generateChildState(possibleMove, child, prevLocationRelevantInterval, false);

            afterLastConstraint = child.move.timeNow > constraints.getLastConstraintStartTime();
            possibleMoveTime = !afterLastConstraint ? possibleMove.timeNow + 1 : child.move.timeNow;

            // If the move time is within or right before the current interval, create the move and check constraints
            if (possibleMove.timeNow >= currInterval.start - 1) {
                possibleMove = new Move(child.move.agent, possibleMoveTime, prevLocation, currLocation);
                if (constraints.accepts(possibleMove)) {
                    child = generateChildState(possibleMove, child, currInterval, false);
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

    /**
     * @param agent if null, will only consider vertex constraints that are not agent-specific
     * @param map
     * @return the safe intervals for each location
     */
    public static Map<I_Location, List<Interval>> vertexConstraintsToSafeTimeIntervals(I_ConstraintSet constraints, @Nullable Agent agent, I_Map map) {
        /*
          Originally constraints are by location and time. For the SIPP algorithm,
          we convert the constraints into time intervals by location
         */
        Map<I_Location, ArrayList<Integer>> timeIntervals = new HashMap<>();

        for (Map.Entry<I_Location, ArrayList<Constraint>> entry : constraints.getLocationConstraintsTimeSorted().entrySet()) {
            for (Constraint constraint : entry.getValue()) {
                // skip constraints that are not vertex constraints
                if ((constraint.getPrevLocation() == null) && (constraint.agent == null || constraint.agent.equals(agent))){
                    List<Integer> timesList = timeIntervals.computeIfAbsent(entry.getKey(), k -> new ArrayList<>());
                    timesList.add(constraint.time);
                }
            }
        }

        Map<I_Location, List<Interval>> intervalMap = map instanceof I_ExplicitMap explicitMap ?
                new ArrayMap<>(explicitMap.getNumMapLocations()) : new HashMap<>();
        for (I_Location location : timeIntervals.keySet()) {
            List<Integer> timestamps = timeIntervals.get(location);

            // Convert the timestamps to intervals
            ArrayList<Interval> intervals = timestampsToSafeIntervals(timestamps);

            // Update the output map with the intervals for the current location
            intervalMap.put(location, intervals);
        }

        for (GoalConstraint goalConstraint : constraints.getGoalConstraints().values()) {
            // handle goal constraints by trimming from the last safe interval the range [goalTime, inf]
            List<Interval> locationIntervals = intervalMap.computeIfAbsent(goalConstraint.location, k -> new ArrayList<>());
            Interval lastInterval = locationIntervals.isEmpty() ? Interval.DEFAULT_INTERVAL :
                    locationIntervals.remove(locationIntervals.size() - 1);
            if (Config.DEBUG >= 1 && lastInterval.end < Integer.MAX_VALUE) {
                throw new IllegalStateException("Last interval should end at infinity because there is at most one goal" +
                        " constraint pe location: " + lastInterval);
            }
            locationIntervals.add(new Interval(lastInterval.start, goalConstraint.time - 1));
        }
        return intervalMap;
    }

    private static ArrayList<Interval> timestampsToSafeIntervals(List<Integer> timestamps) {
        // Sort timestamps in ascending order
        Collections.sort(timestamps);

        ArrayList<Interval> safeIntervals = new ArrayList<>();

        // Add the first interval if it starts from more than 0
        if (timestamps.get(0) > 0) {
            safeIntervals.add(new Interval(0, timestamps.get(0) - 1));
        }

        // Iterate through timestamps to find safe intervals
        for (int i = 1; i < timestamps.size(); i++) {
            if (timestamps.get(i) > timestamps.get(i - 1) + 1) {
                safeIntervals.add(new Interval(timestamps.get(i - 1) + 1, timestamps.get(i) - 1));
            }
        }

        // Add the last interval extending to positive infinity
        safeIntervals.add(new Interval(timestamps.get(timestamps.size() - 1) + 1, Integer.MAX_VALUE));

        return safeIntervals;
    }

    public class AStarSIPPState extends AStarState {
        private final Interval timeInterval;

        public AStarSIPPState(Move move, AStarSIPPState prev, int g, int conflicts, Interval timeInterval, boolean visitedTarget) {
            super(move, prev, g, conflicts, visitedTarget, false); // todo add support for isALastMove for TMAPF
            this.timeInterval = timeInterval;
        }

        @Override
        public boolean equals(Object o) { // todo visited target to support TMAPF. Maybe just get from super and expend? Also, what about visited target in the comparator?
            if (this == o) return true;
            if (!(o instanceof AStarSIPPState that)) return false;

            if (timeInterval.start != that.timeInterval.start) return false;
            assert move != null;
            assert that.move != null;
            return move.currLocation.equals(that.move.currLocation);
        }

        @Override
        public int hashCode() {
            assert move != null;
            int result = move.currLocation.hashCode();
            result = 31 * result + this.timeInterval.start;
            return result;
        }

        @Override
        protected @NotNull List<Move> getOrderedMoves() {
            // todo randomize the transition times
            return super.getOrderedMoves();

        }
    }
}

