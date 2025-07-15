package BasicMAPF.Solvers.MultiAgentAStar;

import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A* search over the joint state space of multiple agents to solve MAPF directly.
 * This solver is optimal but only feasible for very small problems due to the exponential growth of the state space.
 */
public class MultiAgentAStar extends A_Solver {

    /* = Static fields = */
    private static final Comparator<MAAStarState> EQUAL_STATES_PREFER_SMALLER_G = Comparator.comparingDouble(MAAStarState::getG)
            .thenComparingInt(state -> state.id); // Tie-breaker based on id
    private static final Comparator<MAAStarState> F_THEN_H_SOC_OPEN_LIST_COMP = MAAStarState::compareTo;

    /* = Fields = */
    private I_OpenList<MAAStarState> openList;
    private HashSet<MAAStarState> closedList;
    private SingleAgentGAndH singleAgentGAndH;
    private I_ConstraintSet externalConstraints;
    private List<Agent> agents;
    private List<I_Location> goalLocations;
    private int numRegeneratedNodes;
    private int problemStartTime;
    private final Comparator<MAAStarState> openListComparator;

    public MultiAgentAStar(@Nullable Comparator<MAAStarState> openListComparator) {
        super.name = CanonicalSolversFactory.MAASTAR_NAME;
        this.openListComparator = Objects.requireNonNullElse(openListComparator, F_THEN_H_SOC_OPEN_LIST_COMP);
    }

    public MultiAgentAStar() {
        this(null);
    }

    /* = A_Solver overrides = */

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.agents = new ArrayList<>(instance.agents);
        this.openList = new OpenListTree<>(this.openListComparator);
        this.closedList = new HashSet<>();
        this.expandedNodes = 0;
        this.generatedNodes = 0;
        this.problemStartTime = parameters.problemStartTime;
        this.numRegeneratedNodes = 0;
        this.singleAgentGAndH = Objects.requireNonNullElseGet(parameters.singleAgentGAndH, () -> new DistanceTableSingleAgentHeuristic(this.agents, instance.map));
        this.externalConstraints = parameters.constraints;

        // Set up goal locations
        this.goalLocations = new ArrayList<>(agents.size());
        List<I_Location> startLocations = new ArrayList<>(agents.size());
        for (Agent agent : this.agents) {
            startLocations.add(instance.map.getMapLocation(agent.source));
            this.goalLocations.add(instance.map.getMapLocation(agent.target));
        }

