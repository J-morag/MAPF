package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import org.jetbrains.annotations.Nullable;
import java.util.*;

/**
 * Lazy Constraints Addition Search.
 * Okumura, Keisuke. "Lacam: Search-based algorithm for quick multi-agent pathfinding." Proceedings of the AAAI Conference on Artificial Intelligence. 2023.
 */
public class LaCAM_Solver extends A_Solver {

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
     * heuristic to use in the low level search to find the closest nodes to an agent's goal
     */
    protected DistanceTableSingleAgentHeuristic heuristic;

    /**
     * Map saving for each agent its goal location, representing the goal configuration.
     */
    protected HashMap<Agent, I_Location> goalConfiguration;

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

    protected I_ConstraintSet solverConstraints;

    protected MAPF_Instance instance;
    protected int failedToFindConfigCounter;
    protected long totalTimeFindConfigurations;

    protected HashMap<Agent, I_Location> occupiedNowConfig;
    protected HashMap<Agent, I_Location> occupiedNextConfig;

    protected ConstraintSet instanceConstraints;

    protected int improveVisitsCounter;

    protected int timeStep;
    /**
     * Constructor.
     * @param solutionCostFunction how to calculate the cost of a solution
     * @param transientMAPFSettings indicates whether to solve transient-MAPF.
     */
    LaCAM_Solver(I_SolutionCostFunction solutionCostFunction, TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        super.name = "LaCAM" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "");
    }

    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.solverConstraints = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.open = new Stack<>();
        this.explored = new HashMap<>();
        this.goalConfiguration = new HashMap<>();
        this.agents = new HashMap<>();
        this.failedToFindConfigCounter = 0;
        this.totalTimeFindConfigurations = 0;
        this.instance = instance;
        this.occupiedNowConfig = new HashMap<>();
        this.occupiedNextConfig = new HashMap<>();
        this.instanceConstraints = null;
        this.timeStep = parameters.problemStartTime + 1;
        this.improveVisitsCounter = 0;

        // distance between every vertex in the graph to each agent's goal
        if (parameters.singleAgentGAndH instanceof DistanceTableSingleAgentHeuristic) {
            this.heuristic = (DistanceTableSingleAgentHeuristic) parameters.singleAgentGAndH;
        }
        else {
            this.heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, instance.map);
        }
    }
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        HashMap<Agent, I_Location> initialConfiguration = new HashMap<>();
        for (Agent agent : instance.agents) {
            initialConfiguration.put(agent, instance.map.getMapLocation(agent.source));
            this.goalConfiguration.put(agent, instance.map.getMapLocation(agent.target));
            this.agents.put(agent.iD, agent);
        }
        LowLevelNode C_init = initNewLowLevelNode();
        HashMap<Agent, Float> priorities = initPriorities(initialConfiguration);
        ArrayList<Agent> order = sortByPriority(priorities);
        HighLevelNode N_init = initNewHighLevelNode(initialConfiguration, C_init,order, priorities, null);
        this.open.push(N_init);
        this.explored.put(initialConfiguration, N_init);

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
                    this.totalLowLevelNodesExpanded++;
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

            HighLevelNode reInsertionNode = this.explored.get(newConfiguration);
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
        return null;
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
        HighLevelNode N_new = new HighLevelNode(newConfiguration, C_init, newOrder, newPriorities, N);

        for (Map.Entry<Agent, Boolean> entry : N_new.reachedGoalsMap.entrySet()) {
            Agent agent = entry.getKey();
            Boolean reachedGoal = entry.getValue();
            if (!reachedGoal && N_new.configuration.get(agent).getCoordinate().equals(agent.target)) {
                N_new.reachedGoalsMap.put(agent, true);
            }
        }
        this.expandedNodes++;
        this.open.push(N_new);
        this.explored.put(newConfiguration, N_new);
    }

    /**
     * The following function responsible for creation of new low-level nodes.
     * The function initialize new low-level node and increase a low-level node counter.
     */
    protected LowLevelNode initNewLowLevelNode() {
        LowLevelNode C_init = new LowLevelNode(null, null, null);
        this.totalLowLevelNodesExpanded++;
        return C_init;
    }

    /**
     * The following function responsible for creation of new high-level nodes.
     * The function initialize new high-level node and increase a high-level node counter.
     */
    protected HighLevelNode initNewHighLevelNode(HashMap<Agent, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HashMap<Agent, Float> priorities, HighLevelNode parent) {
        HighLevelNode N_init = new HighLevelNode(configuration, root, order, priorities, parent);
        this.expandedNodes++;
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
                I_Location goalLocation = this.goalConfiguration.get(currentAgent);
                if (!(currentLocation.equals(goalLocation))) {
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
        return true;
    }

    /**
     * This function is called when the algorithm reaches the Goal configuration,
     * this function performs backtracking to find the path from the start configuration to the goal.
     * @param N - current High Level Node.
     * @return Solution for the MAPF problem.
     */
    protected Solution backTrack(HighLevelNode N) {
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
        for (Agent agent : instance.agents) {
            SingleAgentPlan plan = agentPlans.get(agent);
            // trim the plan to remove excess "stay" moves at the end

            int lastChangedLocationTime = plan.getEndTime();
            for (; lastChangedLocationTime >= 1; lastChangedLocationTime--) {
                if (! plan.moveAt(lastChangedLocationTime).prevLocation.equals(plan.getLastMove().currLocation)) {
                    break;
                }
            }
            if (lastChangedLocationTime < plan.getEndTime() && lastChangedLocationTime > 0) {
                SingleAgentPlan trimmedPlan = new SingleAgentPlan(agent);
                for (int t = 1; t <= lastChangedLocationTime; t++) {
                    trimmedPlan.addMove(plan.moveAt(t));
                }
                solution.putPlan(trimmedPlan);
            }
            else solution.putPlan(agentPlans.get(agent));
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

        this.instanceConstraints = new ConstraintSet(this.solverConstraints);
        // bottom of low level tree - each agent have a constraint
        // exactly one configuration is possible
        if (C.depth == this.instance.agents.size()) {
            while (C.parent != null) {
                this.occupiedNextConfig.put(C.who, C.where);
                C = C.parent;
            }
            return this.occupiedNextConfig;
        }

        // create constraints according to the low-level node
        else {
            while (C.parent != null) {

                // vertex conflict
                if (this.occupiedNextConfig.containsValue(C.where)) {
                    return null; // vertex conflict detected!
                }

                // swap conflict
                I_Location currentLocation = occupiedNowConfig.get(C.who);
                I_Location nextLocation = C.where;
                for (Map.Entry<Agent, I_Location> entry : occupiedNowConfig.entrySet()) {
                    Agent otherAgent = entry.getKey();
                    I_Location otherAgentLocation = entry.getValue();
                    if (nextLocation.equals(otherAgentLocation) && !otherAgent.equals(C.who)
                            && this.occupiedNowConfig.get(otherAgent) != null
                            && !this.occupiedNowConfig.get(otherAgent).equals(currentLocation)) {
                        return null; // Swap conflict detected!
                    }
                }
                Constraint swapConstraint = new Constraint(1, C.where, N.configuration.get(C.who));
                this.instanceConstraints.add(swapConstraint);
                Constraint locationConstraint = new Constraint(1, C.where);
                this.instanceConstraints.add(locationConstraint);
                this.occupiedNextConfig.put(C.who, C.where);
                C = C.parent;
            }
        }

        this.timeStep = N.timeStep;
        for (Agent agent : N.order) {
            if (this.occupiedNextConfig.containsKey(agent)) continue; // move already chose for agent or agent have a constraint
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

        for (I_Location nextLocation : candidates) {

            // vertex conflict
            if (this.occupiedNextConfig.containsValue(nextLocation)) continue;

            // find current agent occupying nextLocation in current config (Now)
            Agent occupyingAgent = null;
            if (this.occupiedNowConfig.containsValue(nextLocation)) {
                for (Map.Entry<Agent, I_Location> entry : this.occupiedNowConfig.entrySet()) {
                    Agent otherAgent = entry.getKey();
                    I_Location otherAgentLocation = entry.getValue();
                    if (nextLocation.equals(otherAgentLocation)) {
                        // same agent, needs to stay in current location
                        if (otherAgent.equals(currentAgent)) {
                            this.occupiedNextConfig.put(currentAgent, nextLocation);
                            return true;
                        }
                        occupyingAgent = otherAgent;
                        break;
                    }
                }
            }

            // swap
            if (occupyingAgent != null && this.occupiedNextConfig.get(occupyingAgent) != null && this.occupiedNextConfig.get(occupyingAgent).equals(currentLocation)) continue;

            // check constraints
            Move newMove = new Move(currentAgent, this.timeStep, currentLocation, nextLocation);
            if (!this.instanceConstraints.accepts(newMove)) {
                continue;
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
        this.occupiedNextConfig.put(currentAgent, currentLocation);
        return false;
    }

    /**
     * helper function to find all neighbors of single agent
     * @param location to find his neighbors
     * @return List contains all neighbors of current I_Location
     */
    protected List<I_Location> findAllNeighbors(I_Location location) {
        return location.outgoingEdges();
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue("# failed configs", this.failedToFindConfigCounter);
        instanceReport.putFloatValue("Time in configs", (float) this.totalTimeFindConfigurations);
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
        this.heuristic = null;
        this.goalConfiguration = null;
        this.agents = null;
    }
}
