package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceBenchmarkTest {

    @Test
    public void CBSStressTest() {
        I_Solver solver = new CBS_Solver();
        long timeout = 1000 * 60;
        int numAgents = 30;
        StressTest(solver, timeout, numAgents);
    }

    @Test
    public void ICTSStressTest() {
        I_Solver solver = new ICTS_Solver();
        long timeout = 1000 * 30;
        int numAgents = 30;
        StressTest(solver, timeout, numAgents);
    }

    @Test
    public void PrioritisedPlanningStressTest() {
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.none),
                null, null, null);
        long timeout = 1000 * 30;
        int numAgents = 50;
        StressTest(solver, timeout, numAgents);
    }

    @Test
    public void LNSStressTest() {
        I_Solver solver = new LargeNeighborhoodSearch_Solver();
        long timeout = 1000 * 30;
        int numAgents = 50;
        StressTest(solver, timeout, numAgents);
    }

    @Test
    public void PIBTStressTest() {
        I_Solver solver = new PIBT_Solver(null, null);
        long timeout = 1000 * 30;
        int numAgents = 50;
        StressTest(solver, timeout, numAgents);
    }

    @Test
    public void AStarStressTest() {
        I_Solver solver = new SingleAgentAStar_Solver();
        long timeout = 1000 * 30;
        int numAgents = 1;
        StressTest(solver, timeout, numAgents);
    }
    
    private static void StressTest(I_Solver solver, long timeout, int numAgents) {
        S_Metrics.clearAll();
        boolean useAsserts = true;

        String nameSolver = solver.name();

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, new int[]{numAgents}));

        int countSolved = 0;
        int countFailed = 0;
        int runtime = 0;
        int runtimeLowLevel = 0;
        int expansionsHighLevel = 0;
        int expansionsLowLevel = 0;
        int sumCost = 0;

        MAPF_Instance instance;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");
            
            // build report
            InstanceReport report = S_Metrics.newInstanceReport();
            report.putStringValue(InstanceReport.StandardFields.experimentName, "StressTest " + nameSolver);
            report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            report.putStringValue(InstanceReport.StandardFields.solver, nameSolver);

            RunParameters runParametersBaseline = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP();

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
                if (useAsserts) assertTrue(valid);
                
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
                sumCost += solution.sumIndividualCosts();
                System.out.println(nameSolver + " Cost: " + solution.sumIndividualCosts());
            }
            System.out.println();
        }

        long timeoutS = timeout/1000;
        float avgCost = sumCost/(float)countSolved;
        float avgRuntime = runtime/(float)countSolved;
        float avgRuntimeLowLevel = runtimeLowLevel/(float)countSolved;
        float avgExpansionsHighLevel = expansionsHighLevel/(float)countSolved;
        float avgExpansionsLowLevel = expansionsLowLevel/(float)countSolved;

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + timeoutS);
        System.out.println(nameSolver + " solved: " + countSolved + " (failed: " + countFailed + ")");
        System.out.println("totals (solved instances) :");
        System.out.println(nameSolver + " avg. cost: " + avgCost);
        System.out.println(nameSolver + " avg. time (ms): " + avgRuntime);
        System.out.println(nameSolver + " avg. time low level  (ms): " + avgRuntimeLowLevel);
        System.out.println(nameSolver + " avg. expansions high level: " + avgExpansionsHighLevel);
        System.out.println(nameSolver + " avg. expansions low level: " + avgExpansionsLowLevel);

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
        addMetric(jsonArray, nameSolver, "Fails", "Instances", countFailed);
        addMetric(jsonArray, nameSolver, "Average Cost", "SOC", avgCost);
        addMetric(jsonArray, nameSolver, "Average Runtime", "Milliseconds", avgRuntime);
        addMetric(jsonArray, nameSolver, "Average Runtime Low Level", "Milliseconds", avgRuntimeLowLevel);
        addMetric(jsonArray, nameSolver, "Average Expansions High Level", "Expansions", avgExpansionsHighLevel);
        addMetric(jsonArray, nameSolver, "Average Expansions Low Level", "Expansions", avgExpansionsLowLevel);

        // Writing the JSON array to a file
        System.out.println("Writing results to JSON file: " + outPath);
        try (FileWriter file = new FileWriter(outPath)) {
            file.write(jsonArray.toString(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addMetric(JSONArray jsonArray, String nameSolver, String metricName, String unit, int value) {
        JSONObject jsonCountSolved = new JSONObject();
        jsonCountSolved.put("name", nameSolver + " - " + metricName);
        jsonCountSolved.put("unit", unit);
        jsonCountSolved.put("value", value);
        jsonArray.put(jsonCountSolved);
    }
    private static void addMetric(JSONArray jsonArray, String nameSolver, String metricName, String unit, float value) {
        JSONObject jsonCountSolved = new JSONObject();
        jsonCountSolved.put("name", nameSolver + " - " + metricName);
        jsonCountSolved.put("unit", unit);
        jsonCountSolved.put("value", value);
        jsonArray.put(jsonCountSolved);
    }
}
