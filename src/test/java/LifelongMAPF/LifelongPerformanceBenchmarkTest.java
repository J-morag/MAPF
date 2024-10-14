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
import Environment.Metrics.Metrics;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import org.json.JSONArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static BasicMAPF.Solvers.PerformanceBenchmarkTest.addMetric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LifelongPerformanceBenchmarkTest {

    public static final boolean USE_ASSERTS = true;
    public static final long TIMEOUT = 1000 * 500;
    public static final String PATH = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory,
            "MovingAIWarehouseMaps"});

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    public void StressTest() {
        runStressTestWithSolver(LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5());
        runStressTestWithSolver(LifelongSolversFactory.Avoid5ASFP_Cap18_Timeout1p5());
        runStressTestWithSolver(LifelongSolversFactory.PIBT_h10());
    }

    private static void runStressTestWithSolver(I_Solver solver) {
        Metrics.clearAll();
        String nameSolver = solver.name();
        InstanceManager instanceManager = new InstanceManager(PATH, new InstanceBuilder_MovingAI(null, true),
                new InstanceProperties(null, -1d, new int[]{600}));

        int countSolved = 0;
        int countFailed = 0;
        int runtime = 0;
        int runtimeLowLevel = 0;
        int expansionsHighLevel = 0;
        int expansionsLowLevel = 0;
        int sumThroughput = 0;

        MAPF_Instance instance;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // build report
            InstanceReport report = Metrics.newInstanceReport();
            report.putStringValue(InstanceReport.StandardFields.experimentName, "StressTest " + nameSolver);
            report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            report.putStringValue(InstanceReport.StandardFields.solver, nameSolver);

            RunParameters runParametersBaseline = new LifelongRunParameters(new RunParametersBuilder().setTimeout(TIMEOUT).setInstanceReport(report).createRP(),
                    null, 301);

            // solve
            Solution solution = solver.solve(instance, runParametersBaseline);

            // report

            boolean solved = solution != null;
            countSolved += solved ? 1 : 0;
            countFailed += solved ? 0 : 1;
            System.out.println(nameSolver + " Solved?: " + (solved ? "yes" : "no") );

            if(solution != null){
                boolean valid = solution.solves(instance);
                System.out.println(nameSolver + " Valid?: " + (valid ? "yes" : "no"));
                if (USE_ASSERTS) assertTrue(valid);

                // runtimes
                runtime += report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                System.out.println(nameSolver + " runtime: " + report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                runtimeLowLevel += report.getIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS);
                System.out.println(nameSolver + " runtime low level: " + report.getIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS));

                // expansions
                if (report.getIntegerValue(InstanceReport.StandardFields.expandedNodes) != null){
                    expansionsHighLevel += report.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                    System.out.println(nameSolver + " Expansions High Level: " + report.getIntegerValue(InstanceReport.StandardFields.expandedNodes));
                }
                if (report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel) != null) {
                    expansionsLowLevel += report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
                    System.out.println(nameSolver + " Expansions Low Level: " + report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel));
                }

                // cost
                if (report.getIntegerValue("throughputAtT300") != null){
                    sumThroughput += report.getIntegerValue("throughputAtT300");
                    System.out.println(nameSolver + " throughputAtT300: " + report.getIntegerValue("throughputAtT300"));
                }
            }
            System.out.println();
        }

        long timeoutS = TIMEOUT/1000;
        float avgRuntime = runtime/(float)countSolved;
        float avgRuntimeLowLevel = runtimeLowLevel/(float)countSolved;
        float avgExpansionsHighLevel = expansionsHighLevel/(float)countSolved;
        float avgExpansionsLowLevel = expansionsLowLevel/(float)countSolved;
        float avgThroughput = sumThroughput/(float)countSolved;

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + timeoutS);
        System.out.println(nameSolver + " solved: " + countSolved + " (failed: " + countFailed + ")");
        System.out.println("totals (solved instances) :");
        System.out.println(nameSolver + " avg. throughputAtT300: " + avgThroughput);
        System.out.println(nameSolver + " avg. time (ms): " + avgRuntime);
        System.out.println(nameSolver + " avg. time low level  (ms): " + avgRuntimeLowLevel);
        System.out.println(nameSolver + " avg. expansions high level: " + avgExpansionsHighLevel);
        System.out.println(nameSolver + " avg. expansions low level: " + avgExpansionsLowLevel);

        assertEquals(0, countFailed);

        // save results (JSON)

        String jsonOutputDir = IO_Manager.testOut_Directory;
        File directory = new File(jsonOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String outPath = IO_Manager.buildPath(new String[]{jsonOutputDir, "bench-result-" + nameSolver + ".json"});

        // Convert data to JSON
        JSONArray jsonArray = new JSONArray();

        // Create JSON objects for each benchmark metric
        addMetric(jsonArray, nameSolver, "Average Throughput", "Throughput @ T=200", avgThroughput);
        addMetric(jsonArray, nameSolver, "Average Runtime (Reciprocal)", "1 / Milliseconds", 1.0f / avgRuntime);
        addMetric(jsonArray, nameSolver, "Average Runtime Low Level  (Reciprocal)", "1 / Milliseconds", 1.0f / avgRuntimeLowLevel);

        // Writing the JSON array to a file
        System.out.println("Writing results to JSON file: " + outPath);
        try (FileWriter file = new FileWriter(outPath)) {
            file.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
