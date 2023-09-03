package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Extends {@link Agent} by providing a list of waypoints the agent must achieve in order.
 */
public class LifelongAgent extends Agent {

    /**
     * An unmodifiable list of waypoints for a lifelong agent. Treat the first one as the agent's source.
     */
    public final List<I_Coordinate> waypoints;

    public LifelongAgent(Agent agent, I_Coordinate[] waypoints){
        this(agent.iD, agent.source, agent.target, agent.priority, waypoints);
    }

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, I_Coordinate[] waypoints) {
        this(iD, source, target, 1, waypoints);
    }

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, int priority, @NotNull I_Coordinate[] waypoints) {
        super(iD, source, target, priority);
        if (waypoints.length == 0){
            throw new IllegalArgumentException("Waypoints must contain at least one coordinate");
        }
        I_Coordinate prevCoordinate = source;
        if (!prevCoordinate.equals(waypoints[0])){
            throw new IllegalArgumentException("First waypoint must be the agent's source");
        }
        for (int i = 1; i < waypoints.length; i++){
            I_Coordinate currCoordinate = waypoints[i];
            if (prevCoordinate.equals(currCoordinate)){
                throw new IllegalArgumentException("Repeated waypoints must not be adjacent (waypoint " + i + " is repeated)");
            }
            prevCoordinate = currCoordinate;
        }
        if (!prevCoordinate.equals(target)){
            throw new IllegalArgumentException("Last waypoint must be the agent's target. Expected: " + target + ", got: " + prevCoordinate);
        }
        this.waypoints = List.of(waypoints);
    }

}
