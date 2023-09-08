package LifelongMAPF;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class WaypointsGeneratorFixed implements WaypointsGenerator {
    private final List<I_Coordinate> waypoints;
    private int index = 0;

    public WaypointsGeneratorFixed(I_Coordinate[] waypoints) {
        this.waypoints = List.of(waypoints);
    }

    public List<I_Coordinate> waypointsSequence(){
        return waypoints;
    }

    public I_Coordinate waypointAtIndex(int i){
        return waypoints.get(i);
    }

    /**
     * Get the next waypoint from this generator. May generate it on-the-fly (or even infinitely) if it is a random generator.
     * @return the next waypoint or null if there are no more waypoints
     */
    @Nullable public I_Coordinate nextWaypoint(){
        return nextWaypoint(null);
    }

    /**
     * Get the next waypoint from this generator. May generate it on-the-fly (or even infinitely) if it is a random generator.
     * Tries to avoid the given waypoints if about to assign a waypoint that is in the set, assigning instead a similar waypoint.
     * @return the next waypoint or null if there are no more waypoints
     */
    @Nullable public I_Coordinate nextWaypoint(@Nullable Set<I_Location> waypointsToAvoid){
        // TODO log a warning if waypointsToAvoid is not null and not empty
        return nextWaypointInternal();
    }

    @Nullable private I_Coordinate nextWaypointInternal() {
        return index < waypoints.size() ? waypoints.get(index++) : null;
    }
}
