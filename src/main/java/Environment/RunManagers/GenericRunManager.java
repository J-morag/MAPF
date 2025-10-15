package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.*;
import Environment.Experiment;
import Environment.Visualization.I_VisualizeSolution;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenericRunManager extends A_RunManager {

    private final String instancesDir;
    private final int[] agentNums;
    private final I_InstanceBuilder instanceBuilder;
    private final String experimentName;
    private final boolean skipAfterFail;
    private final String instancesRegex;
    private List<I_Solver> solversOverride;
    private final Integer timeoutEach;

    public GenericRunManager(@NotNull String instancesDir, int[] agentNums, @NotNull I_InstanceBuilder instanceBuilder,
                             @NotNull String experimentName, boolean skipAfterFail, String instancesRegex,
                             String resultsOutputDir, String resultsFilePrefix, I_VisualizeSolution solutionVisualizer,
                             Integer timeoutEach, @Nullable List<I_Solver> solversOverride) {
        super(resultsOutputDir, solutionVisualizer);
        if (agentNums == null){
            throw new IllegalArgumentException("AgentNums can't be null");
        }
        this.instancesDir = instancesDir;
        this.agentNums = agentNums;
        this.instanceBuilder = instanceBuilder;
        this.experimentName = experimentName;
        this.skipAfterFail = skipAfterFail;
        this.instancesRegex = instancesRegex;
        this.resultsFilePrefix = resultsFilePrefix;
        this.timeoutEach = timeoutEach;
        this.solversOverride = solversOverride;
    }
    @Override
    void setSolvers() {
        if (solversOverride != null){
            super.solvers = solversOverride;
            return;
        }
//        super.solvers.add(CanonicalSolversFactory.createPPRRUntilFirstSolutionSolver());
//
//        super.solvers.add(CanonicalSolversFactory.createCBSSolver());

        PrioritisedPlanning_Solver pp = CanonicalSolversFactory.createPPSolver();
        pp.setName("PP");
        super.solvers.add(pp);

//        PriorityConstrainedSearch PP_asPaPS = new PCSBuilder().setMddSearcherFactory(new OnePathAStarMDDBuilderFactory()).createPCS();
//        PP_asPaPS.name = "PP_asPaPS";
//        super.solvers.add(PP_asPaPS);

        PrioritisedPlanning_Solver PrPr = CanonicalSolversFactory.createPPRRAnytimeSolver();
        PrPr.setName("PP-RR");
        super.solvers.add(PrPr);

        PrioritisedPlanning_Solver PPRStar = CanonicalSolversFactory.createPPRStarAnytimeSolver();
        PPRStar.setName("PPR*");
        super.solvers.add(PPRStar);
//
        PathAndPrioritySearch pcs = CanonicalSolversFactory.createPCSSolver();
        pcs.setName("PCS");
        super.solvers.add(pcs);

//        PathAndPrioritySearch pcs_lexical = CanonicalSolversFactory.createPCSLexicalSolver();
//        super.solvers.add(pcs_lexical);

//        PriorityConstrainedSearch pcs_dup_detect = new PCSBuilder().setUseDuplicateDetection(true).createPCS();
//        pcs_dup_detect.name = "pcs_dup_detect";
//        super.solvers.add(pcs_dup_detect);

        NaivePaPS NaivePaPS = CanonicalSolversFactory.createNaivePaPSSolver();
        NaivePaPS.name = "NaivePaPS";
        super.solvers.add(NaivePaPS);

        PathAndPrioritySearch naivePaPSUnifiedOpen = CanonicalSolversFactory.createNaivePaPSUnifiedOpenSolver();
        naivePaPSUnifiedOpen.name = "naivePaPSUnifiedOpen";
        super.solvers.add(naivePaPSUnifiedOpen);

//        OptimalPrioritySearch OPSNoPartial = new PCSBuilder().createOPS();
//        OPSNoPartial.name = "OPSNoPartial";
//        super.solvers.add(OPSNoPartial);

        PathAndPrioritySearch PaPS_H1 = new PaPSBuilder().setPaPSHeuristic(new PaPSHeuristicDefault()).createPaPS();
        PaPS_H1.name = "PaPS_H1";
        super.solvers.add(PaPS_H1);

        PathAndPrioritySearch PaPS_H2 = new PaPSBuilder().createPaPS();
        PaPS_H2.name = "PaPS_H2";
        super.solvers.add(PaPS_H2);

        PathAndPrioritySearch PaPS_noMDDTieBreak = new PaPSBuilder().setNodeComparator(new PathAndPrioritySearch.DEFAULT_PaPS_COMPARATOR()).createPaPS();
        PaPS_noMDDTieBreak.name = "PaPS_noMDDTieBreak";
        super.solvers.add(PaPS_noMDDTieBreak);

//        OptimalPrioritySearch OPS_dup_detect = new PCSBuilder().setUseDuplicateDetection(true).createOPS();
//        OPS_dup_detect.name = "OPS_dup_detect";
//        super.solvers.add(OPS_dup_detect);

        PathAndPrioritySearch PFCS = CanonicalSolversFactory.createPFCSSolver();
        PFCS.setName("PFCS");
        super.solvers.add(PFCS);

        PathAndPrioritySearch NaivePFCS = CanonicalSolversFactory.createNaivePFCSUnifiedOpenSolver();
        NaivePFCS.setName("NaivePFCS");
        super.solvers.add(NaivePFCS);

        // Pseudo Oracle

        // 150 orderings, 1 path each
        PrioritisedPlanning_Solver orderings150PathRandomization1 = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 150, RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        orderings150PathRandomization1.pathRandomizationAttemptsPerOrdering = 1;
        orderings150PathRandomization1.name = "orderings" + 150 + "PathRandomizations" + 1;
        super.solvers.add(orderings150PathRandomization1);

        // 150 orderings, 50 paths each
        PrioritisedPlanning_Solver orderings150PathRandomization50 = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 150, RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        orderings150PathRandomization50.pathRandomizationAttemptsPerOrdering = 50;
        orderings150PathRandomization50.name = "orderings" + 150 + "PathRandomizations" + 50;
        super.solvers.add(orderings150PathRandomization50);

        // Pseudo Oracle By Runs

        // 300 orderings, 1 path each
        PrioritisedPlanning_Solver orderings300PathRandomization1 = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 300, RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        orderings300PathRandomization1.pathRandomizationAttemptsPerOrdering = 1;
        orderings300PathRandomization1.name = "orderings" + 300 + "PathRandomizations" + 1;
        super.solvers.add(orderings300PathRandomization1);

        // 6 orderings, 50 paths each
        PrioritisedPlanning_Solver orderings6PathRandomization50 = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 6, RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        orderings6PathRandomization50.pathRandomizationAttemptsPerOrdering = 50;
        orderings6PathRandomization50.name = "orderings" + 6 + "PathRandomizations" + 50;
        super.solvers.add(orderings6PathRandomization50);


        // 150 orderings, 2 paths each
        PrioritisedPlanning_Solver orderings150PathRandomization2 = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 150, RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        orderings150PathRandomization2.pathRandomizationAttemptsPerOrdering = 2;
        orderings150PathRandomization2.name = "orderings" + 150 + "PathRandomizations" + 2;
        super.solvers.add(orderings150PathRandomization2);

        // 100 orderings, 3 paths each
        PrioritisedPlanning_Solver orderings100PathRandomization3 = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 100, RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        orderings100PathRandomization3.pathRandomizationAttemptsPerOrdering = 3;
        orderings100PathRandomization3.name = "orderings" + 100 + "PathRandomizations" + 3;
        super.solvers.add(orderings100PathRandomization3);

    }

    public void overrideSolvers(@NotNull List<I_Solver> solvers){
        this.solversOverride = solvers;
    }

    @Override
    void setExperiments() {
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums, instancesRegex);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, instanceBuilder, properties);

        /*  =   Add new experiment   =  */
        Experiment experiment = new Experiment(experimentName, instanceManager, null, timeoutEach);
        experiment.skipAfterFail = this.skipAfterFail;
        experiment.visualizer = this.visualizer;
        this.experiments.add(experiment);
    }

}
