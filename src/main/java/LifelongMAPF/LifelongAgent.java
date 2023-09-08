package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.jetbrains.annotations.NotNull;

/**
 * Extends {@link Agent} by providing a list of waypoints the agent must achieve in order.
 */
public class LifelongAgent extends Agent {

    public final WaypointsGeneratorFactory waypointsGeneratorFactory;

    public LifelongAgent(Agent agent, @NotNull I_Coordinate[] waypoints){
        this(agent.iD, agent.source, agent.target, agent.priority, waypoints);
    }

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, I_Coordinate[] waypoints) {
        this(iD, source, target, 1, waypoints);
    }

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, int priority, @NotNull I_Coordinate[] waypoints) {
        this(iD, source, target, priority, new WaypointsGeneratorFactory(waypoints));
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
    }

    public WaypointsGenerator createWaypointsGenerator(){
        return waypointsGeneratorFactory.create();
    }

    public LifelongAgent(Agent agent, @NotNull WaypointsGeneratorFactory waypointsGeneratorFactory){
        this(agent.iD, agent.source, agent.target, agent.priority, waypointsGeneratorFactory);
    }

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, int priority,  @NotNull WaypointsGeneratorFactory waypointsGeneratorFactory) {
        super(iD, source, target, priority);
        this.waypointsGeneratorFactory = waypointsGeneratorFactory;
    }

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, @NotNull WaypointsGeneratorFactory waypointsGeneratorFactory) {
        this(iD, source, target, 1, waypointsGeneratorFactory);
    }
}
