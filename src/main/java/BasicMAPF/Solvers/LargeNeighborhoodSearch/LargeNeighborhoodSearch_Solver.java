package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;

import java.util.*;

/**
 * Implements Large Neighborhood Search for MAPF.
 * Implementation and default parameters are based on:
 * Li, Jiaoyang, et al. "Anytime multi-agent path finding via large neighborhood search." International Joint Conference on Artificial Intelligence (IJCAI). 2021.
 */
public class LargeNeighborhoodSearch_Solver extends A_Solver {

    /*  = Fields related to the MAPF instance =  */
    /**
     * An array of {@link Agent}s to plan for, ordered by priority (descending).
     */
    private List<Agent> agents;

    /*  = Fields related to the class instance =  */

    /**
     * A {@link I_Solver solver} to be used for finding the initial plan.
     */
    private final I_Solver initialSolver;
    /**
     * A {@link I_Solver solver} to be used for solving sub-problems for a subset of agents while avoiding other agents.
     */
    private final I_Solver iterationsSolver;
    private final List<I_DestroyHeuristic> destroyHeuristics;
    private final I_SolutionCostFunction solutionCostFunction;
    private final double reactionFactor;
    private final int neighborhoodSize;

    /**
     * if agents share goals, they will not conflict at their goal.
     */
    private final boolean sharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not conflict
     */
    private final boolean sharedSources;
    private final TransientMAPFSettings transientMAPFSettings;

    /*  = Fields related to the run =  */

    private SingleAgentGAndH subSolverHeuristic;
    private I_ConstraintSet constraints;
    private Random random;
    private int completedDestroyAndRepairIterations;
    private double[] destroyHeuristicsWeights;
    private double sumWeights;

    /*  = Constructors =  */

