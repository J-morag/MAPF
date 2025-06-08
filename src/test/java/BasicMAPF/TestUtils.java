package BasicMAPF;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.DateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtils {

    public static Map<String, Map<String, String>> readResultsCSV(String pathToCsv) throws IOException {
        Map<String, Map<String, String>> result  = new HashMap<>();
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));

        String headerRow = csvReader.readLine();
        String[] header = headerRow.split(",");
        int fileNameIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if(header[i].equals("File")) {fileNameIndex = i;}
        }

        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] tupleAsArray = row.split(",");
            if(tupleAsArray.length < 1 ) continue;
            Map<String, String> tupleAsMap = new HashMap<>(tupleAsArray.length);
            for (int i = 0; i < tupleAsArray.length; i++) {
                String value = tupleAsArray[i];
                tupleAsMap.put(header[i], value);
            }

            String key = tupleAsArray[fileNameIndex];
            result.put(key, tupleAsMap);
        }
        csvReader.close();

        return result;
    }

    public static void addRandomConstraints(Agent agent, List<I_Location> locations, Random rand, I_ConstraintSet constraints,
                                            int maxTime, int numConstraintsEachType) {
        for (int t = 1; t <= maxTime; t++) {
            Set<I_Location> checkDuplicates = new HashSet<>();
            Set<Constraint> edgeConstraints = new HashSet<>();
            for (int j = 0; j < numConstraintsEachType; j++) {
                // vertex constraint
                I_Location randomLocation;
                do {
                    randomLocation = locations.get(rand.nextInt(locations.size()));
                }
                while (checkDuplicates.contains(randomLocation));
                checkDuplicates.add(randomLocation);
                Constraint constraint = new Constraint(agent, t, null, randomLocation);
                constraints.add(constraint);

                // edge constraint
                I_Location toLocation;
                I_Location prevLocation;
                Constraint edgeConstraint;
                do {
                    toLocation = locations.get(rand.nextInt(locations.size()));
                    prevLocation = locations.get(rand.nextInt(locations.size()));
                    edgeConstraint = new Constraint(agent, t, prevLocation, toLocation);
                }
                while (toLocation.equals(prevLocation) || edgeConstraints.contains(edgeConstraint));
                edgeConstraints.add(edgeConstraint);
                constraints.add(edgeConstraint);
            }
        }
    }

    public static List<I_Location> planLocations(SingleAgentPlan planFromAStar) {
        List<I_Location> aStarPlanLocations = new ArrayList<>();
        for (Move move :
                planFromAStar) {
            if (move.timeNow == 1) {
                aStarPlanLocations.add(move.prevLocation);
            }
            aStarPlanLocations.add(move.currLocation);
        }
        return aStarPlanLocations;
    }

    @NotNull
    public static List<Integer> getPlanCosts(Agent agent, SingleAgentGAndH costFunction, List<I_Location> planLocations) {
        List<Integer> UCSPlanCosts = new ArrayList<>();
        UCSPlanCosts.add(0);
        I_Location prev = null;
        for (I_Location curr :
                planLocations) {
            if (prev != null){
                UCSPlanCosts.add(costFunction.cost(new Move(agent, 1, prev, curr)));
            }
            prev = curr;
        }
        return UCSPlanCosts;
    }

    public static class UnitCostAndNoHeuristic implements SingleAgentGAndH {
        @Override
        public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
            return 0;
        }

        @Override
        public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
            return 0;
        }

        @Override
        public int cost(Move move) {
            return SingleAgentGAndH.super.cost(move);
        }

        @Override
        public boolean isConsistent() {
            return true;
        }

        @Override
        public String toString() {
            return "All edges = 1";
        }
    }

    public static final SingleAgentGAndH unitCostAndNoHeuristic = new UnitCostAndNoHeuristic();

    public static void TestingBenchmark(I_Solver solver, int timeoutSeconds, boolean isOptimal, boolean expectToSolveAll){
        Metrics.clearAll();
        boolean useAsserts = true;

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU());

        MAPF_Instance instance;
        // load the pre-made benchmark
        try {
            long timeout = timeoutSeconds * 1000L;
            long softTimeout = Math.min(500L, timeout);
            Map<String, Map<String, String>> benchmarks = readResultsCSV(path + "/Results.csv");
            int numSolved = 0;
            int numFailed = 0;
            int numValid = 0;
            int numOptimal = 0;
            int numValidSuboptimal = 0;
            int numInvalidOptimal = 0;
            // run all benchmark instances. this code is mostly copied from Environment.Experiment.
            while ((instance = instanceManager.getNextInstance()) != null) {
                //build report
                InstanceReport report = Metrics.newInstanceReport();
                report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
                report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                report.putStringValue(InstanceReport.StandardFields.solver, solver.getName());

                RunParameters runParameters = new RunParametersBuilder().setTimeout(timeout).setSoftTimeout(softTimeout)
                        .setInstanceReport(report).createRP();

                //solve
                System.out.println("---------- solving "  + instance.name + " ----------");
                Solution solution = solver.solve(instance, runParameters);

                // validate
                Map<String, String> benchmarkForInstance = benchmarks.get(instance.name);
                if(benchmarkForInstance == null){
                    System.out.println("can't find benchmark for " + instance.name);
                    continue;
                }

                boolean solved = solution != null;
                System.out.println("Solved?: " + (solved ? "yes" : "no"));
                if (useAsserts && expectToSolveAll) assertNotNull(solution);
                if (solved) numSolved++;
                else numFailed++;

                System.out.printf("Time(ms): %,d%n", report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
                System.out.printf("Expanded nodes: %,d%n", report.getIntegerValue(InstanceReport.StandardFields.expandedNodes));
                System.out.printf("Generated nodes: %,d%n", report.getIntegerValue(InstanceReport.StandardFields.generatedNodes));
                System.out.printf("Expanded nodes (low level): %,d%n", report.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel));
                System.out.printf("Generated nodes (low level): %,d%n", report.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel));

                if(solution != null){
                    boolean valid = solution.solves(instance);
                    System.out.println("Valid?: " + (valid ? "yes" : "no"));
                    if (!valid) {
                        System.out.println("reason: " + solution.firstConflict());
                        System.out.println("solution: " + solution);
                    }
                    if (useAsserts) assertTrue(valid);

                    int optimalCost = Integer.parseInt(benchmarkForInstance.get("Plan Cost"));
                    int costWeGot = solution.sumIndividualCosts();
                    boolean optimal = optimalCost==costWeGot;
                    System.out.println("cost is " + (optimal ? "optimal (" + costWeGot +")" :
                            ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                    if (useAsserts && isOptimal)
                        assertEquals(optimalCost, costWeGot);
                    report.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                    if (useAsserts && costWeGot < optimalCost)
                        fail("cost is impossibly low"); // actually shouldn't happen as long as solution solves the problem

                    report.putIntegerValue("Runtime Delta",
                            report.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) - (int)Float.parseFloat(benchmarkForInstance.get("Plan time")));

                    if(valid) numValid++;
                    if(optimal) numOptimal++;
                    if(valid && !optimal) numValidSuboptimal++;
                    if(!valid && optimal) numInvalidOptimal++;
                }
            }

            System.out.println("--- TOTALS: ---");
            System.out.println("timeout for each (seconds): " + ((float)timeout/1000));
            System.out.println("soft timeout for each (seconds): " + ((float)softTimeout/1000));
            System.out.println("solved: " + numSolved);
            System.out.println("failed: " + numFailed);
            System.out.println("valid: " + numValid);
            System.out.println("optimal: " + numOptimal);
            System.out.println("valid but not optimal: " + numValidSuboptimal);
            System.out.println("not valid but optimal: " + numInvalidOptimal);

            //save results
            DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
            String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
            File directory = new File(resultsOutputDir);
            if (! directory.exists()){
                directory.mkdir();
            }
            String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir,
                    "res_ " + solver.getName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() +
                            "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
            try {
                Metrics.exportCSV(new FileOutputStream(updatedPath),
                        new String[]{
                                InstanceReport.StandardFields.instanceName,
                                InstanceReport.StandardFields.numAgents,
                                InstanceReport.StandardFields.timeoutThresholdMS,
                                InstanceReport.StandardFields.solved,
                                InstanceReport.StandardFields.elapsedTimeMS,
                                "Runtime Delta",
                                InstanceReport.StandardFields.solutionCost,
                                "Cost Delta",
                                InstanceReport.StandardFields.totalLowLevelTimeMS,
                                InstanceReport.StandardFields.generatedNodes,
                                InstanceReport.StandardFields.expandedNodes,
                                InstanceReport.StandardFields.generatedNodesLowLevel,
                                InstanceReport.StandardFields.expandedNodesLowLevel});
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            fail();
        }

    }

    /**
     * This contains diverse instances, comparing the performance of two algorithms.
     */
    public static void comparativeTest(I_Solver baselineSolver, String nameBaseline, boolean isOptimalBaseline,
                                       boolean isCompleteBaseline, I_Solver competitorSolver, String nameExperimental, boolean isOptimalExperimental,
                                       boolean isCompleteExperimental, int[] agentNums, int timeoutSeconds, int rerunsWithShuffledAgents){
        Metrics.clearAll();
        boolean useAsserts = true;

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
                new InstanceProperties(null, -1d, agentNums));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance;
        long timeout = timeoutSeconds * 1000L;
        int solvedByBaseline = 0;
        int solvedByExperimental = 0;
        int solvedByBoth = 0;
        int sumRuntimeBaseline = 0;
        int sumRuntimeExperimental = 0;
        int sumCostBaseline = 0;
        int sumCostExperimental = 0;
        // expansions
        int sumHighLevelExpandedBaselineOnAll = 0;
        int sumHighLevelExpandedBaselineOnSolved = 0;
        int sumHighLevelExpandedExperimentalOnAll = 0;
        int sumHighLevelExpandedExperimentalOnSolved = 0;
        float sumHighLevelExpansionRateOnSolvedBaseline = 0f;
        float sumHighLevelExpansionRateOnSolvedExperimental = 0f;
        // generations
        int sumHighLevelGeneratedBaselineOnAll = 0;
        int sumHighLevelGeneratedBaselineOnSolved = 0;
        int sumHighLevelGeneratedExperimentalOnAll = 0;
        int sumHighLevelGeneratedExperimentalOnSolved = 0;
        float sumHighLevelGenerationRateOnSolvedBaseline = 0f;
        float sumHighLevelGenerationRateOnSolvedExperimental = 0f;

        while ((instance = instanceManager.getNextInstance()) != null) {
            for (int j = 0; j <= rerunsWithShuffledAgents; j++) {
                System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");
                List<Agent> order = new ArrayList<>(instance.agents);
                if (j > 0){
                    Random rand = new Random(j);
                    Collections.shuffle(order, rand);
                }
                Agent[] orderedAgents = order.toArray(new Agent[0]);
                System.out.println("order: " + Arrays.toString(orderedAgents));

                // run baseline (without the improvement)
                //build report
                InstanceReport reportBaseline = Metrics.newInstanceReport();
                reportBaseline.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeTest");
                reportBaseline.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                reportBaseline.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                reportBaseline.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

                RunParameters runParametersBaseline = new RunParametersBuilder().setTimeout(timeout)
                        .setInstanceReport(reportBaseline).setPriorityOrder(orderedAgents).createRP();

                //solve
                Solution solutionBaseline = baselineSolver.solve(instance, runParametersBaseline);

                // run experiment (with the improvement)
                //build report
                InstanceReport reportExperimental = Metrics.newInstanceReport();
                reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeTest");
                reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
                reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
                reportExperimental.putStringValue(InstanceReport.StandardFields.solver, nameBaseline);

                RunParameters runParametersExperimental = new RunParametersBuilder().setTimeout(timeout)
                        .setInstanceReport(reportExperimental).setPriorityOrder(orderedAgents).createRP();

                //solve
                Solution solutionExperimental = competitorSolver.solve(instance, runParametersExperimental);

                // compare

                boolean baselineSolved = solutionBaseline != null;
                solvedByBaseline += baselineSolved ? 1 : 0;
                boolean experimentalSolved = solutionExperimental != null;
                solvedByExperimental += experimentalSolved ? 1 : 0;
                System.out.print(nameBaseline + " Solved?: " + (baselineSolved ? "yes" : "no") + "; ");
                solvedByBoth += baselineSolved && experimentalSolved ? 1 : 0;

                int highLevelExpandedBaseline = 0;
                float highLevelExpansionRateBaseline = 0;
                int highLevelGeneratedBaseline = 0;
                float highLevelGenerationRateBaseline = 0;

                if(solutionBaseline != null){
                    boolean valid = solutionBaseline.solves(instance);
                    System.out.println(nameBaseline + " Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);
                    try {
                        highLevelExpandedBaseline = reportBaseline.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                        sumHighLevelExpandedBaselineOnSolved += highLevelExpandedBaseline;
                        highLevelExpansionRateBaseline = reportBaseline.getFloatValue(InstanceReport.StandardFields.expansionRate);
                        sumHighLevelExpansionRateOnSolvedBaseline += highLevelExpansionRateBaseline;
                        highLevelGeneratedBaseline = reportBaseline.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
                        sumHighLevelGeneratedBaselineOnSolved += highLevelGeneratedBaseline;
                        highLevelGenerationRateBaseline = reportBaseline.getFloatValue(InstanceReport.StandardFields.generationRate);
                        sumHighLevelGenerationRateOnSolvedBaseline += highLevelGenerationRateBaseline;
                    }
                    catch (NullPointerException e){
                    }
                }
                else System.out.println();

                System.out.print(nameExperimental + " Solved?: " + (experimentalSolved ? "yes" : "no") + "; ");

                int highLevelExpandedExperimental = 0;
                float highLevelExpansionRateExperimental = 0;
                int highLevelGeneratedExperimental = 0;
                float highLevelGenerationRateExperimental = 0;

                if(solutionExperimental != null){
                    boolean valid = solutionExperimental.solves(instance);
                    System.out.println(" " + nameExperimental + " Valid?: " + (valid ? "yes" : "no"));
                    if (useAsserts) assertTrue(valid);
                    try {
                        highLevelExpandedExperimental = reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
                        sumHighLevelExpandedExperimentalOnSolved += highLevelExpandedExperimental;
                        highLevelExpansionRateExperimental = reportExperimental.getFloatValue(InstanceReport.StandardFields.expansionRate);
                        sumHighLevelExpansionRateOnSolvedExperimental += highLevelExpansionRateExperimental;
                        highLevelGeneratedExperimental = reportExperimental.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
                        sumHighLevelGeneratedExperimentalOnSolved += highLevelGeneratedExperimental;
                        highLevelGenerationRateExperimental = reportExperimental.getFloatValue(InstanceReport.StandardFields.generationRate);
                        sumHighLevelGenerationRateOnSolvedExperimental += highLevelGenerationRateExperimental;
                    }
                    catch (NullPointerException e){
                    }
                }
                else System.out.println();

                System.out.print(nameBaseline + " expanded nodes: " + highLevelExpandedBaseline + " ; ");
                System.out.printf(nameBaseline + " expansion rate: %.2f\n", highLevelExpansionRateBaseline);
                sumHighLevelExpandedBaselineOnAll += highLevelExpandedBaseline;
                // print low level expansion rate
                System.out.println(nameBaseline + " low level expanded nodes: " + reportBaseline.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel) + " ; ");

                System.out.print(nameBaseline + " generated nodes: " + highLevelGeneratedBaseline + " ; ");
                System.out.printf(nameBaseline + " generation rate: %.2f\n", highLevelGenerationRateBaseline);
                sumHighLevelGeneratedBaselineOnAll += highLevelGeneratedBaseline;
                // print low level generation rate
                System.out.println(nameBaseline + " low level generated nodes: " + reportBaseline.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel) + " ; ");

                System.out.print(nameExperimental + " expanded nodes: " + highLevelExpandedExperimental + " ; ");
                System.out.printf(nameExperimental + " expansion rate: %.2f\n", highLevelExpansionRateExperimental);
                sumHighLevelExpandedExperimentalOnAll += highLevelExpandedExperimental;
                // print low level expansion rate
                System.out.println(nameExperimental + " low level expanded nodes: " + reportExperimental.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel) + " ; ");

                System.out.print(nameExperimental + " generated nodes: " + highLevelGeneratedExperimental + " ; ");
                System.out.printf(nameExperimental + " generation rate: %.2f\n", highLevelGenerationRateExperimental);
                sumHighLevelGeneratedExperimentalOnAll += highLevelGeneratedExperimental;
                // print low level generation rate
                System.out.println(nameExperimental + " low level generated nodes: " + reportExperimental.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel) + " ; ");

                int runtimeBaseline = reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                int runtimeExperimental = reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);

                if (isCompleteBaseline && !baselineSolved){
                    if (runtimeBaseline < timeout){
                        System.out.println(nameBaseline + " is complete and proved " + instance.extendedName + " unsolvable in " + runtimeBaseline + "ms");
                        if (experimentalSolved){
                            System.out.println("but " + nameExperimental + " solved it!");
                            if (useAsserts)
                                assertFalse(experimentalSolved);
                        }
                    }

                }
                if (isCompleteExperimental && !experimentalSolved){
                    if (runtimeExperimental < timeout){
                        System.out.println(nameExperimental + " is complete and proved " + instance.extendedName + " unsolvable in " + runtimeExperimental + "ms");
                        if (baselineSolved){
                            System.out.println("but " + nameBaseline + " solved it!");
                            if (useAsserts)
                                assertFalse(baselineSolved);
                        }
                    }
                }

                if(solutionBaseline != null && solutionExperimental != null){
                    // runtimes
                    sumRuntimeBaseline += runtimeBaseline;
                    sumRuntimeExperimental += runtimeExperimental;
                    reportBaseline.putIntegerValue("Runtime Delta",
                            reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                    - reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));

                    // cost
                    int costBaseline = solutionBaseline.sumIndividualCosts();
                    sumCostBaseline += costBaseline;
                    int costExperimental = solutionExperimental.sumIndividualCosts();
                    sumCostExperimental += costExperimental;

                    if (isOptimalBaseline && isOptimalExperimental){
                        if (costExperimental > costBaseline){
                            System.out.println(nameBaseline + " cost: " + costBaseline);
                            System.out.println(nameExperimental + " cost: " + costExperimental);
                            for (Agent agent : instance.agents) {
                                SingleAgentPlan planBaseline = solutionBaseline.getPlanFor(agent);
                                SingleAgentPlan planExperimental = solutionExperimental.getPlanFor(agent);
                                if (planBaseline.size() < planExperimental.size()) {
                                    for (int i = 1; i <= planBaseline.getEndTime(); i++) {
                                        Move moveBaseline = planBaseline.moveAt(i);
                                        Move moveExperimental = planExperimental.moveAt(i);
                                        if (!moveBaseline.equals(moveExperimental)){
                                            System.out.println("agent " + agent.iD + " has different moves at time " + i + ": " +
                                                    moveBaseline.toString().replace("\n", "") + " vs " + moveExperimental.toString().replace("\n", ""));
                                        }
                                    }
                                    System.out.println("agent " + agent.iD + " has different plan lengths: " +
                                            planBaseline.size() + " vs " + planExperimental.size()
                                            + " with plans: \n" + planBaseline + "\nvs\n" + planExperimental);
                                }
                            }
                        }
                        if (useAsserts)
                            assertEquals(costBaseline, costExperimental);
                    }
                    if (isOptimalBaseline && !isOptimalExperimental){
                        if (costBaseline > costExperimental){
                            System.out.printf("(optimal) baseline cost: %d, larger than (sub-optimal) experimental cost: %d!\n", costBaseline, costExperimental);
                            if (useAsserts)
                                assertTrue(costBaseline <= costExperimental);
                        }
                    }
                    if (isOptimalExperimental && !isOptimalBaseline){
                        if (costExperimental > costBaseline){
                            System.out.printf("(optimal) experimental cost: %d, larger than (sub-optimal) baseline cost: %d!\n", costExperimental, costBaseline);
                            if (useAsserts)
                                assertTrue(costExperimental <= costBaseline);
                        }
                    }
                }
            }
        }

        outputResults(timeout, solvedByBoth, nameBaseline, nameExperimental, solvedByBaseline, solvedByExperimental,
                sumRuntimeBaseline, sumRuntimeExperimental, sumCostBaseline, sumCostExperimental, sumHighLevelExpandedExperimentalOnAll,
                sumHighLevelExpandedExperimentalOnSolved, sumHighLevelExpandedBaselineOnAll, sumHighLevelExpandedBaselineOnSolved,
                sumHighLevelExpansionRateOnSolvedBaseline, sumHighLevelExpansionRateOnSolvedExperimental,
                sumHighLevelGeneratedExperimentalOnAll, sumHighLevelGeneratedExperimentalOnSolved,
                sumHighLevelGeneratedBaselineOnAll, sumHighLevelGeneratedBaselineOnSolved,
                sumHighLevelGenerationRateOnSolvedBaseline, sumHighLevelGenerationRateOnSolvedExperimental);
    }

    public static void outputResults(long timeout, int solvedByBoth, String nameBaseline, String nameExperimental,
                                     int solvedByBaseline, int solvedByExperimental, int runtimeBaseline,
                                     int runtimeExperimental, int sumCostBaseline, int sumCostExperimental,
                                     int sumHighLevelExpandedExperimentalOnAll, int sumHighLevelExpandedExperimentalOnSolved,
                                     int sumHighLevelExpandedBaselineOnAll, int sumHighLevelExpandedBaselineOnSolved,
                                     float sumHighLevelExpansionRateOnSolvedBaseline, float sumHighLevelExpansionRateOnSolvedExperimental,
                                     int sumHighLevelGeneratedExperimentalOnAll, int sumHighLevelGeneratedExperimentalOnSolved,
                                     int sumHighLevelGeneratedBaselineOnAll, int sumHighLevelGeneratedBaselineOnSolved,
                                     float sumHighLevelGenerationRateOnSolvedBaseline, float sumHighLevelGenerationRateOnSolvedExperimental) {

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout / 1000));
        System.out.println(nameBaseline + " solved: " + solvedByBaseline);
        System.out.println(nameExperimental + " solved: " + solvedByExperimental);
        System.out.println(nameBaseline + " expanded nodes: " + sumHighLevelExpandedBaselineOnAll);
        System.out.println(nameExperimental + " expanded nodes: " + sumHighLevelExpandedExperimentalOnAll);
        System.out.println(nameBaseline + " generated nodes: " + sumHighLevelGeneratedBaselineOnAll);
        System.out.println(nameExperimental + " generated nodes: " + sumHighLevelGeneratedExperimentalOnAll);

        System.out.println("totals (on instances where both solved) :");
        System.out.println(nameBaseline + " time: " + runtimeBaseline);
        System.out.println(nameExperimental + " time: " + runtimeExperimental);
        System.out.printf(nameBaseline + " avg. cost: %.2f\n", (double) sumCostBaseline / solvedByBoth);
        System.out.printf(nameExperimental + " avg. cost: %.2f\n", (double) sumCostExperimental / solvedByBoth);
        System.out.printf(nameBaseline + " avg. expanded nodes: %.2f\n", (double) sumHighLevelExpandedBaselineOnSolved / solvedByBoth);
        System.out.printf(nameExperimental + " avg. expanded nodes: %.2f\n", (double) sumHighLevelExpandedExperimentalOnSolved / solvedByBoth);
        System.out.printf(nameBaseline + " avg. expansion rate: %.2f\n", (double) sumHighLevelExpansionRateOnSolvedBaseline / solvedByBoth);
        System.out.printf(nameExperimental + " avg. expansion rate: %.2f\n", (double) sumHighLevelExpansionRateOnSolvedExperimental / solvedByBoth);
        System.out.printf(nameBaseline + " avg. generated nodes: %.2f\n", (double) sumHighLevelGeneratedBaselineOnSolved / solvedByBoth);
        System.out.printf(nameExperimental + " avg. generated nodes: %.2f\n", (double) sumHighLevelGeneratedExperimentalOnSolved / solvedByBoth);
        System.out.printf(nameBaseline + " avg. generation rate: %.2f\n", (double) sumHighLevelGenerationRateOnSolvedBaseline / solvedByBoth);
        System.out.printf(nameExperimental + " avg. generation rate: %.2f\n", (double) sumHighLevelGenerationRateOnSolvedExperimental / solvedByBoth);


        //save results
        DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir,
                "res_ " + nameBaseline + " vs " + nameExperimental + "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
        try {
            Metrics.exportCSV(new FileOutputStream(updatedPath),
                    new String[]{
                            InstanceReport.StandardFields.instanceName,
                            InstanceReport.StandardFields.solver,
                            InstanceReport.StandardFields.numAgents,
                            InstanceReport.StandardFields.timeoutThresholdMS,
                            InstanceReport.StandardFields.solved,
                            InstanceReport.StandardFields.elapsedTimeMS,
                            "Runtime Delta",
                            InstanceReport.StandardFields.solutionCost,
                            "Cost Delta",
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

    public static void solveAndPrintSolutionReportForMultipleSolvers(List<I_Solver> solvers, List<String> solverNames, MAPF_Instance testInstance, List<RunParameters> parameters, List<String> fields) {
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
