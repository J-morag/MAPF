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

public class PaPSHeuristicDefault implements I_PaPSHeuristic {
    @Override
    public int @Nullable [] getH(Agent[] priorityOrderedAgents, int numMDDsAlreadyInNode, @NotNull I_ConstraintSet constraints,
                                 MAPF_Instance currentInstance, SingleAgentGAndH singleAgentHeuristic,
                                 @Nullable Map<I_Location, List<TimeInterval>> safeIntervalsByLocation) {
        return I_PaPSHeuristic.super.getH(priorityOrderedAgents, numMDDsAlreadyInNode, constraints, currentInstance, singleAgentHeuristic, safeIntervalsByLocation);
    }
}
