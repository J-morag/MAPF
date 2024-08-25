package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.LaCAM.LaCAM_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LNSBuilder;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSBuilder;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static BasicMAPF.TestUtils.addRandomConstraints;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PerformanceBenchmarkTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    public void CBSStressTest() {
        I_Solver solver = new CBSBuilder().createCBS_Solver();
        long timeout = 1000 * 30;
        int numAgents = 30;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void CBS_SIPPStressTest() {
        CBS_Solver solver = new CBSBuilder().setLowLevelSolver(new SingleAgentAStarSIPP_Solver()).createCBS_Solver();
        solver.name = "CBS_SIPP";
        long timeout = 1000 * 30;
        int numAgents = 30;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void ICTSStressTest() {
        I_Solver solver = new ICTS_Solver();
        long timeout = 1000 * 30;
        int numAgents = 30;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void PCSStressTest() {
        I_Solver solver = new PCSBuilder().createPCS();
        long timeout = 1000 * 30;
        int numAgents = 20;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void PrioritisedPlanningStressTest() {
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 9, RestartsStrategy.RestartsKind.none),
                null, null, null);
        long timeout = 1000 * 30;
        int numAgents = 100;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void LNSStressTest() {
        I_Solver solver = new LNSBuilder().createLNS();
        long timeout = 1000 * 30;
        int numAgents = 100;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void PIBTStressTest() {
        I_Solver solver = new PIBT_Solver(null, null);
        long timeout = 1000 * 30;
        int numAgents = 500;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void LaCAMStressTest() {
        I_Solver solver = new LaCAM_Solver();
        long timeout = 1000 * 30;
        int numAgents = 500;
        stressTest(solver, timeout, numAgents, false);
    }

    @Test
    public void AStarStressTest() {
        I_Solver solver = new SingleAgentAStar_Solver();
        long timeout = 1000 * 10;
        int numAgents = 5;
        stressTest(solver, timeout, numAgents, true);
    }

    @Test
    public void SIPPStressTest() {
        I_Solver solver = new SingleAgentAStarSIPP_Solver();
        long timeout = 1000 * 10;
        int numAgents = 5;
        stressTest(solver, timeout, numAgents, true);
    }
    
    private static void stressTest(I_Solver solver, long timeout, int numAgents, boolean singleAgentSolver) {
        Metrics.clearAll();
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
            if (singleAgentSolver){
                for (Agent agent: instance.agents){
                    MAPF_Instance subproblem = instance.getSubproblemFor(agent);
                    ConstraintSet constraints = new ConstraintSet();
                    List<I_Location> allLocations = new ArrayList<>(((I_ExplicitMap)subproblem.map).getAllLocations());
                    addRandomConstraints(agent, allLocations, new Random(42),
                            constraints, 300, allLocations.size()/20);

                    InstanceReport report = Metrics.newInstanceReport();
                    Solution solution = solveOneInstance(report, nameSolver, subproblem, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report)
                            .setConstraints(constraints).createRP(), solver, subproblem);

                    // report

                    boolean solved = solution != null;
                    countSolved += solved ? 1 : 0;
                    countFailed += solved ? 0 : 1;
                    System.out.println(nameSolver + " Solved?: " + (solved ? "yes" : "no") );

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

                    if(solution != null){
                        boolean valid = solution.solves(subproblem);
                        System.out.println(nameSolver + " Valid?: " + (valid ? "yes" : "no"));
                        if (useAsserts) assertTrue(valid);

                        // cost
                        sumCost += solution.sumIndividualCosts();
                        System.out.println(nameSolver + " Cost: " + solution.sumIndividualCosts());
                    }
                    System.out.println();
                }
            }
            else {
                InstanceReport report = Metrics.newInstanceReport();
                // build report
                Solution solution = solveOneInstance(report, nameSolver, instance,
                        new RunParametersBuilder().setTimeout(timeout).setInstanceReport(report).createRP(), solver, instance);

                // report

                boolean solved = solution != null;
                countSolved += solved ? 1 : 0;
                countFailed += solved ? 0 : 1;
                System.out.println(nameSolver + " Solved?: " + (solved ? "yes" : "no") );

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

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println(nameSolver + " Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);

                    // cost
                    sumCost += solution.sumIndividualCosts();
                    System.out.println(nameSolver + " Cost: " + solution.sumIndividualCosts());
                }
                System.out.println();
            }
        }

        long timeoutS = timeout/1000;
        float avgRuntime = runtime/(float)(countSolved+countFailed);
        float avgRuntimeLowLevel = runtimeLowLevel/(float)(countSolved+countFailed);
        float avgExpansionsHighLevel = expansionsHighLevel/(float)(countSolved+countFailed);
        float avgExpansionsLowLevel = expansionsLowLevel/(float)(countSolved+countFailed);

        float avgCost = sumCost/(float)countSolved;

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + timeoutS);
        System.out.println(nameSolver + " solved: " + countSolved + " (failed: " + countFailed + ")");
        System.out.println("totals (all) :");
        System.out.println(nameSolver + " avg. time (ms): " + avgRuntime);
        System.out.println(nameSolver + " avg. time low level  (ms): " + avgRuntimeLowLevel);
        System.out.println(nameSolver + " avg. expansions high level: " + avgExpansionsHighLevel);
        System.out.println(nameSolver + " avg. expansions low level: " + avgExpansionsLowLevel);
        System.out.println("totals (solved instances) :");
        System.out.println(nameSolver + " avg. cost: " + avgCost);

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

    private static Solution solveOneInstance(InstanceReport report, String nameSolver, MAPF_Instance instance, RunParameters rp, I_Solver solver, MAPF_Instance subproblem) {
        // build report
        report.putStringValue(InstanceReport.StandardFields.experimentName, "StressTest " + nameSolver);
        report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
        report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
        report.putStringValue(InstanceReport.StandardFields.solver, nameSolver);

        // solve
        System.out.println("---------- solving " + instance.extendedName + " with " + instance.agents.size() + " agents ----------");
        return solver.solve(subproblem, rp);
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
