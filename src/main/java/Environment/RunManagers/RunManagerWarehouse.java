package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.LifelongSimulationSolver;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.stream.IntStream;

public class RunManagerWarehouse extends A_RunManager{

    private final String warehouseMaps;
    private final Integer maxNumAgents;
    String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"});

    public RunManagerWarehouse(String warehouseMaps, Integer maxNumAgents) {
        this.warehouseMaps = warehouseMaps;
        this.maxNumAgents = maxNumAgents;
    }

    @Override
    void setSolvers() {
        super.solvers.add(new LifelongSimulationSolver(new PrioritisedPlanning_Solver(null, null, 2, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true)));
        super.solvers.add(new LifelongSimulationSolver(new CBS_Solver(null, null, null, null, null, null, true, true)));
    }

    @Override
    void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.warehouseMaps);
    }

    /* = Experiments =  */

    private void addAllMapsAndInstances(Integer maxNumAgents, String instancesDir){
        maxNumAgents = maxNumAgents != null ? maxNumAgents : -1;

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{15});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{25,50,75,100,125,150});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60});

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, new InstanceBuilder_Warehouse(),properties);

        /*  =   Add new experiment   =  */
        Experiment EntireMovingAIBenchmark = new Experiment("WarehouseInstances", instanceManager);
        EntireMovingAIBenchmark.keepSolutionInReport = false;
        EntireMovingAIBenchmark.sharedGoals = true;
        EntireMovingAIBenchmark.sharedSources = true;
        this.experiments.add(EntireMovingAIBenchmark);
    }

    @Override
    public void runAllExperiments() {
        try {
            S_Metrics.setHeader(new String[]{
                    InstanceReport.StandardFields.experimentName,
                    InstanceReport.StandardFields.mapName,
                    InstanceReport.StandardFields.instanceName,
                    InstanceReport.StandardFields.numAgents,
                    InstanceReport.StandardFields.solver,
                    InstanceReport.StandardFields.solved,
                    InstanceReport.StandardFields.skipped,
                    InstanceReport.StandardFields.valid,
                    InstanceReport.StandardFields.elapsedTimeMS,
                    InstanceReport.StandardFields.generatedNodes,
                    InstanceReport.StandardFields.expandedNodes,
                    InstanceReport.StandardFields.solutionCost,
                    InstanceReport.StandardFields.solution,
                    "attempt #0 cost",
                    "attempt #1 cost",
                    "attempt #2 cost",
                    "attempt #3 cost",
                    "attempt #4 cost",
                    "attempt #5 cost",
                    "attempt #6 cost",
                    "attempt #7 cost",
                    "attempt #8 cost",
                    "attempt #9 cost",
                    "attempt #0 time",
                    "attempt #1 time",
                    "attempt #2 time",
                    "attempt #3 time",
                    "attempt #4 time",
                    "attempt #5 time",
                    "attempt #6 time",
                    "attempt #7 time",
                    "attempt #8 time",
                    "attempt #9 time",
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String pathWithStartTime = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + " .csv";
        try {
            S_Metrics.addOutputStream(new FileOutputStream((pathWithStartTime)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            S_Metrics.addOutputStream(System.out, S_Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.runAllExperiments();
        String pathWithEndTime = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + " .csv";

        try {
            S_Metrics.exportCSV(new FileOutputStream(pathWithEndTime)
//                    ,new String[]{   InstanceReport.StandardFields.experimentName,
//                            InstanceReport.StandardFields.experimentName,
//                            InstanceReport.StandardFields.numAgents,
//                            InstanceReport.StandardFields.solver,
//                            InstanceReport.StandardFields.solved,
//                            InstanceReport.StandardFields.valid,
//                            InstanceReport.StandardFields.elapsedTimeMS,
//                            InstanceReport.StandardFields.solutionCost,
//                            InstanceReport.StandardFields.solution}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        S_Metrics.clearAll();
    }
}
