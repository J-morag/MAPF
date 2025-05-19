package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.AgentAtGoal;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Safe Interval Path Planning with Soft constraints (SIPPS) implementation of the A* algorithm for single-agent pathfinding.
 * This algorithm is an extension of SIPP (Safe Interval Path Planning) to support both soft and hard constraints.
 * Based on the paper:
 * Li, Jiaoyang, et al. "MAPF-LNS2: Fast repairing for multi-agent path finding via large neighborhood search." Proceedings of the AAAI Conference on Artificial Intelligence. Vol. 36. No. 9. 2022.

 * The implementation supports soft constraints using a Conflict Avoidance Table of type RemovableConflictAvoidanceTableWithContestedGoals.
 */
public class SingleAgentAStarSIPPS_Solver extends SingleAgentAStarSIPP_Solver{

    private Map<I_Location, List<AgentAtGoal>> softGoalOccupancies;

    private int lowerBoundOnTravelTime; // TODO - implement heuristic

    /**
     * Maps each AStarSIPPSState to a set of identical states currently in the open list.
     * This allows for O(1) lookup instead of iterating over the open list.
     * It's used to speed up operations like addToOpen by quickly identifying existing equivalent nodes.
     */
    private Map<AStarSIPPSState, HashSet<AStarSIPPSState>> identicalNodesMap;



    public SingleAgentAStarSIPPS_Solver() {
        super(new TieBreakingForLessConflictsLowerFAndHigherG());
        super.name = "SIPPS";
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        Map<I_Location, List<TimeInterval>> safeHardIntervalsByLocation;
        Map<I_Location, List<TimeInterval>> safeSoftIntervalsByLocation;

        // Hard constraints - defined by the constraints given in the constraints set
        if (runParameters.constraints != null) {
            safeHardIntervalsByLocation = this.constraints.vertexConstraintsToSortedSafeTimeIntervals(this.agent, this.map);
        } else {
            // if empty, the default safe interval will be given to each location, which is [0, inf]
            safeHardIntervalsByLocation = new HashMap<>();
        }

        // Soft constraints - defined by the conflict avoidance table given in parameters
        if (runParameters.conflictAvoidanceTable != null) {
            if (!(runParameters.conflictAvoidanceTable instanceof RemovableConflictAvoidanceTableWithContestedGoals)) {
                throw new IllegalArgumentException("conflictAvoidanceTable need to be of type RemovableConflictAvoidanceTableWithContestedGoals using SIPPS, got: " + conflictAvoidanceTable.getClass().getSimpleName());
            }
            this.conflictAvoidanceTable = Objects.requireNonNullElseGet(runParameters.conflictAvoidanceTable, () -> createConflictAvoidanceTable(runParameters.existingSolution));
            safeSoftIntervalsByLocation = ((RemovableConflictAvoidanceTableWithContestedGoals) this.conflictAvoidanceTable).conflictAvoidanceTableToSafeTimeIntervals();
            this.softGoalOccupancies = ((RemovableConflictAvoidanceTableWithContestedGoals) conflictAvoidanceTable).getGoalOccupancies();
        } else {
            this.conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
            safeSoftIntervalsByLocation = new HashMap<>();
            this.softGoalOccupancies = new HashMap<>();
        }
        this.sortedSafeIntervalsByLocation = this.combineSafeIntervals(safeHardIntervalsByLocation, safeSoftIntervalsByLocation);

        List<TimeInterval> safeIntervalsForGoal = this.sortedSafeIntervalsByLocation.get(instance.map.getMapLocation(this.agent.target));
        if (safeIntervalsForGoal != null) {
            TimeInterval lastInterval = safeIntervalsForGoal.get(safeIntervalsForGoal.size()-1);
            this.lowerBoundOnTravelTime = lastInterval.start();
        }
        else {
            this.lowerBoundOnTravelTime = 0;
        }

        if (goalCondition instanceof VisitedTargetAStarGoalCondition) {
            throw new IllegalArgumentException(goalCondition.getClass().getSimpleName() + " not currently supported in " + this.getClass().getSimpleName());
        }
        this.identicalNodesMap = new HashMap<>();
    }

