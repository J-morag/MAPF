package Environment;

import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.Maps.MapDimensions;
import BasicCBS.Solvers.AStar.AllPairsShortestPathAStarHeuristic;
import BasicCBS.Solvers.AStar.DistanceTableAStarHeuristic;
import BasicCBS.Solvers.AStar.RunParameters_SAAStar;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;

import java.util.stream.IntStream;

public class RunManagerMovingAIBenchmark extends A_RunManager{

    private String entireBenchmarkDir = "";
    private Integer maxNumAgents = 60;

    @Override
    void setSolvers() {
//        super.solvers.add(new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(new RunParameters_SAAStar(new DistanceTableAStarHeuristic(new AllPairsShortestPathAStarHeuristic())))));
        super.solvers.add(new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver()));
    }

    @Override
    void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.entireBenchmarkDir);
    }

    /* = Experiments =  */

    private void addAllMapsAndInstances(Integer maxNumAgents, String entireBenchmarkDir){
        maxNumAgents = maxNumAgents != null ? maxNumAgents : -1;

        /*  =   Set Path   =*/
        String path = entireBenchmarkDir;

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),properties);

        /*  =   Add new experiment   =  */
        Experiment EntireMovingAIBenchmark = new Experiment("EntireMovingAIBenchmark", instanceManager);
        this.experiments.add(EntireMovingAIBenchmark);
    }
}
