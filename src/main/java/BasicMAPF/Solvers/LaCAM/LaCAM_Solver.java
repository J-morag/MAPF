package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFBehaviour;
import TransientMAPF.TransientMAPFSolution;

import java.util.*;

public class LaCAM_Solver extends A_Solver {

    /**
     * open stack of high-level nodes.
     * The use in stack means that the algorithm performs a Depth First Search.
     */
    private Stack<HighLevelNode> open;

    /**
     * HashMap to manage configurations that the algorithm already saw.
     */
    private HashMap<HashMap<Agent, I_Location>, HighLevelNode> explored;

    /**
     * heuristic to use in the low level search to find the closest nodes to an agent's goal
     */
    private DistanceTableSingleAgentHeuristic heuristic;

    /**
     * Map saving priority of each agent
     */
    private HashMap<Agent, Double> priorities;

    /**
     * Map saving for each agent his goal location, representing the goal configuration.
     */
    private HashMap<Agent, I_Location> goalConfiguration;

    /**
     * Map saving for each agent's ID his Agent as object.
     */
    private HashMap<Integer, Agent> agents;

    private A_Solver subInstanceSolver;

    /**
     * The cost function to evaluate solutions with.
     */
    private final I_SolutionCostFunction solutionCostFunction;

    /**
     * variable indicates whether the solution returned by the algorithm is transient.
     */
    private final TransientMAPFBehaviour transientMAPFBehaviour;

    private ConstraintSet solverConstraints;

    private MAPF_Instance instance;

    private int highLevelNodesCounter;

    private int lowLevelNodesCounter;
    private int failedToFindConfigCounter;
    private long totalTimeFindConfigurations;

    public HighLevelNode N_Goal;

    private ArrayList<Agent> unhandledAgentsInCurrentConfig;


    /**
     * Constructor.
     * @param solutionCostFunction how to calculate the cost of a solution
     * @param transientMAPFBehaviour indicates whether to solve transient-MAPF.
     */
    public LaCAM_Solver(I_SolutionCostFunction solutionCostFunction, TransientMAPFBehaviour transientMAPFBehaviour) {
        this.transientMAPFBehaviour = Objects.requireNonNullElse(transientMAPFBehaviour, TransientMAPFBehaviour.regularMAPF);
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        super.name = "LaCAM" + (this.transientMAPFBehaviour.isTransientMAPF() ? "t" : "");
    }

    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.solverConstraints = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.open = new Stack<>();
        this.explored = new HashMap<>();
        this.priorities = new HashMap<>();
        this.goalConfiguration = new HashMap<>();
        this.agents = new HashMap<>();
        this.subInstanceSolver = new PIBT_ForLaCAM(null, null, true, 1);
        this.highLevelNodesCounter = 0;
        this.lowLevelNodesCounter = 0;
        this.failedToFindConfigCounter = 0;
        this.totalTimeFindConfigurations = 0;
        this.N_Goal = null;
        this.instance = instance;
        this.unhandledAgentsInCurrentConfig = new ArrayList<>(instance.agents);

        // init agent's priority to unique number
        initPriority(this.instance);

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
        LowLevelNode C_init = new LowLevelNode(null, null, null);
        this.lowLevelNodesCounter++;
        HighLevelNode N_init = new HighLevelNode(initialConfiguration, C_init, get_init_order(instance.agents), null, null, 0, calc_h(initialConfiguration));
        this.highLevelNodesCounter++;
        this.open.push(N_init);
        this.explored.put(initialConfiguration, N_init);

        while (!this.open.empty()) {
            if (checkTimeout()) {
                return null;
            }
            HighLevelNode N = this.open.peek();

            // reached goal configuration, stop and backtrack to return the solution
            if (reachedGoalConfiguration(N.configuration, N)) {
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
                    // found an agent that want to move to chosenAgent's current location
                    // remove from locations the location that this agent was
                    // so, only the first in order agent (higher in tree) could make the move
                    // avoid swap conflict
                    if (tmpC.where.getCoordinate() == chosenLocation.getCoordinate()) {
                        locations.remove(N.configuration.get(tmpC.who));
                    }

                    // current agent can't go to a location chooses by previous agent in the low level tree
                    // avoid vertex conflict
                    locations.remove(tmpC.where);
                    tmpC = tmpC.parent;
                }

                for (I_Location location : locations) {
                    LowLevelNode C_new = new LowLevelNode(C, chosenAgent, location);
                    this.lowLevelNodesCounter++;
                    N.tree.add(C_new);
                }
            }

