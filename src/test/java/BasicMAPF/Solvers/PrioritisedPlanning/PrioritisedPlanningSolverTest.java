package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.GoalConstraint;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.TestUtils;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.Solution;
import TransientMAPF.TransientMAPFSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver.COMPLETED_CONTINGENCY_ATTEMPTS_STR;
import static BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver.COMPLETED_INITIAL_ATTEMPTS_STR;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Instances.*;
import static org.junit.jupiter.api.Assertions.*;

class PrioritisedPlanningSolverTest {

    private final MAPF_Instance instanceUnsolvableBecauseOrderWithInfiniteWait = new MAPF_Instance("instanceUnsolvableWithInfiniteWait", mapWithPocket, new Agent[]{agent43to53, agent55to34});

    I_Solver ppSolver = CanonicalSolversFactory.createPPSolver();


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
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(8, solved.makespan());
    }

    @Test
    void failsBeforeTimeoutWhenFacedWithInfiniteConstraints() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        long timeout = 10*1000;
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
    }

    @Test
    void unsolvable() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertNull(solved);
    }

    @Test
    void failsBeforeTimeoutWithRandomInitialAndContingency() {
        MAPF_Instance testInstance = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00, agent55to34, agent43to53});
        PrioritisedPlanning_Solver solver = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 3, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null, null, null);
        solver.reportIndvAttempts = true;
        long timeout = 10*1000;
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(instanceReport);
        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm, exhausts the orderings search space)
        assertNull(solved);
        // should perform 3 + 21 attempts
        assertNotNull(instanceReport.getIntegerValue("attempt #2 time"));
        assertEquals(3, instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR));
        assertEquals(21, instanceReport.getIntegerValue(COMPLETED_CONTINGENCY_ATTEMPTS_STR));
    }

    @Test
    void failsBeforeTimeoutWithDeterministicInitialAndContingency() {
        MAPF_Instance testInstance = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent55to34, agent43to53, agent00to10, agent10to00});
        PrioritisedPlanning_Solver solver = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.deterministicRescheduling, 3, RestartsStrategy.reorderingStrategy.deterministicRescheduling, null), null, null, null, null, null);
        solver.reportIndvAttempts = true;
        long timeout = 10*1000;
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(instanceReport);
        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
        // should perform 3 + 1 attempts
        assertNotNull(instanceReport.getIntegerValue("attempt #2 time"));
        assertEquals(3, instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR));
        assertEquals(1, instanceReport.getIntegerValue(COMPLETED_CONTINGENCY_ATTEMPTS_STR));
    }

    @Test
    void solvesWhenBadInitialOrderAndHasContingency() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        long timeout = 10*1000;
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null, null, null);
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.reorderingStrategy.deterministicRescheduling, null), null, null, null, null, null);
        solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        // sanity check that it does indeed fail without the contingency
        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(), null, null, null, null, null);
        solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should fail without the contingency
        assertNull(solved);
    }

    @Test
    void ObeysSoftTimeout(){
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances", "MovingAI_Instances", });
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{30}, null);
        InstanceManagerFromFileSystem instanceManager = new InstanceManagerFromFileSystem(path, new InstanceBuilder_MovingAI(), properties);
        InstanceManagerFromFileSystem.InstancePath instancePath = new InstanceManagerFromFileSystem.Moving_AI_Path(
                IO_Manager.buildPath( new String[]{IO_Manager.resources_Directory, "Instances", "MovingAI_Instances", "maze-32-32-2.map"}),
                        IO_Manager.buildPath( new String[]{IO_Manager.resources_Directory, "Instances", "MovingAI_Instances", "maze-32-32-2-even-1.scen"})
                        );
        MAPF_Instance testInstance = instanceManager.getSpecificInstance(instancePath);

        InstanceReport instanceReport = Metrics.newInstanceReport();
        long softTimeout = 100L;
        long hardTimeout = 15L * 1000;

        I_Solver anytimePrPWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 200000, RestartsStrategy.reorderingStrategy.none, false), null, null, null, null, null);
        Solution solved = anytimePrPWithRandomRestarts.solve(testInstance, new RunParametersBuilder().setTimeout(hardTimeout).setInstanceReport(instanceReport).setSoftTimeout(softTimeout).createRP());

        System.out.println(solved);
        assertNotNull(solved);
        assertTrue(solved.solves(testInstance));
        System.out.println("completed initial attempts: " + instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR));
        int runtime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        System.out.println("runtime: " + runtime + "ms");
        assertTrue(runtime >= softTimeout);
        assertTrue(runtime < hardTimeout);

        Metrics.removeReport(instanceReport);
    }

    @Test
    void sortAgents() {
        MAPF_Instance testInstance = instanceCircle1;
        I_Solver solver = new PrioritisedPlanning_Solver((Agent a1, Agent a2) -> a2.priorityClass - a1.priorityClass);

        Agent agent0 = new Agent(0, coor33, coor12, 10);
        Agent agent1 = new Agent(1, coor12, coor33, 1);

        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        agent0 = new Agent(0, coor33, coor12, 1);
        agent1 = new Agent(1, coor12, coor33, 10);

        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(agent0prioritisedSolution.solves(testInstance));
        assertTrue(agent1prioritisedSolution.solves(testInstance));

        assertEquals(agent0prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent0prioritisedSolution.getPlanFor(agent0).size(), 3);

        assertEquals(agent1prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent1prioritisedSolution.getPlanFor(agent1).size(), 3);
    }

    @Test
    void worksWithTMAPFPaths() {
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, new SumServiceTimes(), null, null, null, TransientMAPFSettings.defaultTransientMAPF, null, null);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentXMoving, agentYMoving});

        Solution solvedNormal = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(4 + 4, solvedNormal.sumIndividualCosts());
        assertEquals(4, solvedNormal.makespan());

        Solution solvedPrPT = PrPT.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        assertTrue(solvedPrPT.solves(testInstance));
        assertEquals(4 + 3, solvedPrPT.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2, solvedPrPT.sumServiceTimes()); // TMAPF cost function
        assertEquals(4, solvedPrPT.makespan()); // makespan (normal)
        assertEquals(4, solvedPrPT.makespanServiceTime()); // makespan (TMAPF)
    }

    @Test
    void worksWithTMAPFAndRandomRestarts() {
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, new SumServiceTimes(),
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 2, RestartsStrategy.reorderingStrategy.none, null),
                null, null, TransientMAPFSettings.defaultTransientMAPF, null, null);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentYMoving, agentXMoving});

        I_Solver ppSolverWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 2, RestartsStrategy.reorderingStrategy.none, null),
                null, null, null, null, null);
        Solution solvedNormal = ppSolverWithRandomRestarts.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(8, solvedNormal.sumIndividualCosts());
        // not much of a reason for it to be 6, but this seems to be the solution that is chosen because makespan isn't optimized for
        assertEquals(6, solvedNormal.makespan());

        Solution solvedPrPT = PrPT.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedPrPT.solves(testInstance));
        assertEquals(4 + 3, solvedPrPT.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2, solvedPrPT.sumServiceTimes()); // TMAPF cost function
        assertEquals(4, solvedPrPT.makespan()); // makespan (normal)
        assertEquals(4, solvedPrPT.makespanServiceTime()); // makespan (TMAPF)
    }

    @Test
    void worksWithTMAPFAndBlacklistAndRandomRestarts() {
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, new SumServiceTimes(),
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 2, RestartsStrategy.reorderingStrategy.none, null),
                null, null, TransientMAPFSettings.defaultTransientMAPF, null, null);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentYMoving, agentXMoving});

        I_Solver ppSolverWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 2, RestartsStrategy.reorderingStrategy.none, null),
                null, null, null, null, null);
        Solution solvedNormal = ppSolverWithRandomRestarts.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(8, solvedNormal.sumIndividualCosts());

        Solution solvedPrPT = PrPT.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedPrPT.solves(testInstance));
        assertEquals(4 + 3, solvedPrPT.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2, solvedPrPT.sumServiceTimes()); // TMAPF cost function
    }


    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(ppSolver, 5, false, false);
    }

    @Test
    void TestingBenchmarkWInitialRandomRestarts(){
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 3, RestartsStrategy.reorderingStrategy.none, null), null, null, null, null , null);
        TestUtils.TestingBenchmark(solver, 5, false, false);
    }

    @Test
    void TestingBenchmarkWInitialDeterministicRestarts(){
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.deterministicRescheduling, 3, RestartsStrategy.reorderingStrategy.none, null), null, null, null, null, null);
        TestUtils.TestingBenchmark(solver, 5, false, false);
    }

    @Test
    void comparativeTestHasContingencyVsNoContingency(){
        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null, null, null);
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(null, null, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null, null, null);
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{100}, 10, 0);
    }

    @Test
    void comparativeTestHasAStarRestartsVsNoRestarts(){
        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null, null, null);
        String nameBaseline = "No Restarts";

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 11, RestartsStrategy.reorderingStrategy.none, true), null, null, null, null, null);
        String nameExperimental = "AStar Restarts";

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{100}, 10, 0);
    }


    @Test
    void comparativeTestHasAStarRestartsVsOrderRandomRestarts(){
        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 6, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null, null, null);
        String nameBaseline = "Random Restarts";

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 6, RestartsStrategy.reorderingStrategy.none, true), null, null, null, null, null);
        String nameExperimental = "AStar Restarts";

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{100}, 10, 0);
    }

    @Test
    void sharedGoals(){
        PrioritisedPlanning_Solver ppSolverSharedGoals = new PrioritisedPlanning_Solver(null, null, null, null, true, null, null, null, null);

        MAPF_Instance instanceEmptyPlusSharedGoal1 = new MAPF_Instance("instanceEmptyPlusSharedGoal1", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, new Agent(20, coor14, coor05)});
        MAPF_Instance instanceEmptyPlusSharedGoal2 = new MAPF_Instance("instanceEmptyPlusSharedGoal2", mapEmpty,
                new Agent[]{new Agent(20, coor14, coor05), agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoal3 = new MAPF_Instance("instanceEmptyPlusSharedGoal3", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor14, coor05), agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoal4 = new MAPF_Instance("instanceEmptyPlusSharedGoal4", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor24, coor12), agent43to11, agent04to00});

        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart1 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart1", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, new Agent(20, coor33, coor05)});
        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart2 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart2", mapEmpty,
                new Agent[]{new Agent(20, coor33, coor05), agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart3 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart3", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor33, coor05), agent43to11, agent04to00});
        MAPF_Instance instanceEmptyPlusSharedGoalAndSomeStart4 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndSomeStart4", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor43, coor00), agent43to11, agent04to00});

        // like a duplicate agent except for the id
        MAPF_Instance instanceEmptyPlusSharedGoalAndStart1 = new MAPF_Instance("instanceEmptyPlusSharedGoalAndStart1", mapEmpty,
                new Agent[]{agent33to12, agent12to33, agent53to05, new Agent(20, coor43, coor11), agent43to11, agent04to00});

        MAPF_Instance instanceCircle1SharedGoal = new MAPF_Instance("instanceCircle1SharedGoal", mapCircle, new Agent[]{agent33to12, agent12to33, new Agent(20, coor32, coor12)});
        // like a duplicate agent except for the id
        MAPF_Instance instanceCircle1SharedGoalAndStart = new MAPF_Instance("instanceCircle1SharedGoalAndStart", mapCircle, new Agent[]{agent33to12, agent12to33, new Agent(20, coor33, coor12)});

        MAPF_Instance instanceCircle2SharedGoal = new MAPF_Instance("instanceCircle2SharedGoal", mapCircle, new Agent[]{agent12to33, agent33to12, new Agent(20, coor32, coor12)});
        // like a duplicate agent except for the id
        MAPF_Instance instanceCircle2SharedGoalAndStart = new MAPF_Instance("instanceCircle2SharedGoalAndStart", mapCircle, new Agent[]{agent12to33, agent33to12, new Agent(20, coor33, coor12)});

        System.out.println("should find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceEmptyPlusSharedGoal1, instanceEmptyPlusSharedGoal2, instanceEmptyPlusSharedGoal3, instanceEmptyPlusSharedGoal4,
                instanceEmptyPlusSharedGoalAndSomeStart1, instanceEmptyPlusSharedGoalAndSomeStart2, instanceEmptyPlusSharedGoalAndSomeStart3, instanceEmptyPlusSharedGoalAndSomeStart4,
                instanceEmptyPlusSharedGoalAndStart1, instanceCircle1SharedGoal, instanceCircle1SharedGoalAndStart, instanceCircle2SharedGoal, instanceCircle2SharedGoalAndStart}){
            System.out.println("testing " + testInstance.name);
            Solution solution = ppSolverSharedGoals.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
            assertNotNull(solution);
            assertTrue(solution.solves(testInstance, true, true));
        }

        MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});

        System.out.println("should not find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceUnsolvable, this.instanceUnsolvableBecauseOrderWithInfiniteWait}){
            System.out.println("testing " + testInstance.name);
            Solution solution = ppSolverSharedGoals.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
            assertNull(solution);
        }
    }

    /* Lifelong */

    @Test
    void worksWithRHCRHorizon_instanceCircle1(){
        MAPF_Instance testInstance = instanceCircle1;

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        Solution solved_h1 = PrP_h1.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h2 = PrP_h2.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h3 = PrP_h3.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h4 = PrP_h4.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_hinf = PrP_hinf.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());

        System.out.println(solved_h1);
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h2);
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h3);
        assertEquals(3, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h4);
        assertEquals(3, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_hinf);
        assertEquals(3, solved_hinf.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_hinf.getPlanFor(testInstance.agents.get(1)).getCost());
    }

    @Test
    void worksWithRHCRHorizon_instanceSmallMaze(){
        MAPF_Instance testInstance = new MAPF_Instance("small maze new agents" , mapSmallMaze, new Agent[]{agent30to00, agent00to10});

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        Solution solved_h1 = PrP_h1.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h2 = PrP_h2.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h3 = PrP_h3.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h4 = PrP_h4.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_hinf = PrP_hinf.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());

        System.out.println(solved_h1);
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(1, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h2);
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h3);
        assertEquals(3, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h4);
        assertEquals(3, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(6, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_hinf);
        assertEquals(3, solved_hinf.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(9, solved_hinf.getPlanFor(testInstance.agents.get(1)).getCost());
    }

    @Test
    void worksWithRHCRHorizon_instanceSmallMaze_reverseAgents(){
        MAPF_Instance testInstance = new MAPF_Instance("small maze new agents" , mapSmallMaze, new Agent[]{agent00to10, agent30to00});

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        Solution solved_h1 = PrP_h1.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h2 = PrP_h2.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h3 = PrP_h3.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_h4 = PrP_h4.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());
        Solution solved_hinf = PrP_hinf.solve(testInstance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP());

        System.out.println(solved_h1);
        assertEquals(1, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h2);
        assertEquals(1, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(4, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h3);
        assertEquals(1, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h4);
        assertEquals(1, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(6, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_hinf);
        assertEquals(1, solved_hinf.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(7, solved_hinf.getPlanFor(testInstance.agents.get(1)).getCost());
    }

    @Test
    void worksWithRHCRHorizon_instanceCircle1_andInitialConstraints(){
        MAPF_Instance testInstance = instanceCircle1;

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(null, 2, testInstance.map.getMapLocation(coor22)));
        constraints.add(new Constraint(null, 2, testInstance.map.getMapLocation(coor14)));
        constraints.add(new Constraint(null, 4, testInstance.map.getMapLocation(coor24)));
        constraints.add(new Constraint(null, 4, testInstance.map.getMapLocation(coor32)));
        constraints.add(new Constraint(null, 5, testInstance.map.getMapLocation(coor24)));
        constraints.add(new Constraint(null, 5, testInstance.map.getMapLocation(coor32)));
        RunParameters parameters = new RunParametersBuilder().setInstanceReport(new InstanceReport()).setConstraints(constraints).createRP();

        Solution solved_h1 = PrP_h1.solve(testInstance, parameters);
        Solution solved_h2 = PrP_h2.solve(testInstance, parameters);
        Solution solved_h3 = PrP_h3.solve(testInstance, parameters);
        Solution solved_h4 = PrP_h4.solve(testInstance, parameters);
        Solution solved_hinf = PrP_hinf.solve(testInstance, parameters);

        System.out.println(solved_h1);
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h2);
        assertEquals(4, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h3);
        assertEquals(4, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(6, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h4);
        assertEquals(4, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(7, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_hinf);
        assertEquals(4, solved_hinf.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(8, solved_hinf.getPlanFor(testInstance.agents.get(1)).getCost());
    }

    @Test
    void worksWithRHCRHorizon_instanceSmallMaze_andInitialConstraints(){
        MAPF_Instance testInstance = new MAPF_Instance("small maze new agents" , mapSmallMaze, new Agent[]{agent30to00, agent00to10});

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(null, 2, testInstance.map.getMapLocation(coor00)));
        constraints.add(new Constraint(null, 4, testInstance.map.getMapLocation(coor00)));
        constraints.add(new Constraint(null, 5, testInstance.map.getMapLocation(coor32)));
        RunParameters parameters = new RunParametersBuilder().setInstanceReport(new InstanceReport()).setConstraints(constraints).createRP();

        Solution solved_h1 = PrP_h1.solve(testInstance, parameters);
        Solution solved_h2 = PrP_h2.solve(testInstance, parameters);
        Solution solved_h3 = PrP_h3.solve(testInstance, parameters);
        Solution solved_h4 = PrP_h4.solve(testInstance, parameters);
        Solution solved_hinf = PrP_hinf.solve(testInstance, parameters);


        System.out.println(solved_h1);
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(1, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h2);
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(4, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h3);
        assertEquals(3, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h4);
        assertEquals(5, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(6, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_hinf);
        assertEquals(5, solved_hinf.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(10, solved_hinf.getPlanFor(testInstance.agents.get(1)).getCost());
    }

    @Test
    void worksWithRHCRHorizon_instanceSmallMaze_reverseAgents_andInitialConstraints(){
        MAPF_Instance testInstance = new MAPF_Instance("small maze new agents" , mapSmallMaze, new Agent[]{agent00to10, agent30to00});

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(null, 2, testInstance.map.getMapLocation(coor10)));
        constraints.add(new Constraint(null, 4, testInstance.map.getMapLocation(coor10)));
        constraints.add(new Constraint(null, 6, testInstance.map.getMapLocation(coor01)));
        RunParameters parameters = new RunParametersBuilder().setInstanceReport(new InstanceReport()).setConstraints(constraints).createRP();

        Solution solved_h1 = PrP_h1.solve(testInstance, parameters);
        Solution solved_h2 = PrP_h2.solve(testInstance, parameters);
        Solution solved_h3 = PrP_h3.solve(testInstance, parameters);
        Solution solved_h4 = PrP_h4.solve(testInstance, parameters);
        Solution solved_hinf = PrP_hinf.solve(testInstance, parameters);

        System.out.println(solved_h1);
        assertEquals(1, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h2);
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(4, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h3);
        assertEquals(3, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h4);
        assertEquals(5, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(6, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_hinf);
        assertEquals(5, solved_hinf.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(8, solved_hinf.getPlanFor(testInstance.agents.get(1)).getCost());
    }

    @Test
    void worksWithRHCRHorizon_instanceCircle1_andInitialGoalConstraints(){
        MAPF_Instance testInstance = instanceCircle1;

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new GoalConstraint(null, 2, testInstance.map.getMapLocation(coor32), new Agent(1000, coor34, coor34)));
        constraints.add(new GoalConstraint(null, 2, testInstance.map.getMapLocation(coor24), new Agent(1000, coor34, coor34))); // inf lock started before agent needs to pass there at time 3
        RunParameters parameters = new RunParametersBuilder().setInstanceReport(new InstanceReport()).setConstraints(constraints).createRP();

        Solution solved_h1 = PrP_h1.solve(testInstance, parameters);
        Solution solved_h2 = PrP_h2.solve(testInstance, parameters);
        Solution solved_h3 = PrP_h3.solve(testInstance, parameters);
        Solution solved_h4 = PrP_h4.solve(testInstance, parameters);
        Solution solved_hinf = PrP_hinf.solve(testInstance, parameters);

        System.out.println(solved_h1);
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h2);
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        // at time 3, should ignore the infinite lock on (2,4) that starts at time 2
        assertEquals(5, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h3);
        assertEquals(3, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        // at time 4, should ignore the infinite lock on (2,4) that starts at time 2, and the lock on (3,2) that starts at time 2, but is blocked anyway because the other agent is keeping (1,2) until time 4
        assertEquals(6, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_h4);
        assertEquals(3, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        // at time 5, should ignore the infinite lock on (2,4) that starts at time 2, and the lock on (3,2) that starts at time 2, but is blocked anyway because the other agent is keeping (1,2) until time 4
        assertEquals(7, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());
        System.out.println(solved_hinf);
        // should fail because of infinite lock on (2,4) that starts at time 2
        assertNull(solved_hinf);
    }

    @Test
    void worksWithRHCRHorizon_instanceSmallMaze_andInitialGoalConstraints(){
        MAPF_Instance testInstance = new MAPF_Instance("small maze new agents" , mapSmallMaze, new Agent[]{agent30to00, agent00to10});

        I_Solver PrP_h1 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 1, null);
        I_Solver PrP_h2 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 2, null);
        I_Solver PrP_h3 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 3, null);
        I_Solver PrP_h4 = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, 4, null);
        I_Solver PrP_hinf = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, null, null);

        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new GoalConstraint(null, 1, testInstance.map.getMapLocation(coor10), new Agent(1000, coor34, coor34)));
//        constraints.add(new GoalConstraint(null, 2, testInstance.map.getMapLocation(coor24), new Agent(1000, coor34, coor34)));
        RunParameters parameters = new RunParametersBuilder().setInstanceReport(new InstanceReport()).setConstraints(constraints).createRP();

        Solution solved_h1 = PrP_h1.solve(testInstance, parameters);
        Solution solved_h2 = PrP_h2.solve(testInstance, parameters);
        Solution solved_h3 = PrP_h3.solve(testInstance, parameters);
        Solution solved_h4 = PrP_h4.solve(testInstance, parameters);
        Solution solved_hinf = PrP_hinf.solve(testInstance, parameters);


        System.out.println(solved_h1);
        assertEquals(3, solved_h1.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(2, solved_h1.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h2);
        assertEquals(4, solved_h2.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(3, solved_h2.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h3);
        assertEquals(5, solved_h3.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(4, solved_h3.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_h4);
        assertEquals(6, solved_h4.getPlanFor(testInstance.agents.get(0)).getCost());
        assertEquals(5, solved_h4.getPlanFor(testInstance.agents.get(1)).getCost());

        System.out.println(solved_hinf);
        assertNull(solved_hinf); // (1,0) is taken infinitely so one agent can't finish
    }

    @Test
    void worksWithRHCR_shotgunTest(){
        Integer[] rhcrHorizons = new Integer[]{1, 2, 3, 7, null, Integer.MAX_VALUE};
        for (Integer rhcrHorizon : rhcrHorizons){
            System.out.printf("testing with RHCR horizon %d\n", rhcrHorizon);
            I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, null, null, null, null, rhcrHorizon, null);
            for (MAPF_Instance instance : new MAPF_Instance[]{instanceEmpty1, instanceEmpty2, instanceEmptyEasy,
                    instanceEmptyHarder, instanceCircle1, instanceCircle2, instanceSmallMaze, instanceStartAdjacentGoAround}){
                System.out.println("testing " + instance.name);
                Solution solution = solver.solve(instance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).setTimeout(2000).createRP());
                if (solution != null){
                    for (SingleAgentPlan plan : solution){
                        for (SingleAgentPlan otherPlan : solution){
                            if (plan != otherPlan){
                                A_Conflict firstConf = plan.firstConflict(otherPlan);
                                assertTrue(firstConf == null || firstConf.time > rhcrHorizon);
                            }
                        }
                    }
                }
                else {
                    System.out.println("warning: no solution found.");
                }
            }
        }
    }

}