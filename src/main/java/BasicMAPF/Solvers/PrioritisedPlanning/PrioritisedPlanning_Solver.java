package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.UnmodifiableConstraintSet;
import Environment.Config;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.*;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import TransientMAPF.TransientMAPFUtils;

import java.util.*;

import static com.google.common.math.IntMath.factorial;

/**
 * An implementation of the Prioritised Planning algorithm for Multi Agent Path Finding.
 * It solves {@link MAPF_Instance MAPF problems} very quickly, but does not guarantee optimality, and will very likely
 * return a sub-optimal {@link Solution}.
 */
public class PrioritisedPlanning_Solver extends A_Solver {

    /*  = Fields =  */
    /*  = Constants =  */
    public static final String COMPLETED_INITIAL_ATTEMPTS_STR = "completed initial attempts";
    public static final String COMPLETED_CONTINGENCY_ATTEMPTS_STR = "completed contingency attempts";

    /*  = Fields related to the MAPF instance =  */
    /**
     * An array of {@link Agent}s to plan for, ordered by priority (descending).
     */
    private List<Agent> agents;

    /*  = Fields related to the run =  */

    private I_ConstraintSet constraints;
    private Random orderingsRNG;
    private Random singleAgentSolverRNG;
    /**
     * optional heuristic function to use in the low level solver.
     */
    private SingleAgentGAndH singleAgentGAndH;

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
    public boolean sharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not conflict
     */
    public boolean sharedSources;
    public boolean reportIndvAttempts = false;
    public boolean stableAStarAsSeed = false;

    private Set<I_Coordinate> separatingVerticesSet;

    /*  = Constructors =  */

    /**
     * Constructor.
     * @param lowLevelSolver A {@link I_Solver solver}, to be used for solving sub-problems where only one agent is to
     *                      be planned for, and the existing {@link SingleAgentPlan plans} for other
     *                      {@link Agent}s are to be avoided.
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver) {
        this(lowLevelSolver, null, null, null, null, null, null);
    }

    /**
     * Constructor.
     * @param agentComparator How to sort the agents. This sort determines their priority. High priority first.
     */
    public PrioritisedPlanning_Solver(Comparator<Agent> agentComparator) {
        this(null, agentComparator, null, null, null, null, null);
    }

