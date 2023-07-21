package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SOCCostFunction;
import TransientMAPF.TransientMAPFSolution;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.PartialSolutionsStrategy;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.AStar.GoalConditions.SingleTargetCoordinateGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedAGoalAtSomePointInPlanGoalCondition;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import LifelongMAPF.FailPolicies.FailPolicy;
import LifelongMAPF.I_LifelongCompatibleSolver;
import org.apache.commons.lang.mutable.MutableInt;

import java.util.*;

import static com.google.common.math.IntMath.factorial;

/**
 * An implementation of the Prioritised Planning algorithm for Multi Agent Path Finding.
 * It solves {@link MAPF_Instance MAPF problems} very quickly, but does not guarantee optimality, and will very likely
 * return a sub-optimal {@link Solution}.
 */
public class PrioritisedPlanning_Solver extends A_Solver implements I_LifelongCompatibleSolver {

    /*  = Fields =  */

    /* = Constants = */
    public final static String countInitialAttemptsMetricString = "count initial attempts";
    public final static String countContingencyAttemptsMetricString = "count contingency attempts";
    public final static String maxReachedIndexBeforeTimeoutString = "max reached index";
    private static final int DEBUG = 1;

    /*  =  = Fields related to the MAPF instance =  */
    /**
     * An array of {@link Agent}s to plan for, ordered by priority (descending).
     */
    private List<Agent> agents;
    /**
     * Start time of the problem. Not real-time.
     */
    private int problemStartTime;

    /*  =  = Fields related to the run =  */

    private ConstraintSet constraints;

    private Random random;
    private Set<Agent> failedAgents;
    private RemovableConflictAvoidanceTableWithContestedGoals initialConflictAvoidanceTable;
    int maxReachedIndex;

    /*  =  = Fields related to the class instance =  */

    /**
     * A {@link I_Solver solver}, to be used for solving sub-problems where only one agent is to be planned for, and the
     * existing {@link SingleAgentPlan plans} for other {@link Agent}s are to be avoided.
     */
    private final I_Solver lowLevelSolver;
    /**
     * How to sort the agents. This sort determines their priority. The first agent will be treated as having
     * the highest priority, the one after will have the second highest priority, and so forth.
     */
    private final Comparator<Agent> agentComparator;

    private final RestartsStrategy restartsStrategy;

    /**
     * The cost function to evaluate solutions with.
     */
    private final I_SolutionCostFunction solutionCostFunction;
    /**
     * optional heuristic function to use in the low level solver.
     */
    private AStarGAndH heuristic;

    /**
     * if agents share goals, they will not conflict at their goal.
     */
    public boolean sharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not conflict 
     */
    public boolean sharedSources;
    private Boolean TransientMAPFGoalCondition;
    /**
     * How to approach partial solutions from the multi-agent perspective
     */
    private PartialSolutionsStrategy partialSolutionsStrategy;
    /**
     * How far forward in time to consider conflicts. Further than this time conflicts will be ignored.
     */
    public final Integer RHCR_Horizon;
    public final FailPolicy failPolicy;


    /*  = Constructors =  */

    /**
     * Constructor.
     * @param lowLevelSolver A {@link I_Solver solver}, to be used for solving sub-problems where only one agent is to
     *                      be planned for, and the existing {@link SingleAgentPlan plans} for other
     *                      {@link Agent}s are to be avoided.
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver) {
        this(lowLevelSolver, null, null, null, null, null, null, null, null);
    }

    /**
     * Constructor.
     * @param agentComparator How to sort the agents. This sort determines their priority. High priority first.
     */
    public PrioritisedPlanning_Solver(Comparator<Agent> agentComparator) {
        this(null, agentComparator, null, null, null, null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param lowLevelSolver             A {@link I_Solver solver}, to be used for solving sub-problems for only one agent.
     * @param agentComparator            How to sort the agents. This sort determines their priority. High priority first.
     * @param solutionCostFunction       A cost function to evaluate solutions with. Only used when using random restarts.
     * @param restartsStrategy           how to do restarts.
     * @param sharedGoals                if agents share goals, they will not conflict at their goal.
     * @param transientMAPFGoalCondition if true will use {@link VisitedAGoalAtSomePointInPlanGoalCondition}
     * @param failPolicy                 how to handle single agent failures while solving
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver, Comparator<Agent> agentComparator,
                                      I_SolutionCostFunction solutionCostFunction, RestartsStrategy restartsStrategy,
                                      Boolean sharedGoals, Boolean sharedSources, Boolean transientMAPFGoalCondition,
                                      Integer RHCR_Horizon, FailPolicy failPolicy) {
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.agentComparator = agentComparator;
        this.solutionCostFunction = Objects.requireNonNullElse(solutionCostFunction, new SOCCostFunction());
        this.restartsStrategy = Objects.requireNonNullElse(restartsStrategy, new RestartsStrategy());
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.TransientMAPFGoalCondition = Objects.requireNonNullElse(transientMAPFGoalCondition, false);
        this.RHCR_Horizon = RHCR_Horizon;
        this.failPolicy = failPolicy;
        if (this.RHCR_Horizon != null && this.RHCR_Horizon < 1){
            throw new IllegalArgumentException("RHCR horizon must be >= 1");
        }

        super.name = "PrP" + (this.restartsStrategy.isNoRestarts() ? "" : " + " + this.restartsStrategy);
    }

    /**
     * Default constructor.
     */
    public PrioritisedPlanning_Solver(){
        this(null, null, null, null, null, null, null, null, null);
    }

    /*  = initialization =  */

    /**
     * Initialises the object in preparation to solving an {@link MAPF_Instance}.
     * @param instance - the instance that we will have to solve.
     * @param parameters - parameters that affect the solution process.
     */
    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);

