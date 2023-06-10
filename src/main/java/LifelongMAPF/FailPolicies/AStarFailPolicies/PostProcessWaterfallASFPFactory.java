package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record PostProcessWaterfallASFPFactory(@Nullable ArrayList<Integer> lockableTimeBuckets, @Nullable ArrayList<Integer> congestionBuckets, @Nullable Integer horizon, @Nullable Boolean requireLockableToHorizon) implements I_AStarFailPolicyFactory {

    @Override
    public I_AStarFailPolicy create(@Nullable CongestionMap congestionMap, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        return new PostProcessRankingAStarFP(new WaterfallPPRASFPComparator(lockableTimeBuckets, congestionBuckets, conflictAvoidanceTable, congestionMap, horizon), requireLockableToHorizon, conflictAvoidanceTable, horizon);
    }
}
