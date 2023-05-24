package LifelongMAPF.FailPolicies;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;

public class PostprocessingRankingAStarFP implements I_AStarFailPolicy {

    /**
     * Min value is the best
     */
    public final Comparator<SingleAgentAStar_Solver.AStarState> comparator;

    public PostprocessingRankingAStarFP(Comparator<SingleAgentAStar_Solver.AStarState> comparator) {
        this.comparator = comparator;
    }

    @Override
    public @Nullable SingleAgentPlan getFailPlan(@NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList, @NotNull HashSet<SingleAgentAStar_Solver.AStarState> ClosedList,
                                                 @NotNull SingleAgentPlan existingPlan) {
        SingleAgentAStar_Solver.AStarState best = null;
        for (SingleAgentAStar_Solver.AStarState state : openList) {
            if (best == null || comparator.compare(state, best) < 0) {
                best = state;
            }
        }
        for (SingleAgentAStar_Solver.AStarState state : ClosedList) {
            if (best == null || comparator.compare(state, best) < 0) {
                best = state;
            }
        }
        return best != null ? best.backTracePlan(existingPlan) : null;
    }
}
