package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Metrics.InstanceReport;
import TransientMAPF.SeparatingVerticesFinder;
import LifelongMAPF.I_LifelongCompatibleSolver;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import TransientMAPF.TransientMAPFUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * Lazy Constraints Addition Search.
 * Okumura, Keisuke. "Lacam: Search-based algorithm for quick multi-agent pathfinding." Proceedings of the AAAI Conference on Artificial Intelligence. 2023.
 */
public class LaCAM_Solver extends A_Solver implements I_LifelongCompatibleSolver {

    /**
     * open is a stack of high-level nodes.
     * The use of a stack means that the algorithm performs a Depth First Search.
     */
    protected Stack<HighLevelNode> open;

    /**
     * HashMap to manage configurations that the algorithm already saw.
     */
    protected HashMap<HashMap<Agent, I_Location>, HighLevelNode> explored;

    /**
     * HashMap to manage configurations that the algorithm already saw.
     * This map is used when the solver need to handle constraints, so time step is included in the configuration.
     * For each configuration, we can save several High level nodes, each one with different time step.
     */
    protected HashMap<HashMap<Agent, I_Location>, List<HighLevelNode>> exploredWithExternalConstraints;

    /**
     * boolean determine whether to use explored or exploredWithExternalConstraints for duplicate detection.
     * if true, use exploredWithExternalConstraints. else, use explored.
     */

    protected boolean needToHandleConstraints;

    /**
     * heuristic to use in the low level search to find the closest nodes to an agent's goal
     */
    protected SingleAgentGAndH heuristic;

    /**
     * Map saving for each ID its agent as object.
     */
    protected HashMap<Integer, Agent> agents;

    /**
     * The cost function to evaluate solutions with.
     */
    protected final I_SolutionCostFunction solutionCostFunction;

    /**
     * variable indicates whether the solution returned by the algorithm is transient.
     */
    protected final TransientMAPFSettings transientMAPFSettings;

    /**
     * indicate if it needs to return partial plans in case PIBT can't find solution.
     * if true, instead of returning null, the current (partial) solution will return.
     */
    private final boolean returnPartialSolutions;

    protected I_ConstraintSet constraintsSet;

    protected MAPF_Instance instance;

    protected int failedToFindConfigCounter;
    protected long totalTimeFindConfigurations;
    protected HashMap<Agent, I_Location> occupiedNowConfig;
    protected HashMap<Agent, I_Location> occupiedNextConfig;
    protected int improveVisitsCounter;
    protected Set<I_Location> separatingVerticesSet;
    protected Comparator<I_Location> separatingVerticesComparator;
    protected int timeStep;
    protected HashMap<Agent, Boolean> currentAgentsReachedGoalsMap;
    private int problemStartTime;

    private HighLevelNode bestNodeForPartialSolution;
    private int bestNodeForPartialSolutionNumberOfGoals;
    /**
     * How far forward in time to consider conflicts. Further than this time conflicts will be ignored.
     */
    private final Integer RHCR_Horizon;

    /**
     * if agents share goals, they will not conflict at their goal.
     */
    public boolean ignoresStayAtSharedGoals;


