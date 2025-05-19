package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.ConflictsCount;
import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;

import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Instances.instanceUnsolvable;
import static org.junit.jupiter.api.Assertions.*;

public class LargeNeighborhoodSearch2_SolverTest {

    I_Solver solver = CanonicalSolversFactory.createLNS2Solver();

    InstanceReport instanceReport;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }

    @NotNull
    private RunParameters getDefaultRunParameters() {
        return new RunParametersBuilder().setTimeout(3000).setSoftTimeout(500).setInstanceReport(instanceReport).createRP();
    }

    @Test
    void testValidHeuristicsForLNS2() {
        I_SolutionCostFunction costFunction = new ConflictsCount(false, false);
        ArrayList<I_DestroyHeuristic> heuristics = new ArrayList<>();
        heuristics.add(new RandomDestroyHeuristic());
        heuristics.add(new CollisionBasedDestroyHeuristic());
        heuristics.add(new FailureBasedDestroyHeuristic());
        assertDoesNotThrow(() -> new LNSBuilder().setSolutionCostFunction(costFunction).setDestroyHeuristics(heuristics).setLNS2(true).createLNS());
    }

    @Test
    void testInvalidHeuristicsForLNS2() {
        I_SolutionCostFunction costFunction = new ConflictsCount(false, false);
        ArrayList<I_DestroyHeuristic> heuristics = new ArrayList<>();
        heuristics.add(new MapBasedDestroyHeuristic());
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () ->new LNSBuilder().setSolutionCostFunction(costFunction).setDestroyHeuristics(heuristics).setLNS2(true).createLNS());
        assertTrue(exception.getMessage().contains("Invalid destroy heuristic"));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = solver.solve(testInstance, getDefaultRunParameters());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = solver.solve(testInstance, getDefaultRunParameters());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, getDefaultRunParameters());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void compareBetweenLNS1AndLNS2Test(){
        I_Solver LNS1Solver = CanonicalSolversFactory.createLNS1Solver();
        String nameLNS1 = LNS1Solver.getName();

        I_Solver LNS2Solver = CanonicalSolversFactory.createLNS2Solver();
        String nameLNS2 = LNS2Solver.getName();

        TestUtils.comparativeTest(LNS1Solver, nameLNS1, false, false, LNS2Solver, nameLNS2,
                // "lie" that LNS2 is complete because we expect LNS2 to solve every instance that LNS1 solves
                false, true, new int[]{100}, 10, 0);
    }

    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(solver, 5, false, false);
    }
}
