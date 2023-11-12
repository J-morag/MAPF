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
import LifelongMAPF.I_LifelongCompatibleSolver;
import TransientMAPF.TransientMAPFSolution;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PIBT_Solver extends A_Solver implements I_LifelongCompatibleSolver {

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
     * How far forward in time to consider conflicts. Further than this time conflicts will be ignored.
     */
    public final Integer RHCR_Horizon;

    /**
     * Agent's plans build only from this timestamp.
     */
    public Integer problemStartTime;

    private static final int DEBUG = 0;

    /**
     * Set who saves lists of agent's locations - configurations, as lists.
     * The use of this set is to detect loops.
     */
    private Set<List<I_Location>> configurations;

    /**
     * booleans indicate if it needs to return partial plans in case PIBT can't find solution.
     * if true, instead of return null, the current solution will return.
     */
    private final boolean returnPartialSolutions;

    /**
     * constructor.
     */
    public PIBT_Solver(I_SolutionCostFunction solutionCostFunction, Integer RHCR_Horizon, Boolean returnPartialSolutions) {
        super.name = "PIBT";
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SOCCostFunction::new);
        this.RHCR_Horizon = Objects.requireNonNullElse(RHCR_Horizon, Integer.MAX_VALUE);
        this.returnPartialSolutions = returnPartialSolutions;
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.constraints = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.currentLocations = new HashMap<>();
        this.priorities = new HashMap<>();
        this.agentPlans = new HashMap<>();
        this.timeStamp = parameters.problemStartTime;
        this.problemStartTime = parameters.problemStartTime;
        this.configurations = new HashSet<>();

        for (Agent agent : instance.agents) {
            // init location of each agent to his source location
            this.currentLocations.put(agent, instance.map.getMapLocation(agent.source));

            // init agents plans
            this.agentPlans.put(agent, new SingleAgentPlan(agent, new ArrayList<Move>()));
        }

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
        // each iteration of the while represents timestamp
        while (!(finished())) {

            // algorithm starts agent's plans only from problemTimeStamp
            if (this.problemStartTime > this.timeStamp) {
                this.timeStamp++;
                continue;
            }

            // loop detection
            ArrayList<I_Location> currentConfiguration = new ArrayList<>(instance.agents.size());
            for (Agent agent : instance.agents) {
                currentConfiguration.add(this.currentLocations.get(agent));
            }
            if (this.configurations.contains(currentConfiguration)) {
                if (DEBUG >= 2){
                    System.out.println("LOOP DETECTED");
                }
                return createSolution();
            }
            else {
                this.configurations.add(currentConfiguration);
            }

            if (checkTimeout()) {
                return createSolution();
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

    @Nullable
    private Solution createSolution() {
        if (this.returnPartialSolutions) {
            Solution solution = new TransientMAPFSolution();
            int numberOfNotMovingAgents = 0;
            for (Agent agent : agentPlans.keySet()) {
                if (this.agentPlans.get(agent).size() == 0) {
                    numberOfNotMovingAgents++;
                }
                solution.putPlan(this.agentPlans.get(agent));
                if (this.constraints.rejectsEventually(this.agentPlans.get(agent).getLastMove(),true) != -1) {
                    throw new UnsupportedOperationException("Limited support for constraints. Ignoring infinite constraints, and constrains while a finished agent stays in place");
                }
            }
            if (numberOfNotMovingAgents == this.agentPlans.keySet().size()) {
                return null;
            }
            return solution;
        }
        return null;
    }

    /**
     * recursive main function to solve PIBT.
     * @param current agent making the decision.
     * @param higherPriorityAgent agent which current inherits priority from.
     * @return boolean - is current agent made a valid / invalid move.
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
            if (this.currentLocations.containsValue(best) && needToCheckConflicts()) {
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
     * helper function.
     * update priority of each agent in current timestamp using instance.
     */
    private void updatePriorities(MAPF_Instance instance) {
        for (Agent agent : this.priorities.keySet()) {
            // agent reach his target
            if (this.agentPlans.get(agent).containsTarget()) {
                if (this.priorities.get(agent) != -1.0) {
                    this.configurations = new HashSet<>();
                }
                this.priorities.put(agent, -1.0);
            }
            else {
                double currentPriority = this.priorities.get(agent);
                this.priorities.put(agent, currentPriority + 1);
            }
        }
    }

    /**
     * helper function to find all neighbors of single agent.
     * @param location to find his neighbors.
     * @return List contains all neighbors of current I_Location.
     */
    private List<I_Location> findAllNeighbors(I_Location location) {
        return location.outgoingEdges();
    }

    /**
     * helper function to find best coordinate among candidates.
     * best will be the node which is closest to current agent goal.
     * distances are calculated using this.heuristic.
     * @param candidates list of candidates.
     * @param current Agent currently making decision.
     * @return I_Location - node who is closets to current goal among candidates.
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
     * function to get map entry with maximum value.
     * extract agent with max priority.
     * @param prioritiesMap representing agents and their priority.
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
     * boolean function to check if all agents reach their goals.
     * when an agent reaches his goal, his priority set to -1.0.
     * if one of the agents have priority different then -1.0, then return false.
     * @return boolean indicates if all agents reached their goal.
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
     * function to verify that agent can move in the current timestamp.
     * the function return True if the agent didn't make a move in the current timestamp and false if he did.
     * @param agent - agent to verify next move.
     */
    private boolean canMove(Agent agent) {
        SingleAgentPlan agentPlan = this.agentPlans.get(agent);
        return agentPlan.size() == 0 || agentPlan.getEndTime() < this.timeStamp;
    }

    /**
     * function that adds new move to an agent.
     * @param current - the agent we need to add move to.
     * @param newLocation - the location that the agent is moving to.
     * @return boolean : true if the move added successfully, false otherwise.
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
     * planning horizon - after k timestamps, ignore all conflicts.
     * this function check whether k timestamps have passed.
     * @return boolean: true if conflicts needs to be checked, otherwise return false.
     */
    private boolean needToCheckConflicts() {
        if (this.timeStamp != 0) {
            return this.RHCR_Horizon >= this.timeStamp;
        }
        return true;
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
        this.constraints = null;
        this.configurations = null;
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

    @Override
    public boolean sharedSources() {
        return false;
    }

    @Override
    public boolean sharedGoals() {
        return true;
    }
}