    /**
     * Constructor.
     * @param lowLevelSolver A {@link I_Solver solver}, to be used for solving sub-problems for only one agent.
     * @param agentComparator How to sort the agents. This sort determines their priority. High priority first.
     * @param solutionCostFunction A cost function to evaluate solutions with. Only used when using random restarts.
     * @param restartsStrategy how to do restarts.
     * @param sharedGoals if agents share goals, they will not conflict at their goal.
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver, Comparator<Agent> agentComparator,
                                      I_SolutionCostFunction solutionCostFunction, RestartsStrategy restartsStrategy,
                                      Boolean sharedGoals, Boolean sharedSources,  TransientMAPFSettings transientMAPFSettings) {
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.agentComparator = agentComparator;
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        this.restartsStrategy = Objects.requireNonNullElseGet(restartsStrategy, RestartsStrategy::new);
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        if (Config.WARNING >= 1 && this.sharedGoals && this.transientMAPFSettings.isTransientMAPF()){
            System.err.println("Warning: " + this.name + " has shared goals and is set to transient MAPF. Shared goals is unnecessary if transient.");
        }
        if (this.solutionCostFunction instanceof SumServiceTimes ^ this.transientMAPFSettings.isTransientMAPF()){
            throw new IllegalArgumentException("PrP Solver: cost function and transient MAPF settings are mismatched: " + this.solutionCostFunction + " " + this.transientMAPFSettings);
        }

        super.name = "PrP" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "") + " (" + (this.restartsStrategy.randomizeAStar ? "rand. ": "") + this.lowLevelSolver.getName() + ")" +
                (this.restartsStrategy.isNoRestarts() ? "" : " + " + this.restartsStrategy);
    }

    /**
     * Default constructor.
     */
    public PrioritisedPlanning_Solver(){
        this(null, null, null, null, null, null, null);
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
        this.constraints = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.constraints.setSharedGoals(this.sharedGoals);
        this.constraints.setSharedSources(this.sharedSources);
        this.orderingsRNG = new Random(42);
        this.singleAgentSolverRNG = new Random(42);
        // if we were given a comparator for agents, sort the agents according to this priority order.
        if (this.agentComparator != null){
            this.agents.sort(this.agentComparator);
        }
        // if we were given a specific priority order to use for this instance, overwrite the order given by the comparator.
        if(parameters.priorityOrder != null && parameters.priorityOrder.length > 0) {
            reorderAgentsByPriority(parameters.priorityOrder, this.agents);
        }
        
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

        //transient MAPF
        if (this.transientMAPFSettings.isTransientMAPF() ^ this.singleAgentGAndH.isTransient()){
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": GAndH and transient MAPF settings are mismatched: " + this.singleAgentGAndH.getClass().getSimpleName() + " " + this.transientMAPFSettings);
        }
        if (this.transientMAPFSettings.avoidSeparatingVertices()) {
            this.separatingVerticesSet = TransientMAPFUtils.createSeparatingVerticesSetOfCoordinates(instance, parameters);
        }
    }

    public static void reorderAgentsByPriority(Agent[] requestedOrder, List<Agent> agents) {
        HashSet<Agent> tmpAgents = new HashSet<>(agents);
        agents.clear();

        for (Agent orderedAgent: //add by order
                requestedOrder) {
            if(tmpAgents.contains(orderedAgent)){
                agents.add(orderedAgent);
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
        int bestSolutionCost = Integer.MAX_VALUE;
        int numPossibleOrderings = factorial(this.agents.size());
        Set<List<Agent>> attemptedOrderings = new HashSet<>();
        attemptedOrderings.add(new ArrayList<>(agents));
        Set<List<Agent>> deterministicOrderings = new HashSet<>();
        deterministicOrderings.add(new ArrayList<>(agents));
        RestartsStrategy.reorderingStrategy reorderingStrategy = null;

        // if using any sort of restarts, try more than once
        for (int attemptNumber = 1 ; ; attemptNumber++) {
            Solution solution = new Solution();
            ConstraintSet currentConstraints = new ConstraintSet(initialConstraints);
            Agent agentWeFailedOn = null;
            // get a heuristic per agent according to the current order
            int[] h = agents.stream().mapToInt(agent -> singleAgentGAndH.getHToTargetFromLocation(agent.target, instance.map.getMapLocation(agent.source))).toArray();
            int naiveLowerBound = Arrays.stream(h).sum();
            int currentH = naiveLowerBound;
            int currentCost = 0;

            // solve for each agent while avoiding the plans of previous agents (standard PrP)
            for (int i = 0; i < agents.size(); i++) {
                Agent agent = agents.get(i);
                if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;

                // if the cost of the next agent increases current cost beyond the current best, no need to finish search/iteration. // TODO add heuristic here too (or only here?)
                float maxCost = bestSolution != null ?
                        bestSolutionCost - solutionCostFunction.solutionCost(solution)
                        : Float.POSITIVE_INFINITY;
                // solve the subproblem for one agent
                SingleAgentPlan planForAgent = solveSubproblem(agent, instance, currentConstraints, maxCost, solution, this.restartsStrategy.randomizeAStar);

                // if an agent is unsolvable, then we can't return a valid solution for the instance (at least for this order of planning). return null.
                if (planForAgent == null) {
                    solution = null;
                    agentWeFailedOn = agent;
                    break;
                }
                //save the plan for this agent
                solution.putPlan(planForAgent);

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


                //add constraints to prevent the next agents from conflicting with the new plan
                currentConstraints.addAll(currentConstraints.allConstraintsForPlan(planForAgent));
            }

            /* = random/deterministic restarts = */

            if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;
            bestSolution = chooseBestSolution(bestSolution, solution);
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
                if (agentWeFailedOn != null){
                    this.agents.remove(agentWeFailedOn);
                    this.agents.add(0, agentWeFailedOn);
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

        return finalizeSolution(bestSolution);
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

    private Solution chooseBestSolution(Solution bestSolution, Solution solution) {
        if (bestSolution == null && solution != null){
            bestSolution = solution;
            super.runtimeToFirstSolution = (int) Timeout.elapsedMSSince_NSAccuracy(super.startTime);
            if (Config.INFO >= 2){
                Integer completedInitialAttempts = instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR);
                System.out.println("PrP: Found first solution, with cost " + solutionCostFunction.solutionCost(solution) + " at attempt " + (completedInitialAttempts == null ? 1 : completedInitialAttempts));
            }
        }
        else if (solution != null && solutionCostFunction.solutionCost(solution) < solutionCostFunction.solutionCost(bestSolution)){
            if (Config.INFO >= 2){
                System.out.println("PrP: Found new best solution, with cost " + solutionCostFunction.solutionCost(solution) + " at attempt " + instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR));
            }
            bestSolution = solution;
        }
        return bestSolution;
    }

    private Solution finalizeSolution(Solution bestSolution) {
        return (transientMAPFSettings.isTransientMAPF() && bestSolution != null) ? new TransientMAPFSolution(bestSolution) : bestSolution;
    }

    protected SingleAgentPlan solveSubproblem(Agent currentAgent, MAPF_Instance fullInstance, ConstraintSet constraints,
                                              float maxCost, Solution solutionSoFar, boolean randomizeAStar) {
        //create a sub-problem
        MAPF_Instance subproblem = fullInstance.getSubproblemFor(currentAgent);
        InstanceReport subproblemReport = initSubproblemReport(fullInstance);
        RunParameters subproblemParameters = getSubproblemParameters(subproblem, subproblemReport, constraints, maxCost, solutionSoFar, randomizeAStar);

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
                                                    boolean randomizeAStar){
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        RunParameters_SAAStar params = new RunParameters_SAAStar(new RunParametersBuilder().setTimeout(timeLeftToTimeout).
                setConstraints(new UnmodifiableConstraintSet(constraints)).setInstanceReport(subproblemReport).setAStarGAndH(this.singleAgentGAndH).createRP());
        if (randomizeAStar){
            params.randomNumberGenerator = this.singleAgentSolverRNG;
        }
        else if (stableAStarAsSeed){
            params.randomNumberGenerator = new Random(42);
        }
        params.fBudget = maxCost;
        if (transientMAPFSettings.isTransientMAPF()) {
            params.goalCondition = TransientMAPFUtils.createLowLevelGoalConditionForTransientMAPF(transientMAPFSettings, separatingVerticesSet, agents, subproblem.agents.get(0), solutionSoFar);
        }
        return params;
    }


    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        if(solution != null){
            instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solutionCostFunction.solutionCost(solution));
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, solutionCostFunction.name());
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
}
