package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.I_Solver;
import Environment.Experiment;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import Environment.RunManagers.A_RunManager;
import Environment.Visualization.I_VisualizeSolution;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class A_LifelongRunManager extends A_RunManager {

    public A_LifelongRunManager(String resultsOutputDir) {
        this(resultsOutputDir, null);
    }

    public A_LifelongRunManager(String resultsOutputDir, I_VisualizeSolution visualizer) {
        super(resultsOutputDir, visualizer);
        metricsHeader = getMetricsHeader();
    }

    public String[] getMetricsHeader() {
        return new String[]{
                InstanceReport.StandardFields.experimentName,
                InstanceReport.StandardFields.mapName,
                InstanceReport.StandardFields.instanceName,
                InstanceReport.StandardFields.numAgents,
                InstanceReport.StandardFields.solver,
                InstanceReport.StandardFields.solved,
                InstanceReport.StandardFields.skipped,
                InstanceReport.StandardFields.valid,
                InstanceReport.StandardFields.elapsedTimeMS,
                InstanceReport.StandardFields.totalLowLevelTimeMS,
                InstanceReport.StandardFields.generatedNodes,
                InstanceReport.StandardFields.expandedNodes,
                InstanceReport.StandardFields.solutionCost,
                InstanceReport.StandardFields.solution,
                "reachedTimestepInPlanning",
                "numPlanningIterations",
                "avgGroupSize",
                "avgFailedAgents",
                "avgBlockedAgents",
                "waypointTimes",
                "SOC",
                "makespan",
                "timeTo50%Completion",
                "timeTo80%Completion",
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
                "averageThroughput",
                "averageIndividualThroughput",
                "Adaptive Index reached cutoff"
        };
    }

    @Override
    protected @NotNull S_Metrics.InstanceReportToString getStdoutReportToString() {
        return getInstanceReportToHumanReadableStringSkipWaypointTimes();
    }

    @NotNull
    private static S_Metrics.InstanceReportToString getInstanceReportToHumanReadableStringSkipWaypointTimes() {
        return S_Metrics::instanceReportToHumanReadableStringSkipWaypointTimes;
    }

    @Override
    public void setSolvers() {
        super.solvers.addAll(getSolvers());
    }

    @NotNull
    public static Collection<? extends I_Solver> getSolvers() {
        List<I_Solver> solvers = new ArrayList<>();
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFP());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPWidePartialOneActionFP());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFP());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartial());
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartialRHCR_w05_h03());
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartialRHCR_w05_h03_lookahead3());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartialOneActionFPRHCR_w05_h03_lookahead3());

//        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartial());
        solvers.add(LifelongSolversFactory.baselineRHCR_w05_h03());
//        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartialRHCR_w05_h03());
//        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartialRHCR_w05_h03_lookahead3());
//        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartialOneActionFPRHCR_w05_h03_lookahead3());
//
//        solvers.add(LifelongSolversFactory.allAgentsPrPCutoff25PercentPartialRHCR_w05_h03());
//        solvers.add(LifelongSolversFactory.allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3());
//        solvers.add(LifelongSolversFactory.allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead5());
//        solvers.add(LifelongSolversFactory.allAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3());

//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepUntilFoundThenWidePartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPOneDeepThenWidePartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPWidePartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial());

//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartial());
//        solvers.add(LifelongSolversFactory.allAgentsPrPCutoff25PercentPartial());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w05());


        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03());
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead5());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_lookahead5());

//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05());
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05_h03());
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05_h03_lookahead3());

        return solvers;
    }

    protected void addAllMapsAndInstances(String instancesDir, int[] agentNums){
        /*  =   Set Properties   =  */
        InstanceProperties properties = getInstanceProperties(agentNums);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, getInstanceBuilder(),properties);

        /*  =   Add new experiment   =  */
        Experiment warehouseInstances = new Experiment(getExperimentName(), instanceManager, null, 2 * 5 * 60 * 1000);
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
