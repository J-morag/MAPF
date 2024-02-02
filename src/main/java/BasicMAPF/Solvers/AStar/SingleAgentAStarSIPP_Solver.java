package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SingleAgentAStarSIPP_Solver extends SingleAgentAStar_Solver {

    public SingleAgentAStarSIPP_Solver() {
        super();
        super.name = "SIPP";
    }

    private HashMap<I_Location, List<Interval>> safeIntervalsByLocation;

    private record Interval(int start, int end) {
        public static final Interval DEFAULT_INTERVAL = new Interval(0, Integer.MAX_VALUE);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        safeIntervalsByLocation = vertexConstraintsToSafeTimeIntervals(this.constraints);

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
            openList.add(new AStarSIPPState(lastExistingMove, null, existingPlanTotalCost, 0, lastMoveInterval, visitedTarget(null, existingPlan.containsTarget())));

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
        if (init) {
            return new AStarSIPPState(move, null, getG(null, move), 0, interval, visitedTarget(null, isMoveToTarget(move)));
        }
        return new AStarSIPPState(move, state, getG(state, move), state.conflicts + numConflicts(move), interval, visitedTarget(state, isMoveToTarget(move)));
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
    private void moveIntoSafeInterval(AStarSIPPState state, Move possibleMove, boolean init, I_Location prevLocation, I_Location currLocation, Interval prevLocationRelevantInterval, Interval currInterval) {
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

    private HashMap<I_Location, List<Interval>> vertexConstraintsToSafeTimeIntervals(I_ConstraintSet constraints) {
        /*
          Originally constraints are by location and time. For the SIPP algorithm,
          we convert the constraints into time intervals by location
         */
        HashMap<I_Location, ArrayList<Integer>> timeIntervals = new HashMap<>();

        for (Map.Entry<I_ConstraintGroupingKey, Set<Constraint>> entry : constraints.getEntrySet()) {
            for (Constraint constraint : entry.getValue()) {
                // skip constraints that are not vertex constraints
                if ((constraint.getPrevLocation() == null) && (constraint.agent == null || constraint.agent.equals(this.agent))){
                    List<Integer> timesList = timeIntervals.computeIfAbsent(entry.getKey().getLocation(), k -> new ArrayList<>());
                    timesList.add(entry.getKey().getTime());
                    break;
                }
            }
        }

        HashMap<I_Location, List<Interval>> intervalMap = new HashMap<>();
        for (I_Location location : timeIntervals.keySet()) {
            List<Integer> timestamps = timeIntervals.get(location);

            // Convert the timestamps to intervals using timestampsToIntervals function
            ArrayList<Interval> intervals = timestampsToSafeIntervals(timestamps);

            // Update the output map with the intervals for the current location
            intervalMap.put(location, intervals);
        }

        // handle goal constraints by discarding the safe interval [goalTime+1, inf]
        for (Map.Entry<I_ConstraintGroupingKey, Set<Constraint>> entry : constraints.getEntrySet()) {
            for (Constraint constraint : entry.getValue()) {
                if (constraint instanceof GoalConstraint goalConstraint){
                    List<Interval> locationIntervals = intervalMap.get(goalConstraint.location);
                    if (locationIntervals == null || locationIntervals.isEmpty() ||
                            locationIntervals.get(locationIntervals.size() - 1).end != Integer.MAX_VALUE){
                        throw new IllegalArgumentException("Goal constraints are an extension of vertex constraints, " +
                                "so #location should be in the map and have an interval extending to infinity");
                    }
                    locationIntervals.remove(locationIntervals.size() - 1);
                }
            }
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
            super(move, prev, g, conflicts, visitedTarget);
            this.timeInterval = timeInterval;
        }

        @Override
        public boolean equals(Object o) {
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
    }
}

