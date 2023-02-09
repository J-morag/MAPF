package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.*;
import LifelongMAPF.AgentSelectors.AllStationaryAgentsSubsetSelector;
import LifelongMAPF.LifelongSimulationSolver;
import org.jetbrains.annotations.NotNull;

public class LifelongRunManagerWarehouse extends A_LifelongRunManager {

    private final String warehouseMaps;
    private final Integer maxNumAgents;

    public LifelongRunManagerWarehouse(String warehouseMaps, Integer maxNumAgents) {
        this.warehouseMaps = warehouseMaps;
        this.maxNumAgents = maxNumAgents;
    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.warehouseMaps);
    }

    /* = Experiments =  */

    @NotNull
    @Override
    protected String getExperimentName() {
        return "LifelongWarehouse";
    }

    @Override
    protected @NotNull I_InstanceBuilder getInstanceBuilder() {
        return new InstanceBuilder_Warehouse();
    }

    @Override
    protected @NotNull InstanceProperties getInstanceProperties() {
//        return new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());
        return new InstanceProperties(null, -1, new int[]{maxNumAgents});
    }
}