    @Override
    protected AStarState createNewState(Move move, AStarState prev, int g, int conflicts, TimeInterval timeInterval, boolean visitedTarget, int intervalID) {
        boolean isLastMove = move.currLocation.getCoordinate().equals(move.agent.target) && (timeInterval != null && timeInterval.end() == Integer.MAX_VALUE);
        return new AStarSIPPSState(move, (AStarSIPPState) prev, g, conflicts, timeInterval, visitedTarget, intervalID, false, isLastMove);
    }

    @Override
    protected void addToOpenList(@NotNull AStarState state) {
        // find identical nodes to state
        ArrayList<AStarSIPPSState> identicalNodes = findIdenticalNodes((AStarSIPPSState) state);
        int stateLow = ((AStarSIPPSState) state).timeInterval.start();
        int stateHigh = ((AStarSIPPSState) state).timeInterval.end();
        int stateConflicts = state.conflicts;
        for (AStarSIPPSState q : identicalNodes) {

            // q dominates state, so no need to generate state
            if (q.timeInterval.start() <= stateLow && q.conflicts <= stateConflicts) {
                return;

            // state dominates q, so we can prune q
            } else if (stateLow <= q.timeInterval.start() && stateConflicts <= q.conflicts) {
                this.closed.remove(q);
                this.openList.remove(q);

                // Remove q from the equivalence class in identicalNodesMap
                HashSet<AStarSIPPSState> eqSet = this.identicalNodesMap.get(q);
                if (eqSet != null) {
                    eqSet.remove(q);
                    // Optionally remove the key if the set becomes empty
                    if (eqSet.isEmpty()) {
                        this.identicalNodesMap.remove(q);
                    }
                }

            // intervals overlap
            } else if (stateLow < q.timeInterval.end() && q.timeInterval.start() < stateHigh) {
                // reset the end of the interval with the smaller start to the start of the other interval
                if (stateLow < q.timeInterval.start()) {
                    state = createNewState(state.move, state.prev, state.g, state.conflicts, new TimeInterval(stateLow, q.timeInterval.start()), state.visitedTarget, ((AStarSIPPSState) state).intervalID);
                } else {
                    AStarSIPPSState newQ = (AStarSIPPSState) createNewState(q.move, q.prev, q.g, q.conflicts, new TimeInterval(q.timeInterval.start(), stateLow), q.visitedTarget, q.intervalID);

                    // Remove old q from whichever set its in
                    boolean wasInOpen = this.openList.remove(q);
                    boolean wasInClosed = this.closed.remove(q);
                    HashSet<AStarSIPPSState> eqSet = this.identicalNodesMap.get(q);
                    if (eqSet != null) {
                        eqSet.remove(q);
                        if (eqSet.isEmpty()) {
                            this.identicalNodesMap.remove(q);
                        }
                    }

                    // Add newQ to the correct set
                    if (wasInOpen) {
                        this.openList.add(newQ);
                    } else if (wasInClosed) {
                        this.closed.add(newQ);
                    }
                    this.identicalNodesMap.computeIfAbsent(newQ, k -> new HashSet<>()).add(newQ);
                }
            }
        }
        openList.add(state);
        this.identicalNodesMap.computeIfAbsent((AStarSIPPSState) state, k -> new HashSet<>()).add((AStarSIPPSState) state);
    }


