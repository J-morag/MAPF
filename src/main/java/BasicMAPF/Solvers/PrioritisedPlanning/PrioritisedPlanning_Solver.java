package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAndBlacklistAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.UnmodifiableConstraintSet;
import Environment.Config;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.PartialSolutionsStrategy;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import LifelongMAPF.FailPolicies.FailPolicy;
import LifelongMAPF.I_LifelongCompatibleSolver;
import org.apache.commons.lang.mutable.MutableInt;

import java.util.*;

import static LifelongMAPF.LifelongUtils.horizonAsAbsoluteTime;
import static com.google.common.math.IntMath.factorial;

/**
 * An implementation of the Prioritised Planning algorithm for Multi Agent Path Finding.
 * It solves {@link MAPF_Instance MAPF problems} very quickly, but does not guarantee optimality, and will very likely
 * return a sub-optimal {@link Solution}.
 */
public class PrioritisedPlanning_Solver extends A_Solver implements I_LifelongCompatibleSolver {
    private static final long MINIMUM_TIME_PER_AGENT_MS = 10;

    /*  = Fields =  */

    /* = Constants = */
    public final static String countInitialAttemptsMetricString = "count initial attempts";
    public final static String countContingencyAttemptsMetricString = "count contingency attempts";
    public final static String maxReachedIndexOneBasedBeforeTimeoutString = "max reached index";
    public final static String countSingleAgentFPsTriggeredString = "single agent FPs triggered";
    private static final int DEBUG = 1;

    /*  = Constants =  */
    public static final String COMPLETED_INITIAL_ATTEMPTS_STR = "completed initial attempts";
    public static final String COMPLETED_CONTINGENCY_ATTEMPTS_STR = "completed contingency attempts";

    /*  = Fields related to the MAPF instance =  */
    /**
     * An array of {@link Agent}s to plan for, ordered by priority (descending).
     */
    private List<Agent> agents;
    /**
     * Start time of the problem. Not real-time.
     */
    private int problemStartTime;

    /*  = Fields related to the run =  */

    private I_ConstraintSet constraints;
    private Random orderingsRNG;
    private Random singleAgentSolverRNG;
    /**
     * optional heuristic function to use in the low level solver.
     */
    private SingleAgentGAndH singleAgentGAndH;
    private Set<Agent> failedAgents;
    private RemovableConflictAvoidanceTableWithContestedGoals initialConflictAvoidanceTable;
    int maxReachedIndexOneBased;
    int singleAgentFPsTriggered;

