package BasicMAPF.Solvers.PIBT;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SOCCostFunction;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.GraphMapVertex;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;

import java.util.*;
import java.util.stream.*;

public class PIBT_Solver extends A_Solver {

    /**
     * list contains all not handled agents at timestamp t
     * initialize in every timestamp to A (all agents)
     * agent in list will be sorted in descending order by priority
     */
    private List<Agent> unhandledAgents;

    /**
     * list contains all taken nodes, hence agent can't move to a node in this list
     * when an agent choose node to move to in next timestamp, the node is added to this list
     */
    private List<I_Location> takenNodes;

    /**
     * Map saving current location for each agent
     */
    private HashMap<Agent, I_Location> locations;

    /**
     * Map saving priority of each agent
     */
    private HashMap<Agent, Double> priorities;

    /**
     * Map saving for each agent - distance between each node in the grid to goal
     * key- I_Location (node) which is target of agent
     * value- Map : key- I_Location (node)
     *              value- Integer representing distance between target of current agent's goal and current node
     *
     * Assumption - goals are unique TODO check
     */
    private Map<I_Location, Map<I_Location, Integer>> distancesFromGoal;

    private MAPF_Instance currentInstance;

    /**
     * object Solution represents the final solution that the algorithm returns
     */
    private Solution solution;

    /**
     * HashMap saves for each agents his plan
     * this hash built in iteratively in every time stamp
     * in the end of the algorithm this HashMap represent the final solution
     */
    private HashMap<Agent, SingleAgentPlan> agentPlans;
    private int timeStamp;

    /**
     * constructor
     */
    public PIBT_Solver() {
        this.locations = new HashMap<>();
        this.priorities = new HashMap<>();
        this.distancesFromGoal = new HashMap<>();
        this.solution = new Solution();
        this.agentPlans = new HashMap<>();
        this.timeStamp = 0;
    }

    /**
     * initialize variables relevant to solve MAPF using PIBT
     * @param instance an instance that we are about to solve.
     * @param parameters parameters for this coming run.
     */
    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.currentInstance = instance;
        for (Agent agent : instance.agents) {
            // init location of each agent to his source location
            this.locations.put(agent, instance.map.getMapLocation(agent.source));

            // init agents plans
            this.agentPlans.put(agent, new SingleAgentPlan(agent, new ArrayList<Move>()));
        }

        // init agent's priority to unique number
        initPriority(instance);

