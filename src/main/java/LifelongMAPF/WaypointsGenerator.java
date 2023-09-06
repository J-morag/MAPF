package LifelongMAPF;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface WaypointsGenerator {
    List<I_Coordinate> waypointsSequence();

    I_Coordinate waypointAtIndex(int i);

    /**
     * Get the next waypoint from this generator. May generate it on-the-fly (or even infinitely) if it is a random generator.
     * @return the next waypoint or null if there are no more waypoints
     */
    @Nullable default I_Coordinate nextWaypoint(){
        return nextWaypoint(null);
    }

    /**
     * Get the next waypoint from this generator. May generate it on-the-fly (or even infinitely) if it is a random generator.
     * Tries to avoid the given waypoints if about to assign a waypoint that is in the set, assigning instead a similar waypoint.
     * @return the next waypoint or null if there are no more waypoints
     */
    @Nullable I_Coordinate nextWaypoint(@Nullable Set<I_Location> waypointsToAvoid);

}