    /**
     * Finds nodes identical to a given node (state).
     * Two nodes n1 and n2 have the same identity, denoted as n1 ∼ n2, iff:
     *      n1.v == n2.v && n1.id == n2.id && n1.is_goal == n2.is_goal
     * @param state - a node to find all identical nodes in OPEN and CLOSED for.
     * @return list of nodes identical to given node.
     */
    private ArrayList<AStarSIPPSState> findIdenticalNodes(AStarSIPPSState state) {
        ArrayList<AStarSIPPSState> identicalNodes = new ArrayList<>();

        // O(1) lookup in openMap
        HashSet<AStarSIPPSState> openMatches = this.identicalNodesMap.get(state);
        if (openMatches != null) {
            identicalNodes.addAll(openMatches);
        }

        // Check in CLOSED HashSet (O(1) lookup)
        if (closed.contains(state)) {
            identicalNodes.add(state);
        }

        return identicalNodes;
    }

    @Override
    public void expand(@NotNull AStarState state) {
        if (!(state instanceof AStarSIPPSState)) {
            throw new RuntimeException("SIPPS solver works with states of type AStarSIPPSState, received: " + state.getClass().getSimpleName());
        }

        if (state.move.currLocation.getType() == Enum_MapLocationType.NO_STOP) {
            throw new RuntimeException("UnsupportedOperationException");
        }
        expandedNodes++;
        List<I_Location> neighborLocations = new ArrayList<>(state.move.currLocation.outgoingEdges());
        boolean afterLastConstraint = computeAfterLastConstraintTime(state);

        for (I_Location destination : neighborLocations) {
            List<Pair<Integer, TimeInterval>> reachableIntervalsForLocation = createReachableIntervalsMap(destination, state);
            if (reachableIntervalsForLocation.isEmpty()) continue;

            // Create move
            Move possibleMove = createPossibleMove((AStarSIPPSState) state, afterLastConstraint, destination);
            I_Location prevLocation = possibleMove.prevLocation;
            I_Location neighborLocation = possibleMove.currLocation;
            TimeInterval prevLocationRelevantInterval = ((AStarSIPPState) state).timeInterval;
            int earliestMoveTime = possibleMove.timeNow;
            int latestMoveTime = prevLocationRelevantInterval.end() == Integer.MAX_VALUE ? Integer.MAX_VALUE : prevLocationRelevantInterval.end() + 1;

            for (Pair<Integer, TimeInterval> intervalIDAndIntervalPair : reachableIntervalsForLocation) {
                TimeInterval interval = intervalIDAndIntervalPair.getRight();
                int intervalID = intervalIDAndIntervalPair.getLeft();
                int low = getEarliestTransitionTimeWithoutBreakingHardConstraints(state, possibleMove, state.move.currLocation, destination, prevLocationRelevantInterval, interval);
                if (low == -1) continue;
                int lowPrime = getEarliestTransitionTimeWithoutBreakingHardOrSoftConstraints(interval, possibleMove, low);
                if (earliestMoveTime <= interval.end() && latestMoveTime >= interval.start()) {
                    if (lowPrime != -1 && lowPrime > low) {
                        moveIntoSafeInterval(state, possibleMove, false, possibleMove.prevLocation, possibleMove.currLocation, prevLocationRelevantInterval, new TimeInterval(low, lowPrime), intervalID);
                        moveIntoSafeInterval(state, possibleMove, false, possibleMove.prevLocation, possibleMove.currLocation, prevLocationRelevantInterval, new TimeInterval(lowPrime, interval.end()), intervalID);
                    } else {
                        moveIntoSafeInterval(state, possibleMove, false, prevLocation, neighborLocation, prevLocationRelevantInterval, interval, intervalID);
                    }
                } else {
                    break;
                }
            }
        }
    }

    @Override
    protected boolean computeAfterLastConstraintTime(AStarState child) {
        return (child.move.timeNow > constraints.getLastConstraintStartTime()) && // after the time of last constraint, according to constraints set
                // if conflicts avoidance table exists, soft constraints should be supported too
                (this.conflictAvoidanceTable == null || (child.move.timeNow > conflictAvoidanceTable.getLastOccupancyTime())); // after the time of last conflicts, according to conflictAvoidanceTable
    }

