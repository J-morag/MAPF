package LifelongMAPF;

import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Solvers.RunParameters;
import Environment.Experiment;
import Environment.Metrics.InstanceReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LifleongExperiment extends Experiment {

    private final Long minResponseTime;
    private final Integer maxTimeSteps;

    public LifleongExperiment(String experimentName, InstanceManager instanceManager, Integer numOfInstances, Integer timeoutEach, @Nullable Long minResponseTime, @Nullable Integer maxTimeSteps) {
        super(experimentName, instanceManager, numOfInstances, timeoutEach);
        this.minResponseTime = minResponseTime;
        this.maxTimeSteps = maxTimeSteps;
    }

    public LifleongExperiment(String experimentName, InstanceManager instanceManager, @Nullable Long minResponseTime, @Nullable Integer maxTimeSteps) {
        this(experimentName, instanceManager, null, null, minResponseTime, maxTimeSteps);
    }

    @Override
    protected @NotNull RunParameters getRunParameters(InstanceReport instanceReport) {
        return new LifelongRunParameters(super.getRunParameters(instanceReport), minResponseTime, maxTimeSteps);
    }
}
