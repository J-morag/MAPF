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

    public static final int DEFAULT_TIMEOUT_EACH = 4 * 5 * 60 * 1000;

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
                InstanceReport.StandardFields.numTraversableLocations,
                InstanceReport.StandardFields.avgInDegree,
                InstanceReport.StandardFields.avgOutDegree,
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
                InstanceReport.StandardFields.startDateTime,
                InstanceReport.StandardFields.processorInfo,
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

    public void overrideSolvers(@NotNull List<I_Solver> solvers){
        this.solvers = solvers;
    }

    @NotNull
    public static Collection<? extends I_Solver> getSolvers() {
        List<I_Solver> solvers = new ArrayList<>();
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartialAvoidFPRHCR_w10_h03Lookahead5()); // baseline and friends experiment // comparing macro FPs experiment
//        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartialAvoidFPRHCR_w10_h03()); // baseline and friends experiment
//        solvers.add(LifelongSolversFactory.allAgentsPrPDeepPartialAvoidRHCR_w10_h03()); // baseline and friends experiment // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAllStayFPRHCR_w10_h03Lookahead5()); // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPRHCR_w10_h03Lookahead5()); // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5()); // baseline and friends experiment // lookaheads experiment // comparing macro FPs experiment // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead3()); // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead7()); // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead10()); // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPWidePartialAvoidFPRHCR_w10_h03Lookahead5()); // comparing macro FPs experiment

////        // shorter planning period!
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1()); // greedy -integratedFP +IA
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1IntegratedFP()); // greedy +IS
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1()); // greedy -integratedFP
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1IntegratedFP()); // greedy
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1WaterfallPPRASFP_noLockInf()); // greedy + noLockInf
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1WaterfallPPRASFP_lockPeriod()); // greedy + lockPeriod
//
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1WaterfallPPRASFP_lockInf()); // greedy (inc. inter.)
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1WaterfallPPRASFP_lockInf()); // greedy +IS (inc. inter.)
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1AvoidASFP()); // greedy +IS +IA-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_1ASFP()); // greedy +IS +Go-1-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_2ASFP()); // greedy +IS +Go-2-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_3ASFP()); // greedy +IS +Go-3-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead3Go_3ASFP()); // greedy +IS +Go-3-ASFP + LH3
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_4ASFP()); // greedy +IS +Go-4-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_5ASFP()); // greedy +IS +Go-5-ASFP
//        solvers.add(LifelongSolversFactory.Go_5FPLookahead1Go_5ASFP()); // greedy +Go-5 +Go-5-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_10ASFP()); // greedy +IS +Go-10-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_20ASFP()); // greedy +IS +Go-20-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1Go_100ASFP()); // greedy +IS +Go-100-ASFP

//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1PPRGo_5ASFP()); // greedy +IS +Go-5-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1InterruptsPPRGo_5ASFP()); // greedy +IS +Go-5-ASFP +Interrupts
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1PPRGo_20ASFP()); // greedy +IS +Go-20-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPLookahead1InterruptsPPRGo_20ASFP()); // greedy +IS +Go-20-ASFP +Interrupts
//        solvers.add(LifelongSolversFactory.AvoidFPRHCR_w10_h03Lookahead5PPRGo_20ASFP()); // RHCR +IA +Go-20-ASFP
//        solvers.add(LifelongSolversFactory.AvoidFPRHCR_w10_h03Lookahead5InterruptsPPRGo_20ASFP()); // RHCR +IA +Go-20-ASFP +Interrupts

//        solvers.add(LifelongSolversFactory.Go_3ASFP()); // greedy +IS +Go-3-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_3ASFP()); // greedy +IS +Avoid-3-ASFP
//        solvers.add(LifelongSolversFactory.Go_4ASFP()); // greedy +IS +Go-4-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_4ASFP()); // greedy +IS +Avoid-4-ASFP
//        solvers.add(LifelongSolversFactory.Go_5ASFP()); // greedy +IS +Go-5-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_5ASFP()); // greedy +IS +Avoid-5-ASFP
//        solvers.add(LifelongSolversFactory.Go_10ASFP()); // greedy +IS +Go-10-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_10ASFP()); // greedy +IS +Avoid-10-ASFP
//        solvers.add(LifelongSolversFactory.Go_20ASFP()); // greedy +IS +Go-20-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_20ASFP()); // greedy +IS +Avoid-20-ASFP
//        solvers.add(LifelongSolversFactory.Go_30ASFP()); // greedy +IS +Go-30-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_30ASFP()); // greedy +IS +Avoid-30-ASFP
//        solvers.add(LifelongSolversFactory.WaterfallPPRASFP_lockInf()); // greedy +IS (inc. inter.)

