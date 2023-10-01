package LifelongMAPF;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LifelongStressTest {

    @Test
    public void StressTest() {
        I_Solver solver =
                LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5();
        long timeout = 1000 * 300;

        S_Metrics.clearAll();
        boolean useAsserts = true;

        String nameSolver = solver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "WarehouseMaps"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(null, true),
                new InstanceProperties(null, -1d, new int[]{400, 600}));

        int countSolved = 0;
        int runtime = 0;
        int runtimeLowLevel = 0;
        int ExpantionsHighLevel = 0;
        int ExpantionsLowLevel = 0;
        int sumThroughput = 0;

        MAPF_Instance instance;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // build report
            InstanceReport report = S_Metrics.newInstanceReport();
            report.putStringValue(InstanceReport.StandardFields.experimentName, "StressTest " + nameSolver);
            report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            report.putStringValue(InstanceReport.StandardFields.solver, nameSolver);

            RunParameters runParametersBaseline = new LifelongRunParameters(new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP(),
                    null, 201);

            // solve
            Solution solution = solver.solve(instance, runParametersBaseline);

            // report

            boolean solved = solution != null;
            countSolved += solved ? 1 : 0;
            System.out.println(nameSolver + " Solved?: " + (solved ? "yes" : "no") );

            if(solution != null){
                boolean valid = solution.solves(instance);
                System.out.println(nameSolver + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);

                // runtimes
                runtime += report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                System.out.println(nameSolver + " runtime: " + report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                runtimeLowLevel += report.getIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS);
                System.out.println(nameSolver + " runtime low level: " + report.getIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS));

                // expansions
                if (report.getIntegerValue(InstanceReport.StandardFields.expandedNodes) != null){
                    ExpantionsHighLevel += report.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                    System.out.println(nameSolver + " Expansions High Level: " + report.getIntegerValue(InstanceReport.StandardFields.expandedNodes));
                }
                if (report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel) != null) {
                    ExpantionsLowLevel += report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
                    System.out.println(nameSolver + " Expansions Low Level: " + report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel));
                }

                // cost
                if (report.getIntegerValue("throughputAtT200") != null){
                    sumThroughput += report.getIntegerValue("throughputAtT200");
                    System.out.println(nameSolver + " throughputAtT200: " + report.getIntegerValue("throughputAtT200"));
                }
            }
            System.out.println();
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameSolver + " solved: " + countSolved);
        System.out.println("totals (solved instances) :");
        System.out.println(nameSolver + " avg. throughputAtT200: " + sumThroughput/(float)countSolved);
        System.out.println(nameSolver + " avg. time (ms): " + runtime/(float)countSolved);
        System.out.println(nameSolver + " avg. time low level  (ms): " + runtimeLowLevel/(float)countSolved);
        System.out.println(nameSolver + " avg. expansions high level: " + ExpantionsHighLevel/(float)countSolved);
        System.out.println(nameSolver + " avg. expansions low level: " + ExpantionsLowLevel/(float)countSolved);

        //save results
        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath = resultsOutputDir + "/" + nameSolver + " Stress Test " + dateFormat.format(System.currentTimeMillis()) + ".csv";
        try {
            S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                    new String[]{
                            InstanceReport.StandardFields.instanceName,
                            InstanceReport.StandardFields.solver,
                            InstanceReport.StandardFields.numAgents,
                            InstanceReport.StandardFields.timeoutThresholdMS,
                            InstanceReport.StandardFields.solved,
                            InstanceReport.StandardFields.elapsedTimeMS,
                            InstanceReport.StandardFields.solutionCost,
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
