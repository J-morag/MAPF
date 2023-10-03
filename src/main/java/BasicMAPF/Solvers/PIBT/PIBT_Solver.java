package BasicMAPF.Solvers.PIBT;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SOCCostFunction;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSolution;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PIBT_Solver extends A_Solver {

    /**
     * Set contains all not handled agents at timestamp t.
     * initialize in each timestamp to A (all agents that not reached their goal yet).
     * agent in set will be sorted in descending order by priority.
     */
    private HashSet<Agent> unhandledAgents;

    /**
     * Set contains all taken nodes - nodes required by an agent in the next timeStamp.
     * hence agent can't move to a node in this list.
     * when an agent chooses a node to move to in the next timestamp, the node is added to this set.
     */
    private HashSet<I_Location> takenNodes;

    /**
     * Map saving current location for each agent.
     */
    private HashMap<Agent, I_Location> currentLocations;

    /**
     * Map saving priority of each agent.
     */
    private HashMap<Agent, Double> priorities;

    /**
     * heuristic to use in the low level search to find the closest nodes to an agent's goal.
     */
    private DistanceTableAStarHeuristic heuristic;

    /**
     * HashMap saves for each agent his plan.
     * built iteratively at every time stamp.
     * at the end of the algorithm this HashMap represent the final solution.
     */
    private HashMap<Agent, SingleAgentPlan> agentPlans;
    private int timeStamp;

    /**
     * The cost function to evaluate solutions with.
     */
    private final I_SolutionCostFunction solutionCostFunction;
    private ConstraintSet constraints;

    /**
     * hash map saves for each timeStamp a HashMap, saves for each agent boolean.
     * each boolean indicates whether an agent reached his goal or not.
     */
    private HashMap<Integer, HashMap<Agent, Boolean>> reachingGoalsConfigurations;

    /**
     * constructor.
     */
    public PIBT_Solver(I_SolutionCostFunction solutionCostFunction) {
        super.name = "PIBT";
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SOCCostFunction::new);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.constraints = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.currentLocations = new HashMap<>();
        this.priorities = new HashMap<>();
        this.agentPlans = new HashMap<>();
        this.timeStamp = 0;
        this.reachingGoalsConfigurations = new HashMap<>();

        for (Agent agent : instance.agents) {
            // init location of each agent to his source location
            this.currentLocations.put(agent, instance.map.getMapLocation(agent.source));

            // init agents plans
            this.agentPlans.put(agent, new SingleAgentPlan(agent, new ArrayList<Move>()));
        }

        // init agent's priority to unique number
        initPriority(instance);

        // distance between every vertex in the graph to each agent's goal
        this.heuristic = new DistanceTableAStarHeuristic(instance.agents, instance.map);
    }


    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        boolean loopDetected = false;
        // each iteration of the while represents timestamp
        while (!(finished())) {

            // loop?
            if (this.timeStamp > 0) {
                for (int i = 2; i < this.timeStamp; i++) {
                    int agentsInSameLocation = 0;
                    for (Agent agent : this.agentPlans.keySet()) {
                        SingleAgentPlan currentPlan = this.agentPlans.get(agent);
                        // check whether locations repeat themselves
                        if (currentPlan.moveAt(i).currLocation.equals(currentPlan.getLastMove().currLocation)) {
                            // if locations repeats themselves, check if statuses also repeats themselves
                            if (this.reachingGoalsConfigurations.get(i).get(agent).equals(this.reachingGoalsConfigurations.get(this.timeStamp).get(agent))) {
                                // agent could be in a loop
                                agentsInSameLocation++;
                            }
                        }
                    }
//                    System.out.println("Agents in same location: " + agentsInSameLocation);
                    if (agentsInSameLocation == this.agentPlans.keySet().size()) {
                        System.out.println("Loop detected in timestamp: " + i);
                        System.out.println("current timestamp: " + this.timeStamp);
                        loopDetected = true;
//                        for (Agent agent : this.agentPlans.keySet()) {
//                            System.out.println("***************");
//                            System.out.println(agent);
//                            System.out.println("***************");
//                            System.out.println("Agent's current priority: " + this.priorities.get(agent));
////                            System.out.println(this.agentPlans.get(agent).getLastMove().prevLocation);
////                            System.out.println(this.agentPlans.get(agent).getLastMove().currLocation);
//                            System.out.println("timeStamp: " + (this.timeStamp-1));
//                            System.out.println(this.agentPlans.get(agent).moveAt(this.timeStamp-1).prevLocation);
//                            System.out.println(this.agentPlans.get(agent).moveAt(this.timeStamp-1).currLocation);
//
//                            System.out.println("Agent's status in prev identical timestamp: " + this.reachingGoalsConfigurations.get(i).get(agent));
//                            System.out.println("timeStamp: " + (i-1));
//                            System.out.println(this.agentPlans.get(agent).moveAt(i-1).prevLocation);
//                            System.out.println(this.agentPlans.get(agent).moveAt(i-1).currLocation);
//
//                            System.out.println("-----------");
//
////                            System.out.println(this.agentPlans.get(agent).getLastMove().prevLocation);
////                            System.out.println(this.agentPlans.get(agent).getLastMove().currLocation);
//                            System.out.println("timeStamp: " + (this.timeStamp));
//                            System.out.println(this.agentPlans.get(agent).moveAt(this.timeStamp).prevLocation);
//                            System.out.println(this.agentPlans.get(agent).moveAt(this.timeStamp).currLocation);
//
//                            System.out.println("Agent's status in prev identical timestamp: " + this.reachingGoalsConfigurations.get(i).get(agent));
//                            System.out.println("timeStamp: " + (i));
//                            System.out.println(this.agentPlans.get(agent).moveAt(i).prevLocation);
//                            System.out.println(this.agentPlans.get(agent).moveAt(i).currLocation);
//
//                            System.out.println("-----------");

//                            if (agent.iD == 3 || agent.iD == 20) {
//                                System.out.println(this.agentPlans.get(agent));
//                            }
//                        }

                        break;
                    }
                }
            }
            if (loopDetected) {
                System.out.println("LOOP DETECTED!!");
                return null;
            }
            if (checkTimeout()) {
                return null;
            }
            this.timeStamp++;

            // init agents that have not reached their goal
            this.unhandledAgents = new HashSet<>();
            for (Map.Entry<Agent, Double> entry : this.priorities.entrySet()) {
                Agent agent = entry.getKey();
                this.unhandledAgents.add(agent);
            }

            // nodes wanted in the next timestamp
            this.takenNodes = new HashSet<>();

            while (!this.unhandledAgents.isEmpty()) {
                Map.Entry<Agent, Double> maxEntry = getMaxEntry(this.priorities); // <agent, priority> pair with max priority
                Agent cur = maxEntry.getKey(); // agent with the highest priority
                solvePIBT(cur, null);
            }

            updatePriorities(instance);
            updateStatus();
        }

        Solution solution = new TransientMAPFSolution();
        for (Agent agent : agentPlans.keySet()) {
            solution.putPlan(this.agentPlans.get(agent));
            if (this.constraints.rejectsEventually(this.agentPlans.get(agent).getLastMove(),true) != -1) {
                throw new UnsupportedOperationException("Limited support for constraints. Ignoring infinite constraints, and constrains while a finished agent stays in place");
            }
        }
        return solution;
    }

    /**
     * recursive main function to solve PIBT
     * @param current agent making the decision
     * @param higherPriorityAgent agent which current inherits priority from
     * @return boolean - is current agent made a valid / invalid move
     */
    protected boolean solvePIBT(Agent current, @Nullable Agent higherPriorityAgent) {
        if (current == null) {
            return false;
        }

        this.unhandledAgents.remove(current);

        // add neighbors of current to candidates
        List<I_Location> candidates = new ArrayList<>(findAllNeighbors(this.currentLocations.get(current)));

        // if higherPriorityAgent != null then there is priority inheritance in the current function run
        // hence we need to make sure that the agents inherits priority can't:
        // (originally he interrupts the other agent)
        //  1. stay in current node in the next timestamp
        //  2. move to the node where the higher priority agent is

        // add current location of current agent - for the option to stay in current node in the next timestamp
//        if (higherPriorityAgent == null) {
//            candidates.add(this.currentLocations.get(current)); // 1
//        }
        // prevent move to the node where the higher priority agent is
//        else {
//            candidates.remove(this.currentLocations.get(higherPriorityAgent)); // 2
//        }
        candidates.add(this.currentLocations.get(current)); // 1
        if (higherPriorityAgent != null) {
            candidates.remove(this.currentLocations.get(higherPriorityAgent)); // 2
        }

        // remove all taken nodes by higher priorities agents
        candidates.removeAll(this.takenNodes);

        while (!candidates.isEmpty()) {
            I_Location best = findBest(candidates, current);
            this.takenNodes.add(best);

            // best is taken
            if (this.currentLocations.containsValue(best)) {
                Agent optional = null;
                for (Agent agent: this.currentLocations.keySet()) {
                    if (this.currentLocations.get(agent) == best) {
                        optional = agent;
                        break;
                    }
                }

                if (optional != null) {
                    if (optional.equals(current) && canMove(current)) {
                        // add new move to the agent's plan - the best is to stay in current node
                        if (addNewMoveToAgent(current, this.currentLocations.get(current))){
                            return true;
                        };
                    }

                    else if (solvePIBT(optional, current) && canMove(current)) {
                        // add new move to the agent's plan - change location
                        if (addNewMoveToAgent(current, best)) {
                            return true;
                        };
                    }
                    else {
                        // priority inheritance didn't work, remove best and try next candidate
                        candidates.remove(best);
                    }
                }
            }

            // best is free
            else if (canMove(current)) {
                // add new move to the agent's plan - change location
                if (addNewMoveToAgent(current, best)) {
                    return true;
                };
            }
            candidates.remove(best);
        }

        // finish try all candidates and didn't find new location
        // add new move to the agent's plan - stay in current node
        if (canMove(current)) {
            if (addNewMoveToAgent(current, this.currentLocations.get(current))) {
                this.takenNodes.add(this.currentLocations.get(current));
                return false;
            };
        }
        return false;
    }

    /**
     * helper function
     * update priority of each agent in current timestamp using instance
     */
    private void updatePriorities(MAPF_Instance instance) {
        for (Agent agent : this.priorities.keySet()) {
            // agent reach his target
            if (this.agentPlans.get(agent).containsTarget()) {
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
     * distances are calculated using this.heuristic
     * @param candidates list of candidates
     * @param current Agent currently making decision
     * @return I_Location - node who is closets to current goal among candidates
     */
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
            this.priorities.put(agent, uniqueFactor * i);
            i++;
        }
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
     * if one of the agents have priority different then -1.0, then return false
     * @return boolean indicates if all agents reached their goal
     */
    private boolean finished() {
        for (Double priority : this.priorities.values()) {
            if (priority != -1.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * function to verify that agent can move in the current timestamp
     * the function return True if the agent didn't make a move in the current timestamp and false if he did
     * @param agent - agent to verify next move
     */
    private boolean canMove(Agent agent) {
        SingleAgentPlan agentPlan = this.agentPlans.get(agent);
        return agentPlan.size() == 0 || agentPlan.getEndTime() < this.timeStamp;
    }

    /**
     * function that adds new move to an agent
     * @param current - the agent we need to add move to
     * @param newLocation - the location that the agent is moving to
     * @return boolean : true if the move added successfully, false otherwise
     */
    private boolean addNewMoveToAgent(Agent current, I_Location newLocation) {
        Move move = new Move(current, this.timeStamp, this.currentLocations.get(current), newLocation);
        if (this.constraints.accepts(move)) {
            this.agentPlans.get(current).addMove(move);
            this.currentLocations.put(current, newLocation);
            return true;
        }
        return false;
    }

    /**
     * function that updates a HashMap indicates whether an agent reached his goal or not
     * if an agent has priority -1.0 than he reached his goal and the function adds true to the list
     * otherwise, false
     */
    private void updateStatus() {
        this.reachingGoalsConfigurations.put(this.timeStamp, new HashMap<>());
        for (Agent agent : this.priorities.keySet()) {
            Boolean reachedGoal = (this.priorities.get(agent) == -1.0);
            this.reachingGoalsConfigurations.get(this.timeStamp).put(agent, reachedGoal);
        }
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.agentPlans = null;
        this.heuristic = null;
        this.currentLocations = null;
        this.priorities = null;
        this.takenNodes = null;
        this.unhandledAgents = null;
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
