package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Like {@link GoASFP} but as a post-processing fail policy.
 */
public class PostProcGoASFP implements Comparator<SingleAgentAStar_Solver.AStarState> {

    private final int depth;

    public PostProcGoASFP(int depth) {
        this(depth, null, null, null, null, null);
    }

    public PostProcGoASFP(int depth, @Nullable ArrayList<Integer> lockableTimeBuckets, @Nullable ArrayList<Integer> congestionBuckets,
                          @Nullable RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable,
                          @Nullable CongestionMap congestionMap, @Nullable Integer horizon) {
        this.depth = depth;
    }

    @Override
    public int compare(SingleAgentAStar_Solver.AStarState s1, SingleAgentAStar_Solver.AStarState s2) {
        // TODO not explicit enough, so unsafe, but should be ok since typically g == depth
        int s1Depth = s1.getG();
        int s2Depth = s2.getG();
        if (s1Depth > depth && s2Depth > depth) { // both too deep
            return 0;
        } else if (s1Depth > depth) { // s1 too deep
            return 1;
        } else if (s2Depth > depth) {
            return -1;
        }
        else return s2Depth - s1Depth; // reverse order, to prefer larger values
    }

}
