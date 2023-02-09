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
    private final int[] agentNums;

    public RunManagerWarehouse(String warehouseMaps, int[] agentNums) {
        this.warehouseMaps = warehouseMaps;
        this.agentNums = agentNums;
    }

    @Override
    void setSolvers() {
        super.solvers.add(new PrioritisedPlanning_Solver(null, null, null, null, true, true));
        super.solvers.add(new CBS_Solver(null, null, null, null, null, null, true, true));
    }

    @Override
    void setExperiments() {
        addAllMapsAndInstances(this.warehouseMaps, this.agentNums);
    }

    /* = Experiments =  */

    private void addAllMapsAndInstances(String instancesDir, int[] agentNums){
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums);

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
