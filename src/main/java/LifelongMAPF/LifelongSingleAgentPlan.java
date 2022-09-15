package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import com.google.common.collect.Iterables;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LifelongSingleAgentPlan extends SingleAgentPlan {

    /**
     * Necessary for when an agent enters a destination that another agent is sitting at as its destination
     */
    private final Map<Integer, I_Coordinate> waypointTimes;

    public LifelongSingleAgentPlan(Agent agent, Iterable<Move> moves, Integer[] waypointSegmentsEndTimes) {
        super(agent, moves);
        if (!(agent instanceof LifelongAgent)){
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " must get Lifelong agent");
        }
        this.waypointTimes = extractWaypointTimes(waypointSegmentsEndTimes);
    }

    public LifelongSingleAgentPlan(SingleAgentPlan planToCopy, Integer[] waypointSegmentsEndTimes) {
        super(planToCopy);
        if (!(planToCopy.agent instanceof LifelongAgent)){
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " must get Lifelong agent");
        }
        this.waypointTimes = extractWaypointTimes(waypointSegmentsEndTimes);
    }

    public LifelongSingleAgentPlan(Agent agent, Integer[] waypointSegmentsEndTimes) {
        super(agent);
        if (!(agent instanceof LifelongAgent)){
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " must get Lifelong agent");
        }
        this.waypointTimes = extractWaypointTimes(waypointSegmentsEndTimes);
    }

    /**
     * @return a map with the times when the agent was at its goal (waypoint)
     */
    private Map<Integer, I_Coordinate> extractWaypointTimes(Integer[] waypointSegmentsEndTimes){
        Map<Integer, I_Coordinate> res = new HashMap<>();
        I_Coordinate startWaypointCoordinate = ((LifelongAgent)agent).waypoints.get(0);
        if (waypointSegmentsEndTimes[0] != 0){
            throw new IllegalArgumentException("invalid start waypoint time");
        }
        if (! moveAt(1).prevLocation.getCoordinate().equals(startWaypointCoordinate)){
            throw new IllegalArgumentException("invalid start waypoint location");
        }
//        if (waypointSegmentsEndTimes[waypointSegmentsEndTimes.length - 1] != getEndTime()){
//            throw new IllegalArgumentException("invalid last waypoint time");
//        }
        res.put(0, startWaypointCoordinate);
        for (int i = 1; // first waypoint is start location
             i < waypointSegmentsEndTimes.length; i++){
            int waypointSegmentEndTime = waypointSegmentsEndTimes[i];
            I_Coordinate currWaypoint = ((LifelongAgent)agent).waypoints.get(i);
            if (!currWaypoint.equals(moveAt(waypointSegmentEndTime).currLocation.getCoordinate())){
                throw new IllegalArgumentException("invalid waypoint times or plan");
            }

            res.put(waypointSegmentEndTime, currWaypoint);

            // peek forwards and take all times when the agent sat at this goal (waypoint)
            for (int time = waypointSegmentEndTime + 1; time <= getEndTime(); time++) {
                if (moveAt(time).currLocation.getCoordinate().equals(currWaypoint)){
                    res.put(time, currWaypoint);
                }
            }
        }
        // Last waypoint may repeatedly be given as goal, so take all times at that goal in the last segment
        I_Coordinate lastWaypoint = Iterables.getLast(((LifelongAgent) agent).waypoints);
        for (int time = waypointSegmentsEndTimes[waypointSegmentsEndTimes.length-1]; time > waypointSegmentsEndTimes[waypointSegmentsEndTimes.length-2]; time--) {
            if (moveAt(time).currLocation.getCoordinate().equals(lastWaypoint)){
                res.put(time, lastWaypoint);
            }
        }
        return res;
    }

    @Override
    public A_Conflict firstConflict(SingleAgentPlan other, boolean sharedGoalsEnabled, boolean sharedSourcesEnabled) {
        // find lower and upper bound for time, and check only in that range
        //the min time to check is the max first move time
        int minTime = Math.max(this.getFirstMoveTime(), other.getFirstMoveTime());
        //the max time to check is the min last move time
        int maxTime = Math.min(this.getEndTime(), other.getEndTime());
        // if they both get to their goals at the same time and share it, it can't have a conflict
        if (sharedGoalsEnabled && this.moveAt(this.getEndTime()).currLocation.equals(other.moveAt(other.getEndTime()).currLocation)
                && this.getEndTime() == other.getEndTime()){
            maxTime -= 1;
        }
        boolean localStayingAtSource = true;
        boolean otherStayingAtSource = true;

        for(int time = minTime; time<= maxTime; time++){
            Move localMove = this.moveAt(time);
            localStayingAtSource &= localMove.prevLocation.equals(localMove.currLocation);
            Move otherMoveAtTime = other.moveAt(time);
            otherStayingAtSource &= otherMoveAtTime.prevLocation.equals(otherMoveAtTime.currLocation);

            A_Conflict firstConflict = A_Conflict.conflictBetween(localMove, otherMoveAtTime);
            if(firstConflict != null && !(sharedSourcesEnabled && localStayingAtSource && otherStayingAtSource) &&
                    !(this.waypointTimes.containsKey(time) && ((LifelongSingleAgentPlan)other).waypointTimes.containsKey(time))){ // only change over super TODO isolate?
                return firstConflict;
            }
        }

        // if we've made it all the way here, the plans don't conflict in their shared timespan.
        // now check if one plan ended and then the other plan had a move that conflicts with the first plan's last position (goal)
        return firstConflictAtGoal(other, maxTime, sharedGoalsEnabled);
    }

    @Override
    protected A_Conflict firstConflictAtGoal(SingleAgentPlan other, int maxTime, boolean sharedGoalsEnabled) {
        if (!(other instanceof LifelongSingleAgentPlan)){
            throw new IllegalArgumentException("other plan must also be Lifelong");
        }
        // if they share goals, the last move of the late ending plan can't be a conflict with the early ending plan.
        int sharedGoalsTimeOffset = sharedGoalsEnabled &&
                this.moveAt(this.getEndTime()).currLocation.equals(other.moveAt(other.getEndTime()).currLocation) ? -1 : 0;

        if(this.getEndTime() != other.getEndTime()){
            LifelongSingleAgentPlan lateEndingPlan = this.getEndTime() > maxTime ? this : (LifelongSingleAgentPlan) other;
            LifelongSingleAgentPlan earlyEndingPlan = this.getEndTime() <= maxTime ? this : (LifelongSingleAgentPlan) other;

            // if plans for "start at goal and stay there" are represented as an empty plan, we will have to make this check
            if (earlyEndingPlan.getEndTime() == -1){
                // can skip late ending plan's start location, since if they conflict on start locations it is an
                // impossible instance (so we shouldn't get one)
                for (int t = 1; t < lateEndingPlan.getEndTime() + sharedGoalsTimeOffset; t++) {
                    I_Location steppingIntoLocation = lateEndingPlan.moveAt(t).currLocation;
                    if (steppingIntoLocation.getCoordinate().equals(earlyEndingPlan.agent.target)){
                        Move stayMove = new Move(earlyEndingPlan.agent, t, steppingIntoLocation, steppingIntoLocation);
                        A_Conflict goalConflict = A_Conflict.conflictBetween(lateEndingPlan.moveAt(t), stayMove);
                        return goalConflict;
                    }
                }
            }
            else{
                // look for the late ending plan stepping into the agent from the early ending plan, sitting at its goal.
                I_Location goalLocation = earlyEndingPlan.moveAt(maxTime).currLocation;
                for (int time = maxTime+1; time <= lateEndingPlan.getEndTime()  + sharedGoalsTimeOffset; time++) {
                    Move stayMove = new Move(earlyEndingPlan.agent, time, goalLocation, goalLocation);
                    A_Conflict goalConflict = A_Conflict.conflictBetween(lateEndingPlan.moveAt(time), stayMove);
                    if(goalConflict != null
                            // no need to check location, because conflict exists
                            && ! (lateEndingPlan.waypointTimes.containsKey(goalConflict.time))
                    ){
                        // The only modification over super. TODO isolate?
                        return goalConflict;
                    }
                }
            }
        }
        return null;
    }
}
