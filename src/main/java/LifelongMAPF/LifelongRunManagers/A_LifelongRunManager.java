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
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartialOneActionFPRHCR_w10_h03Lookahead5()); // baseline and friends experiment // comparing macro FPs experiment
////        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartialOneActionFPRHCR_w10_h03()); // baseline and friends experiment
////        solvers.add(LifelongSolversFactory.allAgentsPrPDeepPartialOneActionRHCR_w10_h03()); // baseline and friends experiment // lookaheads experiment
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAllStayFPRHCR_w10_h03Lookahead5()); // micro experiment
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPRHCR_w10_h03Lookahead5()); // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5()); // baseline and friends experiment // lookaheads experiment // comparing macro FPs experiment // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP());
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead3()); // lookaheads experiment
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead7()); // lookaheads experiment
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead10()); // lookaheads experiment
////        solvers.add(LifelongSolversFactory.stationaryAgentsPrPWidePartialOneActionFPRHCR_w10_h03Lookahead5()); // comparing macro FPs experiment
//
//        // shorter planning period!
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleOneActionFPLookahead1()); // greedy -integratedFP +IA
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleOneActionFPLookahead1IntegratedFP()); // greedy +IA
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1()); // greedy -integratedFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1IntegratedFP()); // greedy
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1WaterfallPPRASFP_noLockInf()); // greedy + noLockInf
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1WaterfallPPRASFP_lockPeriod()); // greedy + lockPeriod

//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1WaterfallPPRASFP_lockInf()); // greedy (inc. inter.)
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1WaterfallPPRASFP_lockInf()); // greedy +IA (inc. inter.)
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IAvoidASFP()); // greedy +IA +IA-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_1ASFP()); // greedy +IA +IGo-1-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_2ASFP()); // greedy +IA +IGo-2-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_3ASFP()); // greedy +IA +IGo-3-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead3IGo_3ASFP()); // greedy +IA +IGo-3-ASFP + LH3
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_4ASFP()); // greedy +IA +IGo-4-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_5ASFP()); // greedy +IA +IGo-5-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIGo_5FPLookahead1IGo_5ASFP()); // greedy +IGo-5 +IGo-5-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_10ASFP()); // greedy +IA +IGo-10-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_20ASFP()); // greedy +IA +IGo-20-ASFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1IGo_100ASFP()); // greedy +IA +IGo-100-ASFP



        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1PPRIGo_5ASFP()); // greedy +IA +IGo-5-ASFP
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1InterruptsPPRIGo_5ASFP()); // greedy +IA +IGo-5-ASFP +Interrupts
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1PPRIGo_20ASFP()); // greedy +IA +IGo-20-ASFP
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPLookahead1InterruptsPPRIGo_20ASFP()); // greedy +IA +IGo-20-ASFP +Interrupts
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPRHCR_w10_h03Lookahead5PPRIGo_20ASFP()); // RHCR +IA +IGo-20-ASFP
        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleIAvoidFPRHCR_w10_h03Lookahead5InterruptsPPRIGo_20ASFP()); // RHCR +IA +IGo-20-ASFP +Interrupts

        return solvers;
    }

    protected void addAllMapsAndInstances(String instancesDir, int[] agentNums){
        /*  =   Set Properties   =  */
        InstanceProperties properties = getInstanceProperties(agentNums);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, getInstanceBuilder(),properties);

        /*  =   Add new experiment   =  */
        Experiment warehouseInstances = new Experiment(getExperimentName(), instanceManager, null, getTimeoutEach());
        warehouseInstances.keepSolutionInReport = false;
        warehouseInstances.keepReportAfterCommit = false;
        warehouseInstances.sharedGoals = false;
        warehouseInstances.sharedSources = false;
        this.experiments.add(warehouseInstances);
    }

    protected static int getTimeoutEach() {
        return 4 * 5 * 60 * 1000;
    }

    @NotNull
    protected abstract String getExperimentName();

    protected abstract @NotNull I_InstanceBuilder getInstanceBuilder();
    @NotNull

    protected InstanceProperties getInstanceProperties(int[] agentNums) {
        return new InstanceProperties(null, -1, agentNums);
    }
}
