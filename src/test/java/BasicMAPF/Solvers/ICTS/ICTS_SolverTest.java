package BasicMAPF.Solvers.ICTS;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.MapDimensions;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.*;
import static org.junit.jupiter.api.Assertions.*;

class ICTS_SolverTest {

    InstanceBuilder_BGU builder = new InstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00});
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});
    private MAPF_Instance instanceSmallMaze = new MAPF_Instance("instanceUnsolvable2", mapSmallMaze, new Agent[]{agent04to00, agent00to10});
    private MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});

    I_Solver ictsSolver = new ICTS_Solver();

    @BeforeEach
    void setUp() {

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
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 7, solved.sumIndividualCosts(),solved.makespan(), testInstance); //need to find actual optimal costs
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 2, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 2, 8, 5, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 2, 6, 4, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeout() {
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

//
//    @Test
//    void cbsWithPriorities() {
//        I_Solver solver = new CBS_Solver(null, null, null,
//                (solution, cbs) -> solution.sumIndividualCostsWithPriorities(), null);
//        InstanceReport instanceReport = new InstanceReport();
//
//        Agent agent0 = new Agent(0, coor33, coor12, 10);
//        Agent agent1 = new Agent(1, coor12, coor33, 1);
//
//        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
//        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParameters(instanceReport));
//
//        agent0 = new Agent(0, coor33, coor12, 1);
//        agent1 = new Agent(1, coor12, coor33, 10);
//
//        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent0, agent1});
//        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParameters(instanceReport));
//
//        System.out.println(agent0prioritisedSolution.readableToString());
//        validate(agent0prioritisedSolution, 2, 8, 5, agent0prioritisedInstance);
//
//        System.out.println(agent1prioritisedSolution.readableToString());
//        validate(agent1prioritisedSolution, 2, 8, 5, agent1prioritisedInstance);
//
//        // check that agents were logically prioritised to minimise cost with priorities
//
//        assertEquals(agent0prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
//        assertEquals(agent0prioritisedSolution.getPlanFor(agent0).size(), 3);
//
//        assertEquals(agent1prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
//        assertEquals(agent1prioritisedSolution.getPlanFor(agent1).size(), 3);
//    }
//
//    @Test
//    void cbsWithPrioritiesUsingBuilder() {
//        boolean useAsserts = true;
//
//        I_Solver solver = new CBS_Solver(null, null, null,
//                (solution, cbs) -> solution.sumIndividualCostsWithPriorities(), null);
//        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
//                "TestingBenchmark"});
//        InstanceManager instanceManager = new InstanceManager(path,
//                new InstanceBuilder_BGU(new Priorities(Priorities.PrioritiesPolicy.ROUND_ROBIN, new int[]{1, 3, 5})));
//
//        MAPF_Instance instance = null;
//        long timeout = 30 /*seconds*/
//                *1000L;
//
//        // run all benchmark instances. this code is mostly copied from Environment.Experiment.
//        while ((instance = instanceManager.getNextInstance()) != null) {
//            InstanceReport report = new InstanceReport();
//
//            RunParameters runParameters = new RunParameters(timeout, null, report, null);
//
//            //solve
//            System.out.println("---------- solving "  + instance.name + " ----------");
//            Solution solution = solver.solve(instance, runParameters);
//
//            // validate
//            boolean solved = solution != null;
//            System.out.println("Solved?: " + (solved ? "yes" : "no"));
//
//            if(solution != null){
//                boolean valid = solution.solves(instance);
//                System.out.println("Valid?: " + (valid ? "yes" : "no"));
//                if (useAsserts) assertTrue(valid);
//            }
//        }
//    }

    @Test
    void TestingBenchmark(){
        S_Metrics.clearAll();
        boolean useAsserts = false;

        I_Solver solver = ictsSolver;
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

                RunParameters runParameters = new RunParameters(timeout, null, report, null);

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
                if (useAsserts) assertNotNull(solution);
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
                    if (useAsserts) assertEquals(optimalCost, costWeGot);

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
            String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "CBS_Tests"});
            File directory = new File(resultsOutputDir);
            if (! directory.exists()){
                directory.mkdir();
            }
            String updatedPath = resultsOutputDir + "/Results " + dateFormat.format(System.currentTimeMillis()) + ".csv";
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

    private Map<String, Map<String, String>> readResultsCSV(String pathToCsv) throws IOException {
        Map<String, Map<String, String>> result  = new HashMap<>();
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));

        String headerRow = csvReader.readLine();
        String[] header = headerRow.split(",");
        int fileNameIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if(header[i].equals("File")) {fileNameIndex = i;}
        }

        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] tupleAsArray = row.split(",");
            if(tupleAsArray.length < 1 ) continue;
            Map<String, String> tupleAsMap = new HashMap<>(tupleAsArray.length);
            for (int i = 0; i < tupleAsArray.length; i++) {
                String value = tupleAsArray[i];
                tupleAsMap.put(header[i], value);
            }

            String key = tupleAsArray[fileNameIndex];
            result.put(key, tupleAsMap);
        }
        csvReader.close();

        return result;
    }


    /**
     * This contains diverse instances
     */
    @Test
    void comparativeDiverseTest(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver cbs = new CBS_Solver();
        String nameBaseline = "cbs";
        I_Solver icts = new ICTS_Solver();
        String nameExperimental = "ICTS";
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
//                new InstanceProperties(null, -1d, new int[]{5, 10, 15, 20, 25}));
                new InstanceProperties(null, -1d, new int[]{5, 10}));

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

            RunParameters runParametersBaseline = new RunParameters(timeout, null, reportBaseline, null);

            //solve
            Solution solutionBaseline = cbs.solve(instance, runParametersBaseline);

            // run experimentl (with the improvement)
            //build report
            InstanceReport reportExperimental = S_Metrics.newInstanceReport();
            reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportExperimental.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

            RunParameters runParametersExperimental = new RunParameters(timeout, null, reportExperimental, null);

            //solve
            Solution solutionExperimental = icts.solve(instance, runParametersExperimental);

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
                int optimalCost = solutionBaseline.sumIndividualCosts();
                int costWeGot = solutionExperimental.sumIndividualCosts();
                boolean optimal = optimalCost==costWeGot;
                System.out.println(nameExperimental + " cost is " + (optimal ? "optimal (" + costWeGot +")" :
                        ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                reportBaseline.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                reportExperimental.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                if (useAsserts) assertTrue(optimal);

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
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "CBS_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath = resultsOutputDir + "/Results " + dateFormat.format(System.currentTimeMillis()) + ".csv";
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

}