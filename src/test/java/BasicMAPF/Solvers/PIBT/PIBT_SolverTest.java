package BasicMAPF.Solvers.PIBT;

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
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Map;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestUtils.readResultsCSV;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PIBT_SolverTest {

    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
    private final MAPF_Instance instanceEmptyEasy = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent04to00});
    private final MAPF_Instance instanceEmptyHarder = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, agent00to10, agent55to34, agent34to32, agent31to14, agent40to02});
    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});
    private final MAPF_Instance instanceAgentsInterruptsEachOther = new MAPF_Instance("instanceAgentsInterruptsEachOther", mapWithPocket, new Agent[]{agent43to53, agent55to34});
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});

    private final MAPF_Instance instanceEmpty2 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to35, agent34to32, agent31to14, agent40to02, agent30to33});

    private final MAPF_Instance instanceEmpty3 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent10to00, agent04to00});

    private final MAPF_Instance instanceMultipleInheritance = new MAPF_Instance("instanceMultipleInheritance", mapHLong, new Agent[]{agent00to13, agent10to33, agent20to00, agent21to00});
    I_Solver PIBT_Solver = new PIBT_Solver(null);

    long timeout = 10*1000;

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
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(35, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(22 , solved.sumServiceTimes());
    }

    @Test
    void emptyMapAgentsWithTheSameGoal() {
        MAPF_Instance testInstance = instanceEmpty3;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(8, solved.sumIndividualCosts());
        assertEquals(4, solved.makespan());
        assertEquals(5 , solved.sumServiceTimes());
    }

    @Test
    void emptyMapValidityTest2() {
        MAPF_Instance testInstance = instanceEmpty2;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(30, solved.sumIndividualCosts());
        assertEquals(6, solved.makespan());
        assertEquals(23 , solved.sumServiceTimes());
    }

    @Test
    void emptyMapHarderValidityTest1() {
        MAPF_Instance testInstance = instanceEmptyHarder;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(14, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(10 , solved.sumServiceTimes());
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(14, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(10 , solved.sumServiceTimes());
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void instanceAgentsInterruptsEachOtherTest() {
        MAPF_Instance testInstance = instanceAgentsInterruptsEachOther;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(5, solved.makespan());
        assertEquals(6 , solved.sumServiceTimes());
    }


    @Test
    void TestingBenchmark(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver solver = PIBT_Solver;
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
    void compareBetweenPrPAndPIBTTest(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver PrPSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null,
                null, new RestartsStrategy(), null, null, null);
        String namePrP = PrPSolver.name();

        I_Solver PIBT_Solver = new PIBT_Solver(null);
        String namePIBT = PIBT_Solver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, new int[]{100}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
//        long timeout = 60 /*seconds*/   *1000L;
        long timeout = 10 /*seconds*/   *1000L;
        int solvedByPrP = 0;
        int solvedByPIBT = 0;
        int runtimePrP = 0;
        int runtimePIBT = 0;
        float sumCostPrP = 0;
        int sumCostPIBT = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // run PrP
            //build report
            InstanceReport reportPrP = S_Metrics.newInstanceReport();
            reportPrP.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportPrP.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportPrP.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportPrP.putStringValue(InstanceReport.StandardFields.solver, namePrP);

            RunParameters runParametersPrP = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportPrP).createRP();

            //solve
            Solution solutionPrP = PrPSolver.solve(instance, runParametersPrP);

            // run PIBT
            //build report
            InstanceReport reportPIBT = S_Metrics.newInstanceReport();
            reportPIBT.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportPIBT.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportPIBT.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportPIBT.putStringValue(InstanceReport.StandardFields.solver, namePIBT);

            RunParameters runParametersPIBT = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportPIBT).createRP();

            //solve
            Solution solutionPIBT = PIBT_Solver.solve(instance, runParametersPIBT);

            // compare

            boolean PrPSolved = solutionPrP != null;
            solvedByPrP += PrPSolved ? 1 : 0;
            boolean PIBTSolved = solutionPIBT != null;
            solvedByPIBT += PIBTSolved ? 1 : 0;
            System.out.println(namePrP + " Solved?: " + (PrPSolved ? "yes" : "no") +
                    " ; " + namePIBT + " solved?: " + (PIBTSolved ? "yes" : "no"));

            if(solutionPrP != null){
                boolean valid = solutionPrP.solves(instance);
                System.out.print(namePrP + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionPIBT != null){
                boolean valid = solutionPIBT.solves(instance);
                System.out.println(" " + namePIBT + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }
            else System.out.println();

            if(solutionPrP != null && solutionPIBT != null){
                // runtimes
                runtimePrP += reportPrP.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                runtimePIBT += reportPIBT.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                reportPrP.putIntegerValue("Runtime Delta",
                        reportPIBT.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                - reportPrP.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                 // cost
                sumCostPrP += solutionPrP.sumIndividualCosts();
                sumCostPIBT += solutionPIBT.sumIndividualCosts();
            }
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(namePrP + " solved: " + solvedByPrP);
        System.out.println(namePIBT + " solved: " + solvedByPIBT);
        System.out.println("runtime totals (instances where both solved) :");
        System.out.println(namePrP + " time: " + runtimePrP);
        System.out.println(namePIBT + " time: " + runtimePIBT);
        System.out.println(namePrP + " avg. cost: " + sumCostPrP);
        System.out.println(namePIBT + " avg. cost: " + sumCostPIBT);

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
    void unsolvableMultipleInheritanceTest() {
        MAPF_Instance testInstance = instanceMultipleInheritance;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        assertNull(solved);
    }

    @Test
    void unsolvable() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

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
        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(5, solved.makespan());
        assertEquals(8 , solved.sumServiceTimes());
    }

    // the following test important to check specific scenario where agent reached his goal,
    // but can't stay in place since there is a constraint
    // so, PIBT(agent, null) should find different node for this agent
    @Test
    void emptyMapValidityStayInPlaceConstraint() {
        MAPF_Instance testInstance = instanceEmpty1;

        I_Coordinate coor33 = new Coordinate_2D(3,3);
        Constraint constraint1 = new Constraint(agent12to33, 5, mapEmpty.getMapLocation(coor33));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);

        Solution solved = PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));

        assertEquals(35, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
        assertEquals(22 , solved.sumServiceTimes());
    }

    // the following test important to check specific scenario where agent reached his goal,
    // but can't stay in place since there is a infinite constraint, a constraint about his final location in future timestamp
    // this scenario throws an UnsupportedOperationException
    @Test
    void emptyMapValidityInfiniteConstraintThrowsError() {
        MAPF_Instance testInstance = instanceEmpty1;

        I_Coordinate coor02 = new Coordinate_2D(0,2);
        Constraint constraint1 = new Constraint(agent33to12, 10, mapEmpty.getMapLocation(coor02));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraint1);

        assertThrows(UnsupportedOperationException.class, () -> {
            PIBT_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setConstraints(constraints).setInstanceReport(instanceReport).createRP());
        });
    }
}
