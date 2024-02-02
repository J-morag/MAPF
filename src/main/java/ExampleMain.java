import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import Environment.RunManagers.A_RunManager;
import Environment.RunManagers.GenericRunManager;
import Environment.RunManagers.RunManagerSimpleExample;
import Environment.RunManagers.TestingBenchmarkRunManager;
import Environment.Visualization.GridSolutionVisualizer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;

import static Environment.RunManagers.A_RunManager.DEFAULT_RESULTS_OUTPUT_DIR;
import static Environment.RunManagers.A_RunManager.verifyOutputPath;

/**
 * Runs examples of how to use the framework.
 * Good for sanity checks and getting started.
 * Running a real experiment should be done through an implementation of {@link A_RunManager}.
 * Solving a single Instance is also possible by giving a path.
 * For more information, view the examples below.
 */
public class ExampleMain {
    public static void main(String[] args) {
        if (verifyOutputPath(DEFAULT_RESULTS_OUTPUT_DIR)){
            System.out.println("Will save results to the default directory: " + DEFAULT_RESULTS_OUTPUT_DIR);
            System.out.print("Starting in 3 seconds...");
            try {
                Thread.sleep(1000);
                System.out.print(" 2...");
                Thread.sleep(1000);
                System.out.print(" 1...");
                Thread.sleep(1000);
                System.out.println("Running examples...");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // will solve multiple instances and print a simple report for each instance
            runMultipleExperimentsExample();
            // will solve a single instance using a generic run manager, and output a report to file
            runExperimentUsingGenericRunManager();
            // will solve a set of instances. These instances have known optimal solution costs (found at
            // src\test\resources\TestingBenchmark\Results.csv), and so can be used as a benchmark.
            runTestingBenchmarkExperiment();
            // will solve a single instance, print the solution, and start a visualization of the solution
            solveOneInstanceWithVisualizationExample();
            // all examples will also produce a report in CSV format, and save it to resultsOutputDir (see above)
        }
    }

    public static void solveOneInstanceWithVisualizationExample(){
        // write the reports to System.out
        addConsoleAsOutputStream();

        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances", "BGU_Instances", "den520d-10-0"});
        InstanceManager.InstancePath instancePath = new InstanceManager.InstancePath(path);


        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(null, new InstanceBuilder_BGU());

        MAPF_Instance instance = A_RunManager.getInstanceFromPath(instanceManager, instancePath);

        // Solve
        CBS_Solver solver = new CBS_Solver();
        RunParameters runParameters = new RunParametersBuilder().createRP();
        Solution solution = solver.solve(instance, runParameters);

        //output results
        System.out.println(solution.toString());
        outputResults();

        GridSolutionVisualizer.visualizeSolution(instance, solution, solver.name() + " - " + instance.extendedName);
    }

    public static void runMultipleExperimentsExample(){
        RunManagerSimpleExample runManagerSimpleExample = new RunManagerSimpleExample();
        runManagerSimpleExample.runAllExperiments();
    }

    public static void runTestingBenchmarkExperiment(){
        TestingBenchmarkRunManager testingBenchmarkRunManager = new TestingBenchmarkRunManager();
        testingBenchmarkRunManager.runAllExperiments();
    }

    public static void runExperimentUsingGenericRunManager(){
        GenericRunManager genericRunManager = new GenericRunManager(IO_Manager.buildPath( new String[]{
                IO_Manager.resources_Directory,"Instances", "MovingAI_Instances"}), new int[]{10}, new InstanceBuilder_MovingAI(),
                "GenericRunManagerExampleExperiment", true, "random-64-64-20-even-1.scen",
                DEFAULT_RESULTS_OUTPUT_DIR, "genericRMExampleRes", null, null);
        genericRunManager.runAllExperiments();
    }

    private static void addConsoleAsOutputStream() {
        try {
            Metrics.addOutputStream(System.out, Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * An example of a simple output of results to a file. It is best to handle this inside your custom
     * {@link A_RunManager run managers} instead.
     * Note that you can add more fields here, if you want metrics that are collected and not exported.
     * Note that you can easily add other metrics which are not currently collected. see {@link Metrics}.
     */
    private static void outputResults() {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
        String updatedPath =  IO_Manager.buildPath(new String[]{DEFAULT_RESULTS_OUTPUT_DIR, "results " + dateFormat.format(System.currentTimeMillis())}) + " .csv";
        try {
            Metrics.exportCSV(new FileOutputStream(updatedPath),
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
        Metrics.clearReports();
    }

}
