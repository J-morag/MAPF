package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.ConflictsCount;
import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.MapFactory;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
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

import static BasicMAPF.TestConstants.Coordinates.*;
import static BasicMAPF.TestConstants.Coordinates.coor02;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Maps.mapCorridor;
import static BasicMAPF.TestConstants.Maps.mapEmpty;
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

    @Test
    void testLNS2tTransientSourceEqualsTargetWithCompetingAgentAtTime1() {
        // Agent 0 starts and ends at coor14
        Agent agent0 = new Agent(0, coor14, coor14);
        // Agent 1 starts at coor13 and targets coor15 (must pass through coor14 at t=1)
        Agent agent1 = new Agent(1, coor13, new BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D(1,5));
        MAPF_Instance testInstance = new MAPF_Instance("LNS2t source equals target with competing agent", mapEmpty, new Agent[]{agent0, agent1});

        Solution solved = CanonicalSolversFactory.createLNS2tSolver().solve(testInstance, new RunParametersBuilder().setInstanceReport(Metrics.newInstanceReport()).createRP());
        assertNotNull(solved, "LNS2t did not return a solution");

        System.out.println("Solution found:\n" + solved);
        // Agent 0: visited at time 0, so plan has size 1, and contributes 0 cost to SST
        assertEquals(1, solved.getPlanFor(agent0).size());
        assertEquals(coor14, solved.getPlanFor(agent0).getFirstMove().prevLocation.getCoordinate());
        // agent 1 competes for coor14 at time 1, so agent 0 should clear it at time 1, to reduce solution cost
        assertNotEquals(coor14, solved.getPlanFor(agent0).getFirstMove().currLocation.getCoordinate());
        // Solution should be valid
        assertTrue(solved.isValidSolution());
        assertTrue(solved.solves(testInstance));
        // agent 1 should have cost 2 (move to coor14 at t=1, then to coor15 at t=2)
        assertEquals(2, solved.getPlanFor(agent1).getCost());
        // agent 1 should have two moves
        assertEquals(2, solved.getPlanFor(agent1).size());
        // total SST should be 2
        assertEquals(2, solved.sumServiceTimes());
    }

    @Test
    void testTwoAgentsCrossingPaths_transient() {
        // 3x3 grid, agents start at opposite corners, targets at the other corner
        Enum_MapLocationType E = Enum_MapLocationType.EMPTY;
        I_ExplicitMap map3x3 = MapFactory.newSimple4Connected2D_GraphMap(new Enum_MapLocationType[][]{
                {E, E, E},
                {E, E, E},
                {E, E, E}
        });
        Agent agentA = new Agent(0, new Coordinate_2D(0,0), new Coordinate_2D(2,2));
        Agent agentB = new Agent(1, new Coordinate_2D(2,2), new Coordinate_2D(0,0));
        MAPF_Instance instance = new MAPF_Instance("crossingPaths", map3x3, new Agent[]{agentA, agentB});
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agentA, 5, null, map3x3.getMapLocation(new Coordinate_2D(2,2))));
        constraints.add(new Constraint(agentB, 5, null, map3x3.getMapLocation(new Coordinate_2D(0,0))));
        RunParameters params = new RunParametersBuilder()
                .setTimeout(2000)
                .setConstraints(constraints)
                .createRP();
        I_Solver solver = CanonicalSolversFactory.createLNS2tSolver();
        Solution sol = solver.solve(instance, params);
        SingleAgentPlan planA = sol.getPlanFor(agentA);
        SingleAgentPlan planB = sol.getPlanFor(agentB);
        assertNotNull(planA, "PlanA should not be null");
        assertNotNull(planB, "PlanB should not be null");
        assertTrue(planA.firstVisitToTargetTime() < 5, "AgentA should reach target before t=5");
        assertTrue(planB.firstVisitToTargetTime() < 5, "AgentB should reach target before t=5");
        int visitsA = 0, visitsB = 0;
        for (var move : planA) if (move.currLocation.getCoordinate().equals(agentA.target)) visitsA++;
        for (var move : planB) if (move.currLocation.getCoordinate().equals(agentB.target)) visitsB++;
        assertEquals(1, visitsA, "AgentA should visit target only once");
        assertEquals(1, visitsB, "AgentB should visit target only once");
    }

    @Test
    void testThreeAgentsComplexMap_transient() {
        // 4x2 grid, three agents, some constraints to create potential for forced revisits
        Enum_MapLocationType E = Enum_MapLocationType.EMPTY;
        I_ExplicitMap map4x2 = MapFactory.newSimple4Connected2D_GraphMap(new Enum_MapLocationType[][]{
                {E, E},
                {E, E},
                {E, E},
                {E, E}
        });
        Agent agentA = new Agent(0, new Coordinate_2D(0,0), new Coordinate_2D(3,1));
        Agent agentB = new Agent(1, new Coordinate_2D(3,0), new Coordinate_2D(0,1));
        Agent agentC = new Agent(2, new Coordinate_2D(1,1), new Coordinate_2D(2,0));
        MAPF_Instance instance = new MAPF_Instance("threeAgentsComplex", map4x2, new Agent[]{agentA, agentB, agentC});
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agentA, 6, null, map4x2.getMapLocation(new Coordinate_2D(3,1))));
        constraints.add(new Constraint(agentB, 6, null, map4x2.getMapLocation(new Coordinate_2D(0,1))));
        constraints.add(new Constraint(agentC, 5, null, map4x2.getMapLocation(new Coordinate_2D(2,0))));
        RunParameters params = new RunParametersBuilder()
                .setTimeout(3000)
                .setConstraints(constraints)
                .createRP();
        I_Solver solver = CanonicalSolversFactory.createLNS2tSolver();
        Solution sol = solver.solve(instance, params);
        SingleAgentPlan planA = sol.getPlanFor(agentA);
        SingleAgentPlan planB = sol.getPlanFor(agentB);
        SingleAgentPlan planC = sol.getPlanFor(agentC);
        assertNotNull(planA, "PlanA should not be null");
        assertNotNull(planB, "PlanB should not be null");
        assertNotNull(planC, "PlanC should not be null");
        assertTrue(planA.firstVisitToTargetTime() < 6, "AgentA should reach target before t=6");
        assertTrue(planB.firstVisitToTargetTime() < 6, "AgentB should reach target before t=6");
        assertTrue(planC.firstVisitToTargetTime() < 5, "AgentC should reach target before t=5");
        int visitsA = 0, visitsB = 0, visitsC = 0;
        for (var move : planA) if (move.currLocation.getCoordinate().equals(agentA.target)) visitsA++;
        for (var move : planB) if (move.currLocation.getCoordinate().equals(agentB.target)) visitsB++;
        for (var move : planC) if (move.currLocation.getCoordinate().equals(agentC.target)) visitsC++;
        assertEquals(1, visitsA, "AgentA should visit target only once");
        assertEquals(1, visitsB, "AgentB should visit target only once");
        assertEquals(1, visitsC, "AgentC should visit target only once");
    }
}
