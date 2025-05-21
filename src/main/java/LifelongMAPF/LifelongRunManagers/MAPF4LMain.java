package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.MapFactory;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.LaCAM.LaCAMBuilder;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import Environment.Config;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.RunManagers.GenericRunManager;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.FailPolicies.TerminateFailPolicy;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.LifelongRunParameters;
import LifelongMAPF.LifelongSimulationSolver;
import TransientMAPF.TransientMAPFSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static BasicMAPF.Instances.Maps.Enum_MapLocationType.EMPTY;
import static BasicMAPF.Instances.Maps.Enum_MapLocationType.WALL;
import static Environment.RunManagers.A_RunManager.DEFAULT_RESULTS_OUTPUT_DIR;
import static Environment.RunManagers.A_RunManager.verifyOutputPath;

public class MAPF4LMain {
    public static void main(String[] args) {
        printIntro();

        Config.INFO = 0;

        System.out.println("\nRunning Case Study 1: Narrow Corridor");
        multipleSolversWithTransientBehaviorNarrowCorridor();

        System.out.println("\nRunning Case Study 2: Circle");
        multipleSolversWithTransientBehaviorCircle();

        Config.INFO = 1;

        System.out.println("\nRunning Case Study 3: Benchmark Maps with Shared Targets");
        if (verifyOutputPath(DEFAULT_RESULTS_OUTPUT_DIR)){
            String cwd = System.getProperty("user.dir");
            String instancesDir = IO_Manager.buildPath( new String[]{cwd, "MAPF4L_benchmark_instances"});
            
            int[] agentNums = new int[]{100, 200, 300, 400, 500};
            int timeoutEach = 1000 * 60;
            long responseTime = 5000;

            for (int numTargets : List.of(10, 20, 30, 40)) {
                InstanceBuilder_MovingAI instanceBuilder = new InstanceBuilder_MovingAI(true);
                instanceBuilder.maxNumOfTargets = numTargets;
                LifelongGenericRunManager lifelongGenericRunManager = new LifelongGenericRunManager(instancesDir, agentNums, instanceBuilder,
                        "MAPF4L_Dense_Targets_" + numTargets, true, null, DEFAULT_RESULTS_OUTPUT_DIR,
                        "MAPF4L_Dense_Targets_" + numTargets, null, timeoutEach, responseTime, 1000);
                lifelongGenericRunManager.runAllExperiments();
            }
        }
        else {
            System.out.println("Output path " + DEFAULT_RESULTS_OUTPUT_DIR + " is not valid. Please check the path.");
        }
    }

    private static void printIntro() {
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
    }

    private static void multipleSolversWithTransientBehaviorNarrowCorridor() {
        I_Coordinate start1 = new Coordinate_2D(0, 0);
        I_Coordinate goal1 = new Coordinate_2D(0, 3);
        I_Coordinate start2 = new Coordinate_2D(0, 1);
        I_Coordinate goal2 = new Coordinate_2D(0, 2);

        I_Coordinate[] repeatedPathAgent1 = new I_Coordinate[1000];
        I_Coordinate[] repeatedPathAgent2 = new I_Coordinate[1000];
        for (int i = 0; i < repeatedPathAgent1.length; i++) {
            repeatedPathAgent1[i] = (i % 2 == 0) ? start1 : goal1;
            repeatedPathAgent2[i] = (i % 2 == 0) ? start2 : goal2;
        }

        MAPF_Instance corridorInstance = new MAPF_Instance("1-wide corridor" , MapFactory.newSimple4Connected2D_GraphMap(
                new Enum_MapLocationType[][]{{EMPTY, EMPTY, EMPTY, EMPTY, EMPTY}}
        ), new Agent[]{
                new LifelongAgent(new Agent(1, start1, goal1), repeatedPathAgent1),
                new LifelongAgent(new Agent(2, start2, goal2), repeatedPathAgent2)
        });

        List<String> solverNames = Arrays.asList("PIBT", "PrP", "PrPt");
        long responseTime = 5000;
        int replanningPeriod = 1;
        int RHCR_horizon = 10;
        List<I_Solver> solvers = Arrays.asList(
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PIBT_Solver(null, RHCR_horizon, null, TransientMAPFSettings.defaultTransientMAPF), null, null, null, null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultRegularMAPF, RHCR_horizon, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF, RHCR_horizon, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null)
        );

