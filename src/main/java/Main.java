import BasicMAPF.Instances.InstanceBuilders.*;
import Environment.Config;
import Environment.RunManagers.*;
import Environment.Visualization.I_VisualizeSolution;
import Environment.Visualization.GridSolutionVisualizer;
import Environment.Visualization.MillimetricCoordinatesGraphSolutionVisualizer;
import org.apache.commons.cli.*;

import java.io.File;
import java.util.Arrays;

import static BasicMAPF.Solvers.A_Solver.getProcessorInfo;
import static Environment.Experiment.DEFAULT_TIMEOUT_EACH;
import static Environment.RunManagers.A_RunManager.verifyOutputPath;


public class Main {
    public static final String STR_AGENT_NUMS = "agentNums";
    public static final String STR_MOVING_AI = "MovingAI";
    protected static final String STR_BGU = "BGU";
    public static final String STR_WAREHOUSE = "Warehouse";
    public static final String STR_ARBITRARY = "ArbitraryGraph";
    public static final String STR_INSTANCES_DIR = "instancesDir";
    public static final String STR_INSTANCES_REGEX = "instancesRegex";
    private static final String STR_RESULTS_DIR_OPTION = "resultsOutputDir";
    private static final String STR_RESULTS_FILE_PREFIX = "resultsFilePrefix";
    private static final String STR_TIMEOUT_EACH = "timeoutEach";
    private static final String STR_DEBUG_LEVEL = "debugLevel";
    private static final String STR_INFO_LEVEL = "infoLevel";
    private static final String STR_WARNING_LEVEL = "warningLevel";

    public static void main(String[] args) {
        CLIMain(args);
    }

