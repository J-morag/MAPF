package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.*;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAndBlacklistAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ImmutableConstraintSet;
import TransientMAPF.TransientMAPFBehaviour;
import TransientMAPF.TransientMAPFSolution;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.*;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;

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

    private ConstraintSet constraints;

    private Random random;

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
     * optional heuristic function to use in the low level solver.
     */
    private SingleAgentGAndH singleAgentGAndH;

    /**
     * if agents share goals, they will not conflict at their goal.
     */
    public boolean sharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not conflict
     */
    public boolean sharedSources;
    private final TransientMAPFBehaviour transientMAPFBehaviour;
    public boolean reportIndvAttempts = false;


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
                                      Boolean sharedGoals, Boolean sharedSources,  TransientMAPFBehaviour transientMAPFBehaviour) {
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.agentComparator = agentComparator;
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);
        this.restartsStrategy = Objects.requireNonNullElseGet(restartsStrategy, RestartsStrategy::new);
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.transientMAPFBehaviour = Objects.requireNonNullElse(transientMAPFBehaviour, TransientMAPFBehaviour.regularMAPF);

        super.name = "PrP" + (this.transientMAPFBehaviour.isTransientMAPF() ? "t" : "") + " (" + this.lowLevelSolver.name() + ")" +
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
        this.random = new Random(42);
        // if we were given a comparator for agents, sort the agents according to this priority order.
        if (this.agentComparator != null){
            this.agents.sort(this.agentComparator);
        }

        // heuristic
        this.singleAgentGAndH = Objects.requireNonNullElseGet(
                parameters.singleAgentGAndH,
                () -> this.transientMAPFBehaviour == TransientMAPFBehaviour.transientMAPFsstWithBlacklist ?
                        new ServiceTimeGAndH(new DistanceTableSingleAgentHeuristic(this.agents, instance.map)) :
                        new DistanceTableSingleAgentHeuristic(this.agents, instance.map)
        );

        if (this.singleAgentGAndH instanceof CachingDistanceTableHeuristic){
            ((CachingDistanceTableHeuristic)this.singleAgentGAndH).setCurrentMap(instance.map);
        }
        // if we were given a specific priority order to use for this instance, overwrite the order given by the comparator.
        if(parameters.priorityOrder != null && parameters.priorityOrder.length > 0) {
            reorderAgentsByPriority(parameters.priorityOrder);
        }
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
        int numPossibleOrderings = factorial(this.agents.size());
        Set<List<Agent>> randomOrderings = new HashSet<>(); // TODO prefix tree memoization?
        randomOrderings.add(new ArrayList<>(agents));
        Set<List<Agent>> deterministicOrderings = new HashSet<>();
        deterministicOrderings.add(new ArrayList<>(agents));
        // if using random restarts, try more than once and randomize between them
        for (int attemptNumber = 0;
                ;
             attemptNumber++) {

            Solution solution = new Solution();
            ConstraintSet currentConstraints = new ConstraintSet(initialConstraints);
            Agent agentWeFailedOn = null;
            //solve for each agent while avoiding the plans of previous agents (standard PrP)
            for (Agent agent : agents) {
                if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;

                // if the cost of the next agent increases current cost beyond the current best, no need to finish search/iteration.
                float maxCost = bestSolution != null ?
                        solutionCostFunction.solutionCost(bestSolution) - solutionCostFunction.solutionCost(solution)
                        : Float.POSITIVE_INFINITY;
                //solve the subproblem for one agent
                SingleAgentPlan planForAgent = solveSubproblem(agent, instance, currentConstraints, maxCost, solution);

                // if an agent is unsolvable, then we can't return a valid solution for the instance (at least for this order of planning). return null.
                if (planForAgent == null) {
                    solution = null;
                    agentWeFailedOn = agent;
                    break;
                }
                //save the plan for this agent
                solution.putPlan(planForAgent);

                //add constraints to prevent the next agents from conflicting with the new plan
                currentConstraints.addAll(currentConstraints.allConstraintsForPlan(planForAgent));
            }


            /* = random/deterministic restarts = */

            if (checkTimeout() || (bestSolution != null && checkSoftTimeout())) break;
            if (bestSolution == null){
                bestSolution = solution;
            }
            else if (solution != null && solutionCostFunction.solutionCost(solution) < solutionCostFunction.solutionCost(bestSolution)){
                bestSolution = solution;
            }

            // report the completed attempt
            if (restartsStrategy.hasInitial() && attemptNumber <= restartsStrategy.numInitialRestarts){
                if (reportIndvAttempts){
                    this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " cost", bestSolution != null ? Math.round(this.solutionCostFunction.solutionCost(bestSolution)) : -1);
                    this.instanceReport.putIntegerValue("attempt #" + attemptNumber + " time", (int)((System.nanoTime()/1000000)-super.startTime));
                }
                this.instanceReport.putIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR, attemptNumber + 1);
            }
            else if (attemptNumber > restartsStrategy.numInitialRestarts && restartsStrategy.hasContingency()){
                this.instanceReport.putIntegerValue(COMPLETED_CONTINGENCY_ATTEMPTS_STR, attemptNumber - restartsStrategy.numInitialRestarts);
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
                if (agentWeFailedOn != null){
                    this.agents.remove(agentWeFailedOn);
                    this.agents.add(0, agentWeFailedOn);
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

        return finalizeSolution(bestSolution);
    }

    private Solution finalizeSolution(Solution bestSolution) {
        return (transientMAPFBehaviour.isTransientMAPF() && bestSolution != null) ? new TransientMAPFSolution(bestSolution) : bestSolution;
    }

    protected SingleAgentPlan solveSubproblem(Agent currentAgent, MAPF_Instance fullInstance, ConstraintSet constraints,
                                              float maxCost, Solution solutionSoFar) {
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
        subproblemReport.keepSolutionString = false;
        return subproblemReport;
    }

    protected RunParameters getSubproblemParameters(MAPF_Instance subproblem, InstanceReport subproblemReport, ConstraintSet constraints, float maxCost, Solution solutionSoFar) {
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        RunParameters_SAAStar params = new RunParameters_SAAStar(new RunParametersBuilder().setTimeout(timeLeftToTimeout).
                setConstraints(new ImmutableConstraintSet(constraints)).setInstanceReport(subproblemReport).setAStarGAndH(this.singleAgentGAndH).createRP());

        params.fBudget = maxCost;
        if (transientMAPFBehaviour == TransientMAPFBehaviour.transientMAPF){
            params.goalCondition = new VisitedTargetAStarGoalCondition();
        } else if (transientMAPFBehaviour == TransientMAPFBehaviour.transientMAPFWithBlacklist || transientMAPFBehaviour == TransientMAPFBehaviour.transientMAPFsstWithBlacklist) {
            Set<I_Coordinate> targetsOfAgentsThatHaventPlannedYet = new HashSet<>();
            for (Agent agent: this.agents) {
                if (!agent.equals(subproblem.agents.get(0)) && !solutionSoFar.contains(agent)){
                    targetsOfAgentsThatHaventPlannedYet.add(agent.target);
                }
            }
            params.goalCondition = new VisitedTargetAndBlacklistAStarGoalCondition(targetsOfAgentsThatHaventPlannedYet);
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
            I_SolutionCostFunction.addCommonCostsToReport(solution, instanceReport);
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
    }
}
