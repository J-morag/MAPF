package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.ConflictsCount;
import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import TransientMAPF.TransientMAPFSettings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Coordiantes.coor02;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Instances.instanceUnsolvable;
import static BasicMAPF.TestConstants.Maps.mapCorridor;
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

    @Test
    void corridorSolvedOnlyByTransient() {
        Agent agent1 = new Agent(0, coor00, coor03, 1);
        Agent agent2 = new Agent(1, coor01, coor02, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapCorridor, new Agent[]{agent1, agent2});

        I_Solver LNS2t = new LNSBuilder().setInitialSolver(new solutionsGeneratorForLNS2(null, TransientMAPFSettings.defaultTransientMAPF, null, null, null))
                .setIterationsSolver(new solutionsGeneratorForLNS2(null, TransientMAPFSettings.defaultTransientMAPF, null, null, null))
                .setSolutionCostFunction(new ConflictsCount(false, false)).setLNS2(true).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLNS();
        Solution solveByLNS2t = LNS2t.solve(testInstance, new RunParametersBuilder().setAStarGAndH(new ServiceTimeGAndH(new DistanceTableSingleAgentHeuristic(testInstance.agents, testInstance.map))).setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solveByLNS2t.solves(testInstance));
        System.out.println(solveByLNS2t);

        Solution solvedByLNS2 = solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertNull(solvedByLNS2);
    }
}
