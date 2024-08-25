package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import TransientMAPF.TransientMAPFSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static org.junit.jupiter.api.Assertions.*;

public class LaCAM_SolverTest {

    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});

    private final MAPF_Instance exampleInstance = new MAPF_Instance("exampleInstance", mapTwoWallsSmall, new Agent[]{agent00to02, agent02to00});
    private final MAPF_Instance instanceEmptyEasy = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent04to00});
    private final MAPF_Instance instanceEmptyHarder = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, agent00to10, agent55to34, agent34to32, agent31to14, agent40to02});

    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceAgentsInterruptsEachOther = new MAPF_Instance("instanceAgentsInterruptsEachOther", mapWithPocket, new Agent[]{agent43to53, agent55to34});
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});
    private final MAPF_Instance instanceAgentsNeedsToSwapLocations = new MAPF_Instance("instanceAgentsNeedsToSwapLocations", mapWithPocket, new Agent[]{agent55to34, agent54to55});

    // the next 6 instances from article about LaCAM
    private final MAPF_Instance instanceTreeShapedMap = new MAPF_Instance("instanceTreeShapedMap", mapTree, new Agent[]{agent31to01, agent11to31, agent01to11});
    private final MAPF_Instance instanceCornersShapedMap = new MAPF_Instance("instanceTreeShapedMap", mapCorners, new Agent[]{agent00to44, agent10to34, agent34to10, agent44to00});

    private final MAPF_Instance instanceTunnelShapedMap = new MAPF_Instance("instanceTreeShapedMap", mapTunnel, new Agent[]{agent50to20, agent40to30, agent30to40, agent10to50});
    private final MAPF_Instance instanceStringShapedMap = new MAPF_Instance("instanceStringShapedMap", mapString, new Agent[]{agent50to42, agent42to30, agent30to22, agent22to10, agent10to50});

    private final MAPF_Instance instanceLoopChainShapedMap = new MAPF_Instance("instanceLoopChainShapedMap", mapLoopChain, new Agent[]{agent22to20, agent12to10, agent02to00, agent01to01, agent00to02, agent10to12,agent20to22});

    private final MAPF_Instance instanceConnectorShapedMap = new MAPF_Instance("instanceConnectorShapedMap", mapConnector, new Agent[]{agent00to65, agent65to00, agent10to33, agent55to32, agent01to44, agent64to22});

    I_Solver LaCAM_Solver = new LaCAM_Solver(null, TransientMAPFSettings.defaultRegularMAPF);
    I_Solver LaCAMSt_Solver = new LaCAM_Solver(null, TransientMAPFSettings.defaultTransientMAPF);

    long timeout = 10*1000;

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

    @Test
    void exampleTest() {
        MAPF_Instance testInstance = exampleInstance;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }


    @Test
    void emptyMapValidityWithEasyConstraint() {
        MAPF_Instance testInstance = instanceEmptyEasy;
        I_Coordinate coor13 = new Coordinate_2D(1,3);
        I_Coordinate coor02 = new Coordinate_2D(0,2);
        Constraint constraint1 = new Constraint(agent33to12, 2, mapEmpty.getMapLocation(coor13));
        Constraint constraint2 = new Constraint(agent04to00, 2, mapEmpty.getMapLocation(coor02));
        Constraint constraint3 = new Constraint(agent33to12, 4, mapEmpty.getMapLocation(coor12));
        Constraint constraint4 = new Constraint(agent04to00, 4, mapEmpty.getMapLocation(coor00));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);
        constraints.add(constraint2);
        constraints.add(constraint3);
        constraints.add(constraint4);
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }


    @Test
    void exampleTestLaCAMStar() {
        MAPF_Instance testInstance = exampleInstance;
        Solution solved = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void emptyMapEasyNoConflictsTest() {
        MAPF_Instance testInstance = instanceEmptyEasy;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void emptyMapHarderValidityTest1() {
        MAPF_Instance testInstance = instanceEmptyHarder;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void instanceAgentsInterruptsEachOtherTest() {
        MAPF_Instance testInstance = instanceAgentsInterruptsEachOther;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void agentsNeedToSwapTest() {
        MAPF_Instance testInstance = instanceAgentsNeedsToSwapLocations;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void treeShapedMapTest() {
        MAPF_Instance testInstance = instanceTreeShapedMap;
        I_Solver pibt = new PIBT_Solver(null, null, null);
        Solution solvedPIBT = pibt.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertNull(solvedPIBT);
        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedStar = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedStar.solves(testInstance));
        System.out.println(solvedStar);
    }

    @Test
    void goalsInCornersMapTest() {
        MAPF_Instance testInstance = instanceCornersShapedMap;
        I_Solver pibt = new PIBT_Solver(null, null, null);
        Solution solvedPIBT = pibt.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertNull(solvedPIBT);
        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedStar = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedStar.solves(testInstance));
        System.out.println(solvedStar);
    }

    @Test
    void tunnelShapedMapTest() {
        MAPF_Instance testInstance = instanceTunnelShapedMap;
        I_Solver pibt = new PIBT_Solver(null, null, null);
        Solution solvedPIBT = pibt.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertNull(solvedPIBT);
        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedStar = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedStar.solves(testInstance));
        System.out.println(solvedStar);
    }

    @Test
    void stringShapedMapTest() {
        MAPF_Instance testInstance = instanceStringShapedMap;
        I_Solver pibt = new PIBT_Solver(null, null, null);
        Solution solvedPIBT = pibt.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertNull(solvedPIBT);
        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedStar = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedStar.solves(testInstance));
        System.out.println(solvedStar);
    }

    @Test
    void loopChainShapedMapTest() {
        MAPF_Instance testInstance = instanceLoopChainShapedMap;
        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedStar = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedStar.solves(testInstance));
        System.out.println(solvedStar);
    }

    @Test
    void connectorShapedMapTest() {
        MAPF_Instance testInstance = instanceConnectorShapedMap;
        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedStar = LaCAMSt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedStar.solves(testInstance));
        System.out.println(solvedStar);
    }


    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(LaCAM_Solver, 5, false, false);
    }

    @Test
    void compareBetweenPIBTAndLaCAMTest(){
        I_Solver LaCAMSolver = new LaCAM_Solver(null, null);
        String nameLaCAM = LaCAMSolver.name();

        I_Solver PIBT_Solver = new PIBT_Solver(null, null, null);
        String namePIBT = PIBT_Solver.name();

        TestUtils.comparativeTest(PIBT_Solver, namePIBT, false, LaCAMSolver, nameLaCAM,
                false, new int[]{100}, 5, 0);
    }

    @Test
    void worksWithTMAPFPaths() {
        I_Solver LaCAMt = new LaCAM_Solver(new SumServiceTimes(),  TransientMAPFSettings.defaultTransientMAPF);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentXMoving, agentYMoving});

        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedLaCAMt = LaCAMt.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedLaCAMt.solves(testInstance));

        System.out.println(solvedLaCAM);
        System.out.println(solvedLaCAMt);
    }

    @Test
    void transientExample() {
        I_Solver LaCAMt = new LaCAM_Solver(new SumServiceTimes(),  TransientMAPFSettings.defaultTransientMAPF);
        Agent agent1 = new Agent(0, coor10, coor13, 1);
        Agent agent2 = new Agent(1, coor11, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", transientExampleMap, new Agent[]{agent1, agent2});

        Solution solvedLaCAM = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedLaCAM.solves(testInstance));

        Solution solvedLaCAMt = LaCAMt.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedLaCAM);
        System.out.println(solvedLaCAMt);

        assertTrue(solvedLaCAMt.solves(testInstance));
    }

    @Test
    void compareBetweenLaCAMStarAndLaCAMTest(){
        I_Solver LaCAMSolver = new LaCAM_Solver(null, null);
        String nameLaCAM = LaCAMSolver.name();

        I_Solver LaCAMStar_Solver = new LaCAMStar_Solver(null, null);
        String nameLaCAMStar = LaCAMStar_Solver.name();

        TestUtils.comparativeTest(LaCAMSolver, nameLaCAM, false, LaCAMStar_Solver, nameLaCAMStar,
                false, new int[]{100}, 10, 0);
    }
}
