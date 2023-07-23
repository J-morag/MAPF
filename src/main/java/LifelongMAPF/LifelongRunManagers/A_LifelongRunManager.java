package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.CostFunctions.SOCCostFunction;
import BasicMAPF.CostFunctions.SSTCostFunction;
import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DeepPartialSolutionsStrategy;
import Environment.Experiment;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import Environment.RunManagers.A_RunManager;
import Environment.Visualization.I_VisualizeSolution;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.FailPolicies.AStarFailPolicies.IAvoid1ASFP;
import LifelongMAPF.FailPolicies.OneActionFailPolicy;
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
////
////        // shorter planning period!
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleOneActionFPLookahead1()); // greedy -integratedFP +IA
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleOneActionFPLookahead1IntegratedFP()); // greedy +IA
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1()); // greedy -integratedFP
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1IntegratedFP()); // greedy
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1WaterfallPPRASFP_lockInf()); // greedy
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1WaterfallPPRASFP_noLockInf()); // greedy + noLockInf
//        solvers.add(LifelongSolversFactory.stationaryAgentsPrPReplanSingleStayOnceFPLookahead1WaterfallPPRASFP_lockPeriod()); // greedy + lockPeriod


        // optimizing for SOC

        PrioritisedPlanning_Solver PrPT_SOC = new PrioritisedPlanning_Solver(null, null, new SOCCostFunction(),
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                true, null, true, null, null);
        PrPT_SOC.name = "PrPT_SOC";

        PrioritisedPlanning_Solver PrP_SOC = new PrioritisedPlanning_Solver(null, null, new SOCCostFunction(),
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                true, null, false, null, null);
        PrP_SOC.name = "PrP_SOC";

        // optimizing for SST

        PrioritisedPlanning_Solver PrPT_SST = new PrioritisedPlanning_Solver(null, null, new SSTCostFunction(),
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                true, null, true, null, null);
        PrPT_SST.name = "PrPT_SST";

        PrioritisedPlanning_Solver PrP_SST = new PrioritisedPlanning_Solver(null, null, new SSTCostFunction(),
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                true, null, false, null, null);
        PrP_SST.name = "PrP_SST";



        LifelongSimulationSolver lifelong_PrPT_SOC = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
                PrPT_SOC,
                null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 5);
        lifelong_PrPT_SOC.name = "lifelong_PrPT_SOC";

        LifelongSimulationSolver lifelong_PrP_SOC = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
                PrP_SOC,
                null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 5);
        lifelong_PrP_SOC.name = "lifelong_PrP_SOC";

        LifelongSimulationSolver lifelong_PrPT_SST = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
                PrPT_SST,
                null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 5);
        lifelong_PrPT_SST.name = "lifelong_PrPT_SST";

        LifelongSimulationSolver lifelong_PrP_SST = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
                PrP_SST,
                null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 5);
        lifelong_PrP_SST.name = "lifelong_PrP_SST";

        solvers.add(lifelong_PrPT_SOC);
        solvers.add(lifelong_PrP_SOC);
        solvers.add(lifelong_PrPT_SST);
        solvers.add(lifelong_PrP_SST);

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
