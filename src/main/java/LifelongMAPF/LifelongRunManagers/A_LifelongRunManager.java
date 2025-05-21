package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceManagerFromFileSystem;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LNSBuilder;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import Environment.Experiment;
import Environment.Metrics.Metrics;
import Environment.RunManagers.A_RunManager;
import Environment.Visualization.I_VisualizeSolution;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.FailPolicies.TerminateFailPolicy;
import LifelongMAPF.LifelongSimulationSolver;
import TransientMAPF.TransientMAPFSettings;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public abstract class A_LifelongRunManager extends A_RunManager {

    public static final int DEFAULT_TIMEOUT_EACH = Integer.MAX_VALUE / 2;

    public A_LifelongRunManager(String resultsOutputDir) {
        this(resultsOutputDir, null);
    }

    public A_LifelongRunManager(String resultsOutputDir, I_VisualizeSolution visualizer) {
        super(resultsOutputDir, visualizer);
        metricsHeader = ArrayUtils.addAll(metricsHeader, lifelongMetricsForHeader());
    }

    public String[] lifelongMetricsForHeader() {
        return new String[]{
                "reachedTimestepInPlanning",
                "numPlanningIterations",
                "avgGroupSize",
                "avgFailedAgentsAfterPlanning",
                "avgFailedAgentsAfterPolicy",
                "totalAStarNodesGenerated",
                "totalAStarNodesExpanded",
                "totalAStarRuntimeMS",
                "totalAStarCalls",
                "numAttempts10thPercentile",
                "numAttempts50thPercentile",
                "numAttempts90thPercentile",
                "averageNumAttempts",
                "averageNumAttemptsOver100Agents",
                "averageNumAttemptsOver200Agents",
                "maxFailPolicyIterations",
                "avgFailPolicyIterations",
                "waypointTimes",
                "SOC",
                "makespan",
                "throughputAtT25",
                "throughputAtT50",
                "throughputAtT75",
                "throughputAtT100",
                "throughputAtT150",
                "throughputAtT200",
                "throughputAtT250",
                "throughputAtT300",
                "throughputAtT400",
                "throughputAtT500",
                "throughputAtT600",
                "throughputAtT700",
                "throughputAtT800",
                "throughputAtT900",
                "throughputAtT1000",
                "averageThroughput",
                "averageIndividualThroughput",
                "Adaptive Index reached cutoff"
        };
    }

    @Override
    protected @NotNull Metrics.InstanceReportToString getStdoutReportToString() {
        return getInstanceReportToHumanReadableStringSkipWaypointTimes();
    }

    @NotNull
    private static Metrics.InstanceReportToString getInstanceReportToHumanReadableStringSkipWaypointTimes() {
        return Metrics::instanceReportToHumanReadableStringSkipWaypointTimes;
    }

    @Override
    public void setSolvers() {
        super.solvers.addAll(getSolvers());
    }

    public void overrideSolvers(@NotNull List<I_Solver> solvers){
        this.solvers = solvers;
    }

    @NotNull
    public static Collection<? extends I_Solver> getSolvers() {
        int replanningPeriod = 5;
        int RHCR_horizon = 10;
        List<I_Solver> solvers = Arrays.asList(
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PIBT_Solver(null, RHCR_horizon, null, TransientMAPFSettings.defaultTransientMAPF), null, null, null, null, null),
                new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)), new LNSBuilder().setRHCR_Horizon(RHCR_horizon).createLNS(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)), new LNSBuilder().setRHCR_Horizon(RHCR_horizon).setSolutionCostFunction(new SumServiceTimes()).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLNS(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultRegularMAPF, RHCR_horizon, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF, RHCR_horizon, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null)
        );
        return solvers;
    }

    protected void addAllMapsAndInstances(String instancesDir, int[] agentNums){
        /*  =   Set Properties   =  */
        InstanceProperties properties = getInstanceProperties(agentNums);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManagerFromFileSystem(instancesDir, getInstanceBuilder(),properties);

        /*  =   Add new experiment   =  */
        Experiment warehouseInstances = new Experiment(getExperimentName(), instanceManager, null, DEFAULT_TIMEOUT_EACH);
        warehouseInstances.keepSolutionInReport = false;
        warehouseInstances.keepReportAfterCommit = false;
        warehouseInstances.sharedGoals = false;
        warehouseInstances.sharedSources = false;
        this.experiments.add(warehouseInstances);
    }

    @NotNull
    protected abstract String getExperimentName();

    protected abstract @NotNull I_InstanceBuilder getInstanceBuilder();
    @NotNull

    protected InstanceProperties getInstanceProperties(int[] agentNums) {
        return new InstanceProperties(null, -1, agentNums);
    }
}
