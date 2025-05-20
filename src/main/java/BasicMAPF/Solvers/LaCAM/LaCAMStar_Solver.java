package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.A_Solver;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSettings;
import jdk.jfr.Experimental;

import java.util.*;

/**
 * LaCAM star.
 * Okumura, Keisuke. "Improving lacam for scalable eventually optimal multi-agent pathfinding." arXiv preprint arXiv:2305.03632 (2023).
 */
@Experimental
public class LaCAMStar_Solver extends A_Solver {

    /**
     * LaCAM instance - composition.
     */
    private LaCAM_Solver lacamSolver;
    public HighLevelNodeStar N_Goal;

    /**
     * Constructor.
     * @param solutionCostFunction how to calculate the cost of a solution
     * @param transientMAPFSettings indicates whether to solve transient-MAPF.
     */
    public LaCAMStar_Solver(I_SolutionCostFunction solutionCostFunction, TransientMAPFSettings transientMAPFSettings) {
        this.lacamSolver = new LaCAMBuilder().setSolutionCostFunction(solutionCostFunction).setTransientMAPFBehaviour(transientMAPFSettings).createLaCAM();
        super.name = "LaCAMStar" + (this.lacamSolver.getTransientMAPFSettings().isTransientMAPF() ? "t" : "");
    }