    @Override
    protected void addInitialNodesToOpen(I_Location destination, I_Location sourceLocation) {
        Move possibleMove = new Move(agent, problemStartTime + 1, sourceLocation, destination);
        // if there are conflicts at the first neighbor nodes, maybe staying in current location would be better
        // todo - this is the correct way to initialize open, we should do the same in SIPP
        int numberOfConflicts = this.conflictAvoidanceTable.numConflicts(possibleMove, false);
        if (numberOfConflicts != 0) {
            Move stayInSourceMove = new Move(agent, problemStartTime + 1, sourceLocation, sourceLocation);
            AStarState rootState = createNewState(possibleMove, null, getG(null, stayInSourceMove), 0, null, visitedTarget(null, isMoveToTarget(stayInSourceMove)), 0);
            moveToNeighborLocation(rootState, stayInSourceMove, true);
        }
        AStarState rootState = createNewState(possibleMove, null, getG(null, possibleMove), numberOfConflicts, null, visitedTarget(null, isMoveToTarget(possibleMove)), 0);
        moveToNeighborLocation(rootState, possibleMove, true);
    }


    /**
     * Finds all reachable time intervals for a given location from the current state.
     * This method identifies safe intervals at `location` that overlap with the state's time interval
     * Each reachable interval is paired with the index of that interval.
     * @param location The target location.
     * @param state The current A* search state.
     * @return A list of (index, time interval) pairs representing reachable intervals.
     */
    private List<Pair<Integer, TimeInterval>> createReachableIntervalsMap(I_Location location, AStarState state) {
        int stateLow = ((AStarSIPPState) state).timeInterval.start();
        int stateHigh = ((AStarSIPPState) state).timeInterval.end();

        // store all reachable vertex-index pairs from vertex state.location at a time step within interval [stateLow+1, stateHigh+1)
        List<Pair<Integer, TimeInterval>> reachableIntervals = new ArrayList<>();

        List<TimeInterval> safeIntervalsForLocation = getIntervalsForLocation(location, this.sortedSafeIntervalsByLocation);
        int startIndex = getSafeIntervalsListIterationStartIndex(safeIntervalsForLocation, state.move.timeNow);
        int intervalIDsCounter = startIndex;
        for (int i = startIndex; i < safeIntervalsForLocation.size(); i++) {
            TimeInterval interval = safeIntervalsForLocation.get(i);
            // intervals overlap
            if (interval.start() <= stateHigh && interval.end() >= stateLow + 1  && state.move.timeNow < interval.end()) { //  && state.move.timeNow < interval.end()
                reachableIntervals.add(Pair.of(intervalIDsCounter, interval));
            }
            else if (stateHigh < interval.start()) {
                break;
            }
            intervalIDsCounter++;
        }
        return reachableIntervals;
    }


    /**
     * Creates a possible move for an agent based on the given state and destination.
     *
     * @param state The current A* SIPPS state containing the agent's move information.
     * @param afterLastConstraint A flag indicating whether the move occurs after the last constraint.
     * @param destination The destination location for the move.
     * @return A new Move object representing the possible move.
     */
    private Move createPossibleMove(AStarSIPPState state, boolean afterLastConstraint, I_Location destination) {
        Move possibleMove = new Move(state.move.agent, !afterLastConstraint ? state.move.timeNow + 1 : state.move.timeNow,
                state.move.currLocation, destination);
        if (possibleMove.prevLocation.equals(possibleMove.currLocation) && state.move.isStayAtSource) {
            possibleMove.isStayAtSource = true;
        }
        return possibleMove;
    }

