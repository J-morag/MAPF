package BasicMAPF.Solvers;

import BasicMAPF.CostFunctions.ConflictsCount;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPPS_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.LaCAM.LaCAMBuilder;
import BasicMAPF.Solvers.LaCAM.LaCAM_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LNSBuilder;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.solutionsGeneratorForLNS2;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSBuilder;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PriorityConstrainedSearch;
import TransientMAPF.TransientMAPFSettings;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSCompLexical;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class CanonicalSolversFactory {
    public final static String PP_NAME = "PP";
    public final static String PPt_NAME = "PPt";
    public final static String PP_SIPP_NAME = "PP_SIPP";
    public final static String PP_SIPPt_NAME = "PP_SIPPt";
    public final static String PP_RR_ANYTIME_NAME = "PP_RR";
    public final static String PPt_RR_ANYTIME_NAME = "PPt_RR";
    public final static String PP_SIPP_RR_ANYTIME_NAME = "PP_SIPP_RR";
    public final static String PPt_SIPP_RR_ANYTIME_NAME = "PPt_SIPP_RR";
    public final static String PP_RR_UNTIL_FIRST_SOLUTION_NAME = "PP_RR_UntilFirstSolution";
    public final static String PP_DR_UNTIL_FIRST_SOLUTION_NAME = "PP_DR_UntilFirstSolution";
    public final static String PPRStar_ANYTIME_NAME = "PPRStar";
    public final static String CBS_NAME = "CBS";
    public final static String CBS_SIPP_NAME = "CBS_SIPP";
    public final static String CBSt_NAME = "CBSt";
    public final static String ICTS_NAME = "ICTS";
    public final static String PIBT_NAME = "PIBT";
    public final static String PIBTt_NAME = "PIBTt";
    public final static String LACAM_NAME = "LaCAM";
    public final static String LaCAMt_NAME = "LaCAMt";
    public final static String LNS1_NAME = "LNS1";
    public final static String LNS1_SIPP_NAME = "LNS1_SIPP";
    public final static String LNS1_SIPPt_NAME = "LNS1_SIPPt";
    public final static String LNS1t_NAME = "LNS1t";
    public final static String LNS2_NAME = "LNS2";
    public final static String LNS2t_NAME = "LNS2t";
    public final static String PCS_NAME = "PCS";
    public final static String PCS_LEXICAL_NAME = "PCS_Lexical";
    public final static String ASTAR_NAME = "AStar";
    public final static String SIPP_NAME = "SIPP";
    public final static String SIPPS_NAME = "SIPPS";
    public final static String PIE_NAME = "PIE";
    public final static String PIEt_NAME = "PIEt";
    public final static String PIE_SIPP_NAME = "PIE_SIPP";
    public final static String PIE_SIPPt_NAME = "PIE_SIPPt";


    // A map of solver names to their registrations.
    private static final Map<String, SolverRegistration<? extends I_Solver>> registrations;

    static {
        Map<String, SolverRegistration<? extends I_Solver>> regs = new HashMap<>();

        regs.put(PP_NAME, new SolverRegistration<>(
                PP_NAME,
                "Prioritised Planning - single rollout, no restarts",
                CanonicalSolversFactory::createPPSolver
        ));

        regs.put(PPt_NAME, new SolverRegistration<>(
                PPt_NAME,
                "Prioritised Planning with Transient MAPF - single rollout, no restarts",
                CanonicalSolversFactory::createPPtSolver
        ));

        regs.put(PP_SIPP_NAME, new SolverRegistration<>(
                PP_SIPP_NAME,
                "Prioritised Planning using SIPP - single rollout, no restarts",
                CanonicalSolversFactory::createPPSIPPSolver
        ));

        regs.put(PP_SIPPt_NAME, new SolverRegistration<>(
                PP_SIPPt_NAME,
                "Prioritised Planning using SIPP with Transient MAPF - single rollout, no restarts",
                CanonicalSolversFactory::createPPSIPPtSolver
        ));

        regs.put(PP_RR_ANYTIME_NAME, new SolverRegistration<>(
                PP_RR_ANYTIME_NAME,
                "Prioritised Planning - infinite random ordering restarts",
                CanonicalSolversFactory::createPPRRAnytimeSolver
        ));

        regs.put(PPt_RR_ANYTIME_NAME, new SolverRegistration<>(
                PPt_RR_ANYTIME_NAME,
                "Prioritised Planning with Transient MAPF - infinite random ordering restarts",
                CanonicalSolversFactory::createPPtRRAnytimeSolver
        ));

        regs.put(PP_SIPP_RR_ANYTIME_NAME, new SolverRegistration<>(
                PP_SIPP_RR_ANYTIME_NAME,
                "Prioritised Planning using SIPP - infinite random ordering restarts",
                CanonicalSolversFactory::createPPSIPPRRAnytimeSolver
        ));

        regs.put(PPt_SIPP_RR_ANYTIME_NAME, new SolverRegistration<>(
                PPt_SIPP_RR_ANYTIME_NAME,
                "Prioritised Planning using SIPP with Transient MAPF - infinite random ordering restarts",
                CanonicalSolversFactory::createPPtSIPPRRAnytimeSolver
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

        regs.put(CBS_SIPP_NAME, new SolverRegistration<>(
                CBS_SIPP_NAME,
                "Conflict Based Search using SIPP",
                CanonicalSolversFactory::createCBS_SIPPSolver
        ));

        regs.put(CBSt_NAME, new SolverRegistration<>(
                CBSt_NAME,
                "Conflict Based Search with Transient MAPF",
                CanonicalSolversFactory::createCBStSolver
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

        regs.put(LNS1_SIPP_NAME, new SolverRegistration<>(
                LNS1_SIPP_NAME,
                "Large Neighborhood Search 1 using SIPP",
                CanonicalSolversFactory::createLNS1SIPPSolver
        ));

        regs.put(LNS1_SIPPt_NAME, new SolverRegistration<>(
                LNS1_SIPPt_NAME,
                "Large Neighborhood Search 1 using SIPPt with Transient MAPF",
                CanonicalSolversFactory::createLNS1SIPPtSolver
        ));

        regs.put(LNS1t_NAME, new SolverRegistration<>(
                LNS1t_NAME,
                "Large Neighborhood Search 1 with Transient MAPF",
                CanonicalSolversFactory::createLNS1tSolver
        ));

        regs.put(LNS2_NAME, new SolverRegistration<>(
                LNS2_NAME,
                "Large Neighborhood Search 2",
                CanonicalSolversFactory::createLNS2Solver
        ));

        regs.put(LNS2t_NAME, new SolverRegistration<>(
                LNS2t_NAME,
                "Large Neighborhood Search 2 with Transient MAPF",
                CanonicalSolversFactory::createLNS2tSolver
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

        regs.put(ASTAR_NAME, new SolverRegistration<>(
                ASTAR_NAME,
                "A*",
                CanonicalSolversFactory::createAStarSolver
        ));

        regs.put(SIPP_NAME, new SolverRegistration<>(
                SIPP_NAME,
                "Safe Interval Path Planning",
                CanonicalSolversFactory::createSIPPSolver
        ));

        regs.put(SIPPS_NAME, new SolverRegistration<>(
                SIPPS_NAME,
                " Safe Interval Path Planning with Soft constraints",
                CanonicalSolversFactory::createSIPPSSolver
        ));

        regs.put(PIE_NAME, new SolverRegistration<>(
                PIE_NAME,
                "Planning and Improving while Executing",
                CanonicalSolversFactory::createPIESolver
        ));

        regs.put(PIEt_NAME, new SolverRegistration<>(
                PIEt_NAME,
                "Planning and Improving while Executing with Transient MAPF",
                CanonicalSolversFactory::createPIEtSolver
        ));

        regs.put(PIE_SIPP_NAME, new SolverRegistration<>(
                PIE_SIPP_NAME,
                "Planning and Improving while Executing using SIPP",
                CanonicalSolversFactory::createPIESIPPSolver
        ));

        regs.put(PIE_SIPPt_NAME, new SolverRegistration<>(
                PIE_SIPPt_NAME,
                "Planning and Improving while Executing using SIPP with Transient MAPF",
                CanonicalSolversFactory::createPIESIPPtSolver
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

    public static PrioritisedPlanning_Solver createPPtSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, new SumServiceTimes(),
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, TransientMAPFSettings.defaultTransientMAPF);
    }

    public static PrioritisedPlanning_Solver createPPSIPPSolver() {
        return new PrioritisedPlanning_Solver(
                createSIPPSolver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPSIPPtSolver() {
        return new PrioritisedPlanning_Solver(
                createSIPPSolver(), null, new SumServiceTimes(),
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, TransientMAPFSettings.defaultTransientMAPF);
    }

    public static PrioritisedPlanning_Solver createPPRRAnytimeSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPtRRAnytimeSolver() {
        return new PrioritisedPlanning_Solver(
                null, null, new SumServiceTimes(),
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, TransientMAPFSettings.defaultTransientMAPF);
    }

    public static PrioritisedPlanning_Solver createPPSIPPRRAnytimeSolver() {
        return new PrioritisedPlanning_Solver(
                createSIPPSolver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
    }

    public static PrioritisedPlanning_Solver createPPtSIPPRRAnytimeSolver() {
        return new PrioritisedPlanning_Solver(
                createSIPPSolver(), null, new SumServiceTimes(),
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, null),
                null, null, TransientMAPFSettings.defaultTransientMAPF);
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
        return new PrioritisedPlanning_Solver(
                null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, Integer.MAX_VALUE,
                        RestartsStrategy.reorderingStrategy.none, true),
                null, null, null);
    }

    public static CBS_Solver createCBSSolver() {
        return new CBSBuilder().createCBS_Solver();
    }

    public static CBS_Solver createCBS_SIPPSolver() {
        return new CBSBuilder().setLowLevelSolver(new SingleAgentAStarSIPP_Solver()).createCBS_Solver();
    }

    public static CBS_Solver createCBStSolver() {
        return new CBSBuilder().setCostFunction(new SumServiceTimes()).setTransientMAPFSettings(TransientMAPFSettings.defaultTransientMAPF).createCBS_Solver();
    }

    public static ICTS_Solver createICTSSolver() {
        return new ICTS_Solver();
    }

    public static PIBT_Solver createPIBTSolver() {
        return new PIBT_Solver(null, null, TransientMAPFSettings.defaultRegularMAPF);
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

    public static LargeNeighborhoodSearch_Solver createLNS1tSolver() {
        PrioritisedPlanning_Solver initialSolver = new PrioritisedPlanning_Solver(new SingleAgentAStarSIPP_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF);
        return new LNSBuilder().setInitialSolver(initialSolver).setIterationsSolver(createPPtSolver()).setSolutionCostFunction(new SumServiceTimes()).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLNS();
    }

    public static LargeNeighborhoodSearch_Solver createLNS1SIPPSolver() {
        PrioritisedPlanning_Solver initialSolver = new PrioritisedPlanning_Solver(new SingleAgentAStarSIPP_Solver(), null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null);
        return new LNSBuilder().setInitialSolver(initialSolver).setIterationsSolver(createPPSIPPSolver()).createLNS();
    }

    public static LargeNeighborhoodSearch_Solver createLNS1SIPPtSolver() {
        PrioritisedPlanning_Solver initialSolverTransient = new PrioritisedPlanning_Solver(new SingleAgentAStarSIPP_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF);
        return new LNSBuilder().setInitialSolver(initialSolverTransient).setIterationsSolver(createPPSIPPtSolver()).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).setSolutionCostFunction(new SumServiceTimes()).createLNS();
    }

    public static LargeNeighborhoodSearch_Solver createLNS2Solver() {
        return new LNSBuilder().setInitialSolver(new solutionsGeneratorForLNS2()).setIterationsSolver(new solutionsGeneratorForLNS2()).setSolutionCostFunction(new ConflictsCount(false, false)).setLNS2(true).createLNS();
    }

    public static LargeNeighborhoodSearch_Solver createLNS2tSolver() {
        return new LNSBuilder().setInitialSolver(new solutionsGeneratorForLNS2(null, TransientMAPFSettings.defaultTransientMAPF, null, null, null))
                .setIterationsSolver(new solutionsGeneratorForLNS2(null, TransientMAPFSettings.defaultTransientMAPF, null, null, null))
                .setSolutionCostFunction(new ConflictsCount(false, false)).setLNS2(true).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLNS();
    }

    public static PriorityConstrainedSearch createPCSSolver() {
        return new PCSBuilder().createPCS();
    }

    public static PriorityConstrainedSearch createPCSLexicalSolver() {
        return new PCSBuilder().setNodeComparator(PCSCompLexical.DEFAULT_INSTANCE).createPCS();
    }

    public static SingleAgentAStar_Solver createAStarSolver() {
        return new SingleAgentAStar_Solver();
    }

    public static SingleAgentAStarSIPP_Solver createSIPPSolver() {
        return new SingleAgentAStarSIPP_Solver();
    }

    public static SingleAgentAStarSIPP_Solver createSIPPSSolver() {
        return new SingleAgentAStarSIPPS_Solver();
    }

    public static I_Solver createPIESolver() {
        return new LNSBuilder().setInitialSolver(CanonicalSolversFactory.createLaCAMSolver()).setIterationsSolver(CanonicalSolversFactory.createPPSolver()).createLNS();
    }

    public static I_Solver createPIEtSolver() {
        return new LNSBuilder().setInitialSolver(CanonicalSolversFactory.createLaCAMtSolver()).setIterationsSolver(CanonicalSolversFactory.createPPtSolver())
                .setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).setSolutionCostFunction(new SumServiceTimes()).createLNS();
    }

    public static I_Solver createPIESIPPSolver() {
        return new LNSBuilder().setInitialSolver(CanonicalSolversFactory.createLaCAMSolver()).setIterationsSolver(CanonicalSolversFactory.createPPSIPPSolver()).createLNS();
    }

    public static I_Solver createPIESIPPtSolver() {
        return new LNSBuilder().setInitialSolver(CanonicalSolversFactory.createLaCAMtSolver()).setIterationsSolver(CanonicalSolversFactory.createPPSIPPtSolver())
                .setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).setSolutionCostFunction(new SumServiceTimes()).createLNS();
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
