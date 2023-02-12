package Environment.RunManagers;

import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;

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

    protected A_RunManager(String resultsOutputDir) {
        this.resultsOutputDir = Objects.requireNonNullElse(resultsOutputDir, DEFAULT_RESULTS_OUTPUT_DIR);
        verifyOutputPath(this.resultsOutputDir);
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

    public void runAllExperiments(){
        setOutputStreamsBeforeRunning();

        setSolvers();
        setExperiments();

        for ( Experiment experiment : experiments ) {

            experiment.runExperiment(solvers);

            System.out.println(experiment.experimentName + " - Done!");
        }

        System.out.println("RunAllExperiments - Done!");

        exportAllResults();

        clearMetrics();
    }

    public static MAPF_Instance getInstanceFromPath(InstanceManager manager, InstanceManager.InstancePath absolutePath){
        return manager.getSpecificInstance(absolutePath);
    }

    protected void setOutputStreamsBeforeRunning() {
        // output to stdout while running
        try {
            S_Metrics.addOutputStream(System.out, S_Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // output (only the following fields) to csv while running
        try {
            S_Metrics.setHeader(new String[]{
                    InstanceReport.StandardFields.experimentName,
                    InstanceReport.StandardFields.mapName,
                    InstanceReport.StandardFields.instanceName,
                    InstanceReport.StandardFields.numAgents,
                    InstanceReport.StandardFields.solver,
                    InstanceReport.StandardFields.solved,
                    InstanceReport.StandardFields.valid,
                    InstanceReport.StandardFields.elapsedTimeMS,
                    InstanceReport.StandardFields.solutionCost,
                    InstanceReport.StandardFields.solution});
        } catch (IOException e) {
            e.printStackTrace();
        }

        sleepToAvoidOverridingPreviousResultsFiles();

        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String pathWithStartTime = IO_Manager.buildPath(new String[]{resultsOutputDir, resultsFilePrefix + " " + dateFormat.format(System.currentTimeMillis())}) + " .csv";
        try {
            S_Metrics.addOutputStream(new FileOutputStream((pathWithStartTime)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void exportAllResults() {
        sleepToAvoidOverridingPreviousResultsFiles();
        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String pathWithEndTime =  IO_Manager.buildPath(new String[]{resultsOutputDir, resultsFilePrefix + " " + dateFormat.format(System.currentTimeMillis())}) + " .csv";
        try {
            S_Metrics.exportCSV(new FileOutputStream(pathWithEndTime)            );
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
        S_Metrics.clearAll();
    }

}
