package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.GraphMapVertex;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintGroupingKey;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.Solution;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SingleAgentAStarSIPP_Solver extends SingleAgentAStar_Solver {

    private HashMap<I_Location, ArrayList<Interval>> constraintsByLocation;

    private record Interval(int start, int end) {
    }

    @Override
    protected Solution solveAStar() {

        constraintsByLocation = constraintsToFreeTimeIntervals(this.constraints, this.map.getAllGraphLocations());
        return super.solveAStar();
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

            openList.add(new AStarSIPPState(existingPlan.moveAt(existingPlan.getEndTime()), null, 0, 0, null));
        } else { // the existing plan is empty (no existing plan)
            I_Location sourceLocation = map.getMapLocation(this.sourceCoor);

            List<I_Location> neighborLocations = new ArrayList<>(sourceLocation.outgoingEdges());
            if (Objects.equals(sourceLocation.getCoordinate(), this.targetCoor)) {
                neighborLocations.add(sourceLocation);
            }

            for (I_Location destination : neighborLocations) {
                Move possibleMove = new Move(agent, problemStartTime + 1, sourceLocation, destination);
                AStarSIPPState rootState = new AStarSIPPState(possibleMove, null, this.gAndH.cost(possibleMove), 0, null);
                generateChild(rootState, possibleMove, true);
            }
        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    @Override
    public void expand(@NotNull AStarState state) {
        if (state.move.currLocation.getType() == Enum_MapLocationType.NO_STOP) {
            throw new RuntimeException("UnsupportedOperationException");
        }
        expandedNodes++;
        // can move to neighboring locations
        List<I_Location> neighborLocations = new ArrayList<>(state.move.currLocation.outgoingEdges());

        for (I_Location destination : neighborLocations) {
            Move possibleMove = new Move(state.move.agent, state.move.timeNow + 1,
                    state.move.currLocation, destination);
            generateChild((AStarSIPPState) state, possibleMove, false);
        }
    }
    private void generateChild(AStarSIPPState state, Move possibleMove, boolean init) {
        I_Location prevLocation = possibleMove.prevLocation;
        I_Location currLocation = possibleMove.currLocation;
        List<Interval> freeIntervalsCurrLocation = constraintsByLocation.get(currLocation);
        int nextMoveStartTime = possibleMove.timeNow;
        AStarSIPPState child;
        Interval prevLocationRelevantInterval;
        if (!init){
            prevLocationRelevantInterval = state.timeInterval;
        }
        else {
            prevLocationRelevantInterval = constraintsByLocation.get(prevLocation).get(0);
        }

        for (Interval currInterval : freeIntervalsCurrLocation) {
            if (currInterval.end >= nextMoveStartTime) {
                if ((currInterval.start <= nextMoveStartTime)) {
                    if (!init) {
                        child = new AStarSIPPState(possibleMove, state, state.g + gAndH.cost(possibleMove), 0, currInterval);
                    } else {
                        child = new AStarSIPPState(possibleMove, null, gAndH.cost(possibleMove), 0, currInterval);
                    }
                    addToOpenList(child);
                } else {
                    if (prevLocationRelevantInterval.end >= currInterval.start - 1) {
                        int timeToWait = currInterval.start - nextMoveStartTime;
                        possibleMove = new Move(agent, nextMoveStartTime, prevLocation, prevLocation);
                        if (!init) {
                            child = new AStarSIPPState(possibleMove, state, state.g + gAndH.cost(possibleMove), state.conflicts + numConflicts(possibleMove), prevLocationRelevantInterval);
                        } else {
                            child = new AStarSIPPState(possibleMove, null, gAndH.cost(possibleMove), 0, prevLocationRelevantInterval);
                        }

                        for (int t = 1; t < timeToWait; t++) {
                            possibleMove = new Move(agent, nextMoveStartTime + t, prevLocation, prevLocation);
                            child = new AStarSIPPState(possibleMove, child, child.g + gAndH.cost(possibleMove), state.conflicts + numConflicts(possibleMove), prevLocationRelevantInterval);
                        }
                        possibleMove = new Move(agent, nextMoveStartTime + timeToWait, prevLocation, currLocation);
                        child = new AStarSIPPState(possibleMove, child, child.g + gAndH.cost(possibleMove), state.conflicts + numConflicts(possibleMove), currInterval);
                        addToOpenList(child);
                    }
                    else return;
                }
            }
        }
    }


    private void addToOpenList(AStarSIPPState state) {
        AStarSIPPState existingState;
        if (closed.contains(state)) { // state visited already
            // TODO for inconsistent heuristics - if the new one has a lower f, remove the old one from closed
            // and add the new one to open
        } else if (null != (existingState = (AStarSIPPState) openList.get(state))) { //an equal state is waiting in open
            //keep the one with min G
            state.keepTheStateWithMinG(state, existingState); //O(LOGn)
        } else { // it's a new state
            openList.add(state);
        }
    }

    private static HashMap<I_Location, ArrayList<Interval>> constraintsToFreeTimeIntervals(ConstraintSet constraints, HashMap<I_Coordinate, GraphMapVertex> allGraphLocations) {
        /*
          Originally constraints are by location and time, for the SIPP algorithm
          we convert the production of the constraints into time intervals by location
         */
        HashMap<I_Location, ArrayList<Integer>> timeIntervals = new HashMap<>();
        List<Integer> timesList;
        for (I_ConstraintGroupingKey timeLocation : constraints.getKeySet()) {
            timesList = timeIntervals.computeIfAbsent(timeLocation.getLocation(), k -> new ArrayList<>());
            timesList.add(timeLocation.getTime());
        }

        HashMap<I_Location, ArrayList<Interval>> intervalMap = new HashMap<>();
        for (I_Location location : timeIntervals.keySet()) {
            ArrayList<Integer> timestamps = timeIntervals.get(location);

            // Convert the timestamps to intervals using timestampsToIntervals function
            ArrayList<Interval> intervals = timestampsToFreeIntervals(timestamps);

            // Update the output map with the intervals for the current location
            intervalMap.put(location, intervals);
        }

        // Add [0, inf] interval for locations in allGraphLocations that are not in intervalMap
        for (I_Coordinate coordinate : allGraphLocations.keySet()) {
            I_Location location = allGraphLocations.get(coordinate);
            if (!intervalMap.containsKey(location)) {
                ArrayList<Interval> infInterval = new ArrayList<>();
                infInterval.add(new Interval(0, Integer.MAX_VALUE));
                intervalMap.put(location, infInterval);
            }
        }

        return intervalMap;
    }

    public static ArrayList<Interval> timestampsToFreeIntervals(ArrayList<Integer> timestamps) {
        // Sort timestamps in ascending order
        Collections.sort(timestamps);

        ArrayList<Interval> freeIntervals = new ArrayList<>();

        // Add the first interval if it starts from more than 0
        if (timestamps.get(0) > 0) {
            freeIntervals.add(new Interval(0, timestamps.get(0) - 1));
        }

        // Iterate through timestamps to find free intervals
        for (int i = 1; i < timestamps.size(); i++) {
            if (timestamps.get(i) > timestamps.get(i - 1) + 1) {
                freeIntervals.add(new Interval(timestamps.get(i - 1) + 1, timestamps.get(i) - 1));
            }
        }

        // Add the last interval extending to positive infinity
        freeIntervals.add(new Interval(timestamps.get(timestamps.size() - 1) + 1, Integer.MAX_VALUE));

        return freeIntervals;
    }

    public class AStarSIPPState extends AStarState {
        private final Interval timeInterval;

        public AStarSIPPState(Move move, AStarSIPPState prev, int g, int conflicts, Interval timeInterval) {
            super(move, prev, g, conflicts);
            this.timeInterval = timeInterval;

        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AStarSIPPState that)) return false;

            if (timeInterval.start != that.timeInterval.start) return false;
            return move.currLocation.equals(that.move.currLocation);
        }
        @Override
        public int hashCode() {
            int result = move.currLocation.hashCode();
            result = 31 * result + this.timeInterval.start;
            return result;
        }
    }

}

