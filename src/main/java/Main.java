import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
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
import org.apache.commons.cli.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;


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
    public static final String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"}); // TODO refactor and argumentize
    public static final String STR_AGENT_NUMS = "agentNums";
    //    public static final String resultsOutputDir = IO_Manager.buildPath(new String[]{   IO_Manager.testResources_Directory +
//                                                                                        "\\Reports default directory"});

    private static final String STR_MOVING_AI = "MovingAI";
    private static final String STR_BGU = "BGU";
    public static final String STR_INSTANCES_DIR = "instancesDir";

    public static void main(String[] args) {
        if(verifyOutputPath()){
            if (args.length == 0){
                // Example / no CLI

                // will solve a single instance and print the solution
                solveOneInstanceExample();
                // will solve multiple instances and print a simple report for each instance
                runMultipleExperimentsExample();
                // will solve a set of instances. These instances have known optimal solution costs (found at
                // src\test\resources\TestingBenchmark\Results.csv), and so can be used as a benchmark.
                runTestingBenchmarkExperiment();
                // all examples will also produce a report in CSV format, and save it to resultsOutputDir (see above)
            }
            else {
                // CLI

                Options options = new Options();
                Option skipOption = new Option("s", "skipAfterFail", false,
                        "To skip attempting the same instance with the same solver, but with more agents, if we already failed with less agents.");
                options.addOption(skipOption);

                Option nameOption = Option.builder("n").longOpt("name")
                        .argName("name")
                        .hasArg()
                        .required(false)
                        .desc("Name for the experiment.")
                        .build();
                options.addOption(nameOption);

                Option instancesDirOption = Option.builder("iDir").longOpt(STR_INSTANCES_DIR)
                        .argName(STR_INSTANCES_DIR)
                        .hasArg()
                        .required(true)
                        .desc("Set the directory where maps and instances are to be found.")
                        .build();
                options.addOption(instancesDirOption);

                // TODO output dir

                Option InstancesFormatOption = Option.builder("iForm").longOpt("instancesFormat")
                        .argName("instancesFormat")
                        .hasArg()
                        .required(false)
                        .desc(String.format("Set the format of the instances. " +
                                "Supports %s format (https://movingai.com/benchmarks/formats.html) and %s format.", STR_MOVING_AI, STR_BGU))
                        .build();
                options.addOption(InstancesFormatOption);

                Option agentNumsOption = Option.builder("a").longOpt(STR_AGENT_NUMS)
                        .argName(STR_AGENT_NUMS)
                        .hasArgs()
                        .required(true)
                        .valueSeparator(',')
                        .desc("Set the numbers of agents to try. Use ',' (comma) as a separator and no spaces." +
                                " Will use the maximum available if an instance does not have enough agents.")
                        .build();
                options.addOption(agentNumsOption);

                CommandLine cmd;
                CommandLineParser parser = new DefaultParser();
                HelpFormatter helper = new HelpFormatter();

                try {
                    String instancesDir;
                    int[] agentNums = null;
                    I_InstanceBuilder instanceBuilder = new InstanceBuilder_MovingAI();
                    String experimentName = "No name";
                    boolean skipAfterFail = false;

                    // Parse arguments

                    cmd = parser.parse(options, args);
                    if(cmd.hasOption("s")) {
                        System.out.println("skipAfterFail set: Will skip trying more agents for the same instance and solver after failing.");
                        skipAfterFail = true;
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

                    if (cmd.hasOption("iForm")) {
                        String optInstancesFormat = cmd.getOptionValue("instancesFormat");
                        System.out.println("Instances Format: " + optInstancesFormat);
                        if (optInstancesFormat.equals(STR_MOVING_AI)){
                            instanceBuilder = new InstanceBuilder_MovingAI();
                        } else if (optInstancesFormat.equals(STR_BGU)) {
                            instanceBuilder = new InstanceBuilder_BGU();
                        }
                        else {
                            System.out.printf("Unrecognized instance format: %s", optInstancesFormat);
                            System.exit(0);
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
                    new GenericRunManager(instancesDir, agentNums, instanceBuilder, experimentName, skipAfterFail).runAllExperiments();

                } catch (ParseException e) {
                    System.out.println(e.getMessage());
                    helper.printHelp("Usage:", options);
                    System.exit(0);
                }
            }
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
