package BasicMAPF.Solvers.CBS;

import BasicMAPF.CostFunctions.SOCWithPriorities;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.InstanceBuilders.Priorities;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.TestUtils;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.*;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import TransientMAPF.TransientMAPFSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Instances.*;
import static org.junit.jupiter.api.Assertions.*;

class CBS_SolverTest {

    InstanceBuilder_BGU builder = new InstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    I_Solver cbsSolver = new CBSBuilder().createCBS_Solver();


    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }

    void validate(Solution solution, int numAgents, int optimalSOC, int optimalMakespan, MAPF_Instance instance){
        assertTrue(solution.isValidSolution()); //is valid (no conflicts)
        assertTrue(solution.solves(instance));

        assertEquals(numAgents, solution.size()); // solution includes all agents
        assertEquals(optimalSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(optimalMakespan, solution.makespan()); // makespan is optimal
    }

    @Test
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 7, solved.sumIndividualCosts(),solved.makespan(), testInstance); //need to find actual optimal costs
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 2, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 2, 8, 5, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 2, 6, 4, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeout() {
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(2L*1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = cbsSolver.solve(testInstance, new RunParametersBuilder().setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void cbsWithPriorities() {
        I_Solver solver = new CBSBuilder().setCostFunction(new SOCWithPriorities()).createCBS_Solver();
        InstanceReport instanceReport = new InstanceReport();

        Agent agent0 = new Agent(0, coor33, coor12, 10);
        Agent agent1 = new Agent(1, coor12, coor33, 1);

        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        agent0 = new Agent(0, coor33, coor12, 1);
        agent1 = new Agent(1, coor12, coor33, 10);

        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        System.out.println(agent0prioritisedSolution);
        validate(agent0prioritisedSolution, 2, 8, 5, agent0prioritisedInstance);

        System.out.println(agent1prioritisedSolution);
        validate(agent1prioritisedSolution, 2, 8, 5, agent1prioritisedInstance);

        // check that agents were logically prioritised to minimise cost with priorities

        assertEquals(agent0prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent0prioritisedSolution.getPlanFor(agent0).size(), 3);

        assertEquals(agent1prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent1prioritisedSolution.getPlanFor(agent1).size(), 3);
    }

    @Test
    void cbsWithPrioritiesUsingBuilder() {
        boolean useAsserts = true;

        I_Solver solver = new CBSBuilder().setCostFunction(new SOCWithPriorities()).createCBS_Solver();
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path,
                new InstanceBuilder_BGU(new Priorities(Priorities.PrioritiesPolicy.ROUND_ROBIN, new int[]{1, 3, 5})));

        MAPF_Instance instance = null;
        long timeout = 30 /*seconds*/
                *1000L;

        // run all benchmark instances. this code is mostly copied from Environment.Experiment.
        while ((instance = instanceManager.getNextInstance()) != null) {
            InstanceReport report = new InstanceReport();

            RunParameters runParameters = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP();

            //solve
            System.out.println("---------- solving "  + instance.name + " ----------");
            Solution solution = solver.solve(instance, runParameters);

            // validate
            boolean solved = solution != null;
            System.out.println("Solved?: " + (solved ? "yes" : "no"));

            if(solution != null){
                boolean valid = solution.solves(instance);
                System.out.println("Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }
        }
    }

    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(cbsSolver, 300, true, true);
    }

    @Test
    void sharedGoals(){
        CBS_Solver cbsSolverSharedGoals = new CBSBuilder().setSharedGoals(true).createCBS_Solver();

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

        MAPF_Instance instanceCircleSameEarliestGoalArrivalTimeSameGoal = new MAPF_Instance("instanceCircleSameEarliestGoalArrivalTimeSameGoal",
                mapCircle, new Agent[]{new Agent(20, coor32, coor12), new Agent(21, coor14, coor12)});
        MAPF_Instance instanceDifferentGoalAndInterfering1 = new MAPF_Instance("instanceDifferentGoalAndInterfering1",
                mapSmallMaze, new Agent[]{new Agent(1, coor55, coor32), new Agent(2, coor33, coor43)});
        MAPF_Instance instanceDifferentGoalAndInterfering2 = new MAPF_Instance("instanceDifferentGoalAndInterfering2",
                mapSmallMaze, new Agent[]{new Agent(1, coor33, coor43), new Agent(2, coor55, coor32)});

        System.out.println("should find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceEmptyPlusSharedGoal1, instanceEmptyPlusSharedGoal2, instanceEmptyPlusSharedGoal3, instanceEmptyPlusSharedGoal4,
                instanceEmptyPlusSharedGoalAndSomeStart1, instanceEmptyPlusSharedGoalAndSomeStart2, instanceEmptyPlusSharedGoalAndSomeStart3, instanceEmptyPlusSharedGoalAndSomeStart4,
                instanceEmptyPlusSharedGoalAndStart1, instanceCircle1SharedGoal, instanceCircle1SharedGoalAndStart, instanceCircle2SharedGoal, instanceCircle2SharedGoalAndStart,
                instanceDifferentGoalAndInterfering1, instanceDifferentGoalAndInterfering2, instanceCircleSameEarliestGoalArrivalTimeSameGoal}){
            System.out.println("testing " + testInstance.name);
            Solution solution = cbsSolverSharedGoals.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
            assertNotNull(solution);
            assertTrue(solution.solves(testInstance, true, true));
            if (testInstance.name.equals(instanceCircle1SharedGoal.name)){
                assertEquals(10, solution.sumIndividualCosts());
                assertEquals(5, solution.makespan());
            }
            if (testInstance.name.equals(instanceCircle1SharedGoalAndStart.name)){
                assertEquals(12, solution.sumIndividualCosts());
                assertEquals(5, solution.makespan());
            }
            if (testInstance.name.equals(instanceCircleSameEarliestGoalArrivalTimeSameGoal.name)){
                // arriving at same time treated as okay. would be 5 if they have to arrive one-at-a-time at goal
                assertEquals(4, solution.sumIndividualCosts());
                // arriving at same time treated as okay. would be 3 if they have to arrive one-at-a-time at goal
                assertEquals(2, solution.makespan());
            }
        }

        MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});

        System.out.println("should not find a solution:");
        for (MAPF_Instance testInstance : new MAPF_Instance[]{instanceUnsolvable}){
            System.out.println("testing " + testInstance.name);
            Solution solution = cbsSolverSharedGoals.solve(testInstance, new RunParametersBuilder().setTimeout(5L*1000).setInstanceReport(instanceReport).createRP());
            assertNull(solution);
        }
    }

    @Test
    void worksWithTMAPFPaths() {
        I_Solver CBSt = new CBSBuilder().setTransientMAPFSettings(TransientMAPFSettings.defaultTransientMAPF).createCBS_Solver();
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentXMoving, agentYMoving});

        Solution solvedNormal = cbsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(4 + 4, solvedNormal.sumIndividualCosts());
        assertEquals(6, solvedNormal.makespan());

        Solution solvedCBSt = CBSt.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedCBSt.solves(testInstance));
        assertEquals(4 + 3, solvedCBSt.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2, solvedCBSt.sumServiceTimes()); // TMAPF cost function
        assertEquals(4, solvedCBSt.makespan()); // makespan (normal)
        assertEquals(4, solvedCBSt.makespanServiceTime()); // makespan (TMAPF)

        System.out.println(solvedNormal);
        System.out.println(solvedCBSt);
    }


    @Test
    void transientExample() {
        I_Solver CBSt = new CBSBuilder().setCostFunction(new SumServiceTimes()).setTransientMAPFSettings(TransientMAPFSettings.defaultTransientMAPF).createCBS_Solver();
        Agent agent1 = new Agent(0, coor10, coor13, 1);
        Agent agent2 = new Agent(1, coor11, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", transientExampleMap, new Agent[]{agent1, agent2});

        Solution solvedNormal = cbsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(4 + 1, solvedNormal.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 1, solvedNormal.sumServiceTimes()); // TMAPF cost function
        assertEquals(4, solvedNormal.makespan()); // makespan (normal)
        assertEquals(4, solvedNormal.makespanServiceTime()); // makespan (TMAPF)

        Solution solvedCBSt = CBSt.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedCBSt.solves(testInstance));
        assertEquals(3 + 3, solvedCBSt.sumIndividualCosts()); // normal SOC function
        assertEquals(3 + 1, solvedCBSt.sumServiceTimes()); // TMAPF cost function
        assertEquals(3, solvedCBSt.makespan()); // makespan (normal)
        assertEquals(3, solvedCBSt.makespanServiceTime()); // makespan (TMAPF)

        System.out.println(solvedNormal);
        System.out.println(solvedCBSt);
    }

    @Test
    void worksWithRHCR(){
        Integer[] rhcrHorizons = new Integer[]{1, 2, 3, 7, null, Integer.MAX_VALUE};
        for (Integer rhcrHorizon : rhcrHorizons){
            System.out.printf("testing with RHCR horizon %d\n", rhcrHorizon);
            I_Solver solver = new CBS_Solver(null, null, null, null, null, null, null, null, null, rhcrHorizon);
            for (MAPF_Instance instance : new MAPF_Instance[]{instanceEmpty1, instanceEmpty2, instanceEmpty3, instanceEmptyEasy,
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