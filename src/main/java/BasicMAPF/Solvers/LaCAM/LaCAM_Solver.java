package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SOCCostFunction;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import Environment.Metrics.InstanceReport;
import org.checkerframework.checker.units.qual.A;

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
    private HashMap<HashMap<Integer, I_Location>, HighLevelNode> explored;

    /**
     * heuristic to use in the low level search to find the closest nodes to an agent's goal
     */
    private DistanceTableAStarHeuristic heuristic;

    /**
     * Map saving priority of each agent
     */
    private HashMap<Agent, Double> priorities;

    /**
     * Map saving for each agent his goal location, representing the goal configuration.
     */
    private HashMap<Integer, I_Location> goalConfiguration;

    /**
     * Map saving for each agent's ID his Agent as object.
     */
    private HashMap<Integer, Agent> agents;

    private PIBT_Solver subInstanceSolver;

    /**
     * The cost function to evaluate solutions with.
     */
    private final I_SolutionCostFunction solutionCostFunction;

    public LaCAM_Solver(I_SolutionCostFunction solutionCostFunction) {
        super.name = "LaCAM";
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SOCCostFunction::new);
    }

    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.open = new Stack<>();
        this.explored = new HashMap<>();
        this.priorities = new HashMap<>();
        this.goalConfiguration = new HashMap<>();
        this.agents = new HashMap<>();
        this.subInstanceSolver = new PIBT_Solver(null, 1, true, 1);

        // init agent's priority to unique number
        initPriority(instance);

        // distance between every vertex in the graph to each agent's goal
        if (parameters.aStarGAndH instanceof DistanceTableAStarHeuristic) {
            this.heuristic = (DistanceTableAStarHeuristic) parameters.aStarGAndH;
        }
        else {
            this.heuristic = new DistanceTableAStarHeuristic(instance.agents, instance.map);
        }
    }
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        HashMap<Integer, I_Location> initialConfiguration = new HashMap<>();
        for (Agent agent : instance.agents) {
            initialConfiguration.put(agent.iD, instance.map.getMapLocation(agent.source));
            this.goalConfiguration.put(agent.iD, instance.map.getMapLocation(agent.target));
            this.agents.put(agent.iD, agent);
        }
        LowLevelNode C_init = new LowLevelNode(null, null, null);
        HighLevelNode N_init = new HighLevelNode(initialConfiguration, C_init, get_init_order(instance.agents, instance.map), null);
        this.open.push(N_init);
        this.explored.put(initialConfiguration, N_init);


        while (!this.open.empty()) {
            if (checkTimeout()) {
                return null;
            }
            HighLevelNode N = this.open.peek();

            // reached goal configuration, stop and backtrack to return the solution
            if (reachedGoalConfiguration(N.configuration)) {
                return backTrack(N, instance);
            }

            // finished low level search
            if (N.tree.isEmpty()) {
                this.open.pop();
                continue;
            }

            LowLevelNode C = N.tree.poll();
            if (C.depth < instance.agents.size()) {
                Agent chosenAgent = N.order.get(C.depth);
                I_Location chosenLocation = N.configuration.get(chosenAgent.iD);
                List<I_Location> locations = new ArrayList<>(findAllNeighbors(chosenLocation));

                // add current location
                locations.add(chosenLocation);

                LowLevelNode tmpC = C;
                while (tmpC.who != null) {
                    locations.remove(N.configuration.get(tmpC.who.iD)); // ??
                    locations.remove(tmpC.where); // ??
                    tmpC = tmpC.parent;
                }

                for (I_Location location : locations) {
                    LowLevelNode C_new = new LowLevelNode(C, chosenAgent, location);
                    N.tree.add(C_new);
                }
            }

            HashMap<Integer, I_Location> newConfiguration = getNewConfig(N,C, instance);

            // algorithm couldn't find configuration
            if (newConfiguration == null) {
                continue;
            }

            if (this.explored.get(newConfiguration) != null) {
                continue;
            }

            HighLevelNode N_new = new HighLevelNode(newConfiguration, C_init, getOrder(newConfiguration, N, instance.map), N);
            this.open.push(N_new);
            this.explored.put(newConfiguration, N_new);
        }
        return null;
    }

    /**
     * this function determine whether the current configuration is the goal configuration.
     * @param configuration to check whether it's the goal.
     * @return boolean. true if configurations is the goal configuration, false otherwise.
     */
    private boolean reachedGoalConfiguration(HashMap<Integer, I_Location> configuration) {
        for (Map.Entry<Integer, I_Location> entry : configuration.entrySet()) {
            I_Location currentLocation = entry.getValue();
            I_Location goalLocation = this.goalConfiguration.get(entry.getKey());
            if (!(currentLocation.equals(goalLocation))) {
                return false;
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
    private HashMap<Integer, I_Location> getNewConfig(HighLevelNode N, LowLevelNode C, MAPF_Instance instance) {
        HashMap<Integer, I_Location> newConfiguration = new HashMap<>();
        ConstraintSet constraints = new ConstraintSet();
        int numberOfConstraints = C.depth;
        ArrayList<Integer> agentsWithConstraint = new ArrayList<>();
        RunParameters subProblemParameters;

        // depth is zero hence target low level node is the root
        if (C.depth == 0) {
            subProblemParameters = new RunParametersBuilder().createRP();
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
            subProblemParameters = new RunParametersBuilder().setConstraints(constraints).createRP();
        }

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
     * @param map of current problem instance.
     * helper function to create initialized order of agents.
     * in this function, we determine the order of chosen agents by the distance between their source and target.
     * @return ArrayList of agents in descending order by distance to target.
     */
    private ArrayList<Agent> get_init_order(List<Agent> agents, I_Map map) {
        ArrayList<Agent> sortedAgents = new ArrayList<>(agents);
        HashMap<Agent, Float> agentsDistances = new HashMap<>();
        for (Agent agent : agents) {
            Float distance = this.heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
            agentsDistances.put(agent, distance);
        }
        sortedAgents.sort((agent1, agent2) -> Float.compare(agentsDistances.get(agent2), agentsDistances.get(agent1)));
        return sortedAgents;
    }


    /**
     * @param configuration - current configuration of agent's locations.
     * @param N - current High Level Node.
     * helper function to create order of agents.
     * order of agents can be determined by several heuristics, for simplicity we first try the heuristic we used for the init order,
     * and change in the future when we will test and try to improve the algorithm.
     * @return ArrayList of agents.
     */
    private ArrayList<Agent> getOrder(HashMap<Integer, I_Location> configuration, HighLevelNode N, I_Map map) {
        ArrayList<Agent> sortedAgents = new ArrayList<>(this.agents.values());
        HashMap<Agent, Float> agentsDistances = new HashMap<>();
        for (Integer agentID : configuration.keySet()) {
            Agent agent = this.agents.get(agentID);
            Float distance = this.heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
            agentsDistances.put(agent, distance);
        }
        sortedAgents.sort((agent1, agent2) -> Float.compare(agentsDistances.get(agent2), agentsDistances.get(agent1)));
        return sortedAgents;
    }

    /**
     *
     * @param N - current High Level Node.
     * this function called when the algorithm reach the Goal configuration,
     * this function performs backtracking to find the path from the start configuration to the goal.
     * @return Solution for the MAPF problem.
     */
    private Solution backTrack(HighLevelNode N, MAPF_Instance instance) {
        Solution solution = new Solution();
        HashMap<Agent, SingleAgentPlan> agentPlans = new HashMap<>();

        for (Agent agent : instance.agents) {
            agentPlans.put(agent, new SingleAgentPlan(agent));
        }

        Stack<HashMap<Integer, I_Location>> configurationsInReverse = new Stack<>();
        configurationsInReverse.push(N.configuration);
        while (N.parent != null) {
            configurationsInReverse.push(N.parent.configuration);
            N = N.parent;
        }

        int timeStamp = 0;
        while (configurationsInReverse.size() != 1) {
            timeStamp++;
            HashMap<Integer, I_Location> currentConfig = configurationsInReverse.pop();
            HashMap<Integer, I_Location> nextConfig = configurationsInReverse.peek();
            for (Map.Entry<Integer, Agent> entry : this.agents.entrySet()) {
                Integer agentID = entry.getKey();
                Agent agent = entry.getValue();
                I_Location currentLocation = currentConfig.get(agentID);
                I_Location nextLocation = nextConfig.get(agentID);
                Move newMove = new Move(agent, timeStamp, currentLocation, nextLocation);
                agentPlans.get(agent).addMove(newMove);
            }
        }

        for (Agent agent : instance.agents) {
            solution.putPlan(agentPlans.get(agent));
        }
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
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
            instanceReport.putIntegerValue("SST", solution.sumServiceTimes());
            instanceReport.putIntegerValue("SOC", solution.sumIndividualCosts());
        }
    }
}
