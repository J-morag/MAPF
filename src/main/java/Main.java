import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;
import Environment.RunManagers.*;
import Environment.Visualization.I_VisualizeSolution;
import Environment.Visualization.GridSolutionVisualizer;
import LifelongMAPF.LifelongRunManagers.LifelongGenericRunManager;
import org.apache.commons.cli.*;
import LifelongMAPF.LifelongRunManagers.LifelongRunManagerMovingAI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;

import static Environment.RunManagers.A_RunManager.verifyOutputPath;


/**
 * We wanted to keep {@link #main(String[])} short and simple as possible
 * Things to consider before running:
 *      1. Check that the {@link #exampleResultsOutputDir} is correct
 *      2. Check that {@link #outputResults()} is as you need
 *      3. Running an experiment should be done through a {@link A_RunManager}.
 *          Solving a single Instance is also possible by giving a path.
 * For more information, view the examples below
 */
public class Main {

    // where to put generated reports. The default is a new folder called MAPF_Results, under the user's home directory.
    public static final String exampleResultsOutputDir = A_RunManager.DEFAULT_RESULTS_OUTPUT_DIR;
    public static final String STR_AGENT_NUMS = "agentNums";
    private static final String STR_MOVING_AI = "MovingAI";
    private static final String STR_BGU = "BGU";
    public static final String STR_INSTANCES_DIR = "instancesDir";
    public static final String STR_INSTANCES_REGEX = "instancesRegex";
    private static final String STR_WAREHOUSE = "Warehouse";
    private static final String STR_RESULTS_DIR_OPTION = "resultsOutputDir";
    private static final String STR_RESULTS_FILE_PREFIX = "resultsFilePrefix";

    public static void main(String[] args) {
        if (args.length > 0){
            CLIMain(args);
        }
        else {
            // Example
            staticMain();
        }
    }

    private static void CLIMain(String[] args) {
        Options options = new Options();

        Option skipOption = new Option("s", "skipAfterFail", false,
                "To skip attempting the same instance with the same solver, but with more agents, if we already failed with less agents.");
        options.addOption(skipOption);

        Option visualiseOption = new Option("v", "visualise", false,
                "To visualise the solution. Only  works with grid maps!");
        options.addOption(visualiseOption);

        Option lifelongOption = new Option("l", "lifelong", false,
                String.format("To run lifelong experiments. Doesn't work with %s format", STR_BGU));
        options.addOption(lifelongOption);

        Option nameOption = Option.builder("n").longOpt("name")
                .argName("name")
                .hasArg()
                .required(false)
                .desc("Name for the experiment. Optional.")
                .build();
        options.addOption(nameOption);

        Option instancesDirOption = Option.builder("iDir").longOpt(STR_INSTANCES_DIR)
                .argName(STR_INSTANCES_DIR)
                .hasArg()
                .required(true)
                .desc("Set the directory (path) where maps and instances are to be found. Required.")
                .build();
        options.addOption(instancesDirOption);

        Option resultsDirOption = Option.builder("resDir").longOpt(STR_RESULTS_DIR_OPTION)
                .argName(STR_RESULTS_DIR_OPTION)
                .hasArg()
                .required(false)
                .desc("The directory (path) where results will be saved. Will be created if it doesn't exist. Optional.")
                .build();
        options.addOption(resultsDirOption);

        Option resultsFileOption = Option.builder("resPref").longOpt(STR_RESULTS_FILE_PREFIX)
                .argName(STR_RESULTS_FILE_PREFIX)
                .hasArg()
                .required(false)
                .desc("The prefix to give results file names. Optional.")
                .build();
        options.addOption(resultsFileOption);

        Option instancesRegexOption = Option.builder("iRegex").longOpt(STR_INSTANCES_REGEX)
                .argName(STR_INSTANCES_REGEX)
                .hasArg()
                .required(false)
                .desc("If given, only instances matching this Regex will be used. Optional.")
                .build();
        options.addOption(instancesRegexOption);

        Option InstancesFormatOption = Option.builder("iForm").longOpt("instancesFormat")
                .argName("instancesFormat")
                .hasArg()
                .required(false)
                .desc(String.format("Set the format of the instances. " +
                        "Supports %s format (https://movingai.com/benchmarks/formats.html) and %s format.", STR_MOVING_AI, STR_BGU)
                        + " Optional (default is " + STR_MOVING_AI + ").")
                .build();
        options.addOption(InstancesFormatOption);

        Option agentNumsOption = Option.builder("a").longOpt(STR_AGENT_NUMS)
                .argName(STR_AGENT_NUMS)
                .hasArgs()
                .required(true)
                .valueSeparator(',')
                .desc("Set the numbers of agents to try. Use ',' (comma) as a separator and no spaces." +
                        " Will use the maximum available if an instance does not have enough agents. Required.")
                .build();
        options.addOption(agentNumsOption);

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();

        try {
            String instancesDir;
            int[] agentNums = null;
            I_InstanceBuilder instanceBuilder = new InstanceBuilder_MovingAI();
            String experimentName = "Unnamed Experiment";
            boolean skipAfterFail = false;
            I_VisualizeSolution visualiser = null;
            boolean lifelong = false;
            String instancesRegex = null;
            String resultsOutputDir = null;
            String optResultsFilePrefix = null;

            // Parse arguments

            cmd = parser.parse(options, args);

            if(cmd.hasOption("s")) {
                System.out.println("skipAfterFail set: Will skip trying more agents for the same instance and solver after failing.");
                skipAfterFail = true;
            }

            if (cmd.hasOption("v")) {
                System.out.println("visualise set: Will visualise the solution.");
                visualiser = GridSolutionVisualizer::visualizeSolution;
            }

            if (cmd.hasOption("l")) {
                System.out.println("lifelong set: Will run Lifelong MAPF.");
                lifelong = true;
            }

            if (cmd.hasOption("n")) {
                String optName = cmd.getOptionValue("name");
                System.out.println("Experiment Name: " + optName);
                experimentName = optName;
            }

            String optInstancesDir = cmd.getOptionValue(STR_INSTANCES_DIR);
            System.out.println("Instances Dir: " + optInstancesDir);
            instancesDir = optInstancesDir;
            if (! new File(instancesDir).exists()){
                System.out.printf("Could not locate the provided instances dir (%s)", instancesDir);
                System.exit(0);
            }

            if(cmd.hasOption("resDir")) {
                String optResultsDir = cmd.getOptionValue(STR_RESULTS_DIR_OPTION);
                System.out.println("Trying to set results dir to " + optResultsDir);
                resultsOutputDir = optResultsDir;
                verifyOutputPath(resultsOutputDir);
            }

            if(cmd.hasOption("resPref")) {
                optResultsFilePrefix = cmd.getOptionValue(STR_RESULTS_FILE_PREFIX);
            }

            if (cmd.hasOption(STR_INSTANCES_REGEX)){
                String optInstancesRegex = cmd.getOptionValue(STR_INSTANCES_REGEX);
                System.out.println("Instances Regex: " + optInstancesRegex);
                instancesRegex = optInstancesRegex;
            }

            if (cmd.hasOption("iForm")) {
                String optInstancesFormat = cmd.getOptionValue("instancesFormat");
                System.out.println("Instances Format: " + optInstancesFormat);
                switch (optInstancesFormat) {
                    case STR_MOVING_AI -> instanceBuilder = new InstanceBuilder_MovingAI(lifelong);
                    case STR_BGU -> instanceBuilder = new InstanceBuilder_BGU();
                    case STR_WAREHOUSE -> instanceBuilder = new InstanceBuilder_Warehouse();
                    default -> {
                        System.out.printf("Unrecognized instance format: %s", optInstancesFormat);
                        System.exit(0);
                    }
                }
                if (lifelong && optInstancesFormat.equals(STR_BGU)){
                    throw new IllegalArgumentException("Lifelong MAPF is not supported for BGU instances.");
                }
            }
            else {
                System.out.printf("Using default instance format %s", STR_MOVING_AI);
            }

            String[] optAgents = cmd.getOptionValues(STR_AGENT_NUMS);
            System.out.println("Agent nums: " + Arrays.toString(optAgents));

            try {
                agentNums = Arrays.stream(optAgents).mapToInt(Integer::parseInt).toArray();
            }
            catch (NumberFormatException e){
                System.out.printf("%s should be an array of integers, got %s", STR_AGENT_NUMS, Arrays.toString(optAgents));
                System.exit(0);
            }

            // Run!
            if (lifelong){
                new LifelongGenericRunManager(instancesDir, agentNums, instanceBuilder, experimentName, skipAfterFail, instancesRegex, resultsOutputDir, optResultsFilePrefix, visualiser)
                        .runAllExperiments();
            }
            else {
                new GenericRunManager(instancesDir, agentNums, instanceBuilder, experimentName, skipAfterFail, instancesRegex, resultsOutputDir, optResultsFilePrefix, visualiser)
                        .runAllExperiments();
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options);
            System.exit(0);
        }
    }

