package LifelongMAPF.FailPolicies;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public interface I_AStarFailPolicy {

    SingleAgentPlan getFailPlan(@NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList,
                                @NotNull HashSet<SingleAgentAStar_Solver.AStarState> ClosedList,
                                @NotNull SingleAgentPlan existingPlan);

}
