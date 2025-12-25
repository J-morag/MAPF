package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.ConflictsCount;
import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.RunParameters_SAAStar;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPPS_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.UnmodifiableConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import TransientMAPF.TransientMAPFUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Attempts to find a solution which minimizes conflicts between agents.
 * This is used as an iteration and initial solution generators for LNS2 using SIPPS as a single-agent path-finding solver.
 * The solution is generated using a single iteration of Prioritised Planning, and aims to minimize conflicts.
 */
public class solutionsGeneratorForLNS2 extends A_Solver {

    private final I_Solver lowLevelSolver;
    private MAPF_Instance instance;
    private final boolean sharedGoals;
    private final boolean sharedSources;
    private SingleAgentGAndH singleAgentGAndH;
    private final I_SolutionCostFunction costFunction;
    private Set<I_Coordinate> separatingVerticesSet;
    private RemovableConflictAvoidanceTableWithContestedGoals cat;
    private List<Agent> agents;

    public solutionsGeneratorForLNS2(@Nullable I_Solver lowLevelSolver, @Nullable TransientMAPFSettings transientMAPFSettings, @Nullable Boolean sharedGoals,
                                     @Nullable Boolean sharedSources, @Nullable I_SolutionCostFunction costFunction) {
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, () -> new SingleAgentAStarSIPPS_Solver(this.transientMAPFSettings));
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.costFunction = Objects.requireNonNullElseGet(costFunction, () -> new ConflictsCount(false, false));
        super.name = "initialSolution";
    }

    public solutionsGeneratorForLNS2() {
        this(null, null, null, null, null);
    }

    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        this.instance = instance;

        if (runParameters.conflictAvoidanceTable != null) {
            if (runParameters.conflictAvoidanceTable instanceof RemovableConflictAvoidanceTableWithContestedGoals) {
                this.cat = (RemovableConflictAvoidanceTableWithContestedGoals) runParameters.conflictAvoidanceTable;
            }
            else {
                throw new IllegalArgumentException("Conflict avoidance table must be from type RemovableConflictAvoidanceTableWithContestedGoals, got: " + runParameters.conflictAvoidanceTable.getClass().getSimpleName());
            }
        }
        else {
            this.cat = new RemovableConflictAvoidanceTableWithContestedGoals();
        }

        // heuristic
        if (runParameters.singleAgentGAndH != null){
            this.singleAgentGAndH = runParameters.singleAgentGAndH;
        }
        else {
            if (this.lowLevelSolver instanceof SingleAgentAStar_Solver){
                this.singleAgentGAndH = new DistanceTableSingleAgentHeuristic(new ArrayList<>(instance.agents), instance.map);
            }
            if (this.singleAgentGAndH instanceof CachingDistanceTableHeuristic){
                ((CachingDistanceTableHeuristic)this.singleAgentGAndH).setCurrentMap(instance.map);
            }
            if (this.singleAgentGAndH != null && this.costFunction instanceof SumServiceTimes){
                this.singleAgentGAndH = new ServiceTimeGAndH(this.singleAgentGAndH);
            }
        }

        if (this.transientMAPFSettings.avoidSeparatingVertices()) {
            this.separatingVerticesSet = TransientMAPFUtils.createSeparatingVerticesSetOfCoordinates(instance, runParameters);
        }
        if (this.transientMAPFSettings.isTransientMAPF() ^ this.singleAgentGAndH.isTransient()){
            throw new IllegalArgumentException(this.getClass().getSimpleName() + ": GAndH and transient MAPF settings are mismatched: " + this.singleAgentGAndH.getClass().getSimpleName() + " " + this.transientMAPFSettings);
        }

        this.agents = new ArrayList<>(instance.agents);
        // if we were given a specific priority order to use for this instance, overwrite the order given by the comparator.
        if(runParameters.priorityOrder != null && runParameters.priorityOrder.length > 0) {
            PrioritisedPlanning_Solver.reorderAgentsByPriority(runParameters.priorityOrder, this.agents);
        }
    }

    /**
     * find a path for each agent in the given instance.
     * @param instance to solve.
     * @param parameters parameters
     * @return a Solution that minimizes the number of conflicts.
     */
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        ArrayList<SingleAgentPlan> plannedPlans = new ArrayList<>();
        Solution solution = transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution() : new Solution();
        I_ConstraintSet initialConstraints = Objects.requireNonNullElseGet(parameters.constraints, ConstraintSet::new);
        for (Agent agent : this.agents) {
            if (checkTimeout()) {
                for (SingleAgentPlan planToRemoveFromCat : plannedPlans) {
                    this.cat.removePlan(planToRemoveFromCat);
                }
                return null;
            }
            solution = solveSubProblem(agent, solution, initialConstraints);
            if (solution == null) {
                for (SingleAgentPlan planToRemoveFromCat : plannedPlans) {
                    this.cat.removePlan(planToRemoveFromCat);
                }
                return null;
            }
            SingleAgentPlan newPlan = solution.getPlanFor(agent);
            if (newPlan.size() != 0){
                this.cat.addPlan(newPlan);
                plannedPlans.add(newPlan);
            }
        }
        return solution;
    }


    /**
     * Solves a single agent sub-problem.
     * @param agent to find a path to.
     * @param currentSolution current solution.
     * @param constraints current constraints.
     * @return a solution to a single agent sub-problem. Typically, the same object as currentSolution, after being modified.
     */
    private Solution solveSubProblem(Agent agent, Solution currentSolution, I_ConstraintSet constraints) {
        InstanceReport instanceReport = new InstanceReport();
        instanceReport.keepSolutionString = false;
        RunParameters subProblemParameters = getSubProblemParameters(currentSolution, constraints, instanceReport, agent);
        Solution subproblemSolution = this.lowLevelSolver.solve(this.instance.getSubproblemFor(agent), subProblemParameters);
        digestSubproblemReport(instanceReport);
        return subproblemSolution;
    }

    private RunParameters getSubProblemParameters(Solution currentSolution, I_ConstraintSet constraints, InstanceReport instanceReport, Agent agent) {
        // if there was already a timeout while solving a node, we will get a negative time left, which would be
        // interpreted as "use default timeout". In such a case we should instead give the solver 0 time to solve.
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        RunParameters subproblemParametes = new RunParametersBuilder().setTimeout(timeLeftToTimeout).setConstraints(new UnmodifiableConstraintSet(constraints)).
                setInstanceReport(instanceReport).setExistingSolution(currentSolution).setAStarGAndH(this.singleAgentGAndH).createRP();
        if(this.lowLevelSolver instanceof SingleAgentAStar_Solver){ // upgrades to a better heuristic
            RunParameters_SAAStar AStarSubProblemParameters = new RunParameters_SAAStar(subproblemParametes);
            // TMAPF goal condition
            if (transientMAPFSettings.isTransientMAPF()) {
                AStarSubProblemParameters.goalCondition = TransientMAPFUtils.createLowLevelGoalConditionForTransientMAPF(transientMAPFSettings, separatingVerticesSet, instance.agents, agent, null);
            }
            cat.sharedGoals = this.sharedGoals;
            cat.sharedSources = this.sharedSources;
            AStarSubProblemParameters.conflictAvoidanceTable = this.cat;
            subproblemParametes = AStarSubProblemParameters;
        }
        return subproblemParametes;
    }

    /**
     * Clears local fields, to allow the garbage collector to clear the memory that is no longer in use.
     * All fields should be cleared by this method. Any data that might be relevant later should be passed as part
     * of the {@link Solution} that is output by {@link #solve(MAPF_Instance, RunParameters)}, or written to an {@link Environment.Metrics.InstanceReport}.
     */
    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.instance = null;
        this.singleAgentGAndH = null;
        this.separatingVerticesSet = null;
        this.cat = null;
        this.agents = null;
    }
}