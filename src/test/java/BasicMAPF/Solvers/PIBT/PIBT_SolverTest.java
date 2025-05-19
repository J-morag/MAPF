package BasicMAPF.Solvers.PIBT;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Coordiantes.coor12;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Instances.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PIBT_SolverTest {

    private final MAPF_Instance instanceEmpty = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
    private final MAPF_Instance instanceAgentsInterruptsEachOther = new MAPF_Instance("instanceAgentsInterruptsEachOther", mapWithPocket, new Agent[]{agent43to53, agent55to34});

    private final MAPF_Instance instanceMultipleInheritance = new MAPF_Instance("instanceMultipleInheritance", mapHLong, new Agent[]{agent00to13, agent10to33, agent20to00, agent21to00});
    private final MAPF_Instance instanceAgentsNeedsToSwapLocations = new MAPF_Instance("instanceAgentsNeedsToSwapLocations", mapWithPocket, new Agent[]{agent55to34, agent54to55});

    I_Solver PIBTt_Solver = CanonicalSolversFactory.createPIBTtSolver();
    I_Solver PIBT_Solver = CanonicalSolversFactory.createPIBTSolver();

    long timeout = 10*1000;

    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }


    @Test
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(24, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(22 , solved.sumServiceTimes());
    }

    @Test
    void emptyMapAgentsWithTheSameGoal() {
        MAPF_Instance testInstance = instanceEmptySameTarget;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(8, solved.sumIndividualCosts());
        assertEquals(4, solved.makespan());
        assertEquals(5 , solved.sumServiceTimes());

        Solution solvedByPIBT = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertNull(solvedByPIBT);
    }

    @Test
    void emptyMapValidityTest2() {
        MAPF_Instance testInstance = instanceEmpty2;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(25, solved.sumIndividualCosts());
        assertEquals(6, solved.makespan());
        assertEquals(23 , solved.sumServiceTimes());
    }

    @Test
    void emptyMapHarderValidityTest1() {
        MAPF_Instance testInstance = instanceEmptyHarder;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(10 , solved.sumServiceTimes());
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solvedByPIBTt = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedByPIBTt);
        assertTrue(solvedByPIBTt.solves(testInstance));
        assertEquals(14, solvedByPIBTt.sumIndividualCosts());
        assertEquals(7, solvedByPIBTt.makespan());
        assertEquals(10 , solvedByPIBTt.sumServiceTimes());

        Solution solvedByPIBT = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedByPIBT);
        assertTrue(solvedByPIBT.solves(testInstance));
        assertEquals(18, solvedByPIBT.sumIndividualCosts());
        assertEquals(11, solvedByPIBT.makespan());
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void instanceAgentsInterruptsEachOtherTest() {
        MAPF_Instance testInstance = instanceAgentsInterruptsEachOther;
        Solution solvedByPIBTt = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedByPIBTt);
        assertTrue(solvedByPIBTt.solves(testInstance));
        assertEquals(10, solvedByPIBTt.sumIndividualCosts());
        assertEquals(5, solvedByPIBTt.makespan());
        assertEquals(6 , solvedByPIBTt.sumServiceTimes());

        Solution solvedByPIBT = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedByPIBT);
        assertTrue(solvedByPIBT.solves(testInstance));
        assertEquals(12, solvedByPIBT.sumIndividualCosts());
        assertEquals(7, solvedByPIBT.makespan());
    }


    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(PIBTt_Solver, 5, false, false);
    }

    @Test
    void compareBetweenPrPAndPIBTTest(){
        I_Solver PrPSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null);
        String namePrP = PrPSolver.getName();

        I_Solver PIBT_Solver = new PIBT_Solver(null, Integer.MAX_VALUE, null);
        String namePIBT = PIBT_Solver.getName();

        TestUtils.comparativeTest(PrPSolver, namePrP, false, false, PIBT_Solver, namePIBT,
                false, false, new int[]{100}, 10, 0);
    }


    @Test
    void unsolvableMultipleInheritanceTest() {
        MAPF_Instance testInstance = instanceMultipleInheritance;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        assertNull(solved);
    }

    @Test
    void unsolvableLoopDetection() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        assertNull(solved);
    }

    @Test
    void emptyMapValidityWithEasyConstraint() {
        MAPF_Instance testInstance = instanceEmptyEasy;
        I_Coordinate coor13 = new Coordinate_2D(1,3);
        I_Coordinate coor02 = new Coordinate_2D(0,2);
        Constraint constraint1 = new Constraint(agent33to12, 2, mapEmpty.getMapLocation(coor13));
        Constraint constraint2 = new Constraint(agent04to00, 2, mapEmpty.getMapLocation(coor02));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);
        constraints.add(constraint2);
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());
        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(8, solved.sumIndividualCosts());
        assertEquals(5, solved.makespan());
        assertEquals(8 , solved.sumServiceTimes());
    }

    // the following test important to check specific scenario where agent reached his goal,
    // but can't stay in place since there is a constraint
    // so, PIBT(agent, null) should find different node for this agent
    @Test
    void emptyMapValidityStayInPlaceConstraint() {
        MAPF_Instance testInstance = instanceEmpty;

        I_Coordinate coor33 = new Coordinate_2D(3,3);
        Constraint constraint1 = new Constraint(agent12to33, 5, mapEmpty.getMapLocation(coor33));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);

        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(27, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(22 , solved.sumServiceTimes());
    }


    @Test
    void emptyMapValidityInfiniteConstraintTest() {
        MAPF_Instance testInstance = instanceEmpty;
        I_Coordinate coor02 = new Coordinate_2D(1,2);
        Constraint constraint1 = new Constraint(agent33to12, 10, mapEmpty.getMapLocation(coor02));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);

        Solution solvedByPIBTt = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedByPIBTt);
        assertTrue(solvedByPIBTt.solves(testInstance));
        assertEquals(29, solvedByPIBTt.sumIndividualCosts());
        assertEquals(10, solvedByPIBTt.makespan());
        assertEquals(22 , solvedByPIBTt.sumServiceTimes());

        Solution solvedByPIBT = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());
        System.out.println(solvedByPIBT);
        assertTrue(solvedByPIBT.solves(testInstance));
        assertEquals(30, solvedByPIBT.sumIndividualCosts());
        assertEquals(11, solvedByPIBT.makespan());
    }

    @Test
    void emptyMapValidityInfiniteConstraintTestBothAgents() {
        MAPF_Instance testInstance = instanceCircle1;

        I_Coordinate coor33 = new Coordinate_2D(3,3);
        I_Coordinate coor12 = new Coordinate_2D(1,2);
        Constraint constraint1 = new Constraint(agent12to33, 10, mapCircle.getMapLocation(coor33));
        Constraint constraint2 = new Constraint(agent33to12, 10, mapCircle.getMapLocation(coor12));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);
        constraints.add(constraint2);

        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));

        assertEquals(20, solved.sumIndividualCosts());
        assertEquals(10, solved.makespan());
        assertEquals(10 , solved.sumServiceTimes());
    }

    @Test
    void agentsNeedToSwapReturnNullTest() {
        MAPF_Instance testInstance = instanceAgentsNeedsToSwapLocations;
        Solution solved = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        assertNull(solved);
    }

    @Test
    void transientExample() {
        Agent agent1 = new Agent(0, coor10, coor13, 1);
        Agent agent2 = new Agent(1, coor11, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", transientExampleMap, new Agent[]{agent1, agent2});

        Solution solvedByPIBTt = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedByPIBTt.solves(testInstance));
        System.out.println(solvedByPIBTt);
    }

    @Test
    void corridorSolvedOnlyByTransient() {
        Agent agent1 = new Agent(0, coor00, coor03, 1);
        Agent agent2 = new Agent(1, coor01, coor02, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapCorridor, new Agent[]{agent1, agent2});

        Solution solvedByPIBTt = PIBTt_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedByPIBTt.solves(testInstance));
        System.out.println(solvedByPIBTt);

        Solution solvedByPIBT = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertNull(solvedByPIBT);
    }
}
