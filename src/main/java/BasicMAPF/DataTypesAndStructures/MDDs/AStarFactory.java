package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;

public class AStarFactory implements I_MDDSearcherFactory {
    @Override
    public A_MDDSearcher createSearcher(Timeout timeout, I_Location source, I_Location target,
                                        Agent agent, SingleAgentGAndH heuristic) {
        return new AStarMDDBuilder(timeout, source, target, agent, heuristic);
    }
}