        List<RunParameters> parameters = Arrays.asList(
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500)
        );

        solveAndPrintSolutionReportForMultipleSolvers(solvers, solverNames, corridorInstance, parameters,
                Arrays.asList("Expanded Nodes (High Level)", "Expanded Nodes (Low Level)", "Total Low Level Time (ms)", "Elapsed Time (ms)",  "SOC", "SST", "throughputAtT500", "totalOfflineSolverRuntimeMS"));

    }

    private static void multipleSolversWithTransientBehaviorCircle() {
        I_Coordinate start1 = new Coordinate_2D(3, 1);
        I_Coordinate goal1 = new Coordinate_2D(3, 3);
        I_Coordinate start2 = new Coordinate_2D(3, 4);
        I_Coordinate goal2 = new Coordinate_2D(3, 2);

        I_Coordinate[] repeatedPathAgent1 = new I_Coordinate[1000];
        I_Coordinate[] repeatedPathAgent2 = new I_Coordinate[1000];
        for (int i = 0; i < repeatedPathAgent1.length; i++) {
            repeatedPathAgent1[i] = (i % 2 == 0) ? start1 : goal1;
            repeatedPathAgent2[i] = (i % 2 == 0) ? start2 : goal2;
        }

        MAPF_Instance testInstance = new MAPF_Instance("circle" , MapFactory.newSimple4Connected2D_GraphMap(new Enum_MapLocationType[][]{
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
                {EMPTY, WALL,  WALL,  WALL,  WALL,  EMPTY},
                {EMPTY, WALL,  WALL,  WALL,  WALL,  EMPTY},
                {EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
        }), new Agent[]{
                new LifelongAgent(new Agent(1, start1, goal1), repeatedPathAgent1),
                new LifelongAgent(new Agent(2, start2, goal2), repeatedPathAgent2)
        });

        List<String> solverNames = Arrays.asList("PIBT", "CBS", "CBSt", "PrP", "PrPt");
        long responseTime = 5000;
        int replanningPeriod = 1;
        List<I_Solver> solvers = Arrays.asList(
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), CanonicalSolversFactory.createPIBTtSolver(), null, null, null, null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new CBSBuilder().createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new CBSBuilder().setTransientMAPFSettings(TransientMAPFSettings.defaultTransientMAPF).setCostFunction(new SumServiceTimes()).createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultRegularMAPF, null, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF, null, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null)
        );

        List<RunParameters> parameters = Arrays.asList(
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), responseTime, 500)
        );

        solveAndPrintSolutionReportForMultipleSolvers(solvers, solverNames, testInstance, parameters,
                Arrays.asList("Expanded Nodes (High Level)", "Expanded Nodes (Low Level)", "Total Low Level Time (ms)", "Elapsed Time (ms)",  "SOC", "SST", "throughputAtT500", "totalOfflineSolverRuntimeMS"));
    }

    private static void solveAndPrintSolutionReportForMultipleSolvers(List<I_Solver> solvers, List<String> solverNames, MAPF_Instance testInstance, List<RunParameters> parameters, List<String> fields) {
        List<Integer> columnWidths = new ArrayList<>();
        columnWidths.add(10); // "Method" column width
        for (String field : fields) {
            int fieldWidth = Math.max(field.length(), 15);
            columnWidths.add(fieldWidth);
        }
        System.out.printf("%-" + columnWidths.get(0) + "s", "Method");
        for (int j = 0; j < fields.size(); j++) {
            System.out.printf(" | %-" + columnWidths.get(j + 1) + "s", fields.get(j));
        }
        System.out.println();
        for (int width : columnWidths) {
            System.out.print("-".repeat(width + 3));
        }
        System.out.println();
        for (int i = 0; i < solvers.size(); i++) {
            Solution solution = solvers.get(i).solve(testInstance, parameters.get(i));
            System.out.printf("%-" + columnWidths.get(0) + "s", solverNames.get(i));
            for (int j = 0; j < fields.size(); j++) {
                String field = fields.get(j);
                Object value;
                if (field.equalsIgnoreCase("SOC") && solution != null) {
                    value = solution.sumIndividualCosts();
                } else if (field.equalsIgnoreCase("SST") && solution != null) {
                    value = solution.sumServiceTimes();
                } else {
                    value = parameters.get(i).instanceReport.getIntegerValue(field);
                }
                System.out.printf(" | %-" + columnWidths.get(j + 1) + "s", value != null ? value : "N/A");
            }
            System.out.println();
        }
    }
}
