package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
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
    private final WaterfallPPRASFPComparatorFactory waterfallComparatorFactory;
    private final boolean requireLockableToHorizon;
    private final Integer horizon;

    public PostProcessRankingAStarFP(@NotNull WaterfallPPRASFPComparatorFactory waterfallComparatorFactory,
                                     @Nullable Boolean requireLockableToHorizon, @Nullable Integer horizon) {
        this.waterfallComparatorFactory = waterfallComparatorFactory;
        this.requireLockableToHorizon = Objects.requireNonNullElse(requireLockableToHorizon, true);
        this.horizon = Objects.requireNonNullElse(horizon, Integer.MAX_VALUE);
    }

    @Override
    public SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                       @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList,
                                       @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList, @NotNull SingleAgentPlan existingPlan,
                                       @Nullable CongestionMap congestionMap,
                                       @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        Comparator<SingleAgentAStar_Solver.AStarState> comparator = waterfallComparatorFactory.create(congestionMap, conflictAvoidanceTable, horizon);
        SingleAgentAStar_Solver.AStarState best = null;
        for (SingleAgentAStar_Solver.AStarState state : openList) {
            best = getBetterOfTwoStates(best, state, comparator, conflictAvoidanceTable);
        }
        for (SingleAgentAStar_Solver.AStarState state : ClosedList) {
            best = getBetterOfTwoStates(best, state, comparator, conflictAvoidanceTable);
        }
        SingleAgentPlan res = best != null ? best.backTracePlan(existingPlan) : StayOnceFailPolicy.getStayOncePlan(farthestCommittedTime, a, agentLocation, conflictAvoidanceTable);
        if (DEBUG >= 2) {
            if (requireLockableToHorizon){
                System.out.println("PostProcessRankingAStarFP.getFailPlan: found LockableToInf = " + (best != null));
            }
            System.out.println("PostProcessRankingAStarFP.getFailPlan: res = " + res);
        }
        return res;
    }

    private @Nullable SingleAgentAStar_Solver.AStarState getBetterOfTwoStates(SingleAgentAStar_Solver.AStarState best,
                                                                              SingleAgentAStar_Solver.AStarState state,
                                                                              Comparator<SingleAgentAStar_Solver.AStarState> comparator,
                                                                              @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        if (
                (!requireLockableToHorizon || isLockableToHorizon(state, conflictAvoidanceTable))
                && (best == null || comparator.compare(state, best) < 0)) {
            best = state;
        }
        return best;
    }

    private boolean isLockableToHorizon(SingleAgentAStar_Solver.AStarState state, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        return WaterfallPPRASFPComparator.getTimeUntilNextConstraint(state, conflictAvoidanceTable) >= this.horizon;
    }


}
