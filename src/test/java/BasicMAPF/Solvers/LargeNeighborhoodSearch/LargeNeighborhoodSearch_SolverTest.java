package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.ConflictsCount;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
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

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Instances.*;
import static org.junit.jupiter.api.Assertions.*;

class LargeNeighborhoodSearch_SolverTest {

    I_Solver solver = CanonicalSolversFactory.createLNS1Solver();

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
        Solution solved = solver.solve(testInstance, getDefaultRunParameters());

        assertTrue(solved.solves(testInstance));
    }

    @NotNull
    private RunParameters getDefaultRunParameters() {
        return new RunParametersBuilder().setTimeout(3000).setSoftTimeout(500).setInstanceReport(instanceReport).createRP();
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
    void unsolvable() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = solver.solve(testInstance, getDefaultRunParameters());

        assertNull(solved);
    }

    @Test
    void ObeysSoftTimeout(){
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        long softTimeout = 1000L;
        long hardTimeout = 5L * 1000;
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(hardTimeout).setInstanceReport(instanceReport).setSoftTimeout(softTimeout).createRP());

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
        int runtime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        System.out.println("runtime: " + runtime);
        assertTrue(runtime >= softTimeout && runtime < hardTimeout);

        Metrics.removeReport(instanceReport);
    }

    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(solver, 3, false, false);
    }

    @Test
    void sharedGoals(){
        LargeNeighborhoodSearch_Solver solverWithSharedGoals = new LNSBuilder().setSharedGoals(true).createLNS();

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
            Solution solution = solverWithSharedGoals.solve(testInstance, getDefaultRunParameters());
            assertNotNull(solution);
            assertTrue(solution.solves(testInstance, true, true));
        }

        MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});

        System.out.println("should not find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceUnsolvable}){
            System.out.println("testing " + testInstance.name);
            Solution solution = solverWithSharedGoals.solve(testInstance, getDefaultRunParameters());
            assertNull(solution);
        }
    }

    @Test
    void worksWithTMAPF() {
        I_Solver LNSt = new LNSBuilder().setTransientMAPFBehaviour(new TransientMAPFSettings(true, false, false, false))
                .setSolutionCostFunction(new SumServiceTimes()).createLNS();
        Agent agent1 = new Agent(0, coor42, coor02, 1);
        Agent agent2 = new Agent(1, coor10, coor12, 1);
        Agent agent3 = new Agent(2, coor30, coor32, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agent1, agent2, agent3});

        Solution solvedNormal = solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        System.out.println(solvedNormal);
        assertEquals(4 + 4 + 2, solvedNormal.sumIndividualCosts());
        assertTrue(solvedNormal.makespan() == 4 || solvedNormal.makespan() == 6);

        Solution solvedPrPT = LNSt.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedPrPT.solves(testInstance));
        System.out.println(solvedPrPT);
        assertEquals(4 + 3 + 2, solvedPrPT.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2 + 2, solvedPrPT.sumServiceTimes()); // TMAPF cost function
        assertEquals(4, solvedPrPT.makespan()); // makespan (normal)
        assertEquals(4, solvedPrPT.makespanServiceTime()); // makespan (TMAPF)
    }

    @Test
    void worksWithTMAPFAndBlacklist() {
        I_Solver LNSt = new LNSBuilder().setTransientMAPFBehaviour(new TransientMAPFSettings(true, true, false, false))
                .setSolutionCostFunction(new SumServiceTimes()).createLNS();
        Agent agent1 = new Agent(0, coor42, coor02, 1);
        Agent agent2 = new Agent(1, coor10, coor12, 1);
        Agent agent3 = new Agent(2, coor30, coor32, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agent1, agent2, agent3});

        Solution solvedNormal = solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        System.out.println(solvedNormal);
        assertEquals(4 + 4 + 2, solvedNormal.sumIndividualCosts());
        assertTrue(solvedNormal.makespan() == 4 || solvedNormal.makespan() == 6);

        Solution solvedPrPT = LNSt.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedPrPT.solves(testInstance));
        System.out.println(solvedPrPT);
        assertEquals(4 + 3 + 2, solvedPrPT.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2 + 2, solvedPrPT.sumServiceTimes()); // TMAPF cost function
        assertEquals(4, solvedPrPT.makespan()); // makespan (normal)
        assertEquals(4, solvedPrPT.makespanServiceTime()); // makespan (TMAPF)
    }

    @Test
    void corridorSolvedOnlyByTransientUsingSIPPStAsLowLevel() {
        Agent agent1 = new Agent(0, coor00, coor03, 1);
        Agent agent2 = new Agent(1, coor01, coor02, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapCorridor, new Agent[]{agent1, agent2});

        PrioritisedPlanning_Solver initialSolverTransient = new PrioritisedPlanning_Solver(new SingleAgentAStarSIPP_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF);
        PrioritisedPlanning_Solver iterationsSolverTransient = new PrioritisedPlanning_Solver(new SingleAgentAStarSIPP_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.none, null), null, null, TransientMAPFSettings.defaultTransientMAPF);
        I_Solver LNS1tWithSIPPt = new LNSBuilder().setInitialSolver(initialSolverTransient).setIterationsSolver(iterationsSolverTransient).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).setSolutionCostFunction(new SumServiceTimes()).createLNS();

        Solution solveByLNStWithSIPPt = LNS1tWithSIPPt.solve(testInstance, new RunParametersBuilder().setAStarGAndH(new ServiceTimeGAndH(new DistanceTableSingleAgentHeuristic(testInstance.agents, testInstance.map))).setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solveByLNStWithSIPPt.solves(testInstance));
        System.out.println(solveByLNStWithSIPPt);

        Solution solvedByLNS = solver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertNull(solvedByLNS);
    }
}