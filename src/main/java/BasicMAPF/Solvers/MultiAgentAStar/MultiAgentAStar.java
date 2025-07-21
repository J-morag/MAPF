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

    /* = Constants = */
    private static final Comparator<MAAStarState> EQUAL_STATES_PREFER_SMALLER_G = Comparator.comparingDouble(MAAStarState::getG)
            .thenComparingInt(state -> state.id); // Tie-breaker based on id
    private static final Comparator<MAAStarState> F_THEN_H_SOC_OPEN_LIST_COMP = MAAStarState::compareTo;

    /* = Solver fields = */
    private final Comparator<MAAStarState> openListComparator;

    /* = Run fields = */
    private I_OpenList<MAAStarState> openList;
    private HashSet<MAAStarState> closedList;
    protected SingleAgentGAndH singleAgentGAndH;
    protected I_ConstraintSet externalConstraints;
    protected List<Agent> agents;
    private List<I_Location> goalLocations;
    private int numRegeneratedNodes;
    protected int problemStartTime;

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

        this.goalLocations = new ArrayList<>(agents.size());
        List<I_Location> startLocations = new ArrayList<>(agents.size());
        for (Agent agent : this.agents) {
            startLocations.add(instance.map.getMapLocation(agent.source));
            this.goalLocations.add(instance.map.getMapLocation(agent.target));
        }

        // Create and add the root state
        float[] initialHArr = calculateHeuristicArray(startLocations, null);
        MAAStarState root = getRootState(startLocations, initialHArr);
        this.openList.add(root);
    }

    protected @NotNull MAAStarState getRootState(List<I_Location> startLocations, float[] initialHArr) {
        return getMaaStarState(null, startLocations, new Move[startLocations.size()], problemStartTime, new float[this.agents.size()] /*zeros*/, initialHArr);
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

    protected float[] calculateHeuristicArray(List<I_Location> locations, @Nullable MAAStarState parentState) {
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
        // Check if we're after the last constraint time
        boolean afterLastConstraint = externalConstraints == null || parentState.time > externalConstraints.getLastConstraintStartTime();
        int time = getNextTime(parentState, afterLastConstraint);
        return expandHelper(parentState, time);
    }

    protected int getNextTime(MAAStarState parentState, boolean afterLastConstraint) {
        // Cap the time if we're after the last constraint time, similar to how it's done in SingleAgentAStar
        return  !afterLastConstraint ? parentState.time + 1 : parentState.time;
    }

    /**
     * Generates all possible joint moves for the agents based on their individual possible moves.
     * This method uses a Cartesian product to generate all combinations of moves.
     * If the Cartesian product is too large, it will exit gracefully due to a timeout.
     * @param parentState the state to expand
     * @param time the time step for the joint move
     * @return true if expansion was successful, false if a timeout occurred or reached a combinatorial explosion
     */
    protected boolean expandHelper(MAAStarState parentState, int time) {
        List<List<I_Location>> individualAgentMoves = new ArrayList<>();
        for (int i = 0; i < agents.size(); i++) {
            I_Location currentLoc = parentState.locations.get(i);
            List<I_Location> possibleMoves = new ArrayList<>(currentLoc.outgoingEdges());
            possibleMoves.add(currentLoc); // Add stay move
            individualAgentMoves.add(possibleMoves);
        }
        try {
            // Generate all combinations of joint moves
            // this call can lead to a combinatorial explosion, so we use a timeout to handle it gracefully
            List<List<I_Location>> jointMoves = CartesianProductWithTimeout.cartesianProductWithTimeout(individualAgentMoves,
                    super.getTimeout().getTimeoutTimeRemainingMS(), TimeUnit.MILLISECONDS);
            for (List<I_Location> nextLocations : jointMoves) {
                if (checkTimeout()) {
                    return false;
                }
                expandHelper2(parentState, time, nextLocations);
            }
        }
        catch (ExecutionException e) {
            if (Config.WARNING >= 2) {
                System.err.println(getName() + " is only suitable for few agents or very restricted environments due" +
                        " to the combinatorial explosion. Given " + agents.size() + " agents" +
                        ", the Cartesian product of their moves is too large to handle. Exiting search.");
            }
            return false;
        }
        catch (TimeoutException e) {
            // Timeout occurred while generating joint moves
        }
        catch (InterruptedException e) {
            throw new RuntimeException(this.getClass().getSimpleName() + ": Unexpected error while generating joint moves: " + e.getMessage(), e);
        }
        return true;
    }

    protected void expandHelper2(MAAStarState parentState, int time, List<I_Location> nextLocations) {
        Move[] moves = getMovesToNextLocations(parentState, nextLocations, time);
        if (isValidJointMove(moves, parentState)) {
            float[] newGArr = calculateStepCosts(parentState, moves);
            for (int i = 0; i < newGArr.length; i++) {
                newGArr[i] += parentState.gArr[i]; // Add the cost of the step to the parent's g for each agent
            }
            float[] newHArr = calculateHeuristicArray(nextLocations, parentState);
            MAAStarState successor = getMaaStarState(parentState, nextLocations, moves, time, newGArr, newHArr);
            addToOpenList(successor);
        }
    }

    protected Move[] getMovesToNextLocations(MAAStarState parentState, List<I_Location> nextLocations, int time) {
        if (time == 0){
            // handles the case where there are no constraints at all, giving all states time 0
            time = 1; // to generate valid moves
        }
        Move[] moves = new Move[agents.size()];
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            I_Location currentLoc = parentState.locations.get(i);
            I_Location nextLoc = nextLocations.get(i);
            moves[i] = new Move(agent, time, currentLoc, nextLoc);
        }
        return moves;
    }

    protected @NotNull MAAStarState getMaaStarState(MAAStarState parentState, List<I_Location> nextLocations, Move[] moves, int time, float[] gArr, float[] hArr) {
        return new MAAStarState(time, nextLocations, moves, gArr, hArr, parentState, generatedNodes++);
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
     * @param moves            The joint moves to check for collisions.
     * @param parentState
     * @return true if the joint move is valid, false otherwise.
     */
    protected boolean isValidJointMove(Move[] moves, MAAStarState parentState) {
        for (int i = 0; i < moves.length - 1; i++) {
            Move move1 = moves[i];

            // check for internal conflicts
            for (int j = i + 1; j < moves.length; j++) {
                Move move2 = moves[j];
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
    private float[] calculateStepCosts(MAAStarState parentState, Move[] moves) {
        float[] gArr = new float[agents.size()];
        calculateStepCostsHelper(parentState, moves, gArr);
        return gArr;
    }

    protected void calculateStepCostsHelper(MAAStarState parentState, Move[] moves, float[] gArr) {
        for (int i = 0; i < agents.size(); i++) {
            updateIndexAgentMoveCost(parentState, moves[i], i, gArr);
        }
    }

    protected void updateIndexAgentMoveCost(MAAStarState parentState, Move move, int i, float[] gArr) {
        Agent agent = agents.get(i);
        int currentAgentMoveCost = 0;

        if (!(move.prevLocation.getCoordinate().equals(agent.target) && move.currLocation.getCoordinate().equals(agent.target))){
            // unless the agent is waiting at its target location, it incurs a cost
            currentAgentMoveCost += singleAgentGAndH.cost(move);
        }
        int costOfSkippedWaitAtTargetActions = getCostOfSkippedWaitAtTargetActions(parentState, move, i, agent);
        gArr[i] = currentAgentMoveCost + costOfSkippedWaitAtTargetActions;
    }

    protected int getCostOfSkippedWaitAtTargetActions(MAAStarState parentState, Move move, int i, Agent agent) {
        int costOfSkippedWaitAtTargetActions = 0;
        // If the agent steps out of its target location, walk back through parents and add the costs of all the
        // wait-at-target actions that we got for free
        if (move.prevLocation.getCoordinate().equals(agent.target) && !move.currLocation.getCoordinate().equals(agent.target)) {
            boolean done = false;
            MAAStarState currentParent = parentState.parent;
            MAAStarState currentState = parentState;
            while (currentParent != null && !done) {
                if (currentParent.locations.get(i).getCoordinate().equals(agent.target)) {
                    // The agent was at the target location in the previous state, so it was waiting there
                    costOfSkippedWaitAtTargetActions += singleAgentGAndH.cost(currentState.moves[i]);
                    currentState = currentParent;
                    currentParent = currentParent.parent;
                } else {
                    done = true; // before started waiting at the target location
                }
            }
        }
        return costOfSkippedWaitAtTargetActions;
    }

    protected boolean isGoal(MAAStarState currentState) {
        return currentState.locations.equals(goalLocations);
    }

    private Solution reconstructSolution(MAAStarState goalState) {
        ArrayList<MAAStarState> solutionStatesSequence = getSolutionStatesSequence(goalState);

        ArrayList<ArrayList<Move>> plans = new ArrayList<>(agents.size());
        for (Agent agent : agents) {
            plans.add(new ArrayList<>(solutionStatesSequence.size()));
        }

        for (int stateIndex = 1; stateIndex < solutionStatesSequence.size(); stateIndex++) {
            MAAStarState state = solutionStatesSequence.get(stateIndex);
            for (int agentIdx = 0; agentIdx < agents.size(); agentIdx++) {
                Agent agent = agents.get(agentIdx);

                Move move;
                if (solutionStatesSequence.get(stateIndex-1).time == state.time){
                    // patch move times in case we had moves that don't progress time, because they were after last constraint time
                    I_Location currentLoc = state.locations.get(agentIdx);
                    I_Location prevLoc = solutionStatesSequence.get(stateIndex-1).locations.get(agentIdx);
                    int correctedTime = problemStartTime + stateIndex;
                    move = new Move(agent, correctedTime, prevLoc, currentLoc);
                }
                else{
                    move = state.moves[agentIdx];
                }
                plans.get(agentIdx).add(move);
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

        if (Config.DEBUG >= 2 && openListComparator == F_THEN_H_SOC_OPEN_LIST_COMP) {
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

    protected @NotNull ArrayList<MAAStarState> getSolutionStatesSequence(MAAStarState goalState) {
        ArrayList<MAAStarState> solutionStatesSequence = new ArrayList<>();
        solutionStatesSequence.add(goalState);
        while (goalState.parent != null) {
            goalState = goalState.parent;
            solutionStatesSequence.add(goalState);
        }
        Collections.reverse(solutionStatesSequence); // Reverse to get the correct order from start to goal
        return solutionStatesSequence;
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