        // init distance between every node in the grid to each agent's goal
        initDistances(instance);
    }


    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {

        // each iteration of the while represents timestamp
        while (!(finished())) {
            this.timeStamp++;

            updatePriorities(instance);
            // init agents that has not reached their goal
            this.unhandledAgents = new ArrayList<>();
            for (Map.Entry<Agent, Double> entry : this.priorities.entrySet()) {
                Agent agent = entry.getKey();
                Double priority = entry.getValue();
                if (priority != -1.0) {
                    this.unhandledAgents.add(agent);
                }
            }

            this.takenNodes = new ArrayList<>();


            while (!this.unhandledAgents.isEmpty()) {
                Map.Entry<Agent, Double> maxEntry = getMaxEntry(this.priorities); // <agent, priority> pair wth max priority
                Agent cur = maxEntry.getKey(); // agent with highest priority
                solvePIBT(cur, null);
            }
        }

        // create the final solution
        for (Agent agent : agentPlans.keySet()) {
            this.solution.putPlan(this.agentPlans.get(agent));
        }
        return this.solution;
    }

    /**
     * recursive main function to solve PIBT
     * @param current agent making the decision
     * @param other agent which current inherent priority from (other have higher priority)
     *              other can be null
     * @return solution
     */
    protected String solvePIBT(Agent current, Agent other) {
        if (current == null) {
            return null;
        }
        this.unhandledAgents.remove(current);

        // add neighbors of current to candidates
        List<I_Location> candidates = new ArrayList<>(findAllNeighbors(this.locations.get(current)));

        // if other != null then there is priority inheritance in the current function run
        // hence we need to make sure that the agents inherits priority can't:
        //  1. stay in current node in the next timestamp
        //  2. move to the node where the higher priority agent is

        // add current location of current agent - for the option to stay in current node in the next timestamp
        if (other == null) {
            candidates.add(this.locations.get(current)); // 1
        }
        // prevent move to the node where the higher priority agent is
        else {
            candidates.remove(this.locations.get(other)); // 2
        }

        // remove all unhandled node
        candidates.removeAll(this.takenNodes);

        while (!candidates.isEmpty()) {
            I_Location best = findBest(candidates, current);
            this.takenNodes.add(best);
            if (this.locations.containsValue(best)) {
                Agent optional = null;
                for (Agent agent: this.locations.keySet()) {
                    if (this.locations.get(agent) == best) {
                        optional = agent;
                    }
                }

                if (optional != null) {
                    if (optional.equals(current)) {
                        // add new move to the agent's plan - stay in current node
                        Move move = new Move(current, this.timeStamp, this.locations.get(current), this.locations.get(current));
                        this.agentPlans.get(current).addMove(move);
                        return "valid";
                    }

                    if (solvePIBT(optional, current).equals("valid")) {
                        // add new move to the agent's plan - change location
                        Move move = new Move(current, this.timeStamp, this.locations.get(current), best);
                        this.agentPlans.get(current).addMove(move);
                        this.locations.put(current, best);
                        return "valid";
                    }
                    else {
//                        candidates.removeAll(this.takenNodes); // ?
                        candidates.remove(best);
                    }
                }
            }
            else {
                // add new move to the agent's plan - change location
                Move move = new Move(current, this.timeStamp, this.locations.get(current), best);
                this.agentPlans.get(current).addMove(move);
                this.locations.put(current, best);
                return "valid";
            }


        }
        // add new move to the agent's plan - stay in current node
        Move move = new Move(current, this.timeStamp, this.locations.get(current), this.locations.get(current));
        this.agentPlans.get(current).addMove(move);
        return "invalid";
    }

    /**
     * helper function
     * update priority of each agent in current timestamp using instance
     */
    private void updatePriorities(MAPF_Instance instance) {
        for (Agent agent : this.priorities.keySet()) {
            // agent reach his target
            if (this.locations.get(agent).equals(instance.map.getMapLocation(agent.target))) {
                this.priorities.put(agent, -1.0);
            }
            else {
                double currentPriority = this.priorities.get(agent);
                this.priorities.put(agent, currentPriority + 1);
            }
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

    /**
     * helper function to find best coordinate among candidates
     * best will be the node which is closest to current agent goal
     * @param candidates list of candidates
     * @param current Agent currently making decision
     * @return I_Location - node who is closets to current goal among candidates
     */
    private I_Location findBest(List<I_Location> candidates, Agent current) {
        // save the map containing the distances between current's goal and all nodes in the map instance
        Map<I_Location, Integer> tmpMap = this.distancesFromGoal.get(this.currentInstance.map.getMapLocation(current.target));
        I_Location bestCandidate = null;
        int minDistance = Integer.MAX_VALUE;
        for (I_Location location : candidates) {
            if (tmpMap.containsKey(location)) {
                int distance = tmpMap.get(location);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestCandidate = location;
                }
            }
        }
        return bestCandidate;
    }

    /**
     * init priority of each agent in the beginning of the algorithm
     * each agent have a unique double representing his priority
     * update this.priorities
     */
    private void initPriority(MAPF_Instance instance) {
        int numberOfAgents = instance.agents.size();
        double uniqueFactor = 1.0 / numberOfAgents;
        int i = 1;
        for (Agent agent : instance.agents) {
            // (uniqueFactor * i) is a unique representation for the priority of each agent
            this.priorities.put(agent, uniqueFactor * i); // +1?
            i++;
        }
    }

    /**
     * update this.distancesFromGoal
     */
    private void initDistances(MAPF_Instance instance) {
        DistanceTableAStarHeuristic distanceTableObject = new DistanceTableAStarHeuristic(instance.agents, instance.map);
        this.distancesFromGoal = distanceTableObject.getDistanceDictionaries();
    }

    /**
     * function to get map entry with maximum value
     * extract agent with max priority
     * @param prioritiesMap representing agents and their priority
     * @return map entry <agent, priority>
     */
    public Map.Entry<Agent, Double> getMaxEntry(Map<Agent, Double> prioritiesMap) {
        Map.Entry<Agent, Double> maxEntry = null;
        for (Map.Entry<Agent, Double> entry : prioritiesMap.entrySet()) {
            // second condition is to validate that the agent chosen will also be in the unhandled agents in current timestamp
            if ((maxEntry == null || entry.getValue() > maxEntry.getValue()) && this.unhandledAgents.contains(entry.getKey())) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    /**
     * boolean function to check if all agents reach their goals
     * when an agent reaches his goal, his priority set to -1.0
     * if sum of all priorities is #agents * -1 then every agent reached his goal
     * @return boolean
     */
    private boolean finished() {
        int numOfAgent = this.priorities.keySet().size();
        double sumPriorities = 0;
        for (Agent agent : this.priorities.keySet()) {
            sumPriorities += this.priorities.get(agent);
        }
        return sumPriorities == -1.0 * numOfAgent;
    }
}
