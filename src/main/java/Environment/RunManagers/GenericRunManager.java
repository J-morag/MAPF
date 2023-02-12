package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Experiment;
import org.jetbrains.annotations.NotNull;

public class GenericRunManager extends A_RunManager {

    private final String instancesDir;
    private final int[] agentNums;
    private final I_InstanceBuilder instanceBuilder;
    private final String experimentName;
    private final boolean skipAfterFail;
    private final String instancesRegex;

    public GenericRunManager(@NotNull String instancesDir, int[] agentNums, @NotNull I_InstanceBuilder instanceBuilder, @NotNull String experimentName, boolean skipAfterFail, String instancesRegex, String resultsOutputDir) {
        super(resultsOutputDir);
        if (agentNums == null){
            throw new IllegalArgumentException("AgentNums can't be null");
        }
        this.instancesDir = instancesDir;
        this.agentNums = agentNums;
        this.instanceBuilder = instanceBuilder;
        this.experimentName = experimentName;
        this.skipAfterFail = skipAfterFail;
        this.instancesRegex = instancesRegex;
    }
    @Override
    void setSolvers() {
        // TODO modular solvers?
        super.solvers.add(new PrioritisedPlanning_Solver());
        super.solvers.add(new CBS_Solver());
    }

    @Override
    void setExperiments() {
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums, instancesRegex);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, instanceBuilder, properties);

        /*  =   Add new experiment   =  */
        Experiment experiment = new Experiment(experimentName, instanceManager);
        experiment.skipAfterFail = this.skipAfterFail;
        this.experiments.add(experiment);
    }

}
