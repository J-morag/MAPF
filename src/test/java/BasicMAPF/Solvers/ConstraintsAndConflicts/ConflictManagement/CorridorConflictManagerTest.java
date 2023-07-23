package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;

import static BasicMAPF.TestConstants.Agents.agent13to10;
import static BasicMAPF.TestConstants.Agents.agent30to33;
import static BasicMAPF.TestConstants.Maps.mapHLong;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests by comparing solutions when using CBS with and without corridor reasoning.
 */
class CorridorConflictManagerTest {

    private MAPF_Instance instanceHFromPaper = new MAPF_Instance("instanceHFromPaper", mapHLong,
            new Agent[]{agent30to33, agent13to10});
    I_Solver corridorSolver = new CBS_Solver(null,null,null,null,null,true, null, null);

    void validate(Solution solution, int numAgents, int optimalSOC, int optimalMakespan, MAPF_Instance instance){
        assertTrue(solution.isValidSolution()); //is valid (no conflicts)
        assertTrue(solution.solves(instance));

        assertEquals(numAgents, solution.size()); // solution includes all agents
        assertEquals(optimalSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(optimalMakespan, solution.makespan()); // makespan is optimal
    }

    @Test
    void HMapFromPaperUsesCorridorReasoning() {
        MAPF_Instance testInstance = instanceHFromPaper;
        InstanceReport instanceReport = new InstanceReport();
        Solution solved = corridorSolver.solve(testInstance, new RunParameters(instanceReport));

        System.out.println(solved.readableToString());
        validate(solved, 2, 14, 9, testInstance);
        // did it correctly use corridor reasoning?
        assertEquals(1, instanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodes));
    }

    /**
     * This contains diverse instances
     */
    @Test
    void comparativeDiverseTest(){
        S_Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver regularCBS = new CBS_Solver(null, null, null,
                null, null, false, null, null);
        String nameBaseline = "regularCBS";
        I_Solver corridorCBS = new CBS_Solver(null, null, null,
                null, null, true, null, null);
        String nameExperimental = "corridorCBS";
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
//                new InstanceProperties(null, -1d, new int[]{5, 10, 15, 20, 25}));
                new InstanceProperties(null, -1d, new int[]{5, 10, 15}));

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
            reportBaseline.putStringValue(InstanceReport.StandardFields.solver, "regularCBS");

            RunParameters runParametersBaseline = new RunParameters(timeout, null, reportBaseline, null);

            //solve
            Solution solutionBaseline = regularCBS.solve(instance, runParametersBaseline);

            // run experimentl (with the improvement)
            //build report
            InstanceReport reportExperimental = S_Metrics.newInstanceReport();
            reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeDiverseTest");
            reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportExperimental.putStringValue(InstanceReport.StandardFields.solver, "corridorCBS");

            RunParameters runParametersExperimental = new RunParameters(timeout, null, reportExperimental, null);

            //solve
            Solution solutionExperimental = corridorCBS.solve(instance, runParametersExperimental);

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

            if(solutionBaseline != null && solutionExperimental != null){
                int optimalCost = solutionBaseline.sumIndividualCosts();
                int costWeGot = solutionExperimental.sumIndividualCosts();
                boolean optimal = optimalCost==costWeGot;
                System.out.println(nameExperimental + " cost is " + (optimal ? "optimal (" + costWeGot +")" :
                        ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                reportBaseline.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                reportExperimental.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                if (useAsserts) assertEquals(optimalCost, costWeGot);

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
    }


}