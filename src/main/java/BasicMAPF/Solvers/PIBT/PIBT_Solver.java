package BasicMAPF.Solvers.PIBT;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import TransientMAPF.SeparatingVerticesFinder;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import TransientMAPF.TransientMAPFUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Priority Inheritance with Backtracking (PIBT) algorithm.
 * Okumura, Keisuke, et al. "Priority inheritance with backtracking for iterative multi-agent path finding." Artificial Intelligence 310 (2022).
 */
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
    private SingleAgentGAndH heuristic;

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
    private I_ConstraintSet constraints;

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
     * variable indicates whether the solution returned by the algorithm is transient.
     */
    private final TransientMAPFSettings transientMAPFSettings;

    /**
     * Map saving for each agent its goal location, representing the goal configuration.
     */
    private HashMap<Agent, I_Location> goalConfiguration;

    private boolean agentCantMoveOrStay;
    private boolean allAgentsReachedGoal;
    private Set<I_Location> separatingVerticesSet;
    private Comparator<I_Location> separatingVerticesComparator;
    private MAPF_Instance instance;


    /**
     * constructor.
     */
    public PIBT_Solver(I_SolutionCostFunction solutionCostFunction, Integer RHCR_Horizon, TransientMAPFSettings transientMAPFSettings) {
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        this.RHCR_Horizon = Objects.requireNonNullElse(RHCR_Horizon, Integer.MAX_VALUE);
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        super.name = "PIBT" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "") + (this.transientMAPFSettings.avoidSeparatingVertices() ? "_SV" : "");
    }

    /**
     * Default constructor.
     */
    public PIBT_Solver() {
        this(null, null, TransientMAPFSettings.defaultTransientMAPF);
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
        this.goalConfiguration = new HashMap<>();
        this.agentCantMoveOrStay = false;
        this.allAgentsReachedGoal = false;
        this.instance = instance;

        for (Agent agent : instance.agents) {
            // init location of each agent to his source location
            this.currentLocations.put(agent, instance.map.getMapLocation(agent.source));

            // init agents plans
            this.agentPlans.put(agent, new SingleAgentPlan(agent, new ArrayList<Move>()));

            // init goal configuration
            this.goalConfiguration.put(agent, instance.map.getMapLocation(agent.target));
        }

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

        // init agent's priority to unique number
        initPriority(instance);
    }


    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        // each iteration of the while represents timestamp
        while (!(finished())) {

            // loop detection
            ArrayList<I_Location> currentConfiguration = new ArrayList<>(instance.agents.size());
            for (Agent agent : instance.agents) {
                currentConfiguration.add(this.currentLocations.get(agent));
            }
            if (this.configurations.contains(currentConfiguration) && !this.allAgentsReachedGoal) {
                if (Config.DEBUG >= 1){
                    System.out.println("LOOP DETECTED");
                }
                return null;
            }
            else {
                this.configurations.add(currentConfiguration);
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
            updatePriorities();
        }

        return makeSolution();
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

        if (this.transientMAPFSettings.avoidSeparatingVertices() && this.priorities.get(current) == -1) {
            // sort candidates so that all SV vertices are at the end of the list
            candidates.sort(this.separatingVerticesComparator);
        }

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
     * update priority of each agent in current time step using instance.
     */
    private void updatePriorities() {
        for (Agent agent : this.priorities.keySet()) {
            double currentPriority = this.priorities.get(agent);
            boolean agentHasReachedTarget = this.transientMAPFSettings.isTransientMAPF()
                    ? this.agentPlans.get(agent).containsTarget()
                    : this.agentPlans.get(agent).getLastMove().currLocation.equals(this.goalConfiguration.get(agent));

            // Update priorities based on whether the agent has reached its target
            if (agentHasReachedTarget) {
                if (currentPriority != -1.0) {
                    this.configurations = new HashSet<>();
                }
                this.priorities.put(agent, -1.0);
            } else {
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
        float minDistance = Float.MAX_VALUE;
        for (I_Location location : candidates) {
            float distance = (float) this.heuristic.getHToTargetFromLocation(current.target, location);
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

        // TMAPF
        if (this.transientMAPFSettings.isTransientMAPF()) {
            for (Double priority : this.priorities.values()) {
                if (priority != -1.0) {
                    return false;
                }
            }
        }

        // regular MAPF
        else {
            for (Agent agent : this.agentPlans.keySet()) {
                if (!(this.currentLocations.get(agent).equals(this.goalConfiguration.get(agent)))) {
                    return false;
                }
            }
        }

        this.allAgentsReachedGoal = true;
        for (Agent agent : this.priorities.keySet()) {
            if (this.constraints.firstRejectionTime(this.agentPlans.get(agent).getLastMove()) != -1) {
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

    @NotNull
    private Solution makeSolution() {
        this.timeStamp++;
        Solution solution = transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution() : new Solution();

        for (Agent agent : agentPlans.keySet()) {
            while (this.constraints.firstRejectionTime(this.agentPlans.get(agent).getLastMove()) != -1) {
                solvePIBT(agent, null);
            }
            solution.putPlan(this.agentPlans.get(agent));
        }

        for (Agent agent : agentPlans.keySet()) {
            SingleAgentPlan plan = this.agentPlans.get(agent);
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
            else solution.putPlan(this.agentPlans.get(agent));
        }
        return solution;
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
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
        this.separatingVerticesSet = null;
    }
}
