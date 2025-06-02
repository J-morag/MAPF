package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.MDDs.OnePathAStarMDDBuilderFactory;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.LaCAM.LaCAMBuilder;
import BasicMAPF.Solvers.LaCAM.LaCAM_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LNSBuilder;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.*;
import TransientMAPF.TransientMAPFSettings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class CanonicalSolversFactory {
    public final static String PP_NAME = "PP";
    public final static String PP_SIPP_NAME = "PP_SIPP";
    public final static String PP_RR_ANYTIME_NAME = "PP_RR";
    public final static String PP_SIPP_RR_ANYTIME_NAME = "PP_SIPP_RR";
    public final static String PP_RR_UNTIL_FIRST_SOLUTION_NAME = "PP_RR_UntilFirstSolution";
    public final static String PP_DR_UNTIL_FIRST_SOLUTION_NAME = "PP_DR_UntilFirstSolution";
    public final static String PPRStar_ANYTIME_NAME = "PPRStar";
    public final static String CBS_NAME = "CBS";
    public final static String ICTS_NAME = "ICTS";
    public final static String PIBT_NAME = "PIBT";
    public final static String PIBTt_NAME = "PIBTt";
    public final static String LACAM_NAME = "LaCAM";
    public final static String LaCAMt_NAME = "LaCAMt";
    public final static String LNS1_NAME = "LNS1";
    public final static String PCS_NAME = "PCS";
    public final static String PCS_LEXICAL_NAME = "PCS_Lexical";
    public final static String PaPS_NAME = "PaPS";
    public final static String NAIVE_PaPS_NAME = "NaivePaPS";
    public final static String NAIVE_PaPS_UNIFIED_OPEN_NAME = "NaivePaPSUnifiedOpen";
    public final static String PP_BY_USING_PaPS = "PP_byUsingPaPS";
    public final static String PFCS_NAME = "PFCS";
    public final static String NAIVE_PFCS_UNIFIED_OPEN_NAME = "NaivePFCSUnifiedOpen";
    public final static String ASTAR_NAME = "AStar";
    public final static String SIPP_NAME = "SIPP";

    // A map of solver names to their registrations.
    private static final Map<String, SolverRegistration<? extends I_Solver>> registrations;

    static {
        Map<String, SolverRegistration<? extends I_Solver>> regs = new HashMap<>();

        regs.put(PP_NAME, new SolverRegistration<>(
                PP_NAME,
                "Prioritised Planning - single rollout, no restarts",
                CanonicalSolversFactory::createPPSolver
        ));

        regs.put(PP_SIPP_NAME, new SolverRegistration<>(
                PP_SIPP_NAME,
                "Prioritised Planning using SIPP - single rollout, no restarts",
                CanonicalSolversFactory::createPPSIPPSolver
        ));

        regs.put(PP_RR_ANYTIME_NAME, new SolverRegistration<>(
                PP_RR_ANYTIME_NAME,
                "Prioritised Planning - infinite random ordering restarts",
                CanonicalSolversFactory::createPPRRAnytimeSolver
        ));

        regs.put(PP_SIPP_RR_ANYTIME_NAME, new SolverRegistration<>(
                PP_SIPP_RR_ANYTIME_NAME,
                "Prioritised Planning using SIPP - infinite random ordering restarts",
                CanonicalSolversFactory::createPPSIPPRRAnytimeSolver
        ));

        regs.put(PP_RR_UNTIL_FIRST_SOLUTION_NAME, new SolverRegistration<>(
                PP_RR_UNTIL_FIRST_SOLUTION_NAME,
                "Prioritised Planning - random ordering restarts until first solution",
                CanonicalSolversFactory::createPPRRUntilFirstSolutionSolver
        ));

        regs.put(PP_DR_UNTIL_FIRST_SOLUTION_NAME, new SolverRegistration<>(
                PP_DR_UNTIL_FIRST_SOLUTION_NAME,
                "Prioritised Planning - deterministic rescheduling until first solution",
                CanonicalSolversFactory::createPPDRUntilFirstSolutionSolver
        ));

        regs.put(PPRStar_ANYTIME_NAME, new SolverRegistration<>(
                PPRStar_ANYTIME_NAME,
                "Prioritised Planning with Randomised A* - infinite random restarts with random A* seeds",
                CanonicalSolversFactory::createPPRStarAnytimeSolver
        ));

        regs.put(CBS_NAME, new SolverRegistration<>(
                CBS_NAME,
                "Conflict Based Search",
                CanonicalSolversFactory::createCBSSolver
        ));

        regs.put(ICTS_NAME, new SolverRegistration<>(
                ICTS_NAME,
                "Increasing Cost Tree Search",
                CanonicalSolversFactory::createICTSSolver
        ));

        regs.put(PIBT_NAME, new SolverRegistration<>(
                PIBT_NAME,
                "Priority Inheritance with Backtracking",
                CanonicalSolversFactory::createPIBTSolver
        ));

        regs.put(PIBTt_NAME, new SolverRegistration<>(
                PIBTt_NAME,
                "Priority Inheritance with Backtracking with Transient MAPF",
                CanonicalSolversFactory::createPIBTtSolver
        ));

        regs.put(LACAM_NAME, new SolverRegistration<>(
                LACAM_NAME,
                "Lazy Constraints Addition Search",
                CanonicalSolversFactory::createLaCAMSolver
        ));

        regs.put(LaCAMt_NAME, new SolverRegistration<>(
                LaCAMt_NAME,
                "Lazy Constraints Addition Search with Transient MAPF",
                CanonicalSolversFactory::createLaCAMtSolver
        ));

        regs.put(LNS1_NAME, new SolverRegistration<>(
                LNS1_NAME,
                "Large Neighborhood Search 1",
                CanonicalSolversFactory::createLNS1Solver
        ));

        regs.put(PCS_NAME, new SolverRegistration<>(
                PCS_NAME,
                "Priority Constrained Search",
                CanonicalSolversFactory::createPCSSolver
        ));

        regs.put(PCS_LEXICAL_NAME, new SolverRegistration<>(
                PCS_LEXICAL_NAME,
                "Priority Constrained Search with Lexical cost function",
                CanonicalSolversFactory::createPCSLexicalSolver
        ));

        regs.put(PaPS_NAME, new SolverRegistration<>(
                PaPS_NAME,
                "Path and Priority Search",
                CanonicalSolversFactory::createPaPSSolver
        ));

        regs.put(NAIVE_PaPS_NAME, new SolverRegistration<>(
                NAIVE_PaPS_NAME,
                "Naive Path and Priority Search",
                CanonicalSolversFactory::createNaivePaPSSolver
        ));

        regs.put(NAIVE_PaPS_UNIFIED_OPEN_NAME, new SolverRegistration<>(
                NAIVE_PaPS_UNIFIED_OPEN_NAME,
                "Naive Path and Priority Search with Unified Open List",
                CanonicalSolversFactory::createNaivePaPSUnifiedOpenSolver
        ));

        regs.put(PP_BY_USING_PaPS, new SolverRegistration<>(
                PP_BY_USING_PaPS,
                "PP by using PaPS with one ordering and paths instead of MDDs",
                CanonicalSolversFactory::createPP_byUsingPaPS
        ));

        regs.put(PFCS_NAME, new SolverRegistration<>(
                PFCS_NAME,
                "Path-Function Constrained Search",
                CanonicalSolversFactory::createPFCSSolver
        ));

        regs.put(NAIVE_PFCS_UNIFIED_OPEN_NAME, new SolverRegistration<>(
                NAIVE_PFCS_UNIFIED_OPEN_NAME,
                "Naive Path-Function Constrained Search with Unified Open List",
                CanonicalSolversFactory::createNaivePFCSUnifiedOpenSolver
        ));

        regs.put(ASTAR_NAME, new SolverRegistration<>(
                ASTAR_NAME,
                "A*",
                CanonicalSolversFactory::createAStarSolver
        ));

        regs.put(SIPP_NAME, new SolverRegistration<>(
                SIPP_NAME,
                "SIPP",
                CanonicalSolversFactory::createSIPPSolver
        ));

        registrations = Collections.unmodifiableMap(regs);
    }

    /**
     * Factory method to create a solver by its name.
     */
    public static I_Solver createSolver(@NotNull String name) {
        SolverRegistration<? extends I_Solver> reg = registrations.get(name);
        if (reg == null) {
            throw new IllegalArgumentException("Unknown solver name: " + name);
        }
        return reg.create();
    }

    public static String getDescription(@NotNull String name) {
        SolverRegistration<? extends I_Solver> reg = registrations.get(name);
        if (reg == null) {
            throw new IllegalArgumentException("Unknown solver name: " + name);
        }
        return reg.description();
    }

    /**
     * Get a sorted list of all solver names.
     */
    public static Iterable<String> getSolverNames() {
        List<String> names = new ArrayList<>(registrations.keySet());
        names.sort(Comparator.naturalOrder());
        return names;
    }

    public static PrioritisedPlanning_Solver createPPSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPSIPPSolver() {
        return new PrioritisedPlanning_Solver(
                createSIPPSolver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPRRAnytimeSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPSIPPRRAnytimeSolver() {
        return new PrioritisedPlanning_Solver(
                createSIPPSolver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPRRUntilFirstSolutionSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, null,
                        RestartsStrategy.reorderingStrategy.randomRestarts, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPDRUntilFirstSolutionSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, null,
                        RestartsStrategy.reorderingStrategy.deterministicRescheduling, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPRStarAnytimeSolver() {
        PrioritisedPlanning_Solver pprstar = new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
        pprstar.pathRandomizationAttemptsPerOrdering = Integer.MAX_VALUE;
        return pprstar;
    }

    public static CBS_Solver createCBSSolver() {
        return new CBSBuilder().createCBS_Solver();
    }

    public static ICTS_Solver createICTSSolver() {
        return new ICTS_Solver();
    }

    public static PIBT_Solver createPIBTSolver() {
        return new PIBT_Solver();
    }

    public static PIBT_Solver createPIBTtSolver() {
        return new PIBT_Solver(null, null, TransientMAPFSettings.defaultTransientMAPF);
    }

    public static LaCAM_Solver createLaCAMSolver() {
        return new LaCAMBuilder().createLaCAM();
    }

    public static LaCAM_Solver createLaCAMtSolver() {
        return new LaCAMBuilder().setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLaCAM();
    }

    public static LargeNeighborhoodSearch_Solver createLNS1Solver() {
        return new LNSBuilder().createLNS();
    }

    public static PathAndPrioritySearch createPCSSolver() {
        return new PaPSBuilder().setNoAgentsSplit(true).createPaPS();
    }

    public static PathAndPrioritySearch createPCSLexicalSolver() {
        return new PaPSBuilder().setNoAgentsSplit(true).setNodeComparator(PCSCompLexical.DEFAULT_INSTANCE).createPaPS();
    }

    public static PathAndPrioritySearch createPaPSSolver() {
        return new PaPSBuilder().createPaPS();
    }

    public static NaivePaPS createNaivePaPSSolver() {
        return new NaivePaPS(null, null, -1);
    }

    public static PathAndPrioritySearch createNaivePaPSUnifiedOpenSolver() {
        return new PaPSBuilder().setRootGenerator(new NaivePaPSUnifiedOpenPCSRG()).createPaPS();
    }

    public static PathAndPrioritySearch createPP_byUsingPaPS() {
        return new PaPSBuilder().setMddSearcherFactory(new OnePathAStarMDDBuilderFactory()).createPaPS();
    }

    public static PathAndPrioritySearch createPFCSSolver() {
        return new PaPSBuilder().setMddSearcherFactory(new OnePathAStarMDDBuilderFactory()).createPaPS();
    }

    public static PathAndPrioritySearch createNaivePFCSUnifiedOpenSolver() {
        return new PaPSBuilder().setMddSearcherFactory(new OnePathAStarMDDBuilderFactory()).setRootGenerator(new NaivePaPSUnifiedOpenPCSRG()).createPaPS();
    }

    public static SingleAgentAStar_Solver createAStarSolver() {
        return new SingleAgentAStar_Solver();
    }

    public static SingleAgentAStarSIPP_Solver createSIPPSolver() {
        return new SingleAgentAStarSIPP_Solver();
    }

    /**
     * Inner record to encapsulate the registration of a solver.
     */
    private record SolverRegistration<T extends I_Solver>(String name, String description, Supplier<T> creator) {
        public T create() {
            T solver = creator.get();
            solver.setName(name);
            solver.setDescription(description);
            return solver;
        }
    }
}
