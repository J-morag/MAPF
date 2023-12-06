package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import TransientMAPF.TransientMAPFBehaviour;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.text.DateFormat;
import java.util.Map;

import static BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver.COMPLETED_CONTINGENCY_ATTEMPTS_STR;
import static BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver.COMPLETED_INITIAL_ATTEMPTS_STR;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestUtils.readResultsCSV;
import static org.junit.jupiter.api.Assertions.*;

class PrioritisedPlanningSolverTest {

    private final MAPF_Instance instanceUnsolvableBecauseOrderWithInfiniteWait = new MAPF_Instance("instanceUnsolvableWithInfiniteWait", mapWithPocket, new Agent[]{agent43to53, agent55to34});

    I_Solver ppSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver());


    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
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
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ppSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
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
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 2, RestartsStrategy.RestartsKind.randomRestarts), null, null, null);
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
                new RestartsStrategy(RestartsStrategy.RestartsKind.deterministicRescheduling, 2, RestartsStrategy.RestartsKind.deterministicRescheduling), null, null, null);
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
                new RestartsStrategy(null, null, RestartsStrategy.RestartsKind.randomRestarts), null, null, null);
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        // should be able to solve in one of the restarts
        assertNotNull(solved);

        solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(null, null, RestartsStrategy.RestartsKind.deterministicRescheduling), null, null, null);
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
        MAPF_Instance testInstance = instanceEmptyHarder;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        long softTimeout = 100L;
        long hardTimeout = 5L * 1000;

        I_Solver anytimePrPWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10000, RestartsStrategy.RestartsKind.none), null, null, null);
        Solution solved = anytimePrPWithRandomRestarts.solve(testInstance, new RunParametersBuilder().setTimeout(hardTimeout).setInstanceReport(instanceReport).setSoftTimeout(softTimeout).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        int runtime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        System.out.println("runtime: " + runtime);
        assertTrue(runtime >= softTimeout && runtime < hardTimeout);

        S_Metrics.removeReport(instanceReport);
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
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, null, null, null, null, TransientMAPFBehaviour.transientMAPF);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentXMoving, agentYMoving});

        Solution solvedNormal = ppSolver.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedNormal.solves(testInstance));
        assertEquals(4 + 4, solvedNormal.sumIndividualCosts());

        Solution solvedPrPT = PrPT.solve(testInstance, new RunParametersBuilder().setTimeout(1000L).setInstanceReport(instanceReport).createRP());
        assertTrue(solvedPrPT.solves(testInstance));
        assertEquals(4 + 3, solvedPrPT.sumIndividualCosts()); // normal SOC function
        assertEquals(4 + 2, solvedPrPT.sumServiceTimes()); // TMAPF cost function
    }

    @Test
    void worksWithTMAPFAndRandomRestarts() {
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 1),
                null, null, TransientMAPFBehaviour.transientMAPF);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentYMoving, agentXMoving});

        I_Solver ppSolverWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 1),
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
    void worksWithTMAPFAndBlacklistAndRandomRestarts() {
        I_Solver PrPT = new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 1),
                null, null, TransientMAPFBehaviour.transientMAPFWithBlacklist);
        Agent agentXMoving = new Agent(0, coor42, coor02, 1);
        Agent agentYMoving = new Agent(1, coor10, coor12, 1);
        MAPF_Instance testInstance = new MAPF_Instance("testInstance", mapEmpty, new Agent[]{agentYMoving, agentXMoving});

        I_Solver ppSolverWithRandomRestarts = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 1),
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
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = ppSolver;
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "/Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
//                if (!instance.name.equals("Instance-32-20-20-0")){
//                    continue;
//                }

                //build report
                InstanceReport report = S_Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                RunParameters runParameters = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP();

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
//                if (useAsserts) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + (timeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = S_Metrics.defaultDateFormat;
            String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
            File directory = new File(resultsOutputDir);
            if (! directory.exists()){
                directory.mkdir();
            }
            String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir, 
                "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() + 
                        "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
            try {
                S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                        new String[]{
                                InstanceReport.StandardFields.instanceName,
                                InstanceReport.StandardFields.numAgents,
                                InstanceReport.StandardFields.timeoutThresholdMS,
                                InstanceReport.StandardFields.solved,
                                InstanceReport.StandardFields.elapsedTimeMS,
                                "Runtime Delta",
                                InstanceReport.StandardFields.solutionCost,
                                "Cost Delta",
                                InstanceReport.StandardFields.totalLowLevelTimeMS,
                                InstanceReport.StandardFields.generatedNodes,
                                InstanceReport.StandardFields.expandedNodes,
                                InstanceReport.StandardFields.generatedNodesLowLevel,
                                InstanceReport.StandardFields.expandedNodesLowLevel});
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            fail();
        }

    }

    @Test
    void TestingBenchmarkWInitialRandomRestarts(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 2), null, null, null);
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "/Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
//                if (!instance.name.equals("brc202d-10-8")){
//                    continue;
//                }

                //build report
                InstanceReport report = S_Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                RunParameters runParameters = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP();

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
//                if (useAsserts) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + (timeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = S_Metrics.defaultDateFormat;
            String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
            File directory = new File(resultsOutputDir);
            if (! directory.exists()){
                directory.mkdir();
            }
            String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir, 
                "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() + 
                        "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
            try {
                S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                        new String[]{
                                InstanceReport.StandardFields.instanceName,
                                InstanceReport.StandardFields.numAgents,
                                InstanceReport.StandardFields.timeoutThresholdMS,
                                InstanceReport.StandardFields.solved,
                                InstanceReport.StandardFields.elapsedTimeMS,
                                "Runtime Delta",
                                InstanceReport.StandardFields.solutionCost,
                                "Cost Delta",
                                InstanceReport.StandardFields.totalLowLevelTimeMS,
                                InstanceReport.StandardFields.generatedNodes,
                                InstanceReport.StandardFields.expandedNodes,
                                InstanceReport.StandardFields.generatedNodesLowLevel,
                                InstanceReport.StandardFields.expandedNodesLowLevel});
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            fail();
        }

    }

    @Test
    void TestingBenchmarkWInitialDeterministicRestarts(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.deterministicRescheduling, 2), null, null, null);
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "/Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
//                if (!instance.name.equals("brc202d-10-8")){
//                    continue;
//                }

                //build report
                InstanceReport report = S_Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                RunParameters runParameters = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP();

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
//                if (useAsserts) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + (timeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = S_Metrics.defaultDateFormat;
            String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
            File directory = new File(resultsOutputDir);
            if (! directory.exists()){
                directory.mkdir();
            }
            String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir, 
                "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() + 
                        "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
            try {
                S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                        new String[]{
                                InstanceReport.StandardFields.instanceName,
                                InstanceReport.StandardFields.numAgents,
                                InstanceReport.StandardFields.timeoutThresholdMS,
                                InstanceReport.StandardFields.solved,
                                InstanceReport.StandardFields.elapsedTimeMS,
                                "Runtime Delta",
                                InstanceReport.StandardFields.solutionCost,
                                "Cost Delta",
                                InstanceReport.StandardFields.totalLowLevelTimeMS,
                                InstanceReport.StandardFields.generatedNodes,
                                InstanceReport.StandardFields.expandedNodes,
                                InstanceReport.StandardFields.generatedNodesLowLevel,
                                InstanceReport.StandardFields.expandedNodesLowLevel});
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            fail();
        }
    }

    /**
     * This contains diverse instances, comparing the performance of two algorithms.
     */
    @Test
    void comparativeDiverseTestHasContingencyVsNoContingency(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null);
        String nameBaseline = baselineSolver.name();

        I_Solver competitorSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(null, null, RestartsStrategy.RestartsKind.randomRestarts), null, null, null);
        String nameExperimental = competitorSolver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, new int[]{100}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
