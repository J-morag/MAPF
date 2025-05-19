package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.TestConstants.Instances;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CanonicalSolversFactoryTest {

    public static final int TIMEOUT = 5000;
    private final MAPF_Instance testInstance = Instances.instanceCircle1;

    @Test
    void testCreateSolver() {
        I_Solver solver = CanonicalSolversFactory.createSolver(CanonicalSolversFactory.PP_NAME);
        assertNotNull(solver);
        assertEquals(CanonicalSolversFactory.PP_NAME, solver.getName());
    }

    @Test
    void testGetDescription() {
        String description = CanonicalSolversFactory.getDescription(CanonicalSolversFactory.PP_NAME);
        assertNotNull(description);
        assertEquals("Prioritised Planning - single rollout, no restarts", description);
    }

    @Test
    void testCreateUnknownSolver() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CanonicalSolversFactory.createSolver("UNKNOWN");
        });
        assertEquals("Unknown solver name: UNKNOWN", exception.getMessage());
    }

    @Test
    void testGetDescriptionUnknownSolver() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            CanonicalSolversFactory.getDescription("UNKNOWN");
        });
        assertEquals("Unknown solver name: UNKNOWN", exception.getMessage());
    }

    @Test
    void testAllSolversOnEasyInstance() {
        for (String solverName : CanonicalSolversFactory.getSolverNames()) {
            System.out.println("Testing solver: " + solverName);
            I_Solver solver = CanonicalSolversFactory.createSolver(solverName);
            RunParameters parameters = new RunParametersBuilder().setTimeout(TIMEOUT).createRP();
            Solution solution = solver.solve(testInstance, parameters);
            assertNotNull(solution, "Solution should not be null for solver: " + solverName);
            assertTrue(solution.isValidSolution(), "Solution should be valid for solver: " + solverName);
        }
    }
}