    /**
     * Constructor.
     *
     * @param solutionCostFunction how to calculate the cost of a solution
     * @param destroyHeuristics    list of {@link I_DestroyHeuristic}. If size is 1, use just that heuristic,
     *                             otherwise use Adaptive LNS with all the heuristics.
     * @param sharedGoals          if agents share goals, they will not conflict at their goal.
     * @param sharedSources        if agents share goals, they will not conflict at their source until they move.
     * @param reactionFactor       how quickly ALNS adapts to which heuristic is more successful. default = 0.01 .
     * @param neighborhoodSize     What size neighborhoods to select.
     * @param transientMAPFSettings indicates whether to solve transient-MAPF instead of regular MAPF.
     * @param initialSolver         a solver to use for the initial solution.
     *                              If null, use {@link PrioritisedPlanning_Solver} with random restarts until an initial solution is found.
     * @param iterationsSolver      a solver to use for solving sub-problems for a subset of agents while avoiding other agents.
     *                              If null, use {@link PrioritisedPlanning_Solver} with no restarts.
     *
     */
    LargeNeighborhoodSearch_Solver(I_SolutionCostFunction solutionCostFunction, List<I_DestroyHeuristic> destroyHeuristics,
                                   Boolean sharedGoals, Boolean sharedSources, Double reactionFactor, Integer neighborhoodSize,
                                   I_Solver initialSolver, I_Solver iterationsSolver, TransientMAPFSettings transientMAPFSettings) {

        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        this.solutionCostFunction = Objects.requireNonNullElseGet(solutionCostFunction, SumOfCosts::new);

        this.initialSolver = Objects.requireNonNullElseGet(initialSolver,
                // PP with random restarts until an initial solution is found
                () -> new PrioritisedPlanning_Solver(null, null, this.solutionCostFunction,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.randomRestarts, null),
                sharedGoals, sharedSources, this.transientMAPFSettings));
        this.iterationsSolver = Objects.requireNonNullElseGet(iterationsSolver,
                // PP with just one attempt
                () -> new PrioritisedPlanning_Solver(null, null, this.solutionCostFunction,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.none, null),
                sharedGoals, sharedSources, this.transientMAPFSettings));

        this.destroyHeuristics = destroyHeuristics == null || destroyHeuristics.isEmpty() ?
                List.of(new RandomDestroyHeuristic(), new MapBasedDestroyHeuristic())
                : new ArrayList<>(destroyHeuristics);

        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.reactionFactor = Objects.requireNonNullElse(reactionFactor, 0.01);
        this.neighborhoodSize = Objects.requireNonNullElse(neighborhoodSize, 5);
        if (Config.WARNING >= 1 && this.sharedGoals && this.transientMAPFSettings.isTransientMAPF()){
            System.err.println("Warning: " + this.name + " has shared goals and is set to transient MAPF. Shared goals is unnecessary if transient.");
        }

        super.name = (this.destroyHeuristics.size() > 1 ? "A" : "") + "LNS" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "") + (this.destroyHeuristics.size() == 1 ? "-" + destroyHeuristics.get(0).getClass().getSimpleName() : "");
    }

    /*  = initialization =  */

    /**
     * Initialises the object in preparation for solving an {@link MAPF_Instance}.
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
        this.completedDestroyAndRepairIterations = 0;

        this.destroyHeuristicsWeights = new double[destroyHeuristics.size()];
        Arrays.fill(this.destroyHeuristicsWeights, 1.0);
        this.sumWeights = this.destroyHeuristicsWeights.length;

        // single agent heuristic for the sub solver
        this.subSolverHeuristic = Objects.requireNonNullElseGet(parameters.singleAgentGAndH, () -> new DistanceTableSingleAgentHeuristic(this.agents, instance.map));
        if (this.subSolverHeuristic instanceof CachingDistanceTableHeuristic){
            ((CachingDistanceTableHeuristic)this.subSolverHeuristic).setCurrentMap(instance.map);
        }
    }

    /*  = algorithm =  */

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        return solveLNS(instance, this.constraints);
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
    protected Solution solveLNS(MAPF_Instance instance, I_ConstraintSet initialConstraints) {
        Solution bestSolution = getInitialSolution(instance, initialConstraints);
        while (bestSolution != null && !checkTimeout() && !checkSoftTimeout()){ // anytime behaviour
            // select neighborhood (destroy heuristic)
            int destroyHeuristicIndex = selectDestroyHeuristicIndex();
            I_DestroyHeuristic destroyHeuristic = this.destroyHeuristics.get(destroyHeuristicIndex);
            Set<Agent> agentsSubset = new HashSet<>(destroyHeuristic.selectNeighborhood(bestSolution, Math.min(neighborhoodSize, agents.size()-1), random, instance.map));

            // get solution without selected agents
            Solution destroyedSolution = new Solution();
            Solution oldSubsetSolution = new Solution();
            for (SingleAgentPlan p:
                    bestSolution) {
                if (! agentsSubset.contains(p.agent)){
                    destroyedSolution.putPlan(p);
                }
                else {
                    oldSubsetSolution.putPlan(p);
                }
            }

            // if selected no agents, consider it a fail
            Solution newSubsetSolution = null;
            if (!agentsSubset.isEmpty()){
                // plan while avoiding the unselected agents
                newSubsetSolution = solveSubproblem(destroyedSolution, agentsSubset, instance, initialConstraints, false);
            }

            updateDestroyHeuristicWeight(newSubsetSolution, oldSubsetSolution, destroyHeuristicIndex);

            // if the new subset solution is better, join it with the rest of the solution and save that as current best
            if (newSubsetSolution != null &&
                    solutionCostFunction.solutionCost(newSubsetSolution) < solutionCostFunction.solutionCost(oldSubsetSolution)){
                for (SingleAgentPlan p :
                        newSubsetSolution) {
                    destroyedSolution.putPlan(p);
                }
                bestSolution = destroyedSolution;
            }
            completedDestroyAndRepairIterations++;
        }
        return finalizeSolution(bestSolution);
    }

    private Solution finalizeSolution(Solution bestSolution) {
        return (transientMAPFSettings.isTransientMAPF() && bestSolution != null) ? new TransientMAPFSolution(bestSolution) : bestSolution;
    }

    private void updateDestroyHeuristicWeight(Solution newSubsetSolution, Solution oldSubsetSolution, int destroyHeuristicIndex) {
        float oldSubsetSolutionCost = solutionCostFunction.solutionCost(oldSubsetSolution);
        float newSubsetSolutionCost = newSubsetSolution != null ?
                solutionCostFunction.solutionCost(newSubsetSolution) : oldSubsetSolutionCost;
        double oldWeight = destroyHeuristicsWeights[destroyHeuristicIndex];
        destroyHeuristicsWeights[destroyHeuristicIndex] =
                reactionFactor * Math.max(oldSubsetSolutionCost-newSubsetSolutionCost, 0) +
                (1-reactionFactor) * (destroyHeuristicsWeights[destroyHeuristicIndex]);
        sumWeights += destroyHeuristicsWeights[destroyHeuristicIndex] - oldWeight;
    }

    private Solution getInitialSolution(MAPF_Instance instance, I_ConstraintSet initialConstraints) {
        Solution solution = solveSubproblem(new Solution(), new HashSet<>(this.agents), instance, initialConstraints, true);
        if (solution != null)
            super.runtimeToFirstSolution = (int) (Timeout.getCurrentTimeMS_NSAccuracy() - super.startTime);
        return solution;
    }

    /**
     * Follows Adaptive LNS logic if more than one heuristic is available.
     * @return the index of the chosen destroy heuristic
     */
    private int selectDestroyHeuristicIndex() {
        if (destroyHeuristics.size() == 1){
            return 0;
        }
        else {
            // roulette wheel selection
            double randDouble = random.nextDouble() * sumWeights;
            double currentSumWeights = 0;
            int i = 0;
            for (; i < destroyHeuristicsWeights.length; i++) {
                currentSumWeights += destroyHeuristicsWeights[i];
                if (randDouble <= currentSumWeights){
                    break;
                }
            }
            if (i == destroyHeuristicsWeights.length){ // handle potential floating point problem
                i = destroyHeuristicsWeights.length - 1;
            }
            return i;
        }
    }

    protected Solution solveSubproblem(Solution destroyedSolution, Set<Agent> agentsSubset,
                                       MAPF_Instance fullInstance, I_ConstraintSet outsideConstraints, boolean initial) {
        //create a sub-problem
        MAPF_Instance subproblem = fullInstance.getSubproblemFor(agentsSubset);
        InstanceReport subproblemReport = initSubproblemReport(fullInstance);
        RunParameters subproblemParameters = getSubproblemParameters(subproblem, subproblemReport, outsideConstraints,
                destroyedSolution, agentsSubset);

        //solve sub-problem
        Solution newSubsetSolution = initial ? this.initialSolver.solve(subproblem, subproblemParameters)
                : this.iterationsSolver.solve(subproblem, subproblemParameters);
        digestSubproblemReport(subproblemReport);
        return newSubsetSolution;
    }

    private static InstanceReport initSubproblemReport(MAPF_Instance instance) {
        InstanceReport subproblemReport = new InstanceReport();
        subproblemReport.putStringValue("Parent Instance", instance.name);
        subproblemReport.putStringValue("Parent Solver", PrioritisedPlanning_Solver.class.getSimpleName());
        subproblemReport.keepSolutionString = false;
        return subproblemReport;
    }

    protected RunParameters getSubproblemParameters(MAPF_Instance subproblem, InstanceReport subproblemReport,
                                                    I_ConstraintSet outsideConstraints, Solution destroyedSolution, Set<Agent> agentsSubset) {
        // TODO shorter timeout?
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (Timeout.getCurrentTimeMS_NSAccuracy() - super.startTime), 0);
        ConstraintSet subproblemConstraints = new ConstraintSet(outsideConstraints);
        subproblemConstraints.addAll(outsideConstraints.allConstraintsForSolution(destroyedSolution));
        List<Agent> randomizedAgentsOrder = new ArrayList<>(agentsSubset);
        Collections.shuffle(randomizedAgentsOrder, random);
        return new RunParametersBuilder().setTimeout(timeLeftToTimeout).setConstraints(subproblemConstraints).setInstanceReport(subproblemReport).setAStarGAndH(this.subSolverHeuristic).setPriorityOrder(randomizedAgentsOrder.toArray(new Agent[0])).createRP();
    }

    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue("Neighborhood Size", neighborhoodSize);
        instanceReport.putStringValue("Destroy Heuristics", destroyHeuristics.toString());
        for (int i = 0; i < destroyHeuristics.size(); i++) {
            instanceReport.putFloatValue(destroyHeuristics.get(i).getClass().getSimpleName(), (float)destroyHeuristicsWeights[i]);
        }
        instanceReport.putIntegerValue("Num Iterations", completedDestroyAndRepairIterations);
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
        this.random = null;
        this.destroyHeuristicsWeights = null;
        for (I_DestroyHeuristic ds :
                this.destroyHeuristics) {
            ds.clear();
        }
        this.instanceReport = null;
        this.subSolverHeuristic = null;
    }

}