//        solvers.add(LifelongSolversFactory.Go_20FPGo_20ASFP()); // greedy +Go-20-ASFP +Go-20-ASFP
//        solvers.add(LifelongSolversFactory.Go_20FPAvoid_20ASFP()); // greedy +Go-20-ASFP +Avoid-20-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_20FPAvoid_20ASFP()); // greedy +ISvoid-20-ASFP +Avoid-20-ASFP
//        solvers.add(LifelongSolversFactory.Avoid_20FPGo_20ASFP()); // greedy +ISvoid-20-ASFP +Go-20-ASFP

//        solvers.add(LifelongSolversFactory.LH_1PPRGo_20ASFPCapacity_6()); // greedy +IS +Go-20-ASFP + 6 capacity
//        solvers.add(LifelongSolversFactory.LH_3PPRGo_20ASFPCapacity_6RHCR_w10_h3()); // greedy +IS +Go-20-ASFP + 6 capacity +RHCR_w10_h3
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_20ASFPCapacity_6RHCR_w10_h1()); // greedy +IS +Go-20-ASFP + 6 capacity +RHCR_w10_h3
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_20ASFPCapacity_9()); // greedy +IS +Go-20-ASFP + 9 capacity
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_20ASFPCapacity_12()); // greedy +IS +Go-20-ASFP + 12 capacity
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_20ASFPCapacity_15()); // greedy +IS +Go-20-ASFP + 15 capacity
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_20ASFPCapacity_18()); // greedy +IS +Go-20-ASFP + 18 capacity
//        solvers.add(LifelongSolversFactory.AvoidFPLH_1PPRGo_20ASFP()); // greedy +IS +Go-20-ASFP + inf capacity


//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCapacity_18()); // greedy +IS +Go-10-ASFP + 18 capacity
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCapacity_18DynamicTimeout0p75()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 0.75
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCapacity_18DynamicTimeout1p0()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 1.0
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCapacity_18DynamicTimeout1p25()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 1.25
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCap18Timeout1p5()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCapacity_18DynamicTimeout1p75()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 1.75
//        solvers.add(LifelongSolversFactory.LH_1PPRGo_10ASFPCapacity_18DynamicTimeout2p0()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 2.0


//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_Timeout1p5()); // greedy +IS +Go-5-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Go10ASFP_Cap18_Timeout1p5()); // greedy +IS +Go-10-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Go20ASFP_Cap18_Timeout1p5()); // greedy +IS +Go-20-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Go30ASFP_Cap18_Timeout1p5()); // greedy +IS +Go-20-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Avoid5ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-5-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Avoid10ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-10-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Avoid20ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-20-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Avoid30ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-20-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_WaterfallPPRASFP_Cap18_Timeout1p5()); // greedy +IS +waterfall + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.RandSelectASFP()); // greedy +IS +randomly select ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Approach5ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-5-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Approach10ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-10-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Approach20ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-20-ASFP + 18 capacity + dynamic timeout 1.5
//        solvers.add(LifelongSolversFactory.LH1_Approach30ASFP_Cap18_Timeout1p5()); // greedy +IS +Avoid-20-ASFP + 18 capacity + dynamic timeout 1.5


//        solvers.add(LifelongSolversFactory.subSetSelector_PrP());
        solvers.add(LifelongSolversFactory.allAgentsSelector_PrP());
        solvers.add(LifelongSolversFactory.allAgentsSelector_PIBT());
        solvers.add((LifelongSolversFactory.allAgentsSelector_PrPt()));
//        solvers.add(LifelongSolversFactory.subSetSelector_PIBT());

//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_infiniteHorizon_PIBT());
//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_Horizon1_PIBT());
//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_infiniteHorizon_partialSolution_PIBT());
//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_SubsetSelector_PrP());
//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_AllAgentSelector_PrP());
//        solvers.add(LifelongSolversFactory.LH1_Go5ASFP_Cap18_DisallowedPartialSolution_PrP());

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
