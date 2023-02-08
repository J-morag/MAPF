package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Experiment;
import java.util.stream.IntStream;

public class RunManagerWarehouse extends A_RunManager{

    private final String warehouseMaps;
    private final Integer maxNumAgents;

    public RunManagerWarehouse(String warehouseMaps, Integer maxNumAgents) {
        this.warehouseMaps = warehouseMaps;
        this.maxNumAgents = maxNumAgents;
    }

    @Override
    void setSolvers() {
        super.solvers.add(new PrioritisedPlanning_Solver(null, null, null, null, true, true));
        super.solvers.add(new CBS_Solver(null, null, null, null, null, null, true, true));
    }

    @Override
    void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.warehouseMaps);
    }

    /* = Experiments =  */

    private void addAllMapsAndInstances(Integer maxNumAgents, String instancesDir){
        maxNumAgents = maxNumAgents != null ? maxNumAgents : -1;

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, new InstanceBuilder_Warehouse(),properties);

        /*  =   Add new experiment   =  */
        Experiment EntireMovingAIBenchmark = new Experiment("WarehouseInstances", instanceManager);
        EntireMovingAIBenchmark.keepSolutionInReport = false;
        EntireMovingAIBenchmark.sharedGoals = true;
        EntireMovingAIBenchmark.sharedSources = true;
        this.experiments.add(EntireMovingAIBenchmark);
    }

}
