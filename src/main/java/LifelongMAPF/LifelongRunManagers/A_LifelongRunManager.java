package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DeepPartialSolutionsStrategy;
import Environment.Experiment;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import Environment.RunManagers.A_RunManager;
import Environment.Visualization.I_VisualizeSolution;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.FailPolicies.AStarFailPolicies.I_AStarFailPolicy;
import LifelongMAPF.FailPolicies.AStarFailPolicies.PostProcIGoFactory;
import LifelongMAPF.FailPolicies.AStarFailPolicies.PostProcessRankingAStarFP;
import LifelongMAPF.FailPolicies.IStayFailPolicy;
import LifelongMAPF.FailPolicies.I_SingleAgentFailPolicy;
import LifelongMAPF.I_LifelongCompatibleSolver;
import LifelongMAPF.LifelongSimulationSolver;
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
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPNoPartialIAvoidFPRHCR_w10_h03Lookahead5()); // baseline and friends experiment // comparing macro FPs experiment
//        solvers.add(LifelongSolversFactory.allAgentsPrPNoPartialIAvoidFPRHCR_w10_h03()); // baseline and friends experiment
//        solvers.add(LifelongSolversFactory.allAgentsPrPDeepPartialIAvoidRHCR_w10_h03()); // baseline and friends experiment // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAllStayFPRHCR_w10_h03Lookahead5()); // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPRHCR_w10_h03Lookahead5()); // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5()); // baseline and friends experiment // lookaheads experiment // comparing macro FPs experiment // micro experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5IAvoid1ASFP());
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead3()); // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead7()); // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead10()); // lookaheads experiment
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPWidePartialIAvoidFPRHCR_w10_h03Lookahead5()); // comparing macro FPs experiment

////        // shorter planning period!
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1()); // greedy -integratedFP +IA
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IntegratedFP()); // greedy +IA
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1()); // greedy -integratedFP
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1IntegratedFP()); // greedy
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1WaterfallPPRASFP_noLockInf()); // greedy + noLockInf
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1WaterfallPPRASFP_lockPeriod()); // greedy + lockPeriod
//
//        solvers.add(LifelongSolversFactory.StayOnceFPLookahead1WaterfallPPRASFP_lockInf()); // greedy (inc. inter.)
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1WaterfallPPRASFP_lockInf()); // greedy +IA (inc. inter.)
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IAvoidASFP()); // greedy +IA +IA-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_1ASFP()); // greedy +IA +IGo-1-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_2ASFP()); // greedy +IA +IGo-2-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_3ASFP()); // greedy +IA +IGo-3-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead3IGo_3ASFP()); // greedy +IA +IGo-3-ASFP + LH3
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_4ASFP()); // greedy +IA +IGo-4-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_5ASFP()); // greedy +IA +IGo-5-ASFP
//        solvers.add(LifelongSolversFactory.IGo_5FPLookahead1IGo_5ASFP()); // greedy +IGo-5 +IGo-5-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_10ASFP()); // greedy +IA +IGo-10-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_20ASFP()); // greedy +IA +IGo-20-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1IGo_100ASFP()); // greedy +IA +IGo-100-ASFP

//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1PPRIGo_5ASFP()); // greedy +IA +IGo-5-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1InterruptsPPRIGo_5ASFP()); // greedy +IA +IGo-5-ASFP +Interrupts
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1PPRIGo_20ASFP()); // greedy +IA +IGo-20-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPLookahead1InterruptsPPRIGo_20ASFP()); // greedy +IA +IGo-20-ASFP +Interrupts
//        solvers.add(LifelongSolversFactory.IAvoidFPRHCR_w10_h03Lookahead5PPRIGo_20ASFP()); // RHCR +IA +IGo-20-ASFP
//        solvers.add(LifelongSolversFactory.IAvoidFPRHCR_w10_h03Lookahead5InterruptsPPRIGo_20ASFP()); // RHCR +IA +IGo-20-ASFP +Interrupts

//        solvers.add(LifelongSolversFactory.IGo_3ASFP()); // greedy +IA +IGo-3-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_3ASFP()); // greedy +IA +IAvoid-3-ASFP
//        solvers.add(LifelongSolversFactory.IGo_4ASFP()); // greedy +IA +IGo-4-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_4ASFP()); // greedy +IA +IAvoid-4-ASFP
//        solvers.add(LifelongSolversFactory.IGo_5ASFP()); // greedy +IA +IGo-5-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_5ASFP()); // greedy +IA +IAvoid-5-ASFP
//        solvers.add(LifelongSolversFactory.IGo_10ASFP()); // greedy +IA +IGo-10-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_10ASFP()); // greedy +IA +IAvoid-10-ASFP
//        solvers.add(LifelongSolversFactory.IGo_20ASFP()); // greedy +IA +IGo-20-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_20ASFP()); // greedy +IA +IAvoid-20-ASFP
//        solvers.add(LifelongSolversFactory.IGo_30ASFP()); // greedy +IA +IGo-30-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_30ASFP()); // greedy +IA +IAvoid-30-ASFP
//        solvers.add(LifelongSolversFactory.WaterfallPPRASFP_lockInf()); // greedy +IA (inc. inter.)

//        solvers.add(LifelongSolversFactory.IGo_20FPIGo_20ASFP()); // greedy +IGo-20-ASFP +IGo-20-ASFP
//        solvers.add(LifelongSolversFactory.IGo_20FPIAvoid_20ASFP()); // greedy +IGo-20-ASFP +IAvoid-20-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_20FPIAvoid_20ASFP()); // greedy +IAvoid-20-ASFP +IAvoid-20-ASFP
//        solvers.add(LifelongSolversFactory.IAvoid_20FPIGo_20ASFP()); // greedy +IAvoid-20-ASFP +IGo-20-ASFP

        solvers.add(LifelongSolversFactory.LH_1PPRIGo_20ASFPCapacity_6()); // greedy +IA +IGo-20-ASFP + 6 capacity
        solvers.add(LifelongSolversFactory.LH_3PPRIGo_20ASFPCapacity_6RHCR_w10_h3()); // greedy +IA +IGo-20-ASFP + 6 capacity +RHCR_w10_h3
        solvers.add(LifelongSolversFactory.LH_1PPRIGo_20ASFPCapacity_6RHCR_w10_h1()); // greedy +IA +IGo-20-ASFP + 6 capacity +RHCR_w10_h3
        solvers.add(LifelongSolversFactory.LH_1PPRIGo_20ASFPCapacity_9()); // greedy +IA +IGo-20-ASFP + 9 capacity
        solvers.add(LifelongSolversFactory.LH_1PPRIGo_20ASFPCapacity_12()); // greedy +IA +IGo-20-ASFP + 12 capacity
        solvers.add(LifelongSolversFactory.LH_1PPRIGo_20ASFPCapacity_15()); // greedy +IA +IGo-20-ASFP + 15 capacity
        solvers.add(LifelongSolversFactory.LH_1PPRIGo_20ASFPCapacity_18()); // greedy +IA +IGo-20-ASFP + 18 capacity
        solvers.add(LifelongSolversFactory.IAvoidFPLH_1PPRIGo_20ASFP()); // greedy +IA +IGo-20-ASFP + inf capacity

        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IStayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 15;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(20), false, null);

        I_LifelongCompatibleSolver offlineSolver = new PIBT_Solver(null, Integer.MAX_VALUE);

        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)),
                offlineSolver, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);

        solver.name = "testPIBT";
        solvers.add(solver);

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
