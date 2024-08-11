package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import TransientMAPF.TransientMAPFSettings;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Instances.*;
import static org.junit.jupiter.api.Assertions.*;

class LargeNeighborhoodSearch_SolverTest {

    I_Solver solver = new LNSBuilder().createLNS();

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
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = solver.solve(testInstance, getDefaultRunParameters());

        assertTrue(solved.solves(testInstance));
    }

    @NotNull
    private RunParameters getDefaultRunParameters() {
        return new RunParametersBuilder().setTimeout(3L * 1000).setInstanceReport(instanceReport).createRP();
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
        I_Solver LNSt = new LNSBuilder().setTransientMAPFBehaviour(new TransientMAPFSettings(true, false)).createLNS();
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
        I_Solver LNSt = new LNSBuilder().setTransientMAPFBehaviour(new TransientMAPFSettings(true, true)).createLNS();
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
    void worksWithRHCR(){
        Integer[] rhcrHorizons = new Integer[]{1, 2, 3, 7, null, Integer.MAX_VALUE};
        for (Integer rhcrHorizon : rhcrHorizons){
            System.out.printf("testing with RHCR horizon %d\n", rhcrHorizon);
            LargeNeighborhoodSearch_Solver solver = new LNSBuilder().setRHCR_Horizon(rhcrHorizon).createLNS();
            for (MAPF_Instance instance : new MAPF_Instance[]{instanceEmpty1, instanceEmpty2, instanceEmptyEasy,
                    instanceEmptyHarder, instanceCircle1, instanceCircle2, instanceSmallMaze, instanceStartAdjacentGoAround}){
                System.out.println("testing " + instance.name);
                Solution solution = solver.solve(instance, new RunParametersBuilder().setInstanceReport(new InstanceReport()).setTimeout(2000).createRP());
                if (solution != null){
                    for (SingleAgentPlan plan : solution){
                        for (SingleAgentPlan otherPlan : solution){
                            if (plan != otherPlan){
                                A_Conflict firstConf = plan.firstConflict(otherPlan);
                                if (firstConf != null){
                                    if (firstConf.time <= rhcrHorizon){
                                        System.out.println("first conflict is " + firstConf + " but RHCR horizon is " + rhcrHorizon);
                                        assert false;
                                    }
                                }
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