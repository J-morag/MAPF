package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.I_Solver;
import Environment.Experiment;
import Environment.Metrics.Metrics;
import Environment.RunManagers.A_RunManager;
import Environment.Visualization.I_VisualizeSolution;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        List<I_Solver> solvers = new ArrayList<>();


        // experiment
//        solvers.add(LifelongSolversFactory.PIBTt_h5());
//        solvers.add(LifelongSolversFactory.LaCAM_h5());
//        solvers.add(LifelongSolversFactory.LaCAMt_h5());
//        solvers.add(LifelongSolversFactory.LNS_h5());
//        solvers.add(LifelongSolversFactory.LNSt_h5());
//        solvers.add(LifelongSolversFactory.PrP_h5());
//        solvers.add(LifelongSolversFactory.PrPt_h5());

        solvers.add(LifelongSolversFactory.PIBTt_h10());
        solvers.add(LifelongSolversFactory.LaCAM_h10());
        solvers.add(LifelongSolversFactory.LaCAMt_h10());
        solvers.add(LifelongSolversFactory.LNS_h10());
        solvers.add(LifelongSolversFactory.LNSt_h10());
        solvers.add(LifelongSolversFactory.PrP_h10());
        solvers.add(LifelongSolversFactory.PrPt_h10());

//        solvers.add(LifelongSolversFactory.PIBTt_h50());
//        solvers.add(LifelongSolversFactory.LaCAM_h50());
//        solvers.add(LifelongSolversFactory.LaCAMt_h50());
//        solvers.add(LifelongSolversFactory.LNS_h50());
//        solvers.add(LifelongSolversFactory.LNSt_h50());
//        solvers.add(LifelongSolversFactory.PrP_h50());
//        solvers.add(LifelongSolversFactory.PrPt_h50());
//
//        solvers.add(LifelongSolversFactory.PIBTt_hinf());
//        solvers.add(LifelongSolversFactory.LaCAM_hinf());
//        solvers.add(LifelongSolversFactory.LaCAMt_hinf());
//        solvers.add(LifelongSolversFactory.LNS_hinf());
//        solvers.add(LifelongSolversFactory.LNSt_hinf());
//        solvers.add(LifelongSolversFactory.PrP_hinf());
//        solvers.add(LifelongSolversFactory.PrPt_hinf());
        
        return solvers;
    }

    protected void addAllMapsAndInstances(String instancesDir, int[] agentNums){
        /*  =   Set Properties   =  */
        InstanceProperties properties = getInstanceProperties(agentNums);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, getInstanceBuilder(),properties);

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
