package Environment.RunManagers;

import BasicMAPF.CostFunctions.Makespan;
import BasicMAPF.CostFunctions.MakespanServiceTime;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import Environment.Visualization.I_VisualizeSolution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This in an abstract class that overcomes the need to comment out lines in the 'Main' method
 * Moreover, it focuses the user on what it needs to run an experiment.
 * Any RunManager holds a list of {@link I_Solver} and a list of {@link Experiment}
 */
public abstract class A_RunManager {

    protected List<I_Solver> solvers = new ArrayList<>();
    protected List<Experiment> experiments = new ArrayList<>();

    public static final String DEFAULT_RESULTS_OUTPUT_DIR = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "MAPF_Results"});
    private final String resultsOutputDir;
    protected String resultsFilePrefix = "results";
    protected final I_VisualizeSolution visualizer;

    protected A_RunManager(String resultsOutputDir, I_VisualizeSolution visualizer) {
        this.resultsOutputDir = Objects.requireNonNullElse(resultsOutputDir, DEFAULT_RESULTS_OUTPUT_DIR);
        verifyOutputPath(this.resultsOutputDir);
        this.visualizer = visualizer;
    }
    protected A_RunManager(String resultsOutputDir) {
        this(resultsOutputDir, null);
    }

    public static boolean verifyOutputPath(String path) {
        File directory = new File(path);
        if (! directory.exists()){
            boolean created = directory.mkdir();
            if(!created){
                String errString = "Could not locate or create output directory.";
                System.out.println(errString);
                return false;
            }
        }
        return true;
    }

    abstract void setSolvers();
    abstract void setExperiments();

    /**
     * Runs all experiments.
     * @return true if all solutions are valid, false otherwise.
     */
    public boolean runAllExperiments(){
        setOutputStreamsBeforeRunning();

        setSolvers();
        setExperiments();

        boolean allSolutionsValid = true;

        for ( Experiment experiment : experiments ) {

            allSolutionsValid &= experiment.runExperiment(solvers);

            System.out.println(experiment.experimentName + " - Done!");
        }

        System.out.println("RunAllExperiments - Done!");

        exportAllResults();

        clearMetrics();
        return allSolutionsValid;
    }

    public static MAPF_Instance getInstanceFromPath(InstanceManager manager, InstanceManager.InstancePath absolutePath){
        return manager.getSpecificInstance(absolutePath);
    }

    protected void setOutputStreamsBeforeRunning() {
        // output to stdout while running
        try {
            Metrics.addOutputStream(System.out, Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // output (only the following fields) to csv while running
        try {
            Metrics.setHeader(new String[]{
                    InstanceReport.StandardFields.solver,
                    InstanceReport.StandardFields.experimentName,
                    InstanceReport.StandardFields.mapName,
                    InstanceReport.StandardFields.instanceName,
                    InstanceReport.StandardFields.numAgents,
                    InstanceReport.StandardFields.elapsedTimeMS,
                    InstanceReport.StandardFields.timeoutThresholdMS,
                    InstanceReport.StandardFields.solved,
                    InstanceReport.StandardFields.valid,
                    InstanceReport.StandardFields.solution,
                    InstanceReport.StandardFields.solutionCost,
                    InstanceReport.StandardFields.solutionCostFunction,
                    SumOfCosts.NAME,
                    Makespan.NAME,
                    SumServiceTimes.NAME,
                    MakespanServiceTime.NAME,
                    MakespanServiceTime.NAME,
                    InstanceReport.StandardFields.expandedNodes,
                    InstanceReport.StandardFields.generatedNodes,
                    InstanceReport.StandardFields.startDateTime,
                    InstanceReport.StandardFields.processorInfo,
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        sleepToAvoidOverridingPreviousResultsFiles();

        DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
        String pathWithStartTime = IO_Manager.buildPath(new String[]{resultsOutputDir, "log " + resultsFilePrefix + " " + dateFormat.format(System.currentTimeMillis())}) + " .csv";
        try {
            Metrics.addOutputStream(new FileOutputStream((pathWithStartTime)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void exportAllResults() {
        sleepToAvoidOverridingPreviousResultsFiles();
        DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
        String pathWithEndTime =  IO_Manager.buildPath(new String[]{resultsOutputDir, "res " + resultsFilePrefix + " " + dateFormat.format(System.currentTimeMillis())}) + " .csv";
        try {
            Metrics.exportCSV(new FileOutputStream(pathWithEndTime)            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sleepToAvoidOverridingPreviousResultsFiles() {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected static void clearMetrics() {
        Metrics.clearAll();
    }

}
