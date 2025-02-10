package BasicMAPF.Solvers;

import BasicMAPF.Solvers.CBS.CBSBuilder;
import java.util.function.Supplier;

import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.LaCAM.LaCAMBuilder;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LNSBuilder;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSBuilder;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSCompLexical;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CanonicalSolversFactory {
    public final static String PP_NAME = "PP";
    public final static String PP_RR_ANYTIME_NAME = "PP_RR";
    public final static String PP_RR_UNTIL_FIRST_SOLUTION_NAME = "PP_RR_UntilFirstSolution";
    public final static String PP_DR_UNTIL_FIRST_SOLUTION_NAME = "PP_DR_UntilFirstSolution";
    public final static String CBS_NAME = "CBS";
    public final static String ICTS_NAME = "ICTS";
    public final static String PIBT_NAME = "PIBT";
    public final static String LACAM_NAME = "LACAM";
    public final static String LNS1_NAME = "LNS1";
    public final static String PCS_NAME = "PCS";
    public final static String PCS_LEXICAL_NAME = "PCS_Lexical";


    // A map of solver names to their registrations.
    private static final Map<String, SolverRegistration<? extends I_Solver>> registrations;

    static {
        Map<String, SolverRegistration<? extends I_Solver>> regs = new HashMap<>();

        // Register a basic Prioritised Planning solver.
        regs.put(PP_NAME, new SolverRegistration<>(
                PP_NAME,
                "Prioritised Planning - single rollout, no restarts",
                () -> new PrioritisedPlanning_Solver(
                        null, null, null,
                        new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.none, null),
                        null, null, null)
        ));

        // Register an anytime Prioritised Planning solver with infinite random ordering restarts.
        regs.put(PP_RR_ANYTIME_NAME, new SolverRegistration<>(
                PP_RR_ANYTIME_NAME,
                "Prioritised Planning - infinite random ordering restarts",
                () -> new PrioritisedPlanning_Solver(
                        null, null, null,
                        new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, Integer.MAX_VALUE, RestartsStrategy.reorderingStrategy.none, null),
                        null, null, null)
        ));

        // Register Prioritised Planning with random ordering restarts only until a first solution is found.
        regs.put(PP_RR_UNTIL_FIRST_SOLUTION_NAME, new SolverRegistration<>(
                PP_RR_UNTIL_FIRST_SOLUTION_NAME,
                "Prioritised Planning - random ordering restarts until first solution",
                () -> new PrioritisedPlanning_Solver(
                        null, null, null,
                        new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, null, RestartsStrategy.reorderingStrategy.randomRestarts, null),
                        null, null, null)
        ));

        // Register Prioritised Planning with deterministic restarts only until a first solution is found.
        regs.put(PP_DR_UNTIL_FIRST_SOLUTION_NAME, new SolverRegistration<>(
                PP_DR_UNTIL_FIRST_SOLUTION_NAME,
                "Prioritised Planning - deterministic rescheduling until first solution",
                () -> new PrioritisedPlanning_Solver(
                        null, null, null,
                        new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, null, RestartsStrategy.reorderingStrategy.deterministicRescheduling, null),
                        null, null, null)
        ));

        // Register a basic CBS solver.
        regs.put(CBS_NAME, new SolverRegistration<>(
                CBS_NAME,
                "Conflict Based Search",
                () -> new CBSBuilder().createCBS_Solver()
        ));

        // Register a basic ICTS solver.
        regs.put(ICTS_NAME, new SolverRegistration<>(
                ICTS_NAME,
                "Increasing Cost Tree Search",
                ICTS_Solver::new
        ));

        // Register a basic PIBT solver.
        regs.put(PIBT_NAME, new SolverRegistration<>(
                PIBT_NAME,
                "Priority Inheritance with Backtracking",
                PIBT_Solver::new
        ));

        // Register a basic LACAM solver.
        regs.put(LACAM_NAME, new SolverRegistration<>(
                LACAM_NAME,
                "Lazy Constraints Addition Search",
                () -> new LaCAMBuilder().createLaCAM()
        ));

        // Register a basic LNS1 solver.
        regs.put(LNS1_NAME, new SolverRegistration<>(
                LNS1_NAME,
                "Large Neighborhood Search 1",
                () -> new LNSBuilder().createLNS()
        ));

        // Register a basic PCS solver.
        regs.put(PCS_NAME, new SolverRegistration<>(
                PCS_NAME,
                "Priority Constrained Search",
                () -> new PCSBuilder().createPCS()
        ));

        // Register a basic PCS solver with a Lexical cost function (OPEN list ordering).
        regs.put(PCS_LEXICAL_NAME, new SolverRegistration<>(
                PCS_LEXICAL_NAME,
                "Priority Constrained Search with Lexical cost function",
                () -> new PCSBuilder().setNodeComparator(PCSCompLexical.DEFAULT_INSTANCE).createPCS()
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
     * Get the names of all registered solvers.
     */
    public static Iterable<String> getSolverNames() {
        return registrations.keySet();
    }

    private record SolverRegistration<T extends I_Solver>(String name, String description, Supplier<T> creator) {
        public T create() {
            T solver = creator.get();
            solver.setName(name);
            solver.setDescription(description);
            return solver;
        }
    }
}
