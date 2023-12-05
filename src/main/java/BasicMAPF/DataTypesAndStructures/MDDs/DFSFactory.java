package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;

/**
 * @deprecated This class is deprecated because it isn't covered by any tests or any code,
 * and is probably dominated by the AStarFactory.
 */
@Deprecated
public class DFSFactory implements I_MDDSearcherFactory {
    @Override
    public A_MDDSearcher createSearcher(Timeout timeout, I_Location source, I_Location target, Agent agent, SingleAgentGAndH heuristic) {
        return new DFSMDDBuilder(timeout, source, target, agent, heuristic);
    }

}
