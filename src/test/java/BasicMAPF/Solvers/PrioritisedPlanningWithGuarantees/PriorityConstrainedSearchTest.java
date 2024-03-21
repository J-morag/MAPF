package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

import static BasicMAPF.TestConstants.Agents.agent43to53;
import static BasicMAPF.TestConstants.Agents.agent55to34;
import static BasicMAPF.TestConstants.Coordiantes.coor12;
import static BasicMAPF.TestConstants.Coordiantes.coor33;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Maps.mapCircle;
import static BasicMAPF.TestConstants.Maps.mapWithPocket;
import static BasicMAPF.TestUtils.readResultsCSV;
import static org.junit.jupiter.api.Assertions.*;

class PriorityConstrainedSearchTest {

    private final MAPF_Instance instanceUnsolvableBecauseOrderWithInfiniteWait = new MAPF_Instance("instanceUnsolvableWithInfiniteWait", mapWithPocket, new Agent[]{agent43to53, agent55to34});

    I_Solver PCSSolver = new PCSBuilder().createPCS();

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
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(8, solved.makespan());
    }

    @Test
    void failsBeforeTimeoutWhenUnsolvableOrder() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        InstanceReport instanceReport = new InstanceReport();
        long timeout = 10*1000;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
    }

    @Test
    void unsolvable() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertNull(solved);
    }


    @Test
    void differentOrders() {
        MAPF_Instance testInstance = instanceCircle1;
        I_Solver solver = PCSSolver;

        Agent agent0 = new Agent(0, coor33, coor12);
        Agent agent1 = new Agent(1, coor12, coor33);

        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent1, agent0});
        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(agent0prioritisedSolution.solves(testInstance));
        assertTrue(agent1prioritisedSolution.solves(testInstance));

        assertEquals(3, agent0prioritisedSolution.getPlanFor(agent0).size());
        assertEquals(3, agent1prioritisedSolution.getPlanFor(agent1).size());
    }

    // TODO add PBS paper examples

    @Test
    void TestingBenchmark(){
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = PCSSolver;
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
                //build report
                InstanceReport report = Metrics.newInstanceReport();
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

                System.out.printf("Time(ms): %,d%n", report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                System.out.printf("Expanded nodes: %,d%n", report.getIntegerValue(InstanceReport.StandardFields.expandedNodes));
                System.out.printf("Generated nodes: %,d%n", report.getIntegerValue(InstanceReport.StandardFields.generatedNodes));
                System.out.printf("Expanded nodes (low level): %,d%n", report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel));
                System.out.printf("Generated nodes (low level): %,d%n", report.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel));

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (!valid) {
                        System.out.println("reason: " + solution.firstConflict());
                        System.out.println("solution: " + solution);
                    }
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                    if (useAsserts && costWeGot < optimalCost)
                        fail("cost is impossibly low"); // actually shouldn't happen as long as solution solves the problem

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
            DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
            String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
            File directory = new File(resultsOutputDir);
            if (! directory.exists()){
                directory.mkdir();
            }
            String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir,
                    "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() +
                            "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
            try {
                Metrics.exportCSV(new FileOutputStream(updatedPath),
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
    void comparativeDiverseTestVsPP(){
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver baselineSolver = new PrioritisedPlanning_Solver(new SingleAgentAStarSIPP_Solver());
        String nameBaseline = baselineSolver.name();

        I_Solver competitorSolver = PCSSolver;
        String nameExperimental = competitorSolver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
//                new InstanceProperties(null, -1d, new int[]{100}));
                new InstanceProperties(null, -1d, new int[]{15}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
        long timeout = 3 /*seconds*/   *1000L;
        int solvedByBaseline = 0;
        int solvedByExperimental = 0;
        int runtimeBaseline = 0;
        int runtimeExperimental = 0;
        int sumCostBaseline = 0;
        int sumCostExperimental = 0;
        int sumHighLevelExpandedExperimentalOnAll = 0;
        int sumHighLevelExpandedExperimentalOnSolved = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            for (int j = 0; j < 5; j++) {
                System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");
                List<Agent> order = new ArrayList<>(instance.agents);
                Random rand = new Random(j);
                Collections.shuffle(order, rand);
                Agent[] orderedAgents = order.toArray(new Agent[0]);
                System.out.println("order: " + Arrays.toString(orderedAgents));

                // run baseline (without the improvement)
                //build report
                InstanceReport reportBaseline = Metrics.newInstanceReport();
                reportBaseline.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
                reportBaseline.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                reportBaseline.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                reportBaseline.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

                RunParameters runParametersBaseline = new RunParametersBuilder().setTimeout(timeout)
                        .setInstanceReport(reportBaseline).setPriorityOrder(orderedAgents).createRP();

                //solve
                Solution solutionBaseline = baselineSolver.solve(instance, runParametersBaseline);

                // run experiment (with the improvement)
                //build report
                InstanceReport reportExperimental = Metrics.newInstanceReport();
                reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
                reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                reportExperimental.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

                RunParameters runParametersExperimental = new RunParametersBuilder().setTimeout(timeout)
                        .setInstanceReport(reportExperimental).setPriorityOrder(orderedAgents).createRP();

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
                    sumHighLevelExpandedExperimentalOnSolved += reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                }
                else System.out.println();

                sumHighLevelExpandedExperimentalOnAll += reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                System.out.println("Expanded nodes: "+ nameExperimental + ": " + reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes));

                if(solutionBaseline != null && solutionExperimental != null){
                    // runtimes
                    runtimeBaseline += reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                    runtimeExperimental += reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                    reportBaseline.putIntegerValue("Runtime Delta",
                            reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                    - reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));

                    // cost
                    int costBaseline = solutionBaseline.sumIndividualCosts();
                    sumCostBaseline += costBaseline;
                    int costExperimental = solutionExperimental.sumIndividualCosts();
                    sumCostExperimental += costExperimental;
                    if (costExperimental > costBaseline){
                        System.out.println(nameBaseline + " cost: " + costBaseline);
                        System.out.println(nameExperimental + " cost: " + costExperimental);
                        for (Agent agent : instance.agents) {
                            SingleAgentPlan planBaseline = solutionBaseline.getPlanFor(agent);
                            SingleAgentPlan planExperimental = solutionExperimental.getPlanFor(agent);
                            if (planBaseline.size() < planExperimental.size()) {
                                for (int i = 1; i <= planBaseline.getEndTime(); i++) {
                                    Move moveBaseline = planBaseline.moveAt(i);
                                    Move moveExperimental = planExperimental.moveAt(i);
                                    if (!moveBaseline.equals(moveExperimental)){
                                        System.out.println("agent " + agent.iD + " has different moves at time " + i + ": " +
                                                moveBaseline.toString().replace("\n", "") + " vs " + moveExperimental.toString().replace("\n", ""));
                                    }
                                }
                                System.out.println("agent " + agent.iD + " has different plan lengths: " +
                                        planBaseline.size() + " vs " + planExperimental.size()
                                        + " with plans: \n" + planBaseline + "\nvs\n" + planExperimental);
                            }
                        }
                    }
                    if (useAsserts) // PCS should be priority-optimal
                        assertTrue(costBaseline >= costExperimental);
                }
            }
        }

        outputResults(timeout, nameBaseline, solvedByBaseline, nameExperimental, solvedByExperimental, sumHighLevelExpandedExperimentalOnAll, runtimeBaseline, runtimeExperimental, sumCostBaseline, sumCostExperimental, sumHighLevelExpandedExperimentalOnSolved);
    }

    /**
     * This contains diverse instances, comparing the performance of two algorithms.
     */
    @Test
    @Disabled
    void comparativeDiverseTestAllFeaturesOffVsOn(){
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver baselineSolver = new PCSBuilder().setUseSimpleMDDCache(false).setUsePartialGeneration(false).setPCSHeuristic(new PCSHeuristicDefault()).createPCS();
        String nameBaseline = "PCS-allFeaturesOff";

        I_Solver competitorSolver = new PCSBuilder().setUseSimpleMDDCache(true).setUsePartialGeneration(true).setMDDCacheDepthDeltaMax(Integer.MAX_VALUE).setPCSHeuristic(new PCSHeuristicSIPP()).createPCS();
        String nameExperimental = "PCS-allFeaturesOn";

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
//                new InstanceProperties(null, -1d, new int[]{100}));
                new InstanceProperties(null, -1d, new int[]{15}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
        long timeout = 3 /*seconds*/   *1000L;
        int solvedByBaseline = 0;
        int solvedByExperimental = 0;
        int runtimeBaseline = 0;
        int runtimeExperimental = 0;
        int sumCostBaseline = 0;
        int sumCostExperimental = 0;
        int sumHighLevelExpandedExperimentalOnAll = 0;
        int sumHighLevelExpandedExperimentalOnSolved = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            for (int j = 0; j < 5; j++) {
                System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");
                List<Agent> order = new ArrayList<>(instance.agents);
                Random rand = new Random(j);
                Collections.shuffle(order, rand);
                Agent[] orderedAgents = order.toArray(new Agent[0]);
                System.out.println("order: " + Arrays.toString(orderedAgents));

                // run baseline (without the improvement)
                //build report
                InstanceReport reportBaseline = Metrics.newInstanceReport();
                reportBaseline.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
                reportBaseline.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                reportBaseline.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                reportBaseline.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

                RunParameters runParametersBaseline = new RunParametersBuilder().setTimeout(timeout)
                        .setInstanceReport(reportBaseline).setPriorityOrder(orderedAgents).createRP();

                //solve
                Solution solutionBaseline = baselineSolver.solve(instance, runParametersBaseline);

                // run experiment (with the improvement)
                //build report
                InstanceReport reportExperimental = Metrics.newInstanceReport();
                reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
                reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                reportExperimental.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

                RunParameters runParametersExperimental = new RunParametersBuilder().setTimeout(timeout)
                        .setInstanceReport(reportExperimental).setPriorityOrder(orderedAgents).createRP();

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
                    sumHighLevelExpandedExperimentalOnSolved += reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                }
                else System.out.println();

                sumHighLevelExpandedExperimentalOnAll += reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                System.out.println("Expanded nodes: "+ nameExperimental + ": " + reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes));

                if(solutionBaseline != null && solutionExperimental != null){
                    // runtimes
                    runtimeBaseline += reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                    runtimeExperimental += reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                    reportBaseline.putIntegerValue("Runtime Delta",
                            reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                    - reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));

                    // cost
                    int costBaseline = solutionBaseline.sumIndividualCosts();
                    sumCostBaseline += costBaseline;
                    int costExperimental = solutionExperimental.sumIndividualCosts();
                    sumCostExperimental += costExperimental;
                    if (costExperimental > costBaseline){
                        System.out.println(nameBaseline + " cost: " + costBaseline);
                        System.out.println(nameExperimental + " cost: " + costExperimental);
                        for (Agent agent : instance.agents) {
                            SingleAgentPlan planBaseline = solutionBaseline.getPlanFor(agent);
                            SingleAgentPlan planExperimental = solutionExperimental.getPlanFor(agent);
                            if (planBaseline.size() < planExperimental.size()) {
                                for (int i = 1; i <= planBaseline.getEndTime(); i++) {
                                    Move moveBaseline = planBaseline.moveAt(i);
                                    Move moveExperimental = planExperimental.moveAt(i);
                                    if (!moveBaseline.equals(moveExperimental)){
                                        System.out.println("agent " + agent.iD + " has different moves at time " + i + ": " +
                                                moveBaseline.toString().replace("\n", "") + " vs " + moveExperimental.toString().replace("\n", ""));
                                    }
                                }
                                System.out.println("agent " + agent.iD + " has different plan lengths: " +
                                        planBaseline.size() + " vs " + planExperimental.size()
                                        + " with plans: \n" + planBaseline + "\nvs\n" + planExperimental);
                            }
                        }
                    }
                    if (useAsserts)
                        assertEquals(costBaseline, costExperimental);
                }
            }
        }

        outputResults(timeout, nameBaseline, solvedByBaseline, nameExperimental, solvedByExperimental, sumHighLevelExpandedExperimentalOnAll, runtimeBaseline, runtimeExperimental, sumCostBaseline, sumCostExperimental, sumHighLevelExpandedExperimentalOnSolved);
    }

    private void outputResults(long timeout, String nameBaseline, int solvedByBaseline, String nameExperimental, int solvedByExperimental, int sumHighLevelExpandedExperimentalOnAll, int runtimeBaseline, int runtimeExperimental, int sumCostBaseline, int sumCostExperimental, int sumHighLevelExpandedExperimentalOnSolved) {
        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout / 1000));
        System.out.println(nameBaseline + " solved: " + solvedByBaseline);
        System.out.println(nameExperimental + " solved: " + solvedByExperimental);
        System.out.println(nameExperimental + " expanded nodes: " + sumHighLevelExpandedExperimentalOnAll);

        System.out.println("totals (on instances where both solved) :");
        System.out.println(nameBaseline + " time: " + runtimeBaseline);
        System.out.println(nameExperimental + " time: " + runtimeExperimental);
        System.out.println(nameBaseline + " avg. cost: " + sumCostBaseline);
        System.out.println(nameExperimental + " avg. cost: " + sumCostExperimental);
        System.out.println(nameExperimental + " avg. expanded nodes: " + sumHighLevelExpandedExperimentalOnSolved);

        //save results
        DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir,
                "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() +
                        "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
        try {
            Metrics.exportCSV(new FileOutputStream(updatedPath),
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

}