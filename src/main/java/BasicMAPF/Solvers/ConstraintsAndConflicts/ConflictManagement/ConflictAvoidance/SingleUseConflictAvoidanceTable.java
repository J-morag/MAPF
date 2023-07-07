package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A conflict avoidance table that is meant to be used in a single CT node. Meant to be filled once with plans, queried
 * many times about conflicts with those plans, and then be discarded.
 */
public class SingleUseConflictAvoidanceTable extends A_ConflictAvoidanceTable {
    /**
     * Contains all goal locations and maps them to the time from which they are occupied (indefinitely).
     * Can't have more than one agent occupying a goal, since that would make the problem unsolvable (in classic MAPF).
     */
    private Map<I_Location, Integer> goalOccupancies;

    /**
     * {@inheritDoc}
     */
    public SingleUseConflictAvoidanceTable(@Nullable Iterable<? extends SingleAgentPlan> plans, @Nullable Agent excludedAgent) {
        super(plans, excludedAgent);
    }

    /**
     * {@inheritDoc}
     */
    public SingleUseConflictAvoidanceTable() {
        super();
    }

    @Override
    protected void initDataStructures() {
        if (goalOccupancies == null){
            this.goalOccupancies = new HashMap<>();
        }
    }

    @Override
    protected void addGoalOccupancy(I_Location location, Move finalMove){
        // add 1 to entry time, so as not to count twice with the entry and in allOccupancies, and also not miss the
        // possible swapping conflict on the last move in the plan (if we were to instead remove the last from allOccupancies)
        if(checkGoals){
            this.goalOccupancies.put(location, finalMove.timeNow + 1);
        }
    }

    @Override
    protected int getNumGoalConflicts(Move move, TimeLocation to, boolean isALastMove){
        int numGoalConflicts = 0;
        // check for a goal occupancy conflicting with this move
        if(goalOccupancies.containsKey(to.location) && goalOccupancies.get(to.location) <= to.time){
            if (!(sharedGoals && move.currLocation.getCoordinate().equals(move.agent.target))){
                numGoalConflicts++;
            }
        }
        // doesn't check if this is a goal move and how many conflicts that would create, which should be OK since
        // the number would be determined by the start time of staying in goal, and this method is used for tie-breaking
        // between equal length plans, so staying at goal at a different time would be a different length plan anyway
        // TODO add support for this anyway? Need it for PIBT style paths... But can just use RemovalbeConflictAvoidanceTable
        return numGoalConflicts;
    }

}
