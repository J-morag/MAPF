package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class PostProcIGoFactory implements I_PPRASFPComparatorFactory{
    private final int depth;

    public PostProcIGoFactory(int depth) {
        this.depth = depth;
    }

    @Override
    public Comparator<SingleAgentAStar_Solver.AStarState> create(@Nullable CongestionMap congestionMap, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable, @Nullable Integer horizon) {
        return new PostProcIGo(depth);
    }
}