        this.agents = new ArrayList<>(instance.agents);

        this.problemStartTime = parameters.problemStartTime;

        if (parameters.constraints != null){
            if (this.RHCR_Horizon != null){
                this.constraints = new ConstraintSet(parameters.constraints, horizonAsAbsoluteTime(this.problemStartTime, this.RHCR_Horizon));
            }
            else {
                this.constraints = new ConstraintSet(parameters.constraints);
            }
        }
        else{
            this.constraints = new ConstraintSet();
        }

        this.constraints.sharedGoals = this.sharedGoals;
        this.constraints.sharedSources = this.sharedSources;
        this.random = Objects.requireNonNullElse(parameters.randomNumberGenerator, new Random(42));
        // if we were given a comparator for agents, sort the agents according to this priority order.
        if (this.agentComparator != null){
            this.agents.sort(this.agentComparator);
        }

        this.failedAgents = new HashSet<>();
        this.initialConflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();

        this.maxReachedIndex = -1;

        if(parameters instanceof RunParameters_PP parametersPP){

            //reorder according to requested priority
            if(parametersPP.preferredPriorityOrder != null && parametersPP.preferredPriorityOrder.length > 0) {
                reorderAgentsByPriority(parametersPP.preferredPriorityOrder);
            }

            if(parametersPP.heuristic != null) {
                this.heuristic = parametersPP.heuristic;
                if (this.heuristic instanceof CachingDistanceTableHeuristic){
                    ((CachingDistanceTableHeuristic)this.heuristic).setCurrentMap(instance.map);
                }
            }
            else {this.heuristic = new DistanceTableAStarHeuristic(this.agents, instance.map);} // TODO replace with distance table? should usually be worth it

            this.partialSolutionsStrategy = parametersPP.partialSolutionsStrategy;

            if (parametersPP.failedAgents != null){
                this.failedAgents = parametersPP.failedAgents;
            }

            if (parametersPP.conflictAvoidanceTable != null){
                this.initialConflictAvoidanceTable = parametersPP.conflictAvoidanceTable;
            }
        }

