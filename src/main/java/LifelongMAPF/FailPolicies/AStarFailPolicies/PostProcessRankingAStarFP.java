package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.Solvers.SingleAgentPlan;
import LifelongMAPF.FailPolicies.StayOnceFailPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Objects;
import java.util.Set;

public class PostProcessRankingAStarFP implements I_AStarFailPolicy {
    private static final int DEBUG = 1;

    /**
     * Min value is the best
     */
    public final Comparator<SingleAgentAStar_Solver.AStarState> comparator;
    private final boolean requireLockableToHorizon;
    private final RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable;
    private final Integer horizon;

    public PostProcessRankingAStarFP(@NotNull Comparator<SingleAgentAStar_Solver.AStarState> comparator, @Nullable Boolean requireLockableToHorizon,
                                     @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable, @Nullable Integer horizon) {
        this.comparator = comparator;
        this.requireLockableToHorizon = Objects.requireNonNullElse(requireLockableToHorizon, true);
        this.conflictAvoidanceTable = conflictAvoidanceTable;
        this.horizon = Objects.requireNonNullElse(horizon, Integer.MAX_VALUE);
    }

    @Override
    public @Nullable SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                                 @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList,
                                                 @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList,
                                                 @NotNull SingleAgentPlan existingPlan) {
        SingleAgentAStar_Solver.AStarState best = null;
        for (SingleAgentAStar_Solver.AStarState state : openList) {
            best = getBetterOfTwoStates(best, state);
        }
        for (SingleAgentAStar_Solver.AStarState state : ClosedList) {
            best = getBetterOfTwoStates(best, state);
        }
        SingleAgentPlan res = best != null ? best.backTracePlan(existingPlan) : StayOnceFailPolicy.getStayOncePlan(farthestCommittedTime, a, agentLocation, conflictAvoidanceTable);
        if (DEBUG >= 2) {
            System.out.println("PostProcessRankingAStarFP.getFailPlan: res = " + res);
        }
        return res;
    }

    private SingleAgentAStar_Solver.AStarState getBetterOfTwoStates(SingleAgentAStar_Solver.AStarState best,
                                                                    SingleAgentAStar_Solver.AStarState state) {
        if ((!requireLockableToHorizon || WaterfallPPRASFPComparator.getTimeUntilNextConstraint(state, conflictAvoidanceTable) >= this.horizon)
                && (best == null || comparator.compare(state, best) < 0)) {
            best = state;
        }
        return best;
    }
}
