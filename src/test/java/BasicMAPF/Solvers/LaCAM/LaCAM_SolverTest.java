package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import TransientMAPF.TransientMAPFSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Map;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestUtils.readResultsCSV;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

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
        I_Solver pibt = new PIBT_Solver( null, null);
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
        I_Solver pibt = new PIBT_Solver( null, null);
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
        I_Solver pibt = new PIBT_Solver(null, null);
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
        I_Solver pibt = new PIBT_Solver(null, null);
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
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = LaCAM_Solver;
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance = null;
        // load the pre-made benchmark
        try {
            long timeout = 5 /*seconds*/
                    *1000L;
//            long timeout = 5*60*1000;
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

    @Test
    void compareBetweenPIBTAndLaCAMTest(){
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver LaCAMSolver = new LaCAM_Solver(null, null);
        String nameLaCAM = LaCAMSolver.name();

        I_Solver PIBT_Solver = new PIBT_Solver(null, Integer.MAX_VALUE);
        String namePIBT = PIBT_Solver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, new int[]{100}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
//        long timeout = 60 /*seconds*/   *1000L;
//        long timeout = 10 /*seconds*/   *1000L;
        long timeout = 5*60*1000;
        int solvedByLaCAM = 0;
        int solvedByPIBT = 0;
        int runtimeLaCAM = 0;
        int runtimePIBT = 0;
        float sumCosrLaCAM = 0;
        int sumCostPIBT = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // run LaCAM
            //build report
            InstanceReport reportLaCAM = Metrics.newInstanceReport();
            reportLaCAM.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportLaCAM.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportLaCAM.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportLaCAM.putStringValue(InstanceReport.StandardFields.solver, nameLaCAM);

            RunParameters runParametersLaCAM = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportLaCAM).createRP();

            //solve
            Solution solutionLaCAM = LaCAMSolver.solve(instance, runParametersLaCAM);

            // run PIBT
            //build report
            InstanceReport reportPIBT = Metrics.newInstanceReport();
            reportPIBT.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportPIBT.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportPIBT.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportPIBT.putStringValue(InstanceReport.StandardFields.solver, namePIBT);

            RunParameters runParametersPIBT = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportPIBT).createRP();

            //solve
            Solution solutionPIBT = PIBT_Solver.solve(instance, runParametersPIBT);

            // compare

            boolean LaCAMSolved = solutionLaCAM != null;
            solvedByLaCAM += LaCAMSolved ? 1 : 0;
            boolean PIBTSolved = solutionPIBT != null;
            solvedByPIBT += PIBTSolved ? 1 : 0;
            System.out.println(nameLaCAM + " Solved?: " + (LaCAMSolved ? "yes" : "no") +
                    " ; " + namePIBT + " solved?: " + (PIBTSolved ? "yes" : "no"));

            if(solutionLaCAM != null){
                boolean valid = solutionLaCAM.solves(instance);
                System.out.print(nameLaCAM + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionPIBT != null){
                boolean valid = solutionPIBT.solves(instance);
                System.out.println(" " + namePIBT + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }
            else System.out.println();

            if(solutionLaCAM != null && solutionPIBT != null){
                // runtimes
                runtimeLaCAM += reportLaCAM.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                runtimePIBT += reportPIBT.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                reportLaCAM.putIntegerValue("Runtime Delta",
                        reportPIBT.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                - reportLaCAM.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                // cost
                sumCosrLaCAM += solutionLaCAM.sumIndividualCosts();
                sumCostPIBT += solutionPIBT.sumIndividualCosts();
            }
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameLaCAM + " solved: " + solvedByLaCAM);
        System.out.println(namePIBT + " solved: " + solvedByPIBT);
        System.out.println("runtime totals (instances where both solved) :");
        System.out.println(nameLaCAM + " time: " + runtimeLaCAM);
        System.out.println(namePIBT + " time: " + runtimePIBT);
        System.out.println(nameLaCAM + " avg. cost: " + sumCosrLaCAM);
        System.out.println(namePIBT + " avg. cost: " + sumCostPIBT);

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
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver LaCAMSolver = new LaCAM_Solver(null, null);
        String nameLaCAM = LaCAMSolver.name();

        I_Solver LaCAMStar_Solver = new LaCAMStar_Solver(null, null);
        String nameLaCAMStar = LaCAMStar_Solver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, new int[]{100}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
        long timeout = 10 /*seconds*/   *1000L;
        int solvedByLaCAM = 0;
        int solvedByLaCAMStar = 0;
        int runtimeLaCAM = 0;
        int runtimeLaCAMStar = 0;
        float sumCostLaCAM = 0;
        int sumCostLaCAMStar = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // run LaCAM
            //build report
            InstanceReport reportLaCAM = Metrics.newInstanceReport();
            reportLaCAM.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportLaCAM.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportLaCAM.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportLaCAM.putStringValue(InstanceReport.StandardFields.solver, nameLaCAM);

            RunParameters runParametersLaCAM = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportLaCAM).createRP();

            //solve
            Solution solutionLaCAM = LaCAMSolver.solve(instance, runParametersLaCAM);

            // run LaCAMStar
            //build report
            InstanceReport reportLaCAMStar = Metrics.newInstanceReport();
            reportLaCAMStar.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportLaCAMStar.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportLaCAMStar.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportLaCAMStar.putStringValue(InstanceReport.StandardFields.solver, nameLaCAMStar);

            RunParameters runParametersLaCAMStar = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportLaCAMStar).createRP();

            //solve
            Solution solutionLaCAMStar = LaCAMStar_Solver.solve(instance, runParametersLaCAMStar);

            // compare

            boolean LaCAMSolved = solutionLaCAM != null;
            solvedByLaCAM += LaCAMSolved ? 1 : 0;
            boolean LaCAMStarSolved = solutionLaCAMStar != null;
            solvedByLaCAMStar += LaCAMStarSolved ? 1 : 0;
            System.out.println(nameLaCAM + " Solved?: " + (LaCAMSolved ? "yes" : "no") +
                    " ; " + nameLaCAMStar + " solved?: " + (LaCAMStarSolved ? "yes" : "no"));

            if(solutionLaCAM != null){
                boolean valid = solutionLaCAM.solves(instance);
                System.out.print(nameLaCAM + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionLaCAMStar != null){
                boolean valid = solutionLaCAMStar.solves(instance);
                System.out.println(" " + nameLaCAMStar + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }
            else System.out.println();

            if(solutionLaCAM != null && solutionLaCAMStar != null){
                // runtimes
                runtimeLaCAM += reportLaCAM.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                runtimeLaCAMStar += reportLaCAMStar.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                reportLaCAM.putIntegerValue("Runtime Delta",
                        reportLaCAMStar.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                - reportLaCAM.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                // cost
                sumCostLaCAM += solutionLaCAM.sumIndividualCosts();
                sumCostLaCAMStar += solutionLaCAMStar.sumIndividualCosts();
            }
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameLaCAM + " solved: " + solvedByLaCAM);
        System.out.println(nameLaCAMStar + " solved: " + solvedByLaCAMStar);
        System.out.println("runtime totals (instances where both solved) :");
        System.out.println(nameLaCAM + " time: " + runtimeLaCAM);
        System.out.println(nameLaCAMStar + " time: " + runtimeLaCAMStar);
        System.out.println(nameLaCAM + " avg. cost: " + sumCostLaCAM);
        System.out.println(nameLaCAMStar + " avg. cost: " + sumCostLaCAMStar);

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