        this.partialSolutionsStrategy = Objects.requireNonNullElse(this.partialSolutionsStrategy, new DisallowedPartialSolutionsStrategy());
    }

    private void reorderAgentsByPriority(Agent[] requestedOrder) {
        HashSet<Agent> tmpAgents = new HashSet<>(this.agents);
        this.agents.clear();

        for (Agent orderedAgent: //add by order
                requestedOrder) {
            if(tmpAgents.contains(orderedAgent)){
                this.agents.add(orderedAgent);
                tmpAgents.remove(orderedAgent);
            }
        }
        if (!tmpAgents.isEmpty()){
            throw new IllegalArgumentException("The requested priority order does not contain all agents.");
        }
    }

    /*  = algorithm =  */

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        return solvePrioritisedPlanning(instance, this.constraints);
    }

    /**
     * The main loop that solves the MAPF problem.
     * The basic idea of the algorithm is to solve a single agent path finding problem for each agent while avoiding the
     * plans of previous agents.
     * It returns a valid solution, but does not guarantee optimality.
     * @return a valid, yet non-optimal {@link Solution} to an {@link MAPF_Instance}.
     * @param instance problem instance
     * @param initialConstraints constraints to solve under
     */
    protected Solution solvePrioritisedPlanning(MAPF_Instance instance, ConstraintSet initialConstraints) {
        Solution bestSolution = null;
        Solution bestPartialSolution = new Solution();
        int bestPartialSolutionSingleAgentSuccesses = 0;
        int singleAgentFPsTriggered = 0;
        Set<Agent> bestPartialSolutionFailedAgents = new HashSet<>();
        int numPossibleOrderings = factorial(this.agents.size());
        Set<List<Agent>> randomOrderings = new HashSet<>(); // TODO prefix tree memoization?
        randomOrderings.add(new ArrayList<>(agents));
        Set<List<Agent>> deterministicOrderings = new HashSet<>();
        deterministicOrderings.add(new ArrayList<>(agents));
        int attemptNumber = 0;
        // if using random restarts, try more than once and randomize between them
        for (;;attemptNumber++) {
            Solution solution = new Solution();
            ConstraintSet currentConstraints = new ConstraintSet(initialConstraints);
            RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals(this.initialConflictAvoidanceTable);
            Set<Agent> failedAgents = new HashSet<>();
            Agent firstFailedAgent = null;
            MutableInt failPolicyIterations = new MutableInt(0);
            //solve for each agent while avoiding the plans of previous agents (standard PrP)
            for (int agentIndex = 0; agentIndex < agents.size(); agentIndex++){
                Agent agent = agents.get(agentIndex);
                if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;
                maxReachedIndex = Math.max(maxReachedIndex, agentIndex);

                //solve the subproblem for one agent
                SingleAgentPlan planForAgent = solveSubproblem(agent, instance, currentConstraints,
                        // if the cost of the next agent increases current cost beyond the current best, no need to finish search/iteration.
                        bestSolution != null ? solutionCostFunction.solutionCost(bestSolution) - solutionCostFunction.solutionCost(solution)
                                : Float.POSITIVE_INFINITY, solution);

                if (planForAgent == null || ! planForAgent.containsTarget()) {
                    if (planForAgent != null)
                        singleAgentFPsTriggered++;
                    failedAgents.add(agent);
                    if (firstFailedAgent == null) firstFailedAgent = agent;
                    if (! this.partialSolutionsStrategy.moveToNextPrPIteration(instance, attemptNumber, solution, agent, agentIndex, true, bestSolution != null)) {
                        int numFailedAgentsBeforeFailPolicy = failedAgents.size();

                        SingleAgentPlan initialFailPlan = planForAgent != null ? planForAgent : // So we got a partial plan from A*
                                failPolicy != null ? failPolicy.getFailPolicyPlan(problemStartTime, agent, instance.map.getMapLocation(agent.source), conflictAvoidanceTable):
                                null; // if not using a fail policy
                        if (failPolicy != null){
                            savePlanToSolutionConstraintsAndCongestion(solution, currentConstraints, initialFailPlan);
                            conflictAvoidanceTable.addPlan(initialFailPlan);
                            solution = this.failPolicy.getKSafeSolution(solution, problemStartTime, failedAgents, conflictAvoidanceTable, failPolicyIterations);
                        }

                        if (failedAgents.size() > numFailedAgentsBeforeFailPolicy){ // so the fail policy added more failed agents in failPolicy.getKSafeSolution
                            // reset constraints since the solution changed
                            currentConstraints = new ConstraintSet(initialConstraints);
                            if (this.heuristic instanceof DistanceTableAStarHeuristic distanceTable
                                    && distanceTable.congestionMap != null){
                                distanceTable.congestionMap.clear();
                            }
                            for (SingleAgentPlan singleAgentPlan : solution) {
                                addPlanToConstraints(currentConstraints, singleAgentPlan);
                                addPlanToCongestionMap(singleAgentPlan);
                                // and the conflict avoidance table is already updated when getting k-safe solution
                            }
                        }
                    }

                    int successfulAgents = agentIndex + 1 - failedAgents.size();

                    if (this.partialSolutionsStrategy.allowed() && solution != bestPartialSolution &&
                            (successfulAgents > bestPartialSolutionSingleAgentSuccesses ||
                                    (successfulAgents == bestPartialSolutionSingleAgentSuccesses &&
                                            this.solutionCostFunction.solutionCost(solution) < this.solutionCostFunction.solutionCost(bestPartialSolution)))){
                        bestPartialSolution = solution;
                        bestPartialSolutionSingleAgentSuccesses = successfulAgents;
                        bestPartialSolutionFailedAgents = failedAgents;
                    } else if (this.partialSolutionsStrategy.allowed() && solution != bestPartialSolution &&
                            (failedAgents.size() > bestPartialSolutionFailedAgents.size() ||
                                    (failedAgents.size() == bestPartialSolutionFailedAgents.size() &&
                                            this.solutionCostFunction.solutionCost(solution) < this.solutionCostFunction.solutionCost(bestPartialSolution)))) {
                        break;
                    }

                    if (// TODO smarter failedToPlanForCurrentAgent and alreadyFoundFullSolution when we get partial plans
                        this.partialSolutionsStrategy.moveToNextPrPIteration(instance, attemptNumber, solution, agent, agentIndex, true, bestSolution != null))
                    {
                        break;
                    }
                }
                else {
                    savePlanToSolutionConstraintsAndCongestion(solution, currentConstraints, planForAgent);
                }
            }


            /* = random/deterministic restarts = */

            if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;
            if (failedAgents.size() == 0 &&
                    (bestSolution == null ||
                            (solutionCostFunction.solutionCost(solution) < solutionCostFunction.solutionCost(bestSolution)))){
                bestSolution = solution;
            }

            // report the completed attempt
            this.instanceReport.putIntegerValue("fail policy iterations", failPolicyIterations.intValue());
            if (restartsStrategy.hasInitial() && attemptNumber <= restartsStrategy.numInitialRestarts){
                this.instanceReport.putIntegerValue(countInitialAttemptsMetricString, attemptNumber + 1);
                this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " cost", bestSolution != null ? Math.round(this.solutionCostFunction.solutionCost(bestSolution)) : -1);
                this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " time", (int)((System.nanoTime()/1000000)-super.startTime));
                this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " failed agents", failedAgents.size());
            }
            else if (attemptNumber > restartsStrategy.numInitialRestarts && restartsStrategy.hasContingency()){
                this.instanceReport.putIntegerValue(countContingencyAttemptsMetricString, attemptNumber - restartsStrategy.numInitialRestarts);
            }


            if (attemptNumber + 1 == numPossibleOrderings){
                break; // exhausted all possible orderings
            }

            RestartsStrategy.RestartsKind restartsKind;
            if (restartsStrategy.hasInitial() && attemptNumber < restartsStrategy.numInitialRestarts){
                restartsKind = restartsStrategy.initialRestarts;
            }
            else if (bestSolution == null && attemptNumber >= restartsStrategy.numInitialRestarts && restartsStrategy.hasContingency()){
                restartsKind = restartsStrategy.contingencyRestarts;
            }
            else {
                break;
            }

            if (restartsKind == RestartsStrategy.RestartsKind.randomRestarts){
                do { // do not repeat orderings
                    Collections.shuffle(this.agents, this.random);
                }
                while (randomOrderings.contains(this.agents) || deterministicOrderings.contains(this.agents));

                randomOrderings.add(new ArrayList<>(this.agents));
            }
            else if (restartsKind == RestartsStrategy.RestartsKind.deterministicRescheduling){
                if ( ! failedAgents.isEmpty()){
                    this.agents.remove(firstFailedAgent);
                    this.agents.add(0, firstFailedAgent);
                    if (deterministicOrderings.contains(this.agents)){
                        break; // deterministic ordering can end up in a loop - terminates if repeats itself
                    }

                    deterministicOrderings.add(new ArrayList<>(this.agents));
                }
                else { // deterministic restarts only restarts if no solution was found
                    break;
                }
            }
        }
        instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, bestSolution == null ? 0: 1);
        if (DEBUG >= 2){
            System.out.println("PrP attempts: " + attemptNumber + ", SAFPsTriggered: " + singleAgentFPsTriggered);
        }

        if (this.partialSolutionsStrategy.allowed() && bestSolution == null){
            this.partialSolutionsStrategy.updateAfterSolution(agents.size(), bestPartialSolutionSingleAgentSuccesses);
            this.failedAgents.addAll(bestPartialSolutionFailedAgents); // kind of ugly. This is a part of the output basically. Add to instance report?
            return finalizeSolution(bestPartialSolution);
        }
        else {
            this.partialSolutionsStrategy.updateAfterSolution(agents.size(), bestSolution != null ? bestSolution.size() : 0);
            return finalizeSolution(bestSolution);
        }
    }

    private void savePlanToSolutionConstraintsAndCongestion(Solution solution, ConstraintSet currentConstraints, SingleAgentPlan plan) {
        //save the plan for this agent
        solution.putPlan(plan);
        //add constraints to prevent the next agents from conflicting with the new plan
        addPlanToConstraints(currentConstraints, plan);
        addPlanToCongestionMap(plan);
    }

    private Solution finalizeSolution(Solution bestSolution) {
        return (TransientMAPFGoalCondition && bestSolution != null) ? new TransientMAPFSolution(bestSolution) : bestSolution;
    }

    private void addPlanToCongestionMap(SingleAgentPlan planForAgent) {
        // if using congestion, add this plan to the congestion map
        if (this.heuristic instanceof DistanceTableAStarHeuristic distanceTable
                && distanceTable.congestionMap != null){
            distanceTable.congestionMap.registerPlan(planForAgent); // TODO horizon?
        }
    }

    private void addPlanToConstraints(ConstraintSet currentConstraints, SingleAgentPlan planForAgent) {
        if (this.RHCR_Horizon == null){
            currentConstraints.addAll(currentConstraints.allConstraintsForPlan(planForAgent));
        }
        else {
            currentConstraints.addAll(currentConstraints.allConstraintsForPlan(planForAgent, horizonAsAbsoluteTime(problemStartTime, RHCR_Horizon)));
        }
    }

    private int horizonAsAbsoluteTime(int problemStartTime, int horizon) {
        return Math.addExact(problemStartTime, horizon);
    }

    protected SingleAgentPlan solveSubproblem(Agent currentAgent, MAPF_Instance fullInstance, ConstraintSet constraints, float maxCost, Solution solutionSoFar) {
        //create a sub-problem
        MAPF_Instance subproblem = fullInstance.getSubproblemFor(currentAgent);
        InstanceReport subproblemReport = initSubproblemReport(fullInstance);
        RunParameters subproblemParameters = getSubproblemParameters(subproblem, subproblemReport, constraints, maxCost, solutionSoFar);

        //solve sub-problem
        Solution singleAgentSolution = this.lowLevelSolver.solve(subproblem, subproblemParameters);
        digestSubproblemReport(subproblemReport);
        if (singleAgentSolution != null){
            return singleAgentSolution.getPlanFor(currentAgent);
        }
        else{ //agent is unsolvable
            return null;
        }
    }

    private static InstanceReport initSubproblemReport(MAPF_Instance instance) {
        InstanceReport subproblemReport = new InstanceReport();
        subproblemReport.putStringValue("Parent Instance", instance.name);
        subproblemReport.putStringValue("Parent Solver", PrioritisedPlanning_Solver.class.getSimpleName());
        return subproblemReport;
    }

    protected RunParameters getSubproblemParameters(MAPF_Instance subproblem, InstanceReport subproblemReport, ConstraintSet constraints, float maxCost, Solution solutionSoFar) {
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        RunParameters_SAAStar params = new RunParameters_SAAStar(new RunParameters(timeLeftToTimeout, new ConstraintSet(constraints),
                subproblemReport, new Solution(solutionSoFar) // should probably work without copying, but just to be safe
                , this.problemStartTime), this.heuristic /*nullable*/, maxCost);
        if (TransientMAPFGoalCondition){
            params.goalCondition = new VisitedAGoalAtSomePointInPlanGoalCondition(new SingleTargetCoordinateGoalCondition(subproblem.agents.get(0).target));
        }
        return params;
    }


    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue(maxReachedIndexBeforeTimeoutString, maxReachedIndex);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
            instanceReport.putIntegerValue("SST", solution.sumServiceTimes());
            instanceReport.putIntegerValue("SOC", solution.sumIndividualCosts());
        }
        if (!instanceReport.hasField(countInitialAttemptsMetricString)){
            this.instanceReport.putIntegerValue(countInitialAttemptsMetricString, 0);
        }
        if (!instanceReport.hasField(countContingencyAttemptsMetricString)){
            this.instanceReport.putIntegerValue(countContingencyAttemptsMetricString, 0);
        }
        if (!instanceReport.hasField(maxReachedIndexBeforeTimeoutString)){
            this.instanceReport.putIntegerValue(maxReachedIndexBeforeTimeoutString, 0);
        }
    }

    /**
     * Clears local fields, to allow the garbage collector to clear the memory that is no longer in use.
     * All fields should be cleared by this method. Any data that might be relevant later should be passed as part
     * of the {@link Solution} that is output by {@link #solve(MAPF_Instance, RunParameters)}, or written to an {@link Environment.Metrics.InstanceReport}.
     */
    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.constraints = null;
        this.agents = null;
        this.instanceReport = null;
        this.heuristic = null;
    }

    /*  = interfaces =  */

    @Override
    public boolean sharedSources() {
        return this.sharedSources;
    }

    @Override
    public boolean sharedGoals() {
        return this.sharedGoals;
    }
}
