package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.Experiment;
import Environment.Visualization.I_VisualizeSolution;
import org.jetbrains.annotations.NotNull;

public class GenericRunManager extends A_RunManager {

    private final String instancesDir;
    private final int[] agentNums;
    private final I_InstanceBuilder instanceBuilder;
    private final String experimentName;
    private final boolean skipAfterFail;
    private final String instancesRegex;

    public GenericRunManager(@NotNull String instancesDir, int[] agentNums, @NotNull I_InstanceBuilder instanceBuilder,
                             @NotNull String experimentName, boolean skipAfterFail, String instancesRegex,
                             String resultsOutputDir, String resultsFilePrefix, I_VisualizeSolution solutionVisualizer) {
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
    }
    @Override
    protected void setSolvers() {
        // TODO modular solvers?
        super.solvers.add(new PrioritisedPlanning_Solver(null, null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.randomRestarts),
                null, null, null));
        super.solvers.add(new CBS_Solver());
    }

    @Override
    protected void setExperiments() {
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums, instancesRegex);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, instanceBuilder, properties);

        /*  =   Add new experiment   =  */
        Experiment experiment = new Experiment(experimentName, instanceManager);
        experiment.skipAfterFail = this.skipAfterFail;
        experiment.visualizer = this.visualizer;
        if (instanceBuilder instanceof InstanceBuilder_Warehouse){ // TODO remove when scenarios are fixed to prevent shared sources and goals
            experiment.sharedGoals = true;
            experiment.sharedSources = true;
        }
        this.experiments.add(experiment);
    }

}
