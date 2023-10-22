package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
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
    private DistanceTableAStarHeuristic heuristic;

    /**
     * Map saving priority of each agent
     */
    private HashMap<Agent, Double> priorities;

    /**
     * Map saving for each agent his goal location, representing the goal configuration.
     */
    private HashMap<Agent, I_Location> goalConfiguration;

    private PIBT_Solver subInstanceSolver;

    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.open = new Stack<>();
        this.explored = new HashMap<>();
        this.heuristic = new DistanceTableAStarHeuristic(instance.agents, instance.map);
        this.priorities = new HashMap<>();
        this.goalConfiguration = new HashMap<>();

        this.subInstanceSolver = new PIBT_Solver(null, 1);

        // init agent's priority to unique number
        initPriority(instance);
    }
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        HashMap<Agent, I_Location> initialConfiguration = new HashMap<>();
        for (Agent agent : instance.agents) {
            initialConfiguration.put(agent, instance.map.getMapLocation(agent.source));
            this.goalConfiguration.put(agent, instance.map.getMapLocation(agent.target));
        }
        LowLevelNode C_init = new LowLevelNode(null, null, null);
        HighLevelNode N_init = new HighLevelNode(initialConfiguration, C_init, get_init_order(instance.agents, instance.map), null);
        this.open.push(N_init);
        this.explored.put(initialConfiguration, N_init);


        while (!this.open.empty()) {
            HighLevelNode N = this.open.peek();

            // reached goal configuration, stop and backtrack to return the solution
            if (N.configuration.equals(this.goalConfiguration)) {
                return backTrack(N);
            }

            // low level search end
            if (N.tree == null) {
                this.open.pop();
                continue;
            }

            LowLevelNode C = N.tree.poll();
            if (C.depth <= instance.agents.size()) {
                Agent chosenAgent = N.order.get(C.depth); // -1?
                I_Location chosenLocation = N.configuration.get(chosenAgent);
                List<I_Location> locations = new ArrayList<>(findAllNeighbors(chosenLocation));
                for (I_Location location : locations) {
                    LowLevelNode C_new = new LowLevelNode(C, chosenAgent, location);
                    N.tree.add(C_new);
                }
                // add current location
                LowLevelNode C_new = new LowLevelNode(C, chosenAgent, chosenLocation);
                N.tree.add(C_new);
            }

            HashMap<Agent, I_Location> newConfiguration = getNewConfig(N,C, instance);

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
     * main function of LaCAM algorithm.
     * @param N - current high level node.
     * @param C - current low level node.
     * generates new configuration from current configuration according to N, following constraints defined in C.
     * @return new configuration.
     */
    private HashMap<Agent, I_Location> getNewConfig(HighLevelNode N, LowLevelNode C, MAPF_Instance instance) {
        HashMap<Agent, I_Location> newConfiguration = new HashMap<>();
        ConstraintSet constraints = new ConstraintSet();
        int numberOfConstraints = C.depth;
        ArrayList<Agent> agentsWithConstraint = new ArrayList<>();
        RunParameters subProblemParameters;

        // depth is zero hence target low level node is the root
        if (C.depth == 0) {
            subProblemParameters = new RunParametersBuilder().createRP();
        }
        // create constraint according to the low-level node
        else {
            while (C.parent != null) {
                Constraint constraint = new Constraint(1, C.where);
                constraints.add(constraint);
                agentsWithConstraint.add(C.who);
                newConfiguration.put(C.who, C.where);
                C = C.parent;
            }
            subProblemParameters = new RunParametersBuilder().setConstraints(constraints).createRP();
        }

        Agent[] agentsSubset = new Agent[instance.agents.size() - numberOfConstraints];
        for (int i = 0; i < instance.agents.size(); i++) {
            Agent agent = instance.agents.get(i);
            if (agentsWithConstraint.contains(agent)) {
                continue;
            }
            Agent newAgent = new Agent(agent.iD, N.configuration.get(agent).getCoordinate(), agent.target);
            agentsSubset[i] = newAgent;
        }

        MAPF_Instance subInstance = new MAPF_Instance("subInstance", instance.map, agentsSubset);
        Solution subInstanceSolution = this.subInstanceSolver.solve(subInstance, subProblemParameters);

        if (subInstanceSolution != null) {
            for (Agent agent : agentsSubset) {
                I_Location location = subInstanceSolution.getAgentLocation(agent, 1);
                newConfiguration.put(agent, location);
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
    private ArrayList<Agent> getOrder(HashMap<Agent, I_Location> configuration, HighLevelNode N, I_Map map) {
        ArrayList<Agent> sortedAgents = new ArrayList<>(configuration.keySet());
        HashMap<Agent, Float> agentsDistances = new HashMap<>();
        for (Agent agent : configuration.keySet()) {
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
    private Solution backTrack(HighLevelNode N) {
//        Solution solution = new Solution();
//        HashMap<Agent, SingleAgentPlan> agentPlans = new HashMap<>();
//
//        for (Agent agent : this.goalConfiguration.keySet()) {
//            agentPlans.put(agent, new SingleAgentPlan(agent));
//        }
//        while (N.parent != null) {
//            for (Agent agent : N.configuration.keySet()) {
//                agentPlans.get(agent).
//            }
//
//            N = N.parent;
//        }
//        TODO
        return null;
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
    }
}
