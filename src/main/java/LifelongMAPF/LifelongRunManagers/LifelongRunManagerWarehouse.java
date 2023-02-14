package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import org.jetbrains.annotations.NotNull;

public class LifelongRunManagerWarehouse extends A_LifelongRunManager {

    private final String warehouseMaps;
    private final int[] agentNums;

    public LifelongRunManagerWarehouse(String resultsOutputDir, String warehouseMaps, int[] agentNums) {
        super(resultsOutputDir);
        this.warehouseMaps = warehouseMaps;
        this.agentNums = agentNums;
    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.warehouseMaps, this.agentNums);
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
}
