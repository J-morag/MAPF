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
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class StressTests {

    @Test
    public void CBSStressTest() {
        I_Solver solver = new CBS_Solver();
        long timeout = 1000 * 60;
        int numAgents = 30;
        StressTest(solver, timeout, numAgents);
    }

    @Test
    public void PrioritisedPlanningStressTest() {
        I_Solver solver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.none),
                null, null, null, null, null);
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
        int runtime = 0;
        int runtimeLowLevel = 0;
        int ExpantionsHighLevel = 0;
        int ExpantionsLowLevel = 0;
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
                if (report.getFloatValue(InstanceReport.StandardFields.solutionCost) != null){
                    sumCost += report.getFloatValue(InstanceReport.StandardFields.solutionCost);
                    System.out.println(nameSolver + " Cost: " + report.getFloatValue(InstanceReport.StandardFields.solutionCost));
                }
            }
            System.out.println();
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameSolver + " solved: " + countSolved);
        System.out.println("totals (solved instances) :");
        System.out.println(nameSolver + " avg. cost: " + sumCost/(float)countSolved);
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
