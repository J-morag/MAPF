package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Experiment;

public class RunManagerMovingAIBenchmark extends A_RunManager{

    private final String entireBenchmarkDir;
    private final int[] agentNums;

    public RunManagerMovingAIBenchmark(String entireBenchmarkDir, int[] agentNums) {
        super(null);
        this.entireBenchmarkDir = entireBenchmarkDir;
        this.agentNums = agentNums;
    }


    @Override
    void setSolvers() {
        super.solvers.add(new PrioritisedPlanning_Solver());
        super.solvers.add(new CBS_Solver());
    }

    @Override
    void setExperiments() {
        addAllMapsAndInstances(this.entireBenchmarkDir, this.agentNums);
    }

    /* = Experiments =  */

    private void addAllMapsAndInstances(String entireBenchmarkDir, int[] agentNums){
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(entireBenchmarkDir, new InstanceBuilder_MovingAI(), properties);

        /*  =   Add new experiment   =  */
        Experiment EntireMovingAIBenchmark = new Experiment("EntireMovingAIBenchmark", instanceManager);
        this.experiments.add(EntireMovingAIBenchmark);
    }

}
