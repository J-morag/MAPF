package BasicMAPF.Solvers.ICTS.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;

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
    public A_MDDSearcher createSearcher(Timeout timeout, I_Location source, I_Location target,
                                        Agent agent, SingleAgentGAndH heuristic) {
        return new AStarMDDBuilder(timeout, source, target, agent, heuristic, disappearAtGoal);
    }
}