//        long timeout = 60 /*seconds*/   *1000L;
        long timeout = 10 /*seconds*/   *1000L;
        int solvedByBaseline = 0;
        int solvedByExperimental = 0;
        int runtimeBaseline = 0;
        int runtimeExperimental = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // run baseline (without the improvement)
            //build report
            InstanceReport reportBaseline = S_Metrics.newInstanceReport();
            reportBaseline.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportBaseline.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportBaseline.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportBaseline.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

            RunParameters runParametersBaseline = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportBaseline).createRP();

            //solve
            Solution solutionBaseline = baselineSolver.solve(instance, runParametersBaseline);

            // run experiment (with the improvement)
            //build report
            InstanceReport reportExperimental = S_Metrics.newInstanceReport();
            reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportExperimental.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

            RunParameters runParametersExperimental = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportExperimental).createRP();

            //solve
            Solution solutionExperimental = competitorSolver.solve(instance, runParametersExperimental);

            // compare

            boolean baselineSolved = solutionBaseline != null;
            solvedByBaseline += baselineSolved ? 1 : 0;
            boolean experimentalSolved = solutionExperimental != null;
            solvedByExperimental += experimentalSolved ? 1 : 0;
            System.out.println(nameBaseline + " Solved?: " + (baselineSolved ? "yes" : "no") +
                    " ; " + nameExperimental + " solved?: " + (experimentalSolved ? "yes" : "no"));

            if(solutionBaseline != null){
                boolean valid = solutionBaseline.solves(instance);
                System.out.print(nameBaseline + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionExperimental != null){
                boolean valid = solutionExperimental.solves(instance);
                System.out.println(" " + nameExperimental + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }
            else System.out.println();

            if(solutionBaseline != null && solutionExperimental != null){
                // runtimes
                runtimeBaseline += reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                runtimeExperimental += reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                reportBaseline.putIntegerValue("Runtime Delta",
                        reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                - reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
            }
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameBaseline + " solved: " + solvedByBaseline);
        System.out.println(nameExperimental + " solved: " + solvedByExperimental);
        System.out.println("runtime totals (instances where both solved) :");
        System.out.println(nameBaseline + " time: " + runtimeBaseline);
        System.out.println(nameExperimental + " time: " + runtimeExperimental);

        //save results
        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir, 
                "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() + 
                        "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
        try {
            S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                    new String[]{
                            InstanceReport.StandardFields.instanceName,
                            InstanceReport.StandardFields.solver,
                            InstanceReport.StandardFields.numAgents,
                            InstanceReport.StandardFields.timeoutThresholdMS,
                            InstanceReport.StandardFields.solved,
                            InstanceReport.StandardFields.elapsedTimeMS,
                            "Runtime Delta",
                            InstanceReport.StandardFields.solutionCost,
                            "Cost Delta",
                            InstanceReport.StandardFields.totalLowLevelTimeMS,
                            InstanceReport.StandardFields.generatedNodes,
                            InstanceReport.StandardFields.expandedNodes,
                            InstanceReport.StandardFields.generatedNodesLowLevel,
                            InstanceReport.StandardFields.expandedNodesLowLevel});
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
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