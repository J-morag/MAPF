package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.NotNull;

public interface I_MDDSearcherFactory {
    A_MDDSearcher createSearcher(@NotNull Timeout timeout, @NotNull I_Location source, @NotNull I_Location target, @NotNull Agent agent,
                                 @NotNull SingleAgentGAndH heuristic);
}
