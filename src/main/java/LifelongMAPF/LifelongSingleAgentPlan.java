package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import com.google.common.collect.Iterables;

import java.util.HashMap;
import java.util.Map;

public class LifelongSingleAgentPlan extends SingleAgentPlan {
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
        if (waypointSegmentsEndTimes.length > 1){
            for (int time = waypointSegmentsEndTimes[waypointSegmentsEndTimes.length-1]; time > waypointSegmentsEndTimes[waypointSegmentsEndTimes.length-2]; time--) {
                if (moveAt(time).currLocation.getCoordinate().equals(lastWaypoint)){
                    res.put(time, lastWaypoint);
                }
            }
        }
        return res;
    }
}
