package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import Environment.Experiment;
import Environment.Visualization.I_VisualizeSolution;
import LifelongMAPF.LifleongExperiment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LifelongGenericRunManager extends A_LifelongRunManager{

    private final String instancesDir;
    private final int[] agentNums;
    private final I_InstanceBuilder instanceBuilder;
    private final String experimentName;
    private final boolean skipAfterFail;
    private final String instancesRegex;
    private final Long minResponseTime;
    private final Integer maxTimeSteps;
    public LifelongGenericRunManager(@NotNull String instancesDir, int[] agentNums, @NotNull I_InstanceBuilder instanceBuilder,
                                     @NotNull String experimentName, boolean skipAfterFail, String instancesRegex,
                                     String resultsOutputDir, String resultsFilePrefix, I_VisualizeSolution solutionVisualizer,
                                     @Nullable Long minResponseTime, @Nullable Integer maxTimeSteps) {
        super(resultsOutputDir, solutionVisualizer);
        if (agentNums == null){
            throw new IllegalArgumentException("AgentNums can't be null");
        }
        this.instancesDir = instancesDir;
        this.agentNums = agentNums;
        this.instanceBuilder = instanceBuilder;
        this.experimentName = experimentName;
        this.skipAfterFail = skipAfterFail;
        this.instancesRegex = instancesRegex;
        this.resultsFilePrefix = resultsFilePrefix;
        this.minResponseTime = minResponseTime;
        this.maxTimeSteps = maxTimeSteps;
    }

    @Override
    protected @NotNull String getExperimentName() {
        return this.experimentName;
    }

    @Override
    protected @NotNull I_InstanceBuilder getInstanceBuilder() {
        return this.instanceBuilder;
    }

    @Override
    protected void setExperiments() {
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums, instancesRegex);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, instanceBuilder, properties);

        /*  =   Add new experiment   =  */
        Experiment experiment = new LifleongExperiment(experimentName, instanceManager, null, getTimeoutEach(), minResponseTime, maxTimeSteps);
        experiment.skipAfterFail = this.skipAfterFail;
        experiment.visualizer = this.visualizer;
        this.experiments.add(experiment);
    }

}