    /**
     * Default constructor.
     */
    public LaCAMStar_Solver() {
        this(null, null);
    }

    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.lacamSolver.init(instance, parameters);
        this.N_Goal = null;
    }
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        HashMap<Agent, I_Location> initialConfiguration = new HashMap<>();
        for (Agent agent : instance.agents) {
            initialConfiguration.put(agent, instance.map.getMapLocation(agent.source));
            this.lacamSolver.agents.put(agent.iD, agent);
        }
        LowLevelNode C_init = this.lacamSolver.initNewLowLevelNode();
        HashMap<Agent, Float> priorities = initPriorities(initialConfiguration);
        ArrayList<Agent> order = sortByPriority(priorities);
        HighLevelNode N_init = initNewHighLevelNode(initialConfiguration, C_init, order, priorities,  null, 0, calcHValue(initialConfiguration, null));

        this.lacamSolver.open.push(N_init);
        this.lacamSolver.explored.put(initialConfiguration, N_init);

        while (!this.lacamSolver.open.empty() && !checkTimeout()) {
            HighLevelNodeStar N = (HighLevelNodeStar) this.lacamSolver.open.peek();

            // finished low level search
            if (N.tree.isEmpty()) {
                this.lacamSolver.open.pop();
                continue;
            }

            // discarding redundant nodes
            if (this.N_Goal != null && N.getF() >= this.N_Goal.getF()) {
                this.lacamSolver.open.pop();
                continue;
            }

            // reached goal configuration
            if (this.N_Goal == null && reachedGoalConfiguration(N)) {
                this.N_Goal = N;
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
                    totalLowLevelNodesExpanded++;
                    N.tree.add(C_new);
                }
            }

            long startTime = System.currentTimeMillis();
            HashMap<Agent, I_Location> newConfiguration = getNewConfig(N,C);
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            this.lacamSolver.totalTimeFindConfigurations += elapsedTime;

            // algorithm couldn't find configuration
            if (newConfiguration == null) {
                this.lacamSolver.failedToFindConfigCounter++;
                continue;
            }

            HighLevelNodeStar reInsertionNode = (HighLevelNodeStar) this.lacamSolver.explored.get(newConfiguration);
            if (reInsertionNode != null) {
                N.neighbors.add(reInsertionNode);
                // Dijkstra update
                Deque<HighLevelNodeStar> D = new ArrayDeque<>();
                D.push(N);
                while (!D.isEmpty()) {
                    HighLevelNodeStar N_from = D.pop();
                    for (HighLevelNodeStar N_to : N_from.neighbors) {
                        float g = N_from.getG() + getEdgeCost(N_from, N_to.configuration);
                        if (g < N_to.getG()) {
                            if (!this.lacamSolver.getTransientMAPFSettings().isTransientMAPF() || canUpdate(N_from, N_to)) {
                                N_to.setG(g);
                                N_to.setF(N_to.getG() + N_to.getH());
                                N_to.parent = N_from;
                                D.push(N_to);
                                if (this.N_Goal != null && N_to.getF() < this.N_Goal.getF()) {
                                    this.lacamSolver.open.push(N_to);
                                }
                            }
                        }
                    }
                }

                Random rand = new Random();
                double random = rand.nextDouble();
                double limit = 0.001;

                HighLevelNodeStar N_insert;
                if (random >= limit) {
                    N_insert = reInsertionNode;
                }
                else {
                    N_insert = (HighLevelNodeStar) N_init;
                }

                if (this.N_Goal == null || N_insert.getF() < this.N_Goal.getF()) this.lacamSolver.open.push(N_insert);
            }

            else {
                HashMap<Agent, Float> newPriorities = updatePriorities(N, newConfiguration);
                ArrayList<Agent> newOrder = sortByPriority(newPriorities);
                HighLevelNodeStar N_new = new HighLevelNodeStar(newConfiguration, C_init, newOrder, newPriorities, N, N.g + getEdgeCost(N, newConfiguration), calcHValue(newConfiguration, N));
                for (Map.Entry<Agent, Boolean> entry : N_new.reachedGoalsMap.entrySet()) {
                    Agent agent = entry.getKey();
                    Boolean reachedGoal = entry.getValue();
                    if (!reachedGoal && N_new.configuration.get(agent).getCoordinate().equals(agent.target)) {
                        N_new.reachedGoalsMap.put(agent, true);
                    }
                }
                expandedNodes++;
                this.lacamSolver.explored.put(newConfiguration, N_new);
                if (N_Goal == null || N_new.f < N_Goal.f) this.lacamSolver.open.push(N_new);
            }
        }

        // if open is empty, the solution will be optimal.
        // if reached timeout, the solution will be sub-optimal.
        if (this.N_Goal != null) {
            return backTrack(this.N_Goal);
        }
        // if open is empty, there is no solution.
        // if reached timeout, failed to find a solution.
        return null;
    }

    /**
     * The following function responsible for creation of new high-level nodes.
     * The function initialize new high-level node and increase a high-level node counter.
     */
    protected HighLevelNode initNewHighLevelNode(HashMap<Agent, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HashMap<Agent, Float> priorities, HighLevelNode parent, float g, float h) {
        HighLevelNode N_init = new HighLevelNodeStar(configuration, root, order, priorities, (HighLevelNodeStar) parent, g, h);
        expandedNodes++;
        return N_init;
    }


    /**
     * relevant to TMAPF.
     * Whenever a high-level node is chosen for dijkstra update, the following function checks whether it is valid to perform the update according to reachedGoalMap.
     * If at least one agent reached its goal in currentNode, and did not reach its goal in reInsertionNode, the function return false.
     * @param N_from - current high-level node.
     * @param N_to - next high-level node, chosen for reinsertion.
     * @return true if update is valid, else false.
     */
    private boolean canUpdate(HighLevelNodeStar N_from, HighLevelNodeStar N_to) {
        for (Map.Entry<Agent, Boolean> entry : N_from.reachedGoalsMap.entrySet()) {
            Agent agent = entry.getKey();
            boolean reachedGoal = entry.getValue();
            if (reachedGoal != N_to.reachedGoalsMap.get(agent)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Calculates h values using this.heuristic.
     * Sum total heuristic values of all agents.
     * Relevant for TMAPF too.
     * @return cost.
     */
    private float calcHValue(HashMap<Agent, I_Location> currentConfiguration, HighLevelNodeStar parent) {
        float cost = 0;
        if (this.lacamSolver.getTransientMAPFSettings().isTransientMAPF() && parent != null) {
            for (Agent agent : currentConfiguration.keySet()) {
                if (currentConfiguration.get(agent) != this.lacamSolver.getAgentsTarget(agent) && !parent.reachedGoalsMap.get(agent)) {
                    cost += this.lacamSolver.heuristic.getHToTargetFromLocation(this.lacamSolver.getAgentsTarget(agent).getCoordinate(), currentConfiguration.get(agent));
                }
            }
        }
        else {
            for (Agent agent : currentConfiguration.keySet()) {
                cost += this.lacamSolver.heuristic.getHToTargetFromLocation(this.lacamSolver.getAgentsTarget(agent).getCoordinate(), currentConfiguration.get(agent));
            }
        }
        return cost;
    }


    /**
     * Calculates cost of an edge between two High Level nodes.
     * For each agent that its current location isn't its goal, the cost increases by 1.
     * Relevant for TMAPF too, for each agent that hasn't reached its goal, the cost increases by 1.
     * @return cost.
     */
    private int getEdgeCost(HighLevelNodeStar HNode_from, HashMap<Agent, I_Location> configuration_to) {
        int cost = 0;
        HashMap<Agent, I_Location> configuration_from = HNode_from.configuration;
        if (this.lacamSolver.getTransientMAPFSettings().isTransientMAPF()) {
            for (Agent agent : configuration_from.keySet()) {
                // if the next location of an agent is NOT its goal and the agent did not visit its goal
                if (configuration_to.get(agent) != this.lacamSolver.getAgentsTarget(agent) && !HNode_from.reachedGoalsMap.get(agent)) {
                    cost++;
                }
            }
        }
        else {
            for (Agent agent : configuration_from.keySet()) {
                // if the location of an agent in both current and next configuration is NOT goal location
                if (configuration_from.get(agent) != this.lacamSolver.getAgentsTarget(agent) || configuration_to.get(agent) != this.lacamSolver.getAgentsTarget(agent)) {
                    cost++;
                }
            }
        }
        return cost;
    }

    /**
     * This function determine whether the current configuration is the goal configuration.
     * @return boolean. true if configurations is the goal configuration, false otherwise.
     */
    private boolean reachedGoalConfiguration(HighLevelNodeStar N) {
        return this.lacamSolver.reachedGoalConfiguration(N);
    }

    /**
     * This function called when the algorithm reach the Goal configuration,
     * this function performs backtracking to find the path from the start configuration to the goal.
     * @param N - current High Level Node.
     * @return Solution for the MAPF problem.
     */
    private Solution backTrack(HighLevelNodeStar N) {
        return this.lacamSolver.backTrack(N);
    }

    /**
     * Init priority of each agent for a new High-Level node without a parent.
     * Initialized priorities based on distance heuristic.
     * @return priorities, hashmap stores for each agent his priority.
     */
    private HashMap<Agent, Float> initPriorities(HashMap <Agent, I_Location> currentConfiguration) {
        return this.lacamSolver.initPriorities(currentConfiguration);
    }

    /**
     * Update priority of each agent based on parent and current configuration in specific High-Level node.
     * @param parentNode - parent node of current high-level node.
     * @param newConfiguration - the configuration of current high-level node.
     * @return priorities, hashmap stores for each agent its priority.
     */
    private HashMap<Agent, Float> updatePriorities(HighLevelNodeStar parentNode, HashMap<Agent, I_Location> newConfiguration) {
        return this.lacamSolver.updatePriorities(parentNode, newConfiguration);
    }

    /**
     * This function returns a sorted list of agent based on their priorities.
     * The sort is in descending order.
     * @param priorities - HashMap maps for each agent its priority.
     * @return sorted list of agents.
     */
    public ArrayList<Agent> sortByPriority(HashMap<Agent, Float> priorities) {
        return this.lacamSolver.sortByPriority(priorities);
    }

    /**
     * Creates new configuration using PIBT.
     * @param N current high-level node.
     * @param C current low-level node.
     * @return HashMap representing the new configuration, maps for each agent its next location.
     */
    private HashMap<Agent, I_Location> getNewConfig(HighLevelNodeStar N, LowLevelNode C) {
        return this.lacamSolver.getNewConfig(N, C);
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
    protected void writeMetricsToReport(Solution solution) {
        // messy, but since both use the same instance report, this is a workaround to handle both of them counting separately
        this.lacamSolver.writeMetricsToReport(solution);
        this.expandedNodes += instanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        this.generatedNodes += instanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        this.totalLowLevelNodesGenerated += instanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel);
        this.totalLowLevelNodesExpanded += instanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
        super.writeMetricsToReport(solution);

        instanceReport.putIntegerValue("# of failed config", this.lacamSolver.failedToFindConfigCounter);
        instanceReport.putFloatValue("Time in config", (float) this.lacamSolver.totalTimeFindConfigurations);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, this.lacamSolver.solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, this.lacamSolver.solutionCostFunction.name());
        }
    }

    @Override
    protected void releaseMemory() {
        this.lacamSolver.releaseMemory();
        super.releaseMemory();
    }
}