            long startTime = System.currentTimeMillis();
            HashMap<Agent, I_Location> newConfiguration = getNewRandomConfig(N,C);
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            totalTimeFindConfigurations += elapsedTime;

            // algorithm couldn't find configuration
            if (newConfiguration == null) {
                this.failedToFindConfigCounter++;
                continue;
            }

            if (this.explored.get(newConfiguration) != null) {

                // re-insertion of already seen configuration
                this.open.push(this.explored.get(newConfiguration));

//                HighLevelNode existingNode = this.explored.get(newConfiguration);
//                N.neighbors.add(existingNode);
//                this.open.push(existingNode);

//                // Dijkstra update
//                Deque<HighLevelNode> D = new ArrayDeque<>();
//                D.add(N);
//                while (!D.isEmpty()) {
//                    HighLevelNode N_from = D.pop();
//                    for (HighLevelNode N_to : N_from.neighbors) {
//                        float g = N_from.g + getCost(N_from.configuration, N_to.configuration);
//                        if (g < N_to.g) {
//                            N_to.g = g;
//                            N_to.f = N_to.g + N_to.h;
//                            N_to.parent = N_from;
//                            D.add(N_to);
//
//                            this.open.push(N_to);
//                        }
//                    }
//                }
//                continue;
            }

            else {
                HighLevelNode N_new = new HighLevelNode(newConfiguration, C_init, getOrder(newConfiguration, N), N, N.reachedGoalsMap, N.g + getCost(N.configuration, newConfiguration), calc_h(newConfiguration));
                N.neighbors.add(N_new);
                this.highLevelNodesCounter++;

                // update reachedGoalMap according to new configuration
                for (Map.Entry<Agent, Boolean> entry : N_new.reachedGoalsMap.entrySet()) {
                    Agent agent = entry.getKey();
                    Boolean reachedGoal = entry.getValue();
                    if (!reachedGoal && newConfiguration.get(agent).getCoordinate().equals(agent.target)) {
                        N.reachedGoalsMap.put(agent, true);
                    }
                }
                this.open.push(N_new);
                this.explored.put(newConfiguration, N_new);
            }
        }
        return null;
    }

    private float calc_h(HashMap<Agent, I_Location> currentConfiguration) {
        float cost = 0;
        for (Agent agent : currentConfiguration.keySet()) {
            cost += this.heuristic.getHToTargetFromLocation(this.goalConfiguration.get(agent).getCoordinate(), currentConfiguration.get(agent));
        }
        return cost;
    }

    private int getCost(HashMap<Agent, I_Location> configuration_from, HashMap<Agent, I_Location> configuration_to) {
        int cost = 0;
        for (Agent agent : configuration_from.keySet()) {
            // if the location of an agent in both current and next configuration is NOT goal location
            if (configuration_to.get(agent) != configuration_from.get(agent) && configuration_to.get(agent) != this.goalConfiguration.get(agent)) {
                cost++;
            }
        }
        return cost;
    }

    /**
     * this function determine whether the current configuration is the goal configuration.
     * @param configuration to check whether it's the goal.
     * @return boolean. true if configurations is the goal configuration, false otherwise.
     */
    private boolean reachedGoalConfiguration(HashMap<Agent, I_Location> configuration, HighLevelNode N) {
        if (!this.transientMAPFBehaviour.isTransientMAPF()) {
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
     * main function of LaCAM algorithm.
     * @param N - current high level node.
     * @param C - current low level node.
     * generates new configuration from current configuration according to N, following constraints defined in C.
     * @return new configuration.
     */
    private HashMap<Integer, I_Location> getNewConfig(HighLevelNode N, LowLevelNode C) {
        HashMap<Integer, I_Location> newConfiguration = new HashMap<>();
        ConstraintSet constraints = new ConstraintSet(this.solverConstraints);
        int numberOfConstraints = C.depth;
        ArrayList<Integer> agentsWithConstraint = new ArrayList<>();
        RunParameters subProblemParameters;

        // depth is zero hence target low level node is the root
        // no constraints needed
        if (C.depth == 0) {
            subProblemParameters = new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(this.heuristic).createRP();
        }

        // bottom of low level tree - each agent have a constraint
        // exactly one configuration is possible, no need pibt
        else if (C.depth == this.instance.agents.size()) {
            while (C.parent != null) {
                newConfiguration.put(C.who.iD, C.where);
                C = C.parent;
            }
            return newConfiguration;
        }

        // create constraint according to the low-level node
        else {
            while (C.parent != null) {
                Constraint swapConstraint = new Constraint(1, C.where, N.configuration.get(C.who.iD));
                constraints.add(swapConstraint);
                Constraint locationConstraint = new Constraint(1, C.where);
                constraints.add(locationConstraint);
                agentsWithConstraint.add(C.who.iD);
                newConfiguration.put(C.who.iD, C.where);
                C = C.parent;
            }
            subProblemParameters = new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(this.heuristic).createRP();
        }

        // agent with no constraints
        Agent[] agentsSubset = new Agent[instance.agents.size() - numberOfConstraints];
        Object[] AllAgents = N.configuration.keySet().toArray();
        int index = 0;
        for (Object allAgent : AllAgents) {
            Integer agentID = (Integer) allAgent;
            if (agentsWithConstraint.contains(agentID)) {
                continue;
            }
            Agent newAgent = new Agent(agentID, N.configuration.get(agentID).getCoordinate(), this.agents.get(agentID).target);
            agentsSubset[index] = newAgent;
            index++;
        }

        MAPF_Instance subInstance = new MAPF_Instance("subInstance", instance.map, agentsSubset);
        Solution subInstanceSolution = this.subInstanceSolver.solve(subInstance, subProblemParameters);

        if (subInstanceSolution != null) {
            for (Agent agent : agentsSubset) {
                // cant find step for an agent - not valid configuration
                if (subInstanceSolution.getPlanFor(agent).size() == 0) {
                    return null;
                }
                I_Location location = subInstanceSolution.getAgentLocation(agent, 1);
                newConfiguration.put(agent.iD, location);
            }
            return newConfiguration;
        }
        return null;
    }


    /**
     * @param agents list of agents to sort.
     * helper function to create initialized order of agents.
     * in this function, we determine the order of chosen agents by the distance between their source and target.
     * @return ArrayList of agents in ascending order by distance to target.
     */
    private ArrayList<Agent> get_init_order(List<Agent> agents) {
        ArrayList<Agent> sortedAgents = new ArrayList<>(agents);
        Collections.shuffle(sortedAgents);
        HashMap<Agent, Float> agentsDistances = new HashMap<>();
        for (Agent agent : agents) {
            Float distance = this.heuristic.getHToTargetFromLocation(agent.target, this.instance.map.getMapLocation(agent.source));
            agentsDistances.put(agent, distance);
        }
        sortedAgents.sort((agent1, agent2) -> Float.compare(agentsDistances.get(agent1), agentsDistances.get(agent2)));
        return sortedAgents;
    }

    /**
     * @param configuration - current configuration of agent's locations.
     * @param N - current High Level Node.
     * helper function to create order of agents.
     * order of agents can be determined by several heuristics, for simplicity we first try the heuristic we used for the init order,
     * and change in the future when we will test and try to improve the algorithm.
     * @return ArrayList of agents in ascending order by distance to target.
     */
    private ArrayList<Agent> getOrder(HashMap<Agent, I_Location> configuration, HighLevelNode N) {
        ArrayList<Agent> sortedAgents = new ArrayList<>(this.agents.values());
        Collections.shuffle(sortedAgents);
        HashMap<Agent, Float> agentsDistances = new HashMap<>();
        for (Map.Entry<Agent, I_Location> entry : configuration.entrySet()) {
            Agent agent = entry.getKey();
            I_Location agentLocation = entry.getValue();
            Float distance = this.heuristic.getHToTargetFromLocation(agent.target, this.instance.map.getMapLocation(agentLocation.getCoordinate()));
            agentsDistances.put(agent, distance);
        }
        sortedAgents.sort((agent1, agent2) -> Float.compare(agentsDistances.get(agent1), agentsDistances.get(agent2)));
        return sortedAgents;
    }

    /**
     *
     * @param N - current High Level Node.
     * this function called when the algorithm reach the Goal configuration,
     * this function performs backtracking to find the path from the start configuration to the goal.
     * @return Solution for the MAPF problem.
     */
    private Solution backTrack(HighLevelNode N) {
        HashMap<Agent, SingleAgentPlan> agentPlans = new HashMap<>();
        for (Agent agent : this.instance.agents) {
            agentPlans.put(agent, new SingleAgentPlan(agent));
        }

        Stack<HashMap<Agent, I_Location>> configurationsInReverse = new Stack<>();
        configurationsInReverse.push(N.configuration);

        while (N.parent != null) {
            configurationsInReverse.push(N.parent.configuration);
            N = N.parent;
        }

        int timeStamp = 0;
        while (configurationsInReverse.size() != 1) {
            timeStamp++;
            HashMap<Agent, I_Location> currentConfig = configurationsInReverse.pop();
            HashMap<Agent, I_Location> nextConfig = configurationsInReverse.peek();
            for (Agent agent : this.instance.agents) {
                I_Location currentLocation = currentConfig.get(agent);
                I_Location nextLocation = nextConfig.get(agent);
                Move newMove = new Move(agent, timeStamp, currentLocation, nextLocation);
                if (this.solverConstraints.accepts(newMove)) {
                    agentPlans.get(agent).addMove(newMove);
                }
                else {
                    return null;
                }
            }
        }

        Solution solution;
        if (this.transientMAPFBehaviour.isTransientMAPF()) {
            solution = new TransientMAPFSolution();
        }
        else {
            solution = new Solution();
        }

        for (Agent agent : instance.agents) {
            solution.putPlan(agentPlans.get(agent));
        }
        System.out.println("Number Of High-Level nodes generated: " + this.highLevelNodesCounter);
        return solution;
    }

    /**
     * init priority of each agent in the beginning of the algorithm.
     * each agent have a unique double representing his priority.
     * update this.priorities.
     */
    private void initPriority(MAPF_Instance instance) {
        int numberOfAgents = instance.agents.size();
        double uniqueFactor = 1.0 / numberOfAgents;
        int i = 1;
        for (Agent agent : instance.agents) {
            // (uniqueFactor * i) is a unique representation for the priority of each agent
            this.priorities.put(agent, uniqueFactor * i);
            i++;
        }
    }

    private HashMap<Agent, I_Location> getNewRandomConfig(HighLevelNode N, LowLevelNode C) {
        this.unhandledAgentsInCurrentConfig = new ArrayList<>(instance.agents);
        HashMap<Agent, I_Location> newConfiguration = new HashMap<>();
        ConstraintSet constraints = new ConstraintSet(this.solverConstraints);

        // bottom of low level tree - each agent have a constraint
        // exactly one configuration is possible
        if (C.depth == this.instance.agents.size()) {
            while (C.parent != null) {
                newConfiguration.put(C.who, C.where);
                C = C.parent;
            }
            return newConfiguration;
        }

        // create constraints according to the low-level node
        else {
            while (C.parent != null) {
                Constraint swapConstraint = new Constraint(1, C.where, N.configuration.get(C.who));
                constraints.add(swapConstraint);
                Constraint locationConstraint = new Constraint(1, C.where);
                constraints.add(locationConstraint);
                newConfiguration.put(C.who, C.where);
                this.unhandledAgentsInCurrentConfig.remove(C.who);
                C = C.parent;
            }
        }

        for (Agent agent : N.order) {
            if (newConfiguration.containsKey(agent)) continue; // move already chose for agent
            if (!solvePIBT(agent, null, newConfiguration,N.configuration ,constraints)) {
                return null;
            }
        }
        return newConfiguration;
    }

    private boolean solvePIBT(Agent currentAgent, Agent higherPriorityAgent, HashMap<Agent, I_Location> newConfiguration, HashMap<Agent, I_Location> currentConfiguration, ConstraintSet constraints) {

//        System.out.println(currentAgent);
//        System.out.println(higherPriorityAgent);
        if (!unhandledAgentsInCurrentConfig.contains(currentAgent)) {
            return false;
        }
        this.unhandledAgentsInCurrentConfig.remove(currentAgent);
        I_Location currentLocation = currentConfiguration.get(currentAgent);
        List<I_Location> candidates = new ArrayList<>(findAllNeighbors(currentLocation));

        // avoid swap with higher priority agent
        if (higherPriorityAgent != null) {
            candidates.remove(currentConfiguration.get(higherPriorityAgent));
        }
        else {
            candidates.add(currentLocation);
        }

        // sort in ascending order
        candidates.sort((loc1, loc2) ->
                Double.compare(this.heuristic.getHToTargetFromLocation(currentAgent.target, loc1),
                        this.heuristic.getHToTargetFromLocation(currentAgent.target, loc2)));

        for (I_Location nextLocation : candidates) {

            // vertex conflict
            // location already taken by a higher priority agent || low level constraints
            if (newConfiguration.containsValue(nextLocation)) continue;

            // swap conflict
            if (swapConflict(currentAgent, nextLocation, currentConfiguration, newConfiguration)) continue;

            // check constraints
            Move newMove = new Move(currentAgent, 1, currentLocation, nextLocation);
            if (!constraints.accepts(newMove)) continue;

            Agent occupyingAgent = null;
            // valid move!
            if (!currentConfiguration.containsValue(nextLocation)) {
                newConfiguration.put(currentAgent, nextLocation);
                return true;
            }
            // location not in newConfiguration but he is in currentConfiguration
            // need to perform pibt in order to move the agent to another location
            else {
                for (Map.Entry<Agent, I_Location> entry : currentConfiguration.entrySet()) {
                    Agent otherAgent = entry.getKey();
                    I_Location otherAgentLocation = entry.getValue();
                    if (nextLocation.equals(otherAgentLocation)) {
                        // same agent, needs to stay in current location
                        if (otherAgent.equals(currentAgent)) {
                            newConfiguration.put(currentAgent, nextLocation);
                            return true;
                        }
                        occupyingAgent = otherAgent;
                        break;
                    }
                }
            }

            if (newConfiguration.containsKey(occupyingAgent)) {
                return false;
            }

            // priority inheritance
            if (occupyingAgent == null || solvePIBT(occupyingAgent, currentAgent, newConfiguration, currentConfiguration, constraints)) {
                newConfiguration.put(currentAgent, nextLocation);
                return true;
            }
//            newConfiguration.put(agent.iD, nextLocation);
//            return true;
        }
        return false;
    }

    private boolean swapConflict(Agent currentAgent, I_Location newLocation,Map<Agent, I_Location> currentConfiguration, Map<Agent, I_Location> newConfiguration) {
        // swap conflict
        I_Coordinate newCoordinate = newLocation.getCoordinate();
        for (Map.Entry<Agent, I_Location> entry : newConfiguration.entrySet()) {
            Agent otherAgent = entry.getKey();
            I_Location otherAgentLocation = entry.getValue();
            if (newCoordinate == currentConfiguration.get(otherAgent).getCoordinate() &&
                    otherAgentLocation == currentConfiguration.get(currentAgent)) {
                // swap conflict
                return true;
            }
        }
        return false;
    }

    private I_Location findBest(List<I_Location> candidates, Agent current) {
        I_Location bestCandidate = null;
        Float minDistance = Float.MAX_VALUE;
        for (I_Location location : candidates) {
            Float distance = this.heuristic.getHToTargetFromLocation(current.target, location);
            if (distance < minDistance) {
                minDistance = distance;
                bestCandidate = location;
            }
        }
        return bestCandidate;
    }

    /**
     * helper function to find all neighbors of single agent
     * @param location to find his neighbors
     * @return List contains all neighbors of current I_Location
     */
    private List<I_Location> findAllNeighbors(I_Location location) {
        return location.outgoingEdges();
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.open = null;
        this.explored = null;
        this.heuristic = null;
        this.priorities = null;
        this.goalConfiguration = null;
        this.agents = null;
        this.subInstanceSolver = null;
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue("Expanded Nodes (High Level)", this.highLevelNodesCounter);
        instanceReport.putIntegerValue("Expanded Nodes (Low Level)", this.lowLevelNodesCounter);
        instanceReport.putIntegerValue("# of failed config", this.failedToFindConfigCounter);
        instanceReport.putFloatValue("Time in config", (float) this.totalTimeFindConfigurations);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
            instanceReport.putIntegerValue("SST", solution.sumServiceTimes());
            instanceReport.putIntegerValue("SOC", solution.sumIndividualCosts());
        }
    }
}