    private static void staticMain() {
        if (verifyOutputPath(exampleResultsOutputDir)){
//            new LifelongRunManagerWarehouse(exampleResultsOutputDir, "", 50).runAllExperiments();
            new LifelongRunManagerMovingAI(exampleResultsOutputDir, IO_Manager.buildPath(new String[]{IO_Manager.resources_Directory, "Instances", "MovingAI_Instances"}), new int[]{50, 100, 150, 200, 250, 300}).runAllExperiments();

//            // will solve a single instance, print the solution, and start a visualization of the solution
//            solveOneInstanceWithVisualizationExample();
//            // will solve multiple instances and print a simple report for each instance
//            runMultipleExperimentsExample();
//            // will solve a set of instances. These instances have known optimal solution costs (found at
//            // src\test\resources\TestingBenchmark\Results.csv), and so can be used as a benchmark.
//            runTestingBenchmarkExperiment();
//            // all examples will also produce a report in CSV format, and save it to resultsOutputDir (see above)
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
        RunParameters runParameters = new RunParameters();
        Solution solution = solver.solve(instance, runParameters);

        //output results
        System.out.println(solution.readableToString());
        outputResults();

        GridSolutionVisualizer.visualizeSolution(instance, solution);
    }

    public static void runMultipleExperimentsExample(){
        RunManagerSimpleExample runManagerSimpleExample = new RunManagerSimpleExample();
        runManagerSimpleExample.runAllExperiments();
    }

    public static void runTestingBenchmarkExperiment(){
        TestingBenchmarkRunManager testingBenchmarkRunManager = new TestingBenchmarkRunManager();
        testingBenchmarkRunManager.runAllExperiments();
    }

    private static void addConsoleAsOutputStream() {
        try {
            S_Metrics.addOutputStream(System.out, S_Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * An example of a simple output of results to a file. It is best to handle this inside your custom
     * {@link A_RunManager run managers} instead.
     * Note that you can add more fields here, if you want metrics that are collected and not exported.
     * Note that you can easily add other metrics which are not currently collected. see {@link S_Metrics}.
     */
    private static void outputResults() {
        try {
            Thread.sleep(30);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        DateFormat dateFormat = S_Metrics.defaultDateFormat;
        String updatedPath =  IO_Manager.buildPath(new String[]{exampleResultsOutputDir, "results " + dateFormat.format(System.currentTimeMillis())}) + " .csv";
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
