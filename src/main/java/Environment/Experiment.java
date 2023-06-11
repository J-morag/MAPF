package Environment;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Visualization.I_VisualizeSolution;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Experiment class lets the user to specify the instances it needs for the experiment.
 * A_RunManager holds a list of Experiments, each Experiment receives Name and {@link InstanceManager}
 * {@link InstanceManager} receives a path to the instances' folder and a relevant parser {@link I_InstanceBuilder}
 * Optional, users can filter the instances in the folder by simply adding {@link BasicMAPF.Instances.InstanceProperties} to {@link InstanceManager}
 * Make sure that {@link #setReport(MAPF_Instance, I_Solver)} is defined as you want
 */
public class Experiment {

    public final String experimentName;
    public final int numOfInstances;
    protected InstanceManager instanceManager;
    /**
     * When the experiment encounters an instance that was already tried with the same solver, and failed, and is now
     * being attempted with even more agents, that instance will be skipped.
     */
    public boolean skipAfterFail = true;

    /**
     * If set to false, the solution wil lbe removed from reports before committing them. Solutions can be very large strings,
     * so removing them will save space in long experiments.
     */
    public boolean keepSolutionInReport = false;
    /**
     * If reports are written to an {@link java.io.OutputStream} (through {@link S_Metrics}) immediately upon being committed,
     * it may be preferred to just remove them afterwards, rather than keep accumulating them.
     */
    public boolean keepReportAfterCommit = true;
    /**
     * If set to true, will prompt {@link System} to collect garbage before every instance is attempted. This will reduce
     * average heap use (as it will be cleared often), and give every run a more similar starting point. The main thread
     * will sleep for 100 ms after calling for garbage collection, so that solver performance would not be affected by
     * garbage collection.
     */
    public boolean proactiveGarbageCollection = true;
    public int sleepTimeAfterGarbageCollection = 100;
    public long timeoutEach;
    public boolean sharedGoals = false;
    public boolean sharedSources = false;
    public I_VisualizeSolution visualizer = null;

    public Experiment(String experimentName, InstanceManager instanceManager, Integer numOfInstances, Integer timeoutEach) {
        this.experimentName = experimentName;
        this.instanceManager = instanceManager;
        this.numOfInstances = Objects.requireNonNullElse(numOfInstances, Integer.MAX_VALUE);
        this.timeoutEach = Objects.requireNonNullElse(timeoutEach, 5 * 60 * 1000);
    }

    public Experiment(String experimentName, InstanceManager instanceManager) {
        this(experimentName, instanceManager, null, null);
    }


    public InstanceReport setReport(MAPF_Instance instance, I_Solver solver) {
        InstanceReport instanceReport = S_Metrics.newInstanceReport();

        instanceReport.putStringValue(InstanceReport.StandardFields.experimentName, this.experimentName);
        instanceReport.putStringValue(InstanceReport.StandardFields.instanceName, instance.extendedName);
        instanceReport.putStringValue(InstanceReport.StandardFields.mapName, instance.name);
        putMapStats(instanceReport, instance);
        instanceReport.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
        instanceReport.putIntegerValue(InstanceReport.StandardFields.obstacleRate, instance.getObstaclePercentage());
        instanceReport.putStringValue(InstanceReport.StandardFields.solver, solver.name());

        return instanceReport;
    }

    private void putMapStats(InstanceReport instanceReport, MAPF_Instance instance) {
        if (instance.map instanceof I_ExplicitMap explicitMap) {
            int traversableLocations = 0;
            int sumInDegree = 0;
            int sumOutDegree = 0;
            for (I_Location loc : explicitMap.getAllLocations()) {
                if (loc.getType().equals(Enum_MapLocationType.EMPTY) || loc.getType().equals(Enum_MapLocationType.NO_STOP)) {
                    traversableLocations++;
                    sumInDegree += loc.incomingEdges().size();
                    sumOutDegree += loc.outgoingEdges().size();
                }
            }
            instanceReport.putIntegerValue(InstanceReport.StandardFields.numTraversableLocations , traversableLocations);
            instanceReport.putFloatValue(InstanceReport.StandardFields.avgInDegree, sumInDegree / (float)traversableLocations);
            instanceReport.putFloatValue(InstanceReport.StandardFields.avgOutDegree, sumOutDegree / (float)traversableLocations);
        }
    }


    public void runExperiment(I_Solver solver) {
        if (solver == null) {
            return;
        }

        instanceManager.resetPathIndex();
        /*
         * Keeps a record of failed instances attempted by a solver, and the minimum number of agents attempted on that
         *  instance that produced a failure.
         */
        Map<String, Integer> minNumFailedAgentsForInstance = new HashMap<>();

        for (int i = 0; i < this.numOfInstances; i++) {

            MAPF_Instance instance = instanceManager.getNextInstance();

            if (instance == null) {
                break;
            }

            runInstanceOnSolver(solver, minNumFailedAgentsForInstance, instance);
        }
    }

    public void runExperiment(List<I_Solver> solvers) {
        if (solvers == null) {
            return;
        }

        instanceManager.resetPathIndex();
        /*
         * Keeps a record of failed instances attempted by a solver, and the minimum number of agents attempted on that
         *  instance that produced a failure.
         */
        Map<String, Integer> minNumFailedAgentsForInstance = new HashMap<>();
        int numInvalidSolutions = 0;

        for (int i = 0; i < this.numOfInstances; i++) {

            MAPF_Instance instance = instanceManager.getNextInstance();

            if (instance == null) {
                break;
            }

            for (I_Solver solver :
                    solvers) {
                boolean valid = runInstanceOnSolver(solver, minNumFailedAgentsForInstance, instance);
                if (!valid){
                    numInvalidSolutions++;
                }
            }
        }
        System.out.println("Experiment concluded with " + numInvalidSolutions + " invalid solutions");

    }

    /**
     * @return false if the instance was solved but the solution was invalid; true in all other cases.
     */
    protected boolean runInstanceOnSolver(I_Solver solver, Map<String, Integer> minNumFailedAgentsForInstance, MAPF_Instance instance) {
        boolean validSolution = true;
        if (proactiveGarbageCollection) {
            System.gc();
            try {
                Thread.sleep(sleepTimeAfterGarbageCollection);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // create a report before skipping, so that output will be easier to read (more consistent)
        InstanceReport instanceReport = this.setReport(instance, solver);
        if (skipAfterFail && hasFailedWithLessAgents(instance, minNumFailedAgentsForInstance, solver)) {
            instanceReport.putIntegerValue(InstanceReport.StandardFields.skipped, 1);
            instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, 0);
            try {
                instanceReport.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return validSolution;
        }
        else{
            instanceReport.putIntegerValue(InstanceReport.StandardFields.skipped, 0);
        }

        RunParameters runParameters = new RunParameters(timeoutEach, null, instanceReport, null);

        String instanceName = instance.extendedName;
        int numAgents = instance.agents.size();

        System.out.println("---------- solving " + instanceName + " with " + numAgents + " agents ---------- with solver " + solver.name());
        System.out.println("Start time: " + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()));

        Solution solution = solver.solve(instance, runParameters);

        System.out.println("Solved?: " + (solution != null ? "yes" : "no"));
        if (solution != null) {
            validSolution = isValidSolutionForInstance(instance, solution);
            System.out.println("Solution is " + (validSolution ? "valid" : "invalid!!!"));
            if (!validSolution){ // print a warning
                System.err.println("!+!+!+!+!+!+!+!+!+!\nSolver " + solver.name() + " produced an invalid solution!\nInstance: "
                        + instanceName + "\n#agents: " + numAgents + "\n!+!+!+!+!+!+!+!+!+!");
            }
            instanceReport.putIntegerValue(InstanceReport.StandardFields.valid, validSolution ? 1 : 0);
            System.out.println("Sum of Individual Costs: " + getSolutionCost(solution));

            if (visualizer != null) {
                try {
                    visualizer.visualizeSolution(instance, solution, solver.name() + " - " + instanceName);
                }
                catch (IllegalArgumentException e){
                    System.err.println(e.getMessage());
                }
            }
        } else { // failed to solve
            recordFailure(instance, minNumFailedAgentsForInstance, solver);
        }

        Integer elapsedTime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        if (elapsedTime != null) {
            System.out.println("Elapsed time (ms): " + elapsedTime);
        }

        // Now that the report is complete, commit it
        try {
            instanceReport.commit();
            if (!keepReportAfterCommit) {
                S_Metrics.removeReport(instanceReport);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!keepSolutionInReport) {
            instanceReport.putStringValue(InstanceReport.StandardFields.solution, "");
        }

        return validSolution;
    }

    private boolean isValidSolutionForInstance(MAPF_Instance instance, Solution solution) {
        return solution.solves(instance, sharedGoals, sharedSources);
    }

    protected int getSolutionCost(Solution solution) {
        return solution.sumIndividualCostsWithPriorities();
    }

    private void recordFailure(MAPF_Instance instance, Map<String, Integer> failedInstances, I_Solver solver) {
        Integer prevFailNumAgents = failedInstances.get(instanceAndSolverStringRepresentation(instance, solver));
        if (prevFailNumAgents == null || prevFailNumAgents > instance.agents.size()) {
            failedInstances.put(instanceAndSolverStringRepresentation(instance, solver), instance.agents.size());
        }
    }

    private boolean hasFailedWithLessAgents(MAPF_Instance instance, Map<String, Integer> failedInstances, I_Solver solver) {
        Integer prevFailNumAgents = failedInstances.get(instanceAndSolverStringRepresentation(instance, solver));
        return prevFailNumAgents != null && prevFailNumAgents < instance.agents.size();
    }

    protected String instanceAndSolverStringRepresentation(MAPF_Instance instance, I_Solver solver) {
        return instance.extendedName + solver.name();
    }
}