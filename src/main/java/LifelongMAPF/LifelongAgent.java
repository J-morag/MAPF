package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;

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

    public LifelongAgent(int iD, I_Coordinate source, I_Coordinate target, int priority, I_Coordinate[] waypoints) {
        super(iD, source, target, priority);
        this.waypoints = List.of(waypoints);
    }

}
