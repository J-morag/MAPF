package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface I_AStarFailPolicy {

    SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList,
                                @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList,
                                @NotNull SingleAgentPlan existingPlan);

}