    private static void CLIMain(String[] args) {
        Options options = new Options();
        addOptions(options);

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();


        if (args.length == 0){
            System.out.println("No arguments were given. To run a built-in example, run ExampleMain.main(args).");
            helper.printHelp("java -jar <jar name>", options, true);
        }

        printEnv();

        try {
            String instancesDir;
            int[] agentNums = null;
            I_InstanceBuilder instanceBuilder = new InstanceBuilder_MovingAI();
            String experimentName = "Unnamed Experiment";
            boolean skipAfterFail = false;
            boolean forceBiDiEdges = false;
            I_VisualizeSolution visualiser = null;
            String instancesRegex = null;
            String resultsOutputDir = null;
            String optResultsFilePrefix = "Unnamed";
            Integer timeoutEach = null;
            int debugLevel;
            int infoLevel;
            int warningLevel;

            // Parse arguments

            cmd = parser.parse(options, args);

            if(cmd.hasOption("s")) {
                System.out.println("skipAfterFail set: Will skip trying more agents for the same instance and solver after failing.");
                skipAfterFail = true;
            }

            if(cmd.hasOption("bidi")) {
                System.out.println("forceBiDiEdges set: Will force warehouse maps to have all bi-directional edges.");
                forceBiDiEdges = true;
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
                System.err.printf("Could not locate the provided instances dir (%s)\n", instancesDir);
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
                    case STR_MOVING_AI -> instanceBuilder = new InstanceBuilder_MovingAI();
                    case STR_BGU -> instanceBuilder = new InstanceBuilder_BGU();
                    case STR_WAREHOUSE -> instanceBuilder = new InstanceBuilder_Warehouse(null, forceBiDiEdges);
                    case STR_ARBITRARY -> instanceBuilder = new InstanceBuilder_ArbitraryGraph();
                    default -> {
                        System.out.printf("Unrecognized instance format: %s\n", optInstancesFormat);
                        System.exit(0);
                    }
                }
                if ( ! (instanceBuilder instanceof InstanceBuilder_Warehouse) && forceBiDiEdges) {
                    System.out.println("forceBiDiEdges set but instance format is not warehouse. Ignoring.");
                }
            }
            else {
                System.out.printf("Using default instance format %s\n", STR_MOVING_AI);
            }

            if (cmd.hasOption("v")) {
                System.out.println("visualise set: Will visualise the solution.");
                if (instanceBuilder instanceof InstanceBuilder_MovingAI || instanceBuilder instanceof InstanceBuilder_BGU)
                    visualiser = GridSolutionVisualizer::visualizeSolution;
                else if (instanceBuilder instanceof InstanceBuilder_Warehouse)
                    visualiser = MillimetricCoordinatesGraphSolutionVisualizer::visualizeSolution;
                else {
                    System.out.printf("No visualiser available for instance format %s.%n\n", instanceBuilder.getClass().getName());
                    System.exit(0);
                }
            }

            if (cmd.hasOption(STR_TIMEOUT_EACH)) {
                String optTimeoutEach = cmd.getOptionValue(STR_TIMEOUT_EACH);
                System.out.println("Timeout Each: " + optTimeoutEach);
                try {
                    timeoutEach = Integer.parseInt(optTimeoutEach);
                }
                catch (NumberFormatException e){
                    System.out.printf("%s should be an integer, got %s\n", STR_TIMEOUT_EACH, optTimeoutEach);
                    System.exit(0);
                }
            }

            if (cmd.hasOption(STR_DEBUG_LEVEL)) {
                String optDebugLevel = cmd.getOptionValue(STR_DEBUG_LEVEL);
                System.out.println("Debug Level: " + optDebugLevel);
                try {
                    debugLevel = Integer.parseInt(optDebugLevel);
                    Config.DEBUG = debugLevel;
                }
                catch (NumberFormatException e){
                    System.out.printf("%s should be an integer, got %s\n", STR_DEBUG_LEVEL, optDebugLevel);
                    System.exit(0);
                }
            }

            if (cmd.hasOption(STR_INFO_LEVEL)) {
                String optInfoLevel = cmd.getOptionValue(STR_INFO_LEVEL);
                System.out.println("Info Level: " + optInfoLevel);
                try {
                    infoLevel = Integer.parseInt(optInfoLevel);
                    Config.INFO = infoLevel;
                }
                catch (NumberFormatException e){
                    System.out.printf("%s should be an integer, got %s\n", STR_INFO_LEVEL, optInfoLevel);
                    System.exit(0);
                }
            }

            if (cmd.hasOption(STR_WARNING_LEVEL)) {
                String optWarningLevel = cmd.getOptionValue(STR_WARNING_LEVEL);
                System.out.println("Warning Level: " + optWarningLevel);
                try {
                    warningLevel = Integer.parseInt(optWarningLevel);
                    Config.WARNING = warningLevel;
                }
                catch (NumberFormatException e){
                    System.out.printf("%s should be an integer, got %s\n", STR_WARNING_LEVEL, optWarningLevel);
                    System.exit(0);
                }
            }

            String[] optAgents = cmd.getOptionValues(STR_AGENT_NUMS);
            System.out.println("Agent nums: " + Arrays.toString(optAgents));

            try {
                agentNums = Arrays.stream(optAgents).mapToInt(Integer::parseInt).toArray();
            }
            catch (NumberFormatException e){
                System.out.printf("%s should be an array of integers, got %s\n", STR_AGENT_NUMS, Arrays.toString(optAgents));
                System.exit(0);
            }

            // Run!
            new GenericRunManager(instancesDir, agentNums, instanceBuilder, experimentName, skipAfterFail, instancesRegex, resultsOutputDir, optResultsFilePrefix, visualiser, timeoutEach)
                    .runAllExperiments();

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options, true);
            System.exit(0);
        }
    }

    private static void addOptions(Options options) {
        Option helpOption = new Option("h", "help", false,"");
        options.addOption(helpOption);

        Option skipOption = new Option("s", "skipAfterFail", false,
                "To skip attempting the same instance with the same solver, but with more agents, if we already failed with less agents.");
        options.addOption(skipOption);

        Option visualiseOption = new Option("v", "visualise", false,
                "To visualise the solution.");
        options.addOption(visualiseOption);

        Option forceBiDiEdgesOption = new Option("bidi", "forceBiDiEdges", false,
                "To force warehouse maps to have all bi-directional edges.");
        options.addOption(forceBiDiEdgesOption);

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
                        "Supports %s format (https://movingai.com/benchmarks/formats.html), %s format, and %s format.", STR_MOVING_AI, STR_BGU, STR_ARBITRARY)
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

        Option timeoutEachOption = Option.builder("t").longOpt(STR_TIMEOUT_EACH)
                .argName(STR_TIMEOUT_EACH)
                .hasArg()
                .required(false)
                .desc("Set the timeout for each instance. Integer in milliseconds. Optional. Default is " + DEFAULT_TIMEOUT_EACH + "ms.")
                .build();
        options.addOption(timeoutEachOption);

        Option debugLevelOption = Option.builder("d").longOpt(STR_DEBUG_LEVEL)
                .argName(STR_DEBUG_LEVEL)
                .hasArg()
                .required(false)
                .desc("Set the debug level. Integer. Optional. Default is 1 (light and simple checks that can run during experiments). Use 2 for heavy checks for when debugging code. 3 For extreme checks.")
                .build();
        options.addOption(debugLevelOption);

        Option infoLevelOption = Option.builder("i").longOpt(STR_INFO_LEVEL)
                .argName(STR_INFO_LEVEL)
                .hasArg()
                .required(false)
                .desc("Set the info level. Integer. Optional. Default is 1.")
                .build();
        options.addOption(infoLevelOption);

        Option warningLevelOption = Option.builder("w").longOpt(STR_WARNING_LEVEL)
                .argName(STR_WARNING_LEVEL)
                .hasArg()
                .required(false)
                .desc("Set the warning level. Integer. Optional. Default is 1.")
                .build();
        options.addOption(warningLevelOption);
    }

    private static void printEnv() {
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        // get cpu name
        String cpuName = getProcessorInfo();

        System.out.println("CPU: " + cpuName +
                "; cores (inc. virtual): " + Runtime.getRuntime().availableProcessors() +
                "; max heap: " + formatSize(heapMaxSize));
    }

    public static String formatSize(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double)v / (1L << (z*10)), " KMGTPE".charAt(z));
    }

}
