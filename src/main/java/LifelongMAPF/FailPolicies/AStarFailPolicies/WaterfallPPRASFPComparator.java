package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Waterfall of criteria
 */
public class WaterfallPPRASFPComparator implements Comparator<SingleAgentAStar_Solver.AStarState> {

    public final List<Integer> lockableTimeBuckets;
    private final List<Integer> congestionBuckets;
    private final RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable;
    private final CongestionMap congestionMap;
    private final Integer horizon;

    public WaterfallPPRASFPComparator() {
        this(null, null, null, null, null);
    }

    public WaterfallPPRASFPComparator(@Nullable ArrayList<Integer> lockableTimeBuckets, @Nullable ArrayList<Integer> congestionBuckets,
                                      @Nullable RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable,
                                      @Nullable CongestionMap congestionMap, @Nullable Integer horizon) {
        if (lockableTimeBuckets != null && conflictAvoidanceTable == null){
            throw new IllegalArgumentException("lockableTimeBuckets can't be not null if conflictAvoidanceTable is null");
        }
        if (congestionBuckets != null && congestionMap == null){
            throw new IllegalArgumentException("congestionBuckets can't be not null if congestionMap is null");
        }
        this.lockableTimeBuckets = Objects.requireNonNullElse(lockableTimeBuckets, Collections.singletonList(100000));
        this.congestionBuckets = Objects.requireNonNullElse(congestionBuckets, Arrays.asList(0, 3, 6));
        this.conflictAvoidanceTable = conflictAvoidanceTable;
        this.congestionMap = congestionMap;
        this.horizon = horizon;
    }

    @Override
    public int compare(SingleAgentAStar_Solver.AStarState s1, SingleAgentAStar_Solver.AStarState s2) {
        int compared;

        compared = compareFutureLockableTimeSpan(s1, s2, conflictAvoidanceTable, horizon);
        if (compared != 0) {
            return compared;
        }

        compared = compareCongestion(s1, s2, congestionMap);
        if (compared != 0)
            return compared;

        compared = compareHValue(s1, s2);
        if (compared != 0) {
            return compared;
        }

//        compared = compareGValue(s1, s2);
//        if (compared != 0) {
//            return compared;
//        }

        return compared;
    }

    private int compareFutureLockableTimeSpan(SingleAgentAStar_Solver.AStarState s1, SingleAgentAStar_Solver.AStarState s2,
                                              RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable, Integer horizon) {
        int timeUntilNextConstraint1 = getTimeUntilNextConstraint(s1, conflictAvoidanceTable);
        int timeUntilNextConstraint2 = getTimeUntilNextConstraint(s2, conflictAvoidanceTable);

        if (horizon != null){
            timeUntilNextConstraint1 = Math.min(timeUntilNextConstraint1, horizon);
            timeUntilNextConstraint2 = Math.min(timeUntilNextConstraint2, horizon);
        }

        if (lockableTimeBuckets != null){
            timeUntilNextConstraint1 = Collections.binarySearch(lockableTimeBuckets, timeUntilNextConstraint1);
            timeUntilNextConstraint2 = Collections.binarySearch(lockableTimeBuckets, timeUntilNextConstraint2);
        }

        // in reverse order, to prefer larger values
        return Integer.compare(timeUntilNextConstraint2, timeUntilNextConstraint1);
    }

    public static int getTimeUntilNextConstraint(SingleAgentAStar_Solver.AStarState s, RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
//        int firstConflictTime = constraints.lastRejectAt(s.move.currLocation, s.move.agent);
        int firstConflictTime = conflictAvoidanceTable.firstConflictTime(s.move, true);
        if (firstConflictTime < 0)
            firstConflictTime = Integer.MAX_VALUE;

        return firstConflictTime - s.getMove().timeNow;
    }

    private int compareHValue(SingleAgentAStar_Solver.AStarState s1, SingleAgentAStar_Solver.AStarState s2) {
        return Float.compare(s1.getF() - s1.getG(), s2.getF() - s2.getG());
    }

//    private int compareGValue(SingleAgentAStar_Solver.AStarState s1, SingleAgentAStar_Solver.AStarState s2) {
//        return Integer.compare(s1.getG(), s2.getG());
//    }

    private int compareCongestion(SingleAgentAStar_Solver.AStarState s1, SingleAgentAStar_Solver.AStarState s2, CongestionMap congestionMap) {
        int s1Congestion = congestionMap.congestionAt(s1.move.currLocation);
        int s2Congestion = congestionMap.congestionAt(s2.move.currLocation);
        return congestionBuckets == null ? Integer.compare(s1Congestion, s2Congestion) :
                Integer.compare(Collections.binarySearch(congestionBuckets, s1Congestion), Collections.binarySearch(congestionBuckets, s2Congestion));
    }

}