    /*  = Fields related to the class instance =  */

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
     * if agents share goals, they will not conflict at their goal.
     */
    public boolean ignoresStayAtSharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not conflict
     */
    public boolean sharedSources;
    private final TransientMAPFSettings transientMAPFSettings;
    public boolean reportIndvAttempts = false;
    /**
     * How to approach partial solutions from the multi-agent perspective
     */
    private PartialSolutionsStrategy partialSolutionsStrategy;
    /**
     * How far forward in time to consider conflicts. Further than this time conflicts will be ignored.
     */
    public final Integer RHCR_Horizon;
    public final FailPolicy failPolicy;
    public boolean dynamicAStarTimeAllocation = false;
    public float aStarTimeAllocationFactor = 1.0f;


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
     * @param ignoresStayAtSharedGoals                if agents share goals, they will not conflict at their goal.
     * @param transientMAPFSettings     if true will use be Transient MAPF
     * @param failPolicy                 how to handle single agent failures while solving
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver, Comparator<Agent> agentComparator,
                                      I_SolutionCostFunction solutionCostFunction, RestartsStrategy restartsStrategy,
                                      Boolean ignoresStayAtSharedGoals, Boolean sharedSources, TransientMAPFSettings transientMAPFSettings,
                                      Integer RHCR_Horizon, FailPolicy failPolicy) {
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.agentComparator = agentComparator;
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        this.restartsStrategy = Objects.requireNonNullElseGet(restartsStrategy, RestartsStrategy::new);
        this.ignoresStayAtSharedGoals = Objects.requireNonNullElse(ignoresStayAtSharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        this.RHCR_Horizon = RHCR_Horizon;
        this.failPolicy = failPolicy;
        if (this.RHCR_Horizon != null && this.RHCR_Horizon < 1){
            throw new IllegalArgumentException("RHCR horizon must be >= 1");
        }

        super.name = "PrP" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "") + " (" + (this.restartsStrategy.randomizeAStar ? "rand. ": "") + this.lowLevelSolver.name() + ")" +
                (this.restartsStrategy.isNoRestarts() ? "" : " + " + this.restartsStrategy);
        if (Config.WARNING >= 1 && this.ignoresStayAtSharedGoals && this.transientMAPFSettings.isTransientMAPF()){
            System.err.println("Warning: " + this.name + " ignores shared goals and is set to transient MAPF. Ignoring shared goals is unnecessary if transient.");
        }
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

        this.constraints = parameters.constraints == null ? new ConstraintSet(): new ConstraintSet(parameters.constraints);
        if (this.RHCR_Horizon != null){
            this.constraints.setLastTimeToConsiderConstraints(horizonAsAbsoluteTime(this.problemStartTime, this.RHCR_Horizon));
        }
        this.constraints.setSharedGoals(this.ignoresStayAtSharedGoals);
        this.constraints.setSharedSources(this.sharedSources);

        this.orderingsRNG = new Random(42);
        this.singleAgentSolverRNG = Objects.requireNonNullElseGet(parameters.randomNumberGenerator, () -> new Random(42));
        // if we were given a comparator for agents, sort the agents according to this priority order.
        if (this.agentComparator != null){
            this.agents.sort(this.agentComparator);
        }
        // if we were given a specific priority order to use for this instance, overwrite the order given by the comparator.
        if(parameters.priorityOrder != null && parameters.priorityOrder.length > 0) {
            reorderAgentsByPriority(parameters.priorityOrder);
        }
        this.failedAgents = new HashSet<>();
        this.initialConflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();

        this.maxReachedIndexOneBased = -1;
        this.singleAgentFPsTriggered = 0;

        // heuristic
        if (parameters.singleAgentGAndH != null){
            this.singleAgentGAndH = parameters.singleAgentGAndH;
        }
        else {
            if (this.lowLevelSolver instanceof SingleAgentAStar_Solver){
                this.singleAgentGAndH = new DistanceTableSingleAgentHeuristic(new ArrayList<>(instance.agents), instance.map);
            }
            if (this.singleAgentGAndH instanceof CachingDistanceTableHeuristic){
                ((CachingDistanceTableHeuristic)this.singleAgentGAndH).setCurrentMap(instance.map);
            }
            if (this.singleAgentGAndH != null && this.solutionCostFunction instanceof SumServiceTimes){
                this.singleAgentGAndH = new ServiceTimeGAndH(this.singleAgentGAndH);
            }
        }

        if(parameters instanceof RunParameters_PP parametersPP){
            this.partialSolutionsStrategy = parametersPP.partialSolutionsStrategy;

            if (parametersPP.failedAgents != null){
                this.failedAgents = parametersPP.failedAgents;
            }

            if (parametersPP.conflictAvoidanceTable != null){
                this.initialConflictAvoidanceTable = parametersPP.conflictAvoidanceTable;
            }
        }

        this.partialSolutionsStrategy = Objects.requireNonNullElseGet(this.partialSolutionsStrategy, DisallowedPartialSolutionsStrategy::new);
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
    protected Solution solvePrioritisedPlanning(MAPF_Instance instance, I_ConstraintSet initialConstraints) {
        Solution bestSolution = null;
        Solution bestPartialSolution = new Solution();
        int bestPartialSolutionSingleAgentSuccesses = 0;
        Set<Agent> bestPartialSolutionFailedAgents = new HashSet<>();
        int bestSolutionCost = Integer.MAX_VALUE;
        int numPossibleOrderings = factorial(this.agents.size());
        Set<List<Agent>> attemptedOrderings = new HashSet<>();
        attemptedOrderings.add(new ArrayList<>(agents));
        Set<List<Agent>> deterministicOrderings = new HashSet<>();
        deterministicOrderings.add(new ArrayList<>(agents));
        RestartsStrategy.reorderingStrategy reorderingStrategy = null;
        int attemptNumber = 1;
        // if using any sort of restarts, try more than once
        for (;;attemptNumber++) {
            Solution solution = new Solution();
            ConstraintSet currentConstraints = new ConstraintSet(initialConstraints);
            Agent agentWeFailedOn = null;
            // get a heuristic per agent according to the current order
            int[] h = agents.stream().mapToInt(agent -> singleAgentGAndH.getHToTargetFromLocation(agent.target, instance.map.getMapLocation(agent.source))).toArray();
            int naiveLowerBound = Arrays.stream(h).sum();
            int currentH = naiveLowerBound;
            int currentCost = 0;
            RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals(this.initialConflictAvoidanceTable);
            Set<Agent> failedAgents = new HashSet<>();
            Agent firstFailedAgent = null;
            MutableInt failPolicyIterations = new MutableInt(0);

            // solve for each agent while avoiding the plans of previous agents (standard PrP)
            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;
                maxReachedIndexOneBased = Math.max(maxReachedIndexOneBased, i + 1);

                // if the cost of the next agent increases current cost beyond the current best, no need to finish search/iteration. // TODO add heuristic here too (or only here?)
                float maxCost = bestSolution != null ?
                        bestSolutionCost - solutionCostFunction.solutionCost(solution)
                        : Float.POSITIVE_INFINITY;
                // solve the subproblem for one agent
                SingleAgentPlan planForAgent = solveSubproblem(agent, i, instance, currentConstraints, maxCost, solution, this.restartsStrategy.randomizeAStar);

                if (planForAgent == null || ! planForAgent.containsTarget()) {
                    if (planForAgent != null)
                        singleAgentFPsTriggered++;
                    failedAgents.add(agent);
                    if (firstFailedAgent == null) firstFailedAgent = agent;
                    if (! this.partialSolutionsStrategy.moveToNextPrPIteration(instance, attemptNumber, solution, agent, i, true, bestSolution != null)) {
                        int numFailedAgentsBeforeFailPolicy = failedAgents.size();

                        SingleAgentPlan initialFailPlan = planForAgent != null ? planForAgent : // So we got a partial plan from A*
                                failPolicy != null ? failPolicy.getFailPolicyPlan(problemStartTime, agent, instance.map.getMapLocation(agent.source), conflictAvoidanceTable):
                                null; // if not using a fail policy
                        if (initialFailPlan != null){
                            if (failPolicy == null){
                                throw new IllegalStateException("No fail policy, but got a partial plan from A*");
                            }
                            savePlanToSolutionConstraintsAndCongestion(solution, currentConstraints, initialFailPlan);
                            conflictAvoidanceTable.addPlan(initialFailPlan);
                            solution = this.failPolicy.getKSafeSolution(solution, problemStartTime, failedAgents, conflictAvoidanceTable, failPolicyIterations);
                        }

                        if (failedAgents.size() > numFailedAgentsBeforeFailPolicy){ // so the fail policy added more failed agents in failPolicy.getKSafeSolution
                            // reset constraints since the solution changed
                            currentConstraints = new ConstraintSet(initialConstraints);
                            if (this.singleAgentGAndH instanceof DistanceTableSingleAgentHeuristic distanceTable
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

                    int successfulAgents = i + 1 - failedAgents.size();

                    if (this.partialSolutionsStrategy.allowed() &&
                            (successfulAgents > bestPartialSolutionSingleAgentSuccesses ||
                                    (successfulAgents == bestPartialSolutionSingleAgentSuccesses &&
                                            (this.solutionCostFunction.solutionCost(solution) < this.solutionCostFunction.solutionCost(bestPartialSolution) ||
                                                    solution.size() > bestPartialSolution.size()) // if has same successful but more failed
                                    ))){
                        bestPartialSolution = solution;
                        bestPartialSolutionSingleAgentSuccesses = successfulAgents;
                        bestPartialSolutionFailedAgents = failedAgents;
                    } else if (this.partialSolutionsStrategy.allowed() &&
                            // may never even equal the number of successful agents in the current best partial solution
                            successfulAgents + (agents.size() - failedAgents.size()) < bestPartialSolutionSingleAgentSuccesses) {
                        break;
                    }

                    if (// TODO alreadyFoundFullSolution that takes into account that some plans may be fail plans?
                            this.partialSolutionsStrategy.moveToNextPrPIteration(instance, attemptNumber, solution, agent, i, true, bestSolution != null))
                    {
                        break;
                    }
                }
                else {
                    // if the lower bound on the cost of the solution is already higher than the best solution, we can stop
                    int newPlanCost = singleAgentGAndH.cost(planForAgent);
                    currentCost += newPlanCost;
                    currentH -= h[i];
                    if (bestSolution != null && currentCost + currentH >= bestSolutionCost) {
                        if (Config.INFO >= 2){
                            System.out.println("PrP: Stopping attempt early after " + (i+1) + " agents planned, due to lower bound. Solution cost: "
                                    + currentCost + " solution h: " + currentH + " (lower bound = " + (currentCost+currentH) + ")"
                                    + ", best cost: " + bestSolutionCost);
                        }
                        solution = null;
                        break;
                    }
                    savePlanToSolutionConstraintsAndCongestion(solution, currentConstraints, planForAgent);
                }
            }

            /* = random/deterministic restarts = */

            if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;
            bestSolution = chooseBestSolution(bestSolution, solution, failedAgents);
            bestSolutionCost = bestSolution != null ? this.solutionCostFunction.solutionCost(bestSolution) : bestSolutionCost;

            // report the completed attempt
            reportCompletedAttempt(attemptNumber, bestSolution);

            if (attemptedOrderings.size() == numPossibleOrderings && !restartsStrategy.randomizeAStar){
                break; // exhausted all possible orderings
            }
            else if (bestSolutionCost == naiveLowerBound){
                if (Config.INFO >= 2){
                    System.out.println("PrP: Stopping early due to finding optimal solution");
                }
                break; // found optimal solution
            }

            if (restartsStrategy.hasInitial() && attemptNumber < restartsStrategy.minAttempts){
                reorderingStrategy = restartsStrategy.initialRestarts;
            }
            else if (bestSolution == null && attemptNumber >= restartsStrategy.minAttempts && restartsStrategy.hasContingency()){
                reorderingStrategy = restartsStrategy.contingencyRestarts;
            }
            else if (!restartsStrategy.randomizeAStar){
                break;
            }

            if (reorderingStrategy == RestartsStrategy.reorderingStrategy.randomRestarts){
                do { // do not repeat orderings unless A* is randomized
                    Collections.shuffle(this.agents, this.orderingsRNG);
                }
                while (!restartsStrategy.randomizeAStar && (attemptedOrderings.contains(this.agents) || deterministicOrderings.contains(this.agents)) );

                attemptedOrderings.add(new ArrayList<>(this.agents));
            }
            else if (reorderingStrategy == RestartsStrategy.reorderingStrategy.deterministicRescheduling){
                if ( ! failedAgents.isEmpty()){
                    this.agents.remove(firstFailedAgent);
                    this.agents.add(0, firstFailedAgent);
                    if (!restartsStrategy.randomizeAStar && deterministicOrderings.contains(this.agents)){
                        break; // deterministic ordering can end up in a loop - terminates if repeats itself
                    }

                    ArrayList<Agent> newOrdering = new ArrayList<>(this.agents);
                    deterministicOrderings.add(newOrdering);
                    attemptedOrderings.add(newOrdering);
                }
                else { // deterministic restarts only restarts if no solution was found
                    break;
                }
            }
            // else: don't re-order. if the single agent solver is deterministic, nothing will change
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

    private void reportCompletedAttempt(int attemptNumber, Solution bestSolution) {
        if (attemptNumber <= restartsStrategy.minAttempts){
            if (reportIndvAttempts){
                this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " cost", bestSolution != null ? Math.round(this.solutionCostFunction.solutionCost(bestSolution)) : -1);
                this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " time", (int)((System.nanoTime()/1000000)-super.startTime));
            }
            this.instanceReport.putIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR, attemptNumber);
        }
        else if (attemptNumber > restartsStrategy.minAttempts && restartsStrategy.hasContingency()){
            this.instanceReport.putIntegerValue(COMPLETED_CONTINGENCY_ATTEMPTS_STR, attemptNumber - restartsStrategy.minAttempts);
        }
    }

    private Solution chooseBestSolution(Solution bestSolution, Solution solution, Set<Agent> failedAgents) {
        if (failedAgents.isEmpty() &&
                (bestSolution == null ||
                        (solution != null && solutionCostFunction.solutionCost(solution) < solutionCostFunction.solutionCost(bestSolution)))){
            bestSolution = solution;
            super.runtimeToFirstSolution = (int) Timeout.elapsedMSSince_NSAccuracy(super.startTime);
        }
        return bestSolution;
    }

    private Solution finalizeSolution(Solution bestSolution) {
        return (transientMAPFSettings.isTransientMAPF() && bestSolution != null) ? new TransientMAPFSolution(bestSolution) : bestSolution;
    }

    private void addPlanToCongestionMap(SingleAgentPlan planForAgent) {
        // if using congestion, add this plan to the congestion map
        if (this.singleAgentGAndH instanceof DistanceTableSingleAgentHeuristic distanceTable
                && distanceTable.congestionMap != null){
            distanceTable.congestionMap.registerPlan(planForAgent); // TODO horizon?
        }
    }

    private void addPlanToConstraints(ConstraintSet currentConstraints, SingleAgentPlan planForAgent) {
        currentConstraints.addAll(currentConstraints.allConstraintsForPlan(planForAgent));
    }

    protected SingleAgentPlan solveSubproblem(Agent currentAgent, int agentIndexInCurrentOrdering,
                                              MAPF_Instance fullInstance, ConstraintSet constraints,
                                              float maxCost, Solution solutionSoFar, boolean randomizeAStar) {
        //create a sub-problem
        MAPF_Instance subproblem = fullInstance.getSubproblemFor(currentAgent);
        InstanceReport subproblemReport = initSubproblemReport(fullInstance);
        RunParameters subproblemParameters = getSubproblemParameters(subproblem, subproblemReport, constraints, maxCost, solutionSoFar, randomizeAStar, agentIndexInCurrentOrdering);

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
        subproblemReport.keepSolutionString = false;
        return subproblemReport;
    }

    protected RunParameters getSubproblemParameters(MAPF_Instance subproblem, InstanceReport subproblemReport,
                                                    ConstraintSet constraints, float maxCost, Solution solutionSoFar,
                                                    boolean randomizeAStar, int agentIndexInCurrentOrdering) {
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        int numRemainingAgentsIncludingCurrent = this.agents.size() - agentIndexInCurrentOrdering;
        long allocatedTime = dynamicAStarTimeAllocation ? (long) ((timeLeftToTimeout / numRemainingAgentsIncludingCurrent) * aStarTimeAllocationFactor)
                : timeLeftToTimeout;
        allocatedTime = Math.min(Math.max(allocatedTime, MINIMUM_TIME_PER_AGENT_MS), timeLeftToTimeout);
        RunParameters_SAAStar params = new RunParameters_SAAStar(new RunParametersBuilder().setTimeout(allocatedTime).
                setConstraints(new UnmodifiableConstraintSet(constraints)).setInstanceReport(subproblemReport).setAStarGAndH(this.singleAgentGAndH)
                .setExistingSolution(new Solution(solutionSoFar))  // should probably work without copying, but just to be safe
                .createRP());
        if (randomizeAStar){
            params.randomNumberGenerator = this.singleAgentSolverRNG;
        }
        params.fBudget = maxCost;
        params.problemStartTime = this.problemStartTime;
        if (transientMAPFSettings.isTransientMAPF()) {
            if (transientMAPFSettings.useBlacklist()) {
                Set<I_Coordinate> targetsOfAgentsThatHaventPlannedYet = new HashSet<>();
                for (Agent agent : this.agents) {
                    if (!agent.equals(subproblem.agents.get(0)) && !solutionSoFar.contains(agent)) {
                        targetsOfAgentsThatHaventPlannedYet.add(agent.target);
                    }
                }
                params.goalCondition = new VisitedTargetAndBlacklistAStarGoalCondition(targetsOfAgentsThatHaventPlannedYet);
            } else {
                params.goalCondition = new VisitedTargetAStarGoalCondition();
            }
        }
        params.useFailPolicy = allocatedTime < timeLeftToTimeout;
        return params;
    }


    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue(maxReachedIndexOneBasedBeforeTimeoutString, maxReachedIndexOneBased);
        instanceReport.putIntegerValue(countSingleAgentFPsTriggeredString, singleAgentFPsTriggered);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
        }
        if (!instanceReport.hasField(countInitialAttemptsMetricString)){
            this.instanceReport.putIntegerValue(countInitialAttemptsMetricString, 0);
        }
        if (!instanceReport.hasField(countContingencyAttemptsMetricString)){
            this.instanceReport.putIntegerValue(countContingencyAttemptsMetricString, 0);
        }
        if (!instanceReport.hasField(maxReachedIndexOneBasedBeforeTimeoutString)){
            this.instanceReport.putIntegerValue(maxReachedIndexOneBasedBeforeTimeoutString, 0);
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
        this.singleAgentGAndH = null;
        this.orderingsRNG = null;
        this.singleAgentSolverRNG = null;
    }

    /*  = interfaces =  */

    @Override
    public boolean ignoresStayAtSharedSources() {
        return this.sharedSources;
    }

    @Override
    public boolean ignoresStayAtSharedGoals() {
        return this.ignoresStayAtSharedGoals;
    }

    @Override
    public boolean handlesSharedTargets() {
        return this.transientMAPFSettings.isTransientMAPF();
    }
}
