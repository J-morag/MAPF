package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class IAvoid1ASFPFactory implements I_AStarFailPolicyFactory {
    @Override
    public I_AStarFailPolicy create(@Nullable CongestionMap congestionMap, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        return new IAvoid1ASFP(conflictAvoidanceTable);
    }
}
