package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DeepPartialSolutionsStrategy;
import LifelongMAPF.AgentSelectors.AllStationaryAgentsSubsetSelector;
import LifelongMAPF.LifelongSimulationSolver;
import org.jetbrains.annotations.NotNull;

public class LifelongRunManagerMovingAI extends A_LifelongRunManager {

    private final String mapsPath;
    private final Integer maxNumAgents;

    public LifelongRunManagerMovingAI(String mapsPath, Integer maxNumAgents) {
        this.mapsPath = mapsPath;
        this.maxNumAgents = maxNumAgents;
    }

    @Override
    public void setSolvers() {
        A_Solver stationaryAgentsPrPDeepPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DeepPartialSolutionsStrategy());
        stationaryAgentsPrPDeepPartial.name = "stationaryAgentsPrPDeepPartial";
        super.solvers.add(stationaryAgentsPrPDeepPartial);
    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.mapsPath);
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

    @NotNull
    @Override
    protected InstanceProperties getInstanceProperties() {
//        return new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());
//        return new InstanceProperties(null, -1, new int[]{maxNumAgents});
        return new InstanceProperties(null, -1, new int[]{100, 150, 200});
    }

}