    /**
     * Constructor.
     * @param solutionCostFunction how to calculate the cost of a solution
     * @param transientMAPFSettings indicates whether to solve transient-MAPF.
     * @param RHCR_Horizon How far forward in time to consider conflicts, relevant for RHCR.
     */
    LaCAM_Solver(I_SolutionCostFunction solutionCostFunction, TransientMAPFSettings transientMAPFSettings, Integer RHCR_Horizon, Boolean returnPartialSolutions, Boolean ignoresStayAtSharedGoals) {
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        this.returnPartialSolutions = Objects.requireNonNullElse(returnPartialSolutions, false);
        this.ignoresStayAtSharedGoals = Objects.requireNonNullElse(ignoresStayAtSharedGoals, false);
        this.RHCR_Horizon = Objects.requireNonNullElse(RHCR_Horizon, Integer.MAX_VALUE);;
        super.name = "LaCAM" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "") + (this.transientMAPFSettings.avoidSeparatingVertices() ? "_SV" : "");
    }

    /**
     * Default constructor.
     */
    public LaCAM_Solver() {
        this(null, null, null, null, null);
    }

    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.constraintsSet = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.open = new Stack<>();
        this.needToHandleConstraints = !this.constraintsSet.isEmpty();
        this.explored = new HashMap<>();
        if (!this.constraintsSet.isEmpty()) {
            this.exploredWithExternalConstraints = new HashMap<>();
        }
        this.agents = new HashMap<>();
        this.failedToFindConfigCounter = 0;
        this.totalTimeFindConfigurations = 0;
        this.instance = instance;
        this.occupiedNowConfig = new HashMap<>();
        this.occupiedNextConfig = new HashMap<>();
        this.timeStep = parameters.problemStartTime + 1;
        this.problemStartTime = parameters.problemStartTime + 1;
        this.improveVisitsCounter = 0;
        this.bestNodeForPartialSolution = null;
        this.bestNodeForPartialSolutionNumberOfGoals = 0;
        this.currentAgentsReachedGoalsMap = null;
        if (this.transientMAPFSettings.avoidSeparatingVertices()) {
            if (parameters.separatingVertices != null) {
                this.separatingVerticesSet = parameters.separatingVertices;
            }
            else {
                if (this.instance.map instanceof I_ExplicitMap) {
                    this.separatingVerticesSet = SeparatingVerticesFinder.findSeparatingVertices((I_ExplicitMap) this.instance.map);
                }
                else {
                    throw new IllegalArgumentException("Transient using Separating Vertices only supported for I_ExplicitMap.");
                }
            }
            this.separatingVerticesComparator = TransientMAPFUtils.createSeparatingVerticesComparator(this.separatingVerticesSet);
        }
        // distance between every vertex in the graph to each agent's goal
        this.heuristic = Objects.requireNonNullElseGet(parameters.singleAgentGAndH, () -> new DistanceTableSingleAgentHeuristic(instance.agents, instance.map));
    }
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        HashMap<Agent, I_Location> initialConfiguration = new HashMap<>();
        for (Agent agent : instance.agents) {
            initialConfiguration.put(agent, instance.map.getMapLocation(agent.source));
            this.agents.put(agent.iD, agent);
        }
        LowLevelNode C_init = initLowLevelRoot();
        HashMap<Agent, Float> priorities = initPriorities(initialConfiguration);
        ArrayList<Agent> order = sortByPriority(priorities);
        HighLevelNode N_init = initHighLevelRoot(initialConfiguration, C_init,order, priorities, null);
        this.open.push(N_init);

        if (needToCheckConflicts() && this.needToHandleConstraints) {
            List<HighLevelNode> nodesList = this.exploredWithExternalConstraints.get(initialConfiguration);
            if (nodesList == null) {
                nodesList = new ArrayList<>();
                this.exploredWithExternalConstraints.put(initialConfiguration, nodesList);
            }
            nodesList.add(N_init);

            this.explored.put(initialConfiguration, N_init);
        }
        else {
            this.explored.put(initialConfiguration, N_init);
        }

        while (!this.open.empty() && !checkTimeout()) {
            HighLevelNode N = this.open.peek();

            // reached goal configuration, stop and backtrack to return the solution
            if (reachedGoalConfiguration(N)) {
                Solution solution = backTrack(N);
                if (solution != null) {
                    return solution;
                }
            }

            // finished low level search
            if (N.tree.isEmpty()) {
                this.open.pop();
                continue;
            }

            // low-level search successors
            LowLevelNode C = N.tree.poll();
            if (C.depth < instance.agents.size()) {
                Agent chosenAgent = N.order.get(C.depth);
                I_Location chosenLocation = N.configuration.get(chosenAgent);
                List<I_Location> locations = new ArrayList<>(findAllNeighbors(chosenLocation));

                // add current location
                locations.add(chosenLocation);

                // instead of random inserting of low-level nodes
                // shuffle the order of the location so that the inserting will be randomized
                Collections.shuffle(locations);

                LowLevelNode tmpC = C;
                while (tmpC.who != null) {
                    // found a higher priority agent that wants to move to chosenAgent's current location
                    // so, chosenAgent cannot stay at its current location
                    if (tmpC.where == chosenLocation) {
                        locations.remove(N.configuration.get(tmpC.who));
                    }

                    // current agent can't go to a location chosen by previous agent in the low level tree
                    // avoid vertex conflict
                    locations.remove(tmpC.where);
                    tmpC = tmpC.parent;
                }

                for (I_Location location : locations) {
                    LowLevelNode C_new = new LowLevelNode(C, chosenAgent, location);
                    this.totalLowLevelNodesGenerated++;
                    N.tree.add(C_new);
                }
            }

            long startTime = System.currentTimeMillis();
            HashMap<Agent, I_Location> newConfiguration = getNewConfig(N,C);
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            this.totalTimeFindConfigurations += elapsedTime;

            // algorithm couldn't find configuration
            if (newConfiguration == null) {
                this.failedToFindConfigCounter++;
                continue;
            }

            HighLevelNode reInsertionNode = null;
            if (needToCheckConflicts() && this.needToHandleConstraints) {
                List<HighLevelNode> nodesWithSameLocations = this.exploredWithExternalConstraints.get(newConfiguration);
                if (nodesWithSameLocations != null) {
                    for (HighLevelNode node : nodesWithSameLocations) {
                        if (node.timeStep == this.timeStep) {
                            reInsertionNode = node;
                        }
                    }
                }
            }
            else {
                reInsertionNode = this.explored.get(newConfiguration);
            }


            if (!this.transientMAPFSettings.isTransientMAPF()) {
                if (reInsertionNode != null) {
                    // re-insertion of already seen configuration
                    // by reference
                    this.open.push(reInsertionNode);
                }

                else {
                    createNewHighLevelNode(N, newConfiguration, C_init);
                }
            }

            else {
                if (reInsertionNode != null && reinsertionImproveVisits(N, reInsertionNode)) {
                    this.improveVisitsCounter++;
                    this.open.push(reInsertionNode);
                }
                else {
                    createNewHighLevelNode(N, newConfiguration, C_init);
                }
            }
        }
        if (this.returnPartialSolutions) {
            return backTrack(this.bestNodeForPartialSolution);
        }
        else {
            return null;
        }
    }

    private void findBestNodeToBacktrack(HighLevelNode newHighLevelNode) {
        int counterInGoal = 0;
        for (Map.Entry<Agent, I_Location> entry : newHighLevelNode.configuration.entrySet()) {
            Agent agent = entry.getKey();
            I_Location location = entry.getValue();
            if (this.transientMAPFSettings.isTransientMAPF()) {
                if (location.equals(getAgentsTarget(agent))) {
                    counterInGoal++;
                }
            }
            else {
                if (newHighLevelNode.reachedGoalsMap.get(agent)) {
                    counterInGoal++;
                }
            }
        }

        if (counterInGoal >= this.bestNodeForPartialSolutionNumberOfGoals) {
            this.bestNodeForPartialSolution = newHighLevelNode;
            this.bestNodeForPartialSolutionNumberOfGoals = counterInGoal;
        }
    }


    /**
     * relevant to TMAPF.
     * Whenever a high-level node chosen for reinsertion, the following function checks whether it good at least as the current high-level node,
     * according to reachedGoalMap.
     * If at least one agent reached its goal in currentNode, and did not reach its goal in reInsertionNode, the function return false.
     * @param currentNode - current high-level node.
     * @param reInsertionNode - high-level node chosen for reinsertion.
     * @return true if reInsertionNode improves currentNode., else false.
     */
    protected boolean reinsertionImproveVisits(HighLevelNode currentNode, HighLevelNode reInsertionNode) {
        for (Map.Entry<Agent, Boolean> entry : currentNode.reachedGoalsMap.entrySet()) {
            Agent currentAgent = entry.getKey();
            boolean reachedGoal = entry.getValue();
            if (reachedGoal && !reInsertionNode.reachedGoalsMap.get(currentAgent)) {
                return false;
            }
        }
        return true;
    }

    /**
     * The following function responsible for creation of high-level nodes.
     * @param N - current high-level node, will be the parent of the new node.
     * @param newConfiguration - configuration of the new high-level node.
     * @param C_init - low-level tree.
     */
    protected void createNewHighLevelNode(HighLevelNode N, HashMap<Agent, I_Location> newConfiguration, LowLevelNode C_init) {
        HashMap<Agent, Float> newPriorities = updatePriorities(N, newConfiguration);
        ArrayList<Agent> newOrder = sortByPriority(newPriorities);
        HighLevelNode N_new = new HighLevelNode(newConfiguration, C_init, newOrder, newPriorities, N, this.timeStep);

        for (Map.Entry<Agent, Boolean> entry : N_new.reachedGoalsMap.entrySet()) {
            Agent agent = entry.getKey();
            Boolean reachedGoal = entry.getValue();
            if (!reachedGoal && N_new.configuration.get(agent).getCoordinate().equals(agent.target)) {
                N_new.reachedGoalsMap.put(agent, true);
            }
        }
        this.generatedNodes++;
        this.open.push(N_new);

        if (needToCheckConflicts() && this.needToHandleConstraints) {
            List<HighLevelNode> nodesList = this.exploredWithExternalConstraints.get(newConfiguration);
            if (nodesList == null) {
                nodesList = new ArrayList<>();
                this.exploredWithExternalConstraints.put(newConfiguration, nodesList);
            }
            nodesList.add(N_new);

            this.explored.put(newConfiguration, N_new);

        }
        else {
            this.explored.put(newConfiguration, N_new);
        }

        if (this.returnPartialSolutions) {findBestNodeToBacktrack(N_new);}
    }

    /**
     * The following function responsible for creation of new low-level nodes.
     * The function initialize new low-level node and increase a low-level node counter.
     */
    protected LowLevelNode initLowLevelRoot() {
        LowLevelNode C_init = new LowLevelNode(null, null, null);
        this.totalLowLevelNodesGenerated++;
        return C_init;
    }

    /**
     * The following function responsible for creation of new high-level nodes.
     * The function initialize new high-level node and increase a high-level node counter.
     */
    protected HighLevelNode initHighLevelRoot(HashMap<Agent, I_Location> configuration, LowLevelNode root, ArrayList<Agent> order, HashMap<Agent, Float> priorities, HighLevelNode parent) {
        HighLevelNode N_init = new HighLevelNode(configuration, root, order, priorities, parent, this.timeStep);
        this.generatedNodes++;
        this.bestNodeForPartialSolution = N_init;
        this.bestNodeForPartialSolutionNumberOfGoals = 0;
        return N_init;
    }

    /**
     * This function determine whether the current configuration is the goal configuration.
     * @return boolean. true if configurations is the goal configuration, false otherwise.
     */
    protected boolean reachedGoalConfiguration(HighLevelNode N) {
        HashMap<Agent, I_Location> configuration = N.configuration;
        if (!this.transientMAPFSettings.isTransientMAPF()) {
            for (Map.Entry<Agent, I_Location> entry : configuration.entrySet()) {
                Agent currentAgent = entry.getKey();
                I_Location currentLocation = entry.getValue();
                if (!(currentLocation.equals(getAgentsTarget(currentAgent)))) {
                    return false;
                }
            }
        }
        else {
            for (Boolean reachedGoal : N.reachedGoalsMap.values()) {
                if (!reachedGoal) {
                    return false;
                }
            }
        }

        // final check - if all agents can stay at their current locations for infinite time
        if (this.needToHandleConstraints) {
            for (Agent agent : this.instance.agents) {
                Move finalMove = getNewMove(agent, this.occupiedNextConfig.get(agent), this.occupiedNowConfig.get(agent));
                if (this.constraintsSet.lastRejectionTime(finalMove) != -1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This function is called when the algorithm reaches the Goal configuration,
     * this function performs backtracking to find the path from the start configuration to the goal.
     * @param N - current High Level Node.
     * @return Solution for the MAPF problem.
     */
    protected Solution backTrack(HighLevelNode N) {
        if (N == null) return null;
        HashMap<Agent, SingleAgentPlan> agentPlans = new HashMap<>();
        for (Agent agent : this.instance.agents) {
            agentPlans.put(agent, new SingleAgentPlan(agent));
        }
        Stack<HighLevelNode> configurationsInReverse = new Stack<>();
        configurationsInReverse.push(N);
        while (N.parent != null) {
            configurationsInReverse.push(N.parent);
            N = N.parent;
        }
        while (configurationsInReverse.size() != 1) {
            HighLevelNode currentNode = configurationsInReverse.pop();
            HighLevelNode nextNode = configurationsInReverse.peek();
            HashMap<Agent, I_Location> currentConfig = currentNode.configuration;
            HashMap<Agent, I_Location> nextConfig = nextNode.configuration;
            for (Agent agent : this.instance.agents) {
                I_Location currentLocation = currentConfig.get(agent);
                I_Location nextLocation = nextConfig.get(agent);
                Move newMove = new Move(agent, currentNode.timeStep, currentLocation, nextLocation);
                agentPlans.get(agent).addMove(newMove);
            }
        }

        // init an empty solution
        Solution solution = transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution() : new Solution();
        int numberOfNotMovingAgents = 0;
        for (Agent agent : instance.agents) {
            SingleAgentPlan plan = agentPlans.get(agent);
            // trim the plan to remove excess "stay" moves at the end

            if (plan.size() == 0) {
                // if agent didn't make a move, add a move to stay in current location
                plan.addMove(new Move(agent, this.timeStep, N.configuration.get(agent), N.configuration.get(agent)));
                solution.putPlan(plan);
                numberOfNotMovingAgents++;
                continue;
            }

            int lastChangedLocationTime = plan.getEndTime();
            for (; lastChangedLocationTime >= 1; lastChangedLocationTime--) {
                if (plan.moveAt(lastChangedLocationTime) != null && ! plan.moveAt(lastChangedLocationTime).prevLocation.equals(plan.getLastMove().currLocation)) {
                    break;
                }
            }
            if (lastChangedLocationTime < plan.getEndTime() && lastChangedLocationTime > 0) {
                SingleAgentPlan trimmedPlan = new SingleAgentPlan(agent);
                for (int t = 1; t <= lastChangedLocationTime; t++) {
                    if (plan.moveAt(t) != null) {
                        trimmedPlan.addMove(plan.moveAt(t));
                    }
                }
                solution.putPlan(trimmedPlan);
            }
            else solution.putPlan(agentPlans.get(agent));
        }

        // if all agents have not moved, return null
        if (numberOfNotMovingAgents == this.instance.agents.size()) {
            return null;
        }

        return solution;
    }

    /**
     * Init priority of each agent for a new High-Level node without a parent.
     * Initialized priorities based on distance heuristic.
     * @return priorities, hashmap stores for each agent his priority.
     */
    protected HashMap<Agent, Float> initPriorities(HashMap <Agent, I_Location> currentConfiguration) {
        HashMap<Agent, Float> priorities = new HashMap<>();
        int numberOfAgents = currentConfiguration.keySet().size();
        for (Map.Entry<Agent, I_Location> entry : currentConfiguration.entrySet()) {
            Agent agent = entry.getKey();
            I_Location location = entry.getValue();
            Float priority = ((float) this.heuristic.getHToTargetFromLocation(agent.target, location) / numberOfAgents);
            priorities.put(agent, priority);
        }
        return priorities;
    }

    /**
     * Update priority of each agent based on parent and current configuration in specific High-Level node.
     * @param parentNode - parent node of current high-level node.
     * @param newConfiguration - the configuration of current high-level node.
     * @return priorities, hashmap stores for each agent its priority.
     */
    protected HashMap<Agent, Float> updatePriorities(HighLevelNode parentNode, HashMap<Agent, I_Location> newConfiguration) {
        HashMap<Agent, Float> priorities = new HashMap<>();
        for (Map.Entry<Agent, Float> entry : parentNode.priorities.entrySet()) {
            Agent agent = entry.getKey();
            Float currentPriority = entry.getValue();

            // TMAPF
            if (this.transientMAPFSettings.isTransientMAPF()) {
                // agent reached target
                if (parentNode.reachedGoalsMap.get(agent) || newConfiguration.get(agent).getCoordinate().equals(agent.target)) {
                    priorities.put(agent, currentPriority - currentPriority.intValue());
                }
                else {
                    priorities.put(agent, currentPriority + 1);
                }
            }

            // regular MAPF
            else {
                if (newConfiguration.get(agent).getCoordinate().equals(agent.target)) {
                    priorities.put(agent, currentPriority - currentPriority.intValue());
                }
                else {
                    priorities.put(agent, currentPriority + 1);
                }
            }
        }
        return priorities;
    }

    /**
     * This function returns a sorted list of agent based on their priorities.
     * The sort is in descending order.
     * @param priorities - HashMap maps for each agent its priority.
     * @return sorted list of agents.
     */
    protected ArrayList<Agent> sortByPriority(HashMap<Agent, Float> priorities) {
        List<Map.Entry<Agent, Float>> entryList = new ArrayList<>(priorities.entrySet());
        entryList.sort(Map.Entry.comparingByValue());
        ArrayList<Agent> sortedAgents = new ArrayList<>();
        for (int i = entryList.size() - 1; i >= 0; i--) {
            sortedAgents.add(entryList.get(i).getKey());
        }
        return sortedAgents;
    }

    /**
     * Creates new configuration using PIBT.
     * @param N current high-level node.
     * @param C current low-level node.
     * @return HashMap representing the new configuration, maps for each agent its next location.
     */
    protected HashMap<Agent, I_Location> getNewConfig(HighLevelNode N, LowLevelNode C) {
        this.occupiedNowConfig = new HashMap<>();
        this.occupiedNextConfig = new HashMap<>();

        // set occupied now
        for (Map.Entry<Agent, I_Location> entry : N.configuration.entrySet()) {
            Agent agent = entry.getKey();
            I_Location agentLocation = entry.getValue();
            this.occupiedNowConfig.put(agent, agentLocation);
        }

        // bottom of low level tree - each agent have a constraint
        // exactly one configuration is possible
        if (C.depth == this.instance.agents.size()) {

            // check that low-level do not conflict with problem constraints.
            if (this.needToHandleConstraints) {
                Move newMove = getNewMove(C.who, C.where, this.occupiedNowConfig.get(C.who));
                if (!this.constraintsSet.accepts(newMove)) {
                    return null;
                }
            }
            while (C.parent != null) {

                // check that low-level do not conflict with problem constraints.
                if (needToCheckConflicts() && this.needToHandleConstraints) {
                    Move newMove = getNewMove(C.who, C.where, this.occupiedNowConfig.get(C.who));
                    if (!this.constraintsSet.accepts(newMove)) {
                        return null;
                    }
                }
                this.occupiedNextConfig.put(C.who, C.where);
                C = C.parent;
            }
            return this.occupiedNextConfig;
        }

        // create constraints according to the low-level node
        else {
            while (C.parent != null) {

                // vertex conflict
                if (needToCheckConflicts() && this.occupiedNextConfig.containsValue(C.where)) {
                    return null; // vertex conflict detected!
                }

                // swap conflict
                I_Location currentLocation = occupiedNowConfig.get(C.who);
                I_Location nextLocation = C.where;
                for (Map.Entry<Agent, I_Location> entry : occupiedNowConfig.entrySet()) {
                    Agent otherAgent = entry.getKey();
                    I_Location otherAgentLocation = entry.getValue();
                    if (needToCheckConflicts() && nextLocation.equals(otherAgentLocation) && !otherAgent.equals(C.who)
                            && this.occupiedNowConfig.get(otherAgent) != null
                            && !this.occupiedNowConfig.get(otherAgent).equals(currentLocation)) {
                        return null; // Swap conflict detected!
                    }
                }

                // check that low-level do not conflict with problem constraints.
                if (needToCheckConflicts() && this.needToHandleConstraints) {
                    Move newMove = getNewMove(C.who, nextLocation, currentLocation);
                    if (!this.constraintsSet.accepts(newMove)) {
                        return null;
                    }
                }
                this.occupiedNextConfig.put(C.who, C.where);
                C = C.parent;
            }
        }

        this.timeStep = N.timeStep;
        this.currentAgentsReachedGoalsMap = N.reachedGoalsMap;
        for (Agent agent : N.order) {
            if (this.occupiedNextConfig.containsKey(agent)) continue; // move already chosen for agent or agent has a constraint
            if (!solvePIBT(agent, null)) {
                return null;
            }
        }
        return this.occupiedNextConfig;
    }

    /**
     * Helper function to getNewConfig.
     * This function simulates PIBT algorithm in order to find the best next move for each agent, based on a priority order.
     * @param currentAgent - current agent making a move.
     * @param higherPriorityAgent - higher priority agent which current agents inherits priority from. Can be null.
     * @return boolean. Whether found valid move to currentAgent.
     */
    protected boolean solvePIBT(Agent currentAgent, @Nullable Agent higherPriorityAgent) {

        I_Location currentLocation = this.occupiedNowConfig.get(currentAgent);
        List<I_Location> candidates = new ArrayList<>(findAllNeighbors(currentLocation));

        if (higherPriorityAgent != null) {
            // avoid swap conflict with higher priority agent
            candidates.remove(this.occupiedNowConfig.get(higherPriorityAgent));
            // avoid vertex conflict with higher priority agent
            candidates.remove(this.occupiedNextConfig.get(higherPriorityAgent));
        }
        else {
            candidates.add(currentLocation);
        }

        // Create a Random instance
        Random random = new Random();
        // sort in ascending order of the distance between location to agent's target
        candidates.sort((loc1, loc2) ->
                Double.compare(this.heuristic.getHToTargetFromLocation(currentAgent.target, loc1) + random.nextFloat(),
                        this.heuristic.getHToTargetFromLocation(currentAgent.target, loc2) + random.nextFloat()));

        if (this.transientMAPFSettings.avoidSeparatingVertices() && this.currentAgentsReachedGoalsMap.get(currentAgent)) {
            // sort candidates so that all SV vertices are at the end of the list
            candidates.sort(this.separatingVerticesComparator);
        }

        for (I_Location nextLocation : candidates) {

            // vertex conflict
            if (needToCheckConflicts() && this.occupiedNextConfig.containsValue(nextLocation)) {
                if (this.ignoresStayAtSharedGoals && nextLocation.getCoordinate().equals(currentAgent.target)) {
                    boolean conflictsWithAgentWithDifferentTarget = false;
                    for (Map.Entry<Agent, I_Location> entry : this.occupiedNextConfig.entrySet()) {
                        if (entry.getValue().equals(nextLocation)) {
                            Agent conflictingAgent = entry.getKey();
                            if (!conflictingAgent.target.equals(currentAgent.target)) {
                                conflictsWithAgentWithDifferentTarget = true;
                                break;
                            }
                        }
                    }
                    if (conflictsWithAgentWithDifferentTarget) {
                        continue;
                    }
                }
                else {
                    continue;
                }
            }

            // find current agent occupying nextLocation in current config (Now)
            Agent occupyingAgent = null;
            if (needToCheckConflicts() && this.occupiedNowConfig.containsValue(nextLocation)) {
                for (Map.Entry<Agent, I_Location> entry : this.occupiedNowConfig.entrySet()) {
                    Agent otherAgent = entry.getKey();
                    I_Location otherAgentLocation = entry.getValue();
                    if (nextLocation.equals(otherAgentLocation)) {
                        // same agent, needs to stay in current location
                        if (otherAgent.equals(currentAgent)) {
                            // check that low-level do not conflict with problem constraints.
                            if (needToCheckConflicts() && this.needToHandleConstraints) {
                                Move newMove = getNewMove(currentAgent, nextLocation, currentLocation);
                                if ((!this.constraintsSet.accepts(newMove)) || this.constraintsSet.lastRejectionTime(newMove) != -1) {
                                    break;
                                }
                            }
                            this.occupiedNextConfig.put(currentAgent, nextLocation);
                            return true;
                        }
                        occupyingAgent = otherAgent;
                        break;
                    }
                }
            }

            // swap
            if (needToCheckConflicts() && occupyingAgent != null && this.occupiedNextConfig.get(occupyingAgent) != null
                    && this.occupiedNextConfig.get(occupyingAgent).equals(currentLocation)) continue;

            // check constraints
            if (needToCheckConflicts() && this.needToHandleConstraints) {
                Move newMove = getNewMove(currentAgent, nextLocation, currentLocation);
                if (!this.constraintsSet.accepts(newMove)) {
                    continue;
                }
            }

            // reserve next location
            this.occupiedNextConfig.put(currentAgent, nextLocation);

            // empty or stay
            if (occupyingAgent == null || nextLocation.equals(currentLocation)) return true;

            // priority inheritance
            if (!this.occupiedNextConfig.containsKey(occupyingAgent) && !solvePIBT(occupyingAgent, currentAgent)) {
                continue;
            }
            // success to plan next one step
            return true;
        }
        // stay in current location if no other option available
        if (this.needToHandleConstraints) {
            Move newMove = getNewMove(currentAgent, this.occupiedNowConfig.get(currentAgent), this.occupiedNowConfig.get(currentAgent));
            if (this.constraintsSet.accepts(newMove)) {
                this.occupiedNextConfig.put(currentAgent, currentLocation);
            }
        }
        else {
            this.occupiedNextConfig.put(currentAgent, currentLocation);
        }
        return false;
    }

    @NotNull
    private Move getNewMove(Agent currentAgent, I_Location nextLocation, I_Location currentLocation) {
        return new Move(currentAgent, this.timeStep, currentLocation, nextLocation);
    }

    /**
     /**
     * planning horizon - after k timestamps, ignore all conflicts.
     * this function check whether k timestamps have passed.
     * @return boolean: true if conflicts needs to be checked, otherwise return false.
     */
    private boolean needToCheckConflicts() {
        if (this.timeStep != 0) {
            return this.RHCR_Horizon >= this.timeStep - this.problemStartTime;
        }
        return true;
    }


    /**
     * helper function to find all neighbors of single agent
     * @param location to find his neighbors
     * @return List contains all neighbors of current I_Location
     */
    protected List<I_Location> findAllNeighbors(I_Location location) {
        return location.outgoingEdges();
    }

    public I_Location getAgentsTarget(Agent agent) {
        return this.instance.map.getMapLocation(agent.target);
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue("# of failed config", this.failedToFindConfigCounter);
        instanceReport.putFloatValue("Time in config", (float) this.totalTimeFindConfigurations);
        instanceReport.putIntegerValue("# of improve visits", this.improveVisitsCounter);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
        }
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.open = null;
        this.explored = null;
        this.exploredWithExternalConstraints = null;
        this.heuristic = null;
        this.agents = null;
        this.constraintsSet = null;
        this.instance = null;
        this.occupiedNowConfig = null;
        this.occupiedNextConfig = null;
        this.separatingVerticesSet = null;
    }

    @Override
    public boolean ignoresStayAtSharedSources() {
        return false;
    }

    @Override
    public boolean ignoresStayAtSharedGoals() {
        return this.ignoresStayAtSharedGoals;
    }

    @Override
    public boolean handlesSharedTargets() {
        return this.transientMAPFSettings.isTransientMAPF();
    }

}
