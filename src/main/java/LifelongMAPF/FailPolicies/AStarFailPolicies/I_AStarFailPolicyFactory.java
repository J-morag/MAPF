package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface I_AStarFailPolicyFactory {

    I_AStarFailPolicy create(@Nullable CongestionMap congestionMap,
                             @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable);
}