        // Create and add the root state
        float[] initialHArr = calculateHeuristicArray(startLocations);
        MAAStarState root = getMaaStarState(null, startLocations, problemStartTime, new float[this.agents.size()] /*zeros*/, initialHArr);
        this.openList.add(root);
    }

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        while (!openList.isEmpty()) {
            if (checkTimeout()) {
                return null;
            }

            MAAStarState currentState = openList.poll();
            if (currentState == null) {
                return null; // No more states to explore
            }

            if (closedList.contains(currentState)) {
                continue;
            }
            closedList.add(currentState);
            expandedNodes++;

            if (isGoal(currentState)) {
                return reconstructSolution(currentState);
            }

            // Generate successors
            boolean expanded = expand(currentState);
            if (!expanded) {
                return null; // Exit due to timeout or combinatorial explosion
            }
        }

        return null; // No solution found
    }

    private float[] calculateHeuristicArray(List<I_Location> locations) {
        float[] hArr = new float[agents.size()];
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            I_Location currentLocation = locations.get(i);
            hArr[i] = singleAgentGAndH.getHToTargetFromLocation(agent.target, currentLocation);
        }
        return hArr;
    }

    /**
     * Expands the given parent state by generating all possible joint moves for the agents.
     * @param parentState the state to expand
     * @return true if expansion was successful, false if a timeout occurred or reached a combinatorial explosion
     */
    private boolean expand(MAAStarState parentState) {
        List<List<I_Location>> individualAgentMoves = new ArrayList<>();
        for (int i = 0; i < agents.size(); i++) {
            I_Location currentLoc = parentState.locations.get(i);
            List<I_Location> possibleMoves = new ArrayList<>(currentLoc.outgoingEdges());
            possibleMoves.add(currentLoc); // Add stay move
            individualAgentMoves.add(possibleMoves);
        }

        // Check if we're after the last constraint time
        boolean afterLastConstraint = externalConstraints == null || parentState.time > externalConstraints.getLastConstraintStartTime();

        // Generate all combinations of joint moves
        // Cap the time if we're after the last constraint time, similar to how it's done in SingleAgentAStar
        int time = !afterLastConstraint ? parentState.time + 1 : parentState.time;
        try {
            // this call can lead to a combinatorial explosion, so we use a timeout to handle it gracefully
            List<List<I_Location>> jointMoves = CartesianProductWithTimeout.cartesianProductWithTimeout(individualAgentMoves,
                    super.getTimeout().getTimeoutTimeRemainingMS(), TimeUnit.MILLISECONDS);
            for (List<I_Location> nextLocations : jointMoves) {
                if (checkTimeout()) {
                    return false;
                }
                if (isValidJointMove(parentState.locations, nextLocations, time)) {
                    float[] newGArr = calculateStepCosts(parentState, nextLocations, time);
                    for (int i = 0; i < newGArr.length; i++) {
                        newGArr[i] += parentState.gArr[i]; // Add the cost of the step to the parent's g for each agent
                    }
                    float[] newHArr = calculateHeuristicArray(nextLocations);
                    MAAStarState successor = getMaaStarState(parentState, nextLocations, time, newGArr, newHArr);
                    addToOpenList(successor);
                }
            }
        }
        catch (ExecutionException e) {
            if (Config.WARNING >= 2) {
                System.err.println(getName() + " is only suitable for few agents or very restricted environments due" +
                        " to the combinatorial explosion. Given " + agents.size() + " agents" +
                        ", the Cartesian product of their moves is too large to handle. Exiting search.");
            }
            return false; // Early exit for large problems
        }
        catch (TimeoutException e) {
            // Timeout occurred while generating joint moves
        }
        catch (InterruptedException e) {
            throw new RuntimeException(this.getClass().getSimpleName() + ": Unexpected error while generating joint moves: " + e.getMessage(), e);
        }
        return true;
    }

    private @NotNull MAAStarState getMaaStarState(MAAStarState parentState, List<I_Location> nextLocations, int time, float[] gArr, float[] hArr) {
        return new MAAStarState(time, nextLocations, gArr, hArr, parentState, generatedNodes++);
    }

    protected void addToOpenList(@NotNull MAAStarState state) {
        MAAStarState existingState;
        if (closedList.contains(state)) { // state visited already
            // TODO for inconsistent heuristics -
            //  if the new one has a lower f, remove the old one from closed and add the new one to open
        } else if (null != (existingState = openList.get(state))) { //an equal state is waiting in open
            //keep the one with min G
            keepTheStateWithMinG(state, existingState); //O(LOGn)
        } else { // it's a new state
            openList.add(state);
        }
    }

    protected void keepTheStateWithMinG(MAAStarState existingState, MAAStarState newState) {
        // decide which state to keep, seeing as how they are both equal and in open.
        MAAStarState dropped = openList.keepOne(existingState, newState, EQUAL_STATES_PREFER_SMALLER_G);
        if (dropped == existingState){
            this.numRegeneratedNodes++;
        }
    }

    /**
     * Checks for collisions in a joint move.
     * 1. Vertex collision: Two agents cannot be at the same location at the same time.
     * 2. Swapping/Edge collision: Two agents cannot swap locations along an edge.
     *
     * @param currentLocations The list of locations at time t.
     * @param nextLocations    The list of locations at time t+1.
     * @param time           The time step for the joint move.
     * @return true if the joint move is valid, false otherwise.
     */
    private boolean isValidJointMove(List<I_Location> currentLocations, List<I_Location> nextLocations, int time) {
        if (time == 0){
            // handles the case where there are no constraints at all, giving all states time 0
            time = 1; // to generate valid moves
        }
        // convert to Move objects for standardised processing
        if (currentLocations.size() != nextLocations.size() || currentLocations.size() != agents.size()) {
            throw new IllegalArgumentException("Current and next locations must match the number of agents.");
        }
        Move[] currentMoves = new Move[agents.size()];
        for (int i = 0; i < agents.size(); i++) {
            currentMoves[i] = new Move(agents.get(i), time, currentLocations.get(i), nextLocations.get(i));
        }

        for (int i = 0; i < currentMoves.length - 1; i++) {
            Move move1 = currentMoves[i];

            // check for internal conflicts
            for (int j = i + 1; j < currentMoves.length; j++) {
                Move move2 = currentMoves[j];
                if (A_Conflict.haveConflicts(move1, move2)) {
                    return false;
                }
            }

            // check for conflicts with external constraints
            if (externalConstraints != null) {
                if (externalConstraints.rejects(move1)){
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Calculates the cost of a joint move. As long as an agent is immobile at its target location, it has no cost.
     * Once it moves, it incurs the cost of all the time steps it spent waiting at the target location.
     */
    private float[] calculateStepCosts(MAAStarState parentState, List<I_Location> nextLocations, int time) {
        if (time == 0){
            // handles the case where there are no constraints at all, giving all states time 0
            time = 1; // to generate valid moves
        }
        float[] gArr = new float[agents.size()];
        for (int i = 0; i < agents.size(); i++) {
            I_Location currentLoc = parentState.locations.get(i);
            I_Location nextLoc = nextLocations.get(i);
            Agent agent = agents.get(i);

            // todo - maybe creating too many Move objects here, could be optimised
            if (!(currentLoc.getCoordinate().equals(agent.target) && nextLoc.getCoordinate().equals(agent.target))){
                // unless the agent is waiting at its target location, it incurs a cost for moving
                gArr[i] += singleAgentGAndH.cost(new Move(agent, time, currentLoc, nextLoc));
            }
            // If the agent steps out of its target location, walk back through parents and add the costs of all the
            // wait-at-target actions that we got for free
            if (currentLoc.getCoordinate().equals(agent.target) && !nextLoc.getCoordinate().equals(agent.target)) {
                boolean done = false;
                MAAStarState currentParent = parentState.parent;
                MAAStarState currentState = parentState;
                while (currentParent != null && !done) {
                    if (currentParent.locations.get(i).getCoordinate().equals(agent.target)) {
                        // The agent was at the target location in the previous state, so it was waiting there
                        gArr[i]  += singleAgentGAndH.cost(new Move(agent, time, currentParent.locations.get(i), currentState.locations.get(i)));
                        currentState = currentParent;
                        currentParent = currentParent.parent;
                    } else {
                        done = true; // before started waiting at the target location
                    }
                }
            }
        }
        return gArr;
    }

    private boolean isGoal(MAAStarState currentState) {
        return currentState.locations.equals(goalLocations);
    }

    private Solution reconstructSolution(MAAStarState goalState) {
        ArrayList<MAAStarState> solutionStatesSequence = new ArrayList<>();
        solutionStatesSequence.add(goalState);
        while (goalState.parent != null) {
            goalState = goalState.parent;
            solutionStatesSequence.add(goalState);
        }
        Collections.reverse(solutionStatesSequence); // Reverse to get the correct order from start to goal

        ArrayList<ArrayList<Move>> plans = new ArrayList<>(agents.size());
        for (Agent agent : agents) {
            plans.add(new ArrayList<>(solutionStatesSequence.size()));
        }

        for (int stateIndex = 1; stateIndex < solutionStatesSequence.size(); stateIndex++) {
            MAAStarState state = solutionStatesSequence.get(stateIndex);
            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                I_Location currentLoc = state.locations.get(i);
                I_Location prevLoc = state.parent.locations.get(i);
                // patch move times in case we had moves that don't progress time, because they were after last constraint time
                int correctedTime = state.parent.time == state.time ? problemStartTime + stateIndex : state.time;
                Move move = new Move(agent, correctedTime, prevLoc, currentLoc);
                plans.get(i).add(move);
            }
        }

        // all wait-at-target moves at the end of the plan should be discarded
        for (int i = 0; i < plans.size(); i++) {
            ArrayList<Move> agentPlan = plans.get(i);
            int currMoveIndex = agentPlan.size() - 1;
            while (currMoveIndex > 0 && agentPlan.get(currMoveIndex).prevLocation.getCoordinate().equals(agents.get(i).target)) {
                agentPlan.remove(currMoveIndex);
                currMoveIndex--;
            }
        }

        Solution solution = new Solution();
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            SingleAgentPlan singleAgentPlan = new SingleAgentPlan(agent, plans.get(i));
            solution.putPlan(singleAgentPlan);
        }

        if (Config.DEBUG >= 2){
            if (Math.round(goalState.g) != solution.sumIndividualCosts() ) {
                throw new IllegalStateException("The total cost of the solution does not match the cost of the goal state. " +
                        "Goal state cost: " + Math.round(goalState.g) + ", Solution cost: " + solution.sumIndividualCosts());
            }
            if (Math.round(goalState.h) != 0){
                throw new IllegalStateException("The heuristic of the goal state should be 0, but it is: " + Math.round(goalState.h));
            }
        }
        return solution;
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction,
                this.openListComparator == F_THEN_H_SOC_OPEN_LIST_COMP ? SumOfCosts.NAME : this.openListComparator.getClass().getSimpleName());
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.openList = null;
        this.closedList = null;
        this.agents = null;
        this.singleAgentGAndH = null;
        this.externalConstraints = null;
        this.goalLocations = null;
    }

}
