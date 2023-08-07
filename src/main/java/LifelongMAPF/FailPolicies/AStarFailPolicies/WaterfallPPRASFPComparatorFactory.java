package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public record WaterfallPPRASFPComparatorFactory(@Nullable ArrayList<Integer> lockableTimeBuckets, @Nullable ArrayList<Integer> congestionBuckets) implements I_PPRASFPComparatorFactory{
    public WaterfallPPRASFPComparator create(@Nullable CongestionMap congestionMap,
                                             @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable,
                                             @Nullable Integer horizon){
        return new WaterfallPPRASFPComparator(lockableTimeBuckets, congestionBuckets, conflictAvoidanceTable, congestionMap, horizon);
    }
}