    /**
     * Determines the earliest possible arrival time for a move within a given time interval,
     * ensuring that the move does not violate edge obstacles in both hard and soft constraints.
     * The search will begin from the specified minimum start time, or the start of the interval,
     * whichever is later.
     * @param interval The time interval within which the move should occur.
     * @param possibleMove The move being evaluated for its earliest valid arrival time.
     * @param minStartTime The minimum time step to begin the search from (e.g., earliest time allowed by hard constraints).
     * @return The earliest valid time step for the move that satisfies all constraints, or -1 if no valid time is found.
     */
    private int getEarliestTransitionTimeWithoutBreakingHardOrSoftConstraints(TimeInterval interval, Move possibleMove, int minStartTime) {
        int startTime = Math.max(Math.max(interval.start(), 1), minStartTime);
        // Iterate over time steps in the given interval
        for (int i_t = startTime; i_t <= interval.end(); i_t++) {
            if (i_t < 0) break;

            // Check goal occupancy conflicts
            int finalI_t = i_t;
            if (this.softGoalOccupancies.containsKey(possibleMove.currLocation) &&
                    this.softGoalOccupancies.get(possibleMove.currLocation).stream()
                            .anyMatch(agentAtGoal -> agentAtGoal.time <= finalI_t)) {
                return -1;
            }

            // Create a move at the current time step
            Move moveAtTime = new Move(possibleMove.agent, i_t, possibleMove.prevLocation, possibleMove.currLocation);
            // Check if the move is free from soft constraints
            if (this.conflictAvoidanceTable.numConflicts(moveAtTime, false) == 0) {
                return i_t; // Found the earliest valid time
            }
        }
        return -1; // No valid time found
    }


    /**
     * Iterate on existing agents' plans to create conflict avoidance table.
     * @param existingSolution contains all agents' plans.
     * @return conflict avoidance table.
     */
    private I_ConflictAvoidanceTable createConflictAvoidanceTable(Solution existingSolution) {
        ArrayList<SingleAgentPlan> plans = new ArrayList<>();
        for (SingleAgentPlan plan : existingSolution) {
            plans.add(plan);
        }
        return new RemovableConflictAvoidanceTableWithContestedGoals(plans, null);
    }


    /**
     * Combines safe time intervals from hard and soft constraints to determine the final safe intervals for each location.
     * A safe interval for a vertex is a contiguous period where there are no hard vertex or target obstacles,
     * and either soft obstacles exist at every time step or none exist at all.
     * This function merges hard and soft safe intervals by computing their intersection where both exist,
     * or retaining intervals from one source if the other is absent.
     *
     * @param safeHardIntervals A map of safe intervals constrained by hard constraints.
     * @param safeSoftIntervals A map of safe intervals constrained by soft constraints.
     * @return A map associating each location with its final combined safe time intervals.
     */
    public Map<I_Location, List<TimeInterval>> combineSafeIntervals(Map<I_Location, List<TimeInterval>> safeHardIntervals, Map<I_Location, List<TimeInterval>> safeSoftIntervals) {
        // Process only locations that have soft intervals
        for (Map.Entry<I_Location, List<TimeInterval>> entry : safeSoftIntervals.entrySet()) {
            I_Location location = entry.getKey();
            List<TimeInterval> softIntervals = entry.getValue();
            List<TimeInterval> hardIntervals = getIntervalsForLocation(location, safeHardIntervals);

            // If there are no hard intervals at this location, soft intervals are final
            if (hardIntervals.isEmpty()) {
                safeHardIntervals.put(location, softIntervals);
            } else {
                // Compute intersection of hard and soft intervals
                List<TimeInterval> intersection = combinedSoftAndHardIntervalsForLocation(hardIntervals, softIntervals);
                safeHardIntervals.put(location, intersection);
            }
        }

        return safeHardIntervals;
    }


