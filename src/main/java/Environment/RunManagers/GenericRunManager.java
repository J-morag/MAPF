package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceManagerFromFileSystem;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import Environment.Experiment;
import Environment.Visualization.I_VisualizeSolution;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GenericRunManager extends A_RunManager {

    private final String instancesDir;
    private final int[] agentNums;
    private final I_InstanceBuilder instanceBuilder;
    private final String experimentName;
    private final boolean skipAfterFail;
    private final String instancesRegex;
    private List<I_Solver> solversOverride;
    private final Integer timeoutEach;

    public GenericRunManager(@NotNull String instancesDir, int[] agentNums, @NotNull I_InstanceBuilder instanceBuilder,
                             @NotNull String experimentName, boolean skipAfterFail, String instancesRegex,
                             String resultsOutputDir, String resultsFilePrefix, I_VisualizeSolution solutionVisualizer,
                             Integer timeoutEach, @Nullable List<I_Solver> solversOverride) {
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
        this.timeoutEach = timeoutEach;
        this.solversOverride = solversOverride;
    }

    @Override
    protected void setSolvers() {
        if (solversOverride != null){
            super.solvers = solversOverride;
            return;
        }
        super.solvers.add(CanonicalSolversFactory.createPPRRUntilFirstSolutionSolver());

        super.solvers.add(CanonicalSolversFactory.createCBSSolver());
    }

    public void overrideSolvers(@NotNull List<I_Solver> solvers){
        this.solversOverride = solvers;
    }

    @Override
    protected void setExperiments() {
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums, instancesRegex);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManagerFromFileSystem(instancesDir, instanceBuilder, properties);

        /*  =   Add new experiment   =  */
        Experiment experiment = new Experiment(experimentName, instanceManager, null, timeoutEach);
        experiment.skipAfterFail = this.skipAfterFail;
        experiment.visualizer = this.visualizer;
        this.experiments.add(experiment);
    }

}