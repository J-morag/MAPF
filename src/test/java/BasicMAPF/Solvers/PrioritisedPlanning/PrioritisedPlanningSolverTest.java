package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.TestUtils;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
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
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 3, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null);
        solver.reportIndvAttempts = true;
        long timeout = 10*1000;
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(instanceReport);
        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm, exhausts the orderings search space)
        assertNull(solved);
        // should perform 3 + 21 attempts
        assertNotNull(instanceReport.getIntegerValue("ordering#3 randomization#1 totalTime"));
        assertEquals(3, instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR));
        assertEquals(21, instanceReport.getIntegerValue(COMPLETED_CONTINGENCY_ATTEMPTS_STR));
    }

    @Test
    void failsBeforeTimeoutWithDeterministicInitialAndContingency() {
        MAPF_Instance testInstance = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent55to34, agent43to53, agent00to10, agent10to00});
        PrioritisedPlanning_Solver solver = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.deterministicRescheduling, 3, RestartsStrategy.reorderingStrategy.deterministicRescheduling, null), null, null, null);
        solver.reportIndvAttempts = true;
        long timeout = 10*1000;
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(instanceReport);
        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
        // should perform 3 + 1 attempts
        assertNotNull(instanceReport.getIntegerValue("ordering#3 randomization#1 totalTime"));
        assertEquals(3, instanceReport.getIntegerValue(COMPLETED_INITIAL_ATTEMPTS_STR));
        assertEquals(1, instanceReport.getIntegerValue(COMPLETED_CONTINGENCY_ATTEMPTS_STR));
    }

    @Test
    void solvesWhenBadInitialOrderAndHasContingency() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        long timeout = 10*1000;
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null);
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.reorderingStrategy.deterministicRescheduling, null), null, null, null);
        solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        // sanity check that it does indeed fail without the contingency
        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(), null, null, null);
        solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should fail without the contingency
        assertNull(solved);
    }

    @Test
    void ObeysSoftTimeout(){
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances", "MovingAI_Instances", });
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{30}, null);
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(), properties);
        InstanceManager.InstancePath instancePath = new InstanceManager.Moving_AI_Path(
                IO_Manager.buildPath( new String[]{IO_Manager.resources_Directory, "Instances", "MovingAI_Instances", "maze-32-32-2.map"}),
                        IO_Manager.buildPath( new String[]{IO_Manager.resources_Directory, "Instances", "MovingAI_Instances", "maze-32-32-2-even-1.scen"})
                        );
        MAPF_Instance testInstance = instanceManager.getSpecificInstance(instancePath);

        InstanceReport instanceReport = Metrics.newInstanceReport();
        long softTimeout = 100L;
        long hardTimeout = 15L * 1000;

        I_Solver anytimePrPWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 200000, RestartsStrategy.reorderingStrategy.none, false), null, null, null);
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
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, new SumServiceTimes(), null, null, null, TransientMAPFSettings.defaultTransientMAPF);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentXMoving, agentYMoving});

        Solution solvedNormal = ppSolver.solve(testInstance, new RunParametersBuilder().setTimeout(1000000000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(4 + 4, solvedNormal.sumIndividualCosts());
        assertEquals(4, solvedNormal.makespan());

        Solution solvedPrPT = PrPT.solve(testInstance, new RunParametersBuilder().setTimeout(1000000000L).setInstanceReport(instanceReport).createRP());
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
                null, null, TransientMAPFSettings.defaultTransientMAPF);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentYMoving, agentXMoving});

        I_Solver ppSolverWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 2, RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
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
                null, null, TransientMAPFSettings.defaultTransientMAPF);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentYMoving, agentXMoving});

        I_Solver ppSolverWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 2, RestartsStrategy.reorderingStrategy.none, null),
                null, null, null);
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
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 3, RestartsStrategy.reorderingStrategy.none, null), null, null, null);
        TestUtils.TestingBenchmark(solver, 5, false, false);
    }

    @Test
    void TestingBenchmarkWInitialDeterministicRestarts(){
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.reorderingStrategy.deterministicRescheduling, 3, RestartsStrategy.reorderingStrategy.none, null), null, null, null);
        TestUtils.TestingBenchmark(solver, 5, false, false);
    }

    @Test
    void comparativeTestHasContingencyVsNoContingency(){
        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null);
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(null, null, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null);
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{100}, 10, 0);
    }

    @Test
    void comparativeTestHasAStarRestartsVsNoRestarts(){
        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null);
        String nameBaseline = "No Restarts";

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 11, RestartsStrategy.reorderingStrategy.none, true), null, null, null);
        String nameExperimental = "AStar Restarts";

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{100}, 10, 0);
    }


    @Test
    void comparativeTestHasAStarRestartsVsOrderRandomRestarts(){
        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 6, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, null);
        String nameBaseline = "Random Restarts";

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 6, RestartsStrategy.reorderingStrategy.none, true), null, null, null);
        String nameExperimental = "AStar Restarts";

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{100}, 10, 0);
    }

    @Test
    void sharedGoals(){
        PrioritisedPlanning_Solver ppSolverSharedGoals = new PrioritisedPlanning_Solver(null, null, null, null, true, null, null);

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

}