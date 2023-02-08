package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Experiment;
import java.util.stream.IntStream;

public class RunManagerMovingAIBenchmark extends A_RunManager{

    private final String entireBenchmarkDir;
    private final Integer maxNumAgents;

    public RunManagerMovingAIBenchmark(String entireBenchmarkDir, Integer maxNumAgents) {
        this.entireBenchmarkDir = entireBenchmarkDir;
        this.maxNumAgents = maxNumAgents;
    }


    @Override
    public void setSolvers() {
        super.solvers.add(new PrioritisedPlanning_Solver());
        super.solvers.add(new CBS_Solver());
    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.entireBenchmarkDir);
    }

    /* = Experiments =  */

    private void addAllMapsAndInstances(Integer maxNumAgents, String entireBenchmarkDir){
        maxNumAgents = maxNumAgents != null ? maxNumAgents : -1;

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(entireBenchmarkDir, new InstanceBuilder_MovingAI(),properties);

        /*  =   Add new experiment   =  */
        Experiment EntireMovingAIBenchmark = new Experiment("EntireMovingAIBenchmark", instanceManager);
        this.experiments.add(EntireMovingAIBenchmark);
    }

}
