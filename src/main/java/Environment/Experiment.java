package Environment;

import BasicCBS.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.MAPF_Instance;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Experiment class lets the user to specify the instances it needs for the experiment.
 * A_RunManager holds a list of Experiments, each Experiment receives Name and {@link InstanceManager}
 * {@link InstanceManager} receives a path to the instances' folder and a relevant parser {@link I_InstanceBuilder}
 * Optional, users can filter the instances in the folder by simply adding {@link BasicCBS.Instances.InstanceProperties} to {@link InstanceManager}
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

    public Experiment(String experimentName, InstanceManager instanceManager) {
        this.experimentName = experimentName;
        this.instanceManager = instanceManager;
        this.numOfInstances = Integer.MAX_VALUE;
    }

    public Experiment(String experimentName, InstanceManager instanceManager, int numOfInstances) {
        this.experimentName = experimentName;
        this.instanceManager = instanceManager;
        this.numOfInstances = numOfInstances;
    }


    public InstanceReport setReport(MAPF_Instance instance, I_Solver solver) {
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        /*  = Put values in report =  */
        instanceReport.putStringValue(InstanceReport.StandardFields.experimentName, this.experimentName);
        instanceReport.putStringValue(InstanceReport.StandardFields.instanceName, instance.extendedName);
        instanceReport.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
        instanceReport.putIntegerValue(InstanceReport.StandardFields.obstacleRate, instance.getObstaclePercentage());
        instanceReport.putStringValue(InstanceReport.StandardFields.solver, solver.name());

        return instanceReport;
    }


    public void runExperiment(I_Solver solver) {

        if (solver == null) {
            return;
        }

        instanceManager.resetPathIndex();
        /*
         * Keeps a record of failed instances (by the string {@link MAPF_Instance#name} field) and the minimum number of
         * agents attempted on that instance that produced a failure.
         */
        Map<String, Integer> minNumFailedAgentsForInstance = new HashMap<>();

        for (int i = 0; i < this.numOfInstances; i++) {

            MAPF_Instance instance = instanceManager.getNextInstance();

            if (proactiveGarbageCollection) {
                System.gc();
                try {
                    Thread.sleep(sleepTimeAfterGarbageCollection);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            if (instance == null) {
                break;
            }
            if (skipAfterFail && hasFailedWithLessAgents(instance, minNumFailedAgentsForInstance)) {
                continue;
            }

            InstanceReport instanceReport = this.setReport(instance, solver);
            RunParameters runParameters = new RunParameters(5 * 60 * 1000, null, instanceReport, null);

            System.out.println("---------- solving " + instance.name + " with " + instance.agents.size() + " agents ---------- with solver " + solver.name());
            System.out.println("Start time: " + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()));

            Solution solution = solver.solve(instance, runParameters);

            System.out.println("Solved?: " + (solution != null ? "yes" : "no"));
            if (solution != null) {
                boolean validSolution = solution.solves(instance);
                System.out.println("Solution is " + (validSolution ? "valid" : "invalid!!!"));
                instanceReport.putIntegerValue(InstanceReport.StandardFields.valid, validSolution ? 1 : 0);
                System.out.println("Sum of Individual Costs: " + solution.sumIndividualCosts());
            } else { // failed to solve
                recordFailure(instance, minNumFailedAgentsForInstance);
            }

            Integer elapsedTime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
            if (elapsedTime != null) {
                System.out.println("Elapsed time (ms): " + elapsedTime);
            }

            if (!keepSolutionInReport) {
                instanceReport.putStringValue(InstanceReport.StandardFields.solution, "");
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

        }

    }

    private void recordFailure(MAPF_Instance instance, Map<String, Integer> failedInstances) {
        Integer prevFailNumAgents = failedInstances.get(instanceStringRepresentation(instance));
        if (prevFailNumAgents == null || prevFailNumAgents > instance.agents.size()) {
            failedInstances.put(instanceStringRepresentation(instance), instance.agents.size());
        }
    }

    private boolean hasFailedWithLessAgents(MAPF_Instance instance, Map<String, Integer> failedInstances) {
        Integer prevFailNumAgents = failedInstances.get(instanceStringRepresentation(instance));
        return prevFailNumAgents != null && prevFailNumAgents < instance.agents.size();
    }

    protected String instanceStringRepresentation(MAPF_Instance instance) {
        return instance.name;
    }
}