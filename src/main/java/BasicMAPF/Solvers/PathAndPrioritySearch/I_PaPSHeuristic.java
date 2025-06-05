package BasicMAPF.Solvers.PathAndPrioritySearch;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.DataTypesAndStructures.TimeInterval;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface I_PaPSHeuristic {
    default int @Nullable [] getH(Agent[] priorityOrderedAgents, int numMDDsAlreadyInNode, @NotNull I_ConstraintSet constraints,
                                  MAPF_Instance currentInstance, SingleAgentGAndH singleAgentHeuristic,
                                  @Nullable Map<I_Location, List<TimeInterval>> safeIntervalsByLocation) {
        int[] res = new int[priorityOrderedAgents.length - numMDDsAlreadyInNode];
        for (int i = numMDDsAlreadyInNode; i < priorityOrderedAgents.length; i++) {
            Agent agent = priorityOrderedAgents[i];
            int shortestPathLength = singleAgentHeuristic.getHToTargetFromLocation(agent.target, currentInstance.map.getMapLocation(agent.source));
            res[i - numMDDsAlreadyInNode] = shortestPathLength;
        }
        return res;
    }
}
