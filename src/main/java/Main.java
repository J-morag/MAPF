import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;
import Environment.RunManagers.A_RunManager;
import Environment.RunManagers.RunManagerSimpleExample;
import Environment.RunManagers.TestingBenchmarkRunManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


/**
 * We wanted to keep {@link #main(String[])} short and simple as possible
 * Things to consider before running:
 *      1. Check that the {@link #resultsOutputDir} is correct
 *      2. Check that {@link #outputResults()} is as you need
 *      3. Running an experiment should be done through {@link A_RunManager},
 *          Solving a single Instance is also possible by giving a path.
 * For more information, view the examples below
 */
public class Main {

    // where to put generated reports. The default is a new folder called CBS_Results, under the user's home directory.
    public static final String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"});
//    public static final String resultsOutputDir = IO_Manager.buildPath(new String[]{   IO_Manager.testResources_Directory +
//                                                                                        "\\Reports default directory"});

    public static void main(String[] args) {
        if(verifyOutputPath()){
            // write the reports to System.out
            addConsoleAsOutputStream();
            // will solve a single instance and print the solution
            solveOneInstanceExample();
            // will solve multiple instances and print a simple report for each instance
            runMultipleExperimentsExample();
            // will solve a set of instances. These instances have known optimal solution costs (found at
            // src\test\resources\TestingBenchmark\Results.csv), and so can be used as a benchmark.
            runTestingBenchmarkExperiment();
            // all examples will also produce a report in CSV format, and save it to resultsOutputDir (see above)
        }
    }

    private static void addConsoleAsOutputStream() {
        try {
            S_Metrics.addOutputStream(System.out, S_Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean verifyOutputPath() {
        File directory = new File(resultsOutputDir);
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

    public static void solveOneInstanceExample(){

        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                                                            "Instances", "BGU_Instances", "den520d-10-0"});
        InstanceManager.InstancePath instancePath = new InstanceManager.InstancePath(path);


        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(null, new InstanceBuilder_BGU());

        MAPF_Instance instance = A_RunManager.getInstanceFromPath(instanceManager, instancePath);

        // Solve
        CBS_Solver solver = new CBS_Solver();
        RunParameters runParameters = new RunParameters();
        Solution solution = solver.solve(instance, runParameters);

        //output results
        System.out.println(solution.readableToString());
        outputResults();
    }

    public static void runMultipleExperimentsExample(){
        RunManagerSimpleExample runManagerSimpleExample = new RunManagerSimpleExample();
        runManagerSimpleExample.runAllExperiments();

        outputResults();
    }

    public static void runTestingBenchmarkExperiment(){
        TestingBenchmarkRunManager testingBenchmarkRunManager = new TestingBenchmarkRunManager();
        testingBenchmarkRunManager.runAllExperiments();

        outputResults();
    }


    /**
     * An example of a simple output of results to a file. It is best to handle this inside your custom
     * {@link A_RunManager run managers} instead.
     * Note that you can add more fields here, if you want metrics that are collected and not exported.
     * Note that you can easily add other metrics which are not currently collected. see {@link S_Metrics}.
     */
    private static void outputResults() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String updatedPath = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + " .csv";
        try {
            S_Metrics.exportCSV(new FileOutputStream(updatedPath),
                    new String[]{   InstanceReport.StandardFields.experimentName,
                                    InstanceReport.StandardFields.mapName,
                                    InstanceReport.StandardFields.instanceName,
                                    InstanceReport.StandardFields.numAgents,
                                    InstanceReport.StandardFields.obstacleRate,
                                    InstanceReport.StandardFields.solver,
                                    InstanceReport.StandardFields.solved,
                                    InstanceReport.StandardFields.valid,
                                    InstanceReport.StandardFields.elapsedTimeMS,
                                    InstanceReport.StandardFields.solutionCost,
                                    InstanceReport.StandardFields.solution});
        } catch (IOException e) {
            e.printStackTrace();
        }
        S_Metrics.clearReports();
    }

}
