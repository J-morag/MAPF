package Environment.RunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSBuilder;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSHeuristicDefault;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PCSHeuristicSIPP;
import BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PriorityConstrainedSearch;
import Environment.Experiment;
import Environment.Visualization.I_VisualizeSolution;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GenericRunManager extends A_RunManager {

    private final String instancesDir;
    private final int[] agentNums;
    private final I_InstanceBuilder instanceBuilder;
    private final String experimentName;
    private final boolean skipAfterFail;
    private final String instancesRegex;
    private List<I_Solver> solversOverride;
    private final Integer timeoutEach;

    public GenericRunManager(@NotNull String instancesDir, int[] agentNums, @NotNull I_InstanceBuilder instanceBuilder,
                             @NotNull String experimentName, boolean skipAfterFail, String instancesRegex,
                             String resultsOutputDir, String resultsFilePrefix, I_VisualizeSolution solutionVisualizer,
                             Integer timeoutEach) {
        super(resultsOutputDir, solutionVisualizer);
        if (agentNums == null){
            throw new IllegalArgumentException("AgentNums can't be null");
        }
        this.instancesDir = instancesDir;
        this.agentNums = agentNums;
        this.instanceBuilder = instanceBuilder;
        this.experimentName = experimentName;
        this.skipAfterFail = skipAfterFail;
        this.instancesRegex = instancesRegex;
        this.resultsFilePrefix = resultsFilePrefix;
        this.timeoutEach = timeoutEach;
    }
    @Override
    void setSolvers() {
        // TODO modular solvers?
//        if (solversOverride != null){
//            super.solvers = solversOverride;
//            return;
//        }
//        super.solvers.add(new PrioritisedPlanning_Solver(null, null, null,
//                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.randomRestarts),
//                null, null, null));

        super.solvers.add(new CBS_Solver());

        PrioritisedPlanning_Solver pp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.none),
                null, null, null);
        pp.name = "PP-no-restarts";
        super.solvers.add(pp);

        PrioritisedPlanning_Solver ppRandomAStar = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.AStarRestarts, 10000000, RestartsStrategy.RestartsKind.none),
                null, null, null);
        ppRandomAStar.name = "PP-rand-AStar";
        super.solvers.add(ppRandomAStar);

        PriorityConstrainedSearch pcs = new PCSBuilder().setUseSimpleMDDCache(true).setMDDCacheDepthDeltaMax(1)
                .setUsePartialGeneration(true).setPCSHeuristic(new PCSHeuristicDefault()).createPCS();
        pcs.name = "PCS";
        super.solvers.add(pcs);

        PriorityConstrainedSearch PCS_SIPPH = new PCSBuilder().setUseSimpleMDDCache(true).setMDDCacheDepthDeltaMax(1)
                .setUsePartialGeneration(true).setPCSHeuristic(new PCSHeuristicSIPP()).createPCS();
        PCS_SIPPH.name = "PCS_SIPPH";
        super.solvers.add(PCS_SIPPH);
    }

    public void overrideSolvers(@NotNull List<I_Solver> solvers){
        this.solversOverride = solvers;
    }

    @Override
    void setExperiments() {
        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, agentNums, instancesRegex);

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, instanceBuilder, properties);

        /*  =   Add new experiment   =  */
        Experiment experiment = new Experiment(experimentName, instanceManager, null, timeoutEach);
        experiment.skipAfterFail = this.skipAfterFail;
        experiment.visualizer = this.visualizer;
        this.experiments.add(experiment);
    }

}
