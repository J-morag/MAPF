package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintGroupingKey;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.Solution;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SingleAgentAStarSIPP_Solver extends SingleAgentAStar_Solver {

    HashMap<I_Location, ArrayList<int[]>> constraintsByLocation = null;

    @Override
    protected Solution solveAStar() {

        constraintsByLocation = constraintsToTimeIntervals(this.constraints);

        // if failed to init OPEN then the problem cannot be solved as defined (bad constraints? bad existing plan?)
        if (!initOpen()) return null;

        AStarState currentState;

        while ((currentState = openList.poll()) != null) { //dequeu in the while condition
            if (checkTimeout()) {
                return null;
            }
            // early stopping if we already exceed fBudget.
            if (currentState.getF() > fBudget) {
                return null;
            }
            closed.add(currentState);

            // nicetohave - change to early goal test
            if (isGoalState(currentState)) {
                // check to see if a rejecting constraint on the goal's location exists at some point in the future,
                // which would mean we can't finish the plan there and stay forever
                int firstRejectionAtLocationTime = agentsStayAtGoal ? constraints.rejectsEventually(currentState.move) : -1;

                if (firstRejectionAtLocationTime == -1) { // no rejections
                    // update this.existingPlan which is contained in this.existingSolution
                    currentState.backTracePlan(this.existingPlan);
                    return this.existingSolution; // the goal is good, and we can return the plan.
                } else { // we are rejected from the goal location at some point in the future.
                    expand(currentState);
                }
            } else { //expand
                expand(currentState); //doesn't generate closed or duplicate states
            }
        }
        return null; //no goal state found (problem unsolvable)
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

            openList.add(new AStarState(existingPlan.moveAt(existingPlan.getEndTime()), null, 0, 0));
        } else { // the existing plan is empty (no existing plan)
            I_Location sourceLocation = map.getMapLocation(this.sourceCoor);

            List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(sourceLocation.outgoingEdges());
            if (Objects.equals(sourceLocation.getCoordinate(), this.targetCoor)) {
                neighborLocationsIncludingCurrent.add(sourceLocation);
            }

            for (I_Location destination : neighborLocationsIncludingCurrent) {
                Move possibleMove = new Move(agent, problemStartTime + 1, sourceLocation, destination);
                AStarState rootState = new AStarState(possibleMove, null, this.gAndH.cost(possibleMove), 0);
                if (constraints.accepts(possibleMove)) { //move not prohibited by existing constraint
                    openList.add(rootState);
                } else { // check if it is possible to wait in place until destination will be clear
//                    rootState = new AStarState(possibleMove, null, problemStartTime + 2, 0);
                    int isPossible = waitAndMove(rootState);
                    if (isPossible != -1) {
                        rootState = new AStarState(possibleMove, null, isPossible, 0);
                        openList.add(rootState);
                    }
                }
            }
        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    public void expand(@NotNull AStarState state) {
        expandedNodes++;
        // can move to neighboring locations or stay put
        List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(state.move.currLocation.outgoingEdges());
        // no point to do stay moves or search the time dimension after the time of last constraint.
        // this makes A* complete even when there are goal constraints (infinite constraints)

        for (I_Location destination : neighborLocationsIncludingCurrent) {
            // give all moves after last constraint time the same time so that they're equal. patch the plan later to correct times
            Move possibleMove = new Move(state.move.agent, state.move.timeNow + 1,
                    state.move.currLocation, destination);
            AStarState existingState;
            AStarState child = new AStarState(possibleMove, state,
                    state.g + SingleAgentAStarSIPP_Solver.this.gAndH.cost(possibleMove),
                    state.conflicts + numConflicts(possibleMove));
            boolean isChild = false;
            // move not prohibited by existing constraint
            if (constraints.accepts(possibleMove)) {
                isChild = true;
            } else { // check if it is possible to wait in place until destination will be clear
                int isPossible = waitAndMove(child);
                if (isPossible != -1) {
                    child = new AStarState(possibleMove, state,
                            isPossible,
                            state.conflicts + numConflicts(possibleMove));
                    isChild = true;
                }
            }

            if (isChild) {
                if (closed.contains(child)) { // state visited already
                    // TODO for inconsistent heuristics - if the new one has a lower f, remove the old one from closed
                    // and add the new one to open
                } else if (null != (existingState = openList.get(child))) { //an equal state is waiting in open
                    //keep the one with min G
                    state.keepTheStateWithMinG(child, existingState); //O(LOGn)
                } else { // it's a new state
                    openList.add(child);
                }
            }
        }
    }


    private int waitAndMove(AStarState state) {
        // this function check if it is possible to wait in place until the destination is
        // clear of constraints. if it does, it returns the new g value which is equal to the time
        // the agent waited in place
        int[] prevRelevantInterval = null;
        boolean isPossible = false;
        I_Location prevLocation = state.move.prevLocation;
        I_Location currLocation = state.move.currLocation;
        ArrayList<int[]> prevLocationConstraints = constraintsByLocation.get(prevLocation);
        ArrayList<int[]> currLocationConstraints = constraintsByLocation.get(currLocation);
        int newProblemStartTime = state.move.timeNow + 1;

        // In case there are no constraints in prevLocation, then we just need to find when currLocation is clear
        if (prevLocationConstraints == null) {
            for (int[] currInterval : currLocationConstraints) {
                if (currInterval[0] <= newProblemStartTime) {
                    return currInterval[1] + 1;
                }
            }
        }
        // find relevant time interval of prevLocation
        for (int[] prevInterval : prevLocationConstraints) {
            if (prevInterval[0] <= newProblemStartTime) {
                prevRelevantInterval = prevInterval;
                isPossible = true;
                break;
            }
        }
        // check if it is possible to stay in place until destination is clear
        if (isPossible) {
            for (int[] currInterval : currLocationConstraints) {
                if (newProblemStartTime > currInterval[1]) break;
                if (prevRelevantInterval[1] >= currInterval[0]) {
                    return -1;
                }
            }
            return prevRelevantInterval[1] + 1;
        }
        else return -1;
    }

    private HashMap<I_Location, ArrayList<int[]>> constraintsToTimeIntervals(ConstraintSet constraints) {
        // Originally constraints are by location and time, for the SIPP algorithm
        // we convert the production of the constraints into time intervals by location
        HashMap<I_Location, ArrayList<int[]>> timeIntervals = new HashMap<>();

        for (I_ConstraintGroupingKey timeLocation : constraints.constraints.keySet()) {
            if (!timeIntervals.containsKey(timeLocation.getLocation())) {
                timeIntervals.put(timeLocation.getLocation(), new ArrayList());
            }
                ArrayList locationList = timeIntervals.get(timeLocation.getLocation());
                locationList.add((timeLocation.getTime()));
        }
        for (I_Location location : timeIntervals.keySet()) {
            ArrayList timestamps = timeIntervals.get(location);

            // Convert the timestamps to intervals using timestampsToIntervals function
            List<int[]> intervals = timestampsToIntervals(timestamps);

            // Update the output map with the intervals for the current location
            timeIntervals.put(location, new ArrayList<>(intervals));
        }

        return timeIntervals;
    }


    public List<int[]> timestampsToIntervals(List<Integer> timestamps) {
        // this function gets as input all constraints in a specific location
        // and return all constraints in timeIntervals instead of timeStamps
        List<int[]> intervals = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        for (int i = 0; i < timestamps.size(); i++) {
            int timestamp = timestamps.get(i);

            // Skip duplicates
            if (visited.contains(timestamp)) {
                continue;
            }
            // Start a new interval
            int start = timestamp;
            int end = timestamp;

            // Extend the interval as long as the next timestamp is one unit away
            while (i + 1 < timestamps.size() && timestamps.get(i + 1) == end + 1) {
                end = timestamps.get(i + 1);
                i++;
            }

            // Add the interval to the list
            intervals.add(new int[] {start, end});

            // Mark all timestamps in the interval as visited
            for (int j = start; j <= end; j++) {
                visited.add(j);
            }
        }

        return intervals;
    }
}