    /**
     * Computes the intersection of hard and soft safe time intervals for a given location.
     * This function iterates through both lists of intervals and finds overlapping time ranges,
     * which are added to the final list of safe intervals.
     * @param hardIntervals A list of safe time intervals constrained by hard constraints.
     * @param softIntervals A list of safe time intervals constrained by soft constraints.
     * @return A list of time intervals representing the intersection of hard and soft safe intervals.
     */
    @NotNull
    private static List<TimeInterval> combinedSoftAndHardIntervalsForLocation(List<TimeInterval> hardIntervals, List<TimeInterval> softIntervals) {
        List<TimeInterval> locationFinalSafeIntervals = new ArrayList<>();
        int i = 0, j = 0;

        while (i < hardIntervals.size() && j < softIntervals.size()) {
            TimeInterval hardInterval = hardIntervals.get(i);
            TimeInterval softInterval = softIntervals.get(j);

            // Find intersection
            int start = Math.max(hardInterval.start(), softInterval.start());
            int end = Math.min(hardInterval.end(), softInterval.end());

            // If valid intersection exists, add it
            if (start <= end) {
                locationFinalSafeIntervals.add(new TimeInterval(start, end));
            }

            // Move to the next interval
            if (hardInterval.end() < softInterval.end()) {
                i++;
            } else {
                j++;
            }
        }
        return locationFinalSafeIntervals;
    }


    /**
     * Helper method to get existing intervals for a location.
     * @param location The location for which intervals are needed.
     * @return A list of TimeInterval for the given location.
     */
    private @NotNull List<TimeInterval> getIntervalsForLocation(I_Location location, Map<I_Location, List<TimeInterval>> intervalsMap) {
        // Default singleton list of infinite interval if none exist
        return intervalsMap.getOrDefault(location, DEFAULT_SINGLETON_LIST_OF_INF_INTERVAL);
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.conflictAvoidanceTable = null;
        this.softGoalOccupancies = null;
        this.identicalNodesMap = null;
    }

    /**
     * Comparator for use in A*.
     * Prioritizes AStarState objects based on:
     * First: Number of Conflicts, less is better.
     * Second: F values, lower is better.
     * Third: G values, higher is better.
     */
    public static class TieBreakingForLessConflictsLowerFAndHigherG implements Comparator<AStarState>{
        @Override
        public int compare(AStarState o1, AStarState o2) {
            if(o1.conflicts == o2.conflicts){ // conflicts are equal
                // if conflicts value is equal, we consider the state with lower F to be better.
                float fCompared = o1.getF() - o2.getF();
                if(Math.abs(fCompared) < 0.1){
                    // if f() value is equal, we break ties for higher g.
                    if (o2.g == o1.g){
                        // If still equal, tie-break for smaller ID (older nodes) (arbitrary) to force a total ordering and remain deterministic
                        return o1.id - o2.id;

                    }
                    else {
                        return o2.g - o1.g; //higher g is better
                    }
                }
                else{
                    return fCompared > 0 ? 1 : -1;
                }
            }
            else {
                return o1.conflicts - o2.conflicts; // less conflicts is better
            }
        }
    }


    public class AStarSIPPSState extends AStarSIPPState {

        protected int intervalID;
        protected boolean isGoal;
        public AStarSIPPSState(Move move, AStarSIPPState prev, int g, int conflicts, TimeInterval timeInterval, boolean visitedTarget, int intervalID, boolean isGoal, boolean isLastMove) {
            super(move, prev, g, conflicts, timeInterval, visitedTarget, isLastMove);
            this.intervalID = intervalID;
            this.isGoal = isGoal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AStarSIPPSState)) return false;
            AStarSIPPSState that = (AStarSIPPSState) o;
            return intervalID == that.intervalID && move.currLocation.equals(that.move.currLocation) && isGoal == that.isGoal;
        }

        @Override
        public int hashCode() {
            assert move != null;
            int result = move.currLocation.hashCode();
            result = 31 * result + intervalID;
            result = 31 * result + (isGoal ? 1 : 0);
            return result;
        }

// TODO - examine the heuristic including lowerBoundOnTravelTime
//        @Override
//        protected float calcH() {
//            float hVal = SingleAgentAStarSIPPS_Solver.this.gAndH.getH(this);
//            if (this.conflicts == 0) {
//                return Math.max(hVal, SingleAgentAStarSIPPS_Solver.this.lowerBoundOnTravelTime - this.g);
//            }
//            return hVal;
//        }
    }
}
