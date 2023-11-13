package BasicMAPF.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

public interface I_MDDSearcherFactory {
    A_MDDSearcher createSearcher(Timeout timeout, I_Location source, I_Location target, Agent agent,
                                 DistanceTableSingleAgentHeuristicMDD heuristic);

    void setDefaultDisappearAtGoal(boolean disappearAtGoal);
}
