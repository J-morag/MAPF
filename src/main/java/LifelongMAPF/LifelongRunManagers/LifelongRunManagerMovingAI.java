package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import org.jetbrains.annotations.NotNull;

public class LifelongRunManagerMovingAI extends A_LifelongRunManager {

    private final String mapsPath;
    private final int[] agentNums;

    public LifelongRunManagerMovingAI(String resultsOutputDir, String mapsPath, int[] agentNums) {
        super(resultsOutputDir);
        this.mapsPath = mapsPath;
        this.agentNums = agentNums;
    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.mapsPath, this.agentNums);
    }

    /* = Experiments =  */

    @NotNull
    @Override
    protected String getExperimentName() {
        return "LifelongMovingAI";
    }

    @Override
    protected @NotNull I_InstanceBuilder getInstanceBuilder() {
        return new InstanceBuilder_MovingAI(true);
    }
}
