package BasicMAPF.Solvers.ICTS.MDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;

public class AStarFactory implements I_MDDSearcherFactory {

    private boolean disappearAtGoal = false;

    public AStarFactory(boolean disappearAtGoal) {
        this.disappearAtGoal = disappearAtGoal;
    }

    public AStarFactory() {
    }

    @Override
    public void setDefaultDisappearAtGoal(boolean disappearAtGoal) {
        this.disappearAtGoal = disappearAtGoal;
    }

    @Override
    public A_MDDSearcher createSearcher(ICTS_Solver highLevelSolver, I_Location source, I_Location target,
                                        Agent agent, DistanceTableAStarHeuristicICTS heuristic) {
        return new AStarMDDBuilder(highLevelSolver, source, target, agent, heuristic, disappearAtGoal);
    }
}
