package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;

public class DFSFactory implements I_MDDSearcherFactory {

    private boolean disappearAtGoal = false;

    @Override
    public void setDefaultDisappearAtGoal(boolean disappearAtGoal) {
        this.disappearAtGoal = disappearAtGoal;
    }

    @Override
    public A_MDDSearcher createSearcher(Timeout timeout, I_Location source, I_Location target, Agent agent, SingleAgentGAndH heuristic) {
        return new DFSMDDBuilder(timeout, source, target, agent, heuristic, disappearAtGoal);
    }

}