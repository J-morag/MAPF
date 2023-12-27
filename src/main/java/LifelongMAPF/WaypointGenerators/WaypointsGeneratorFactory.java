package LifelongMAPF.WaypointGenerators;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WaypointsGeneratorFactory {

    private final Integer randomSeed;
    private final Map<String, List<? extends I_Location>> locationBySubtype;
    private final List<String> destinationSubtypesCycle;
    private final I_Location source;
    private final I_Coordinate[] waypoints;

    public WaypointsGeneratorFactory(int randomSeed, Map<String, List<? extends I_Location>> locationBySubtype,
                                     List<String> destinationSubtypesCycle, I_Location source) {
        this.randomSeed = randomSeed;
        this.locationBySubtype = locationBySubtype;
        this.destinationSubtypesCycle = Collections.unmodifiableList(destinationSubtypesCycle);
        this.source = source;
        this.waypoints = null;
    }
    public WaypointsGeneratorFactory(I_Coordinate[] waypoints) {
        this.waypoints = waypoints;
        this.randomSeed = null;
        locationBySubtype = null;
        destinationSubtypesCycle = null;
        this.source = null;
    }


    public WaypointsGenerator create() {
        if (randomSeed != null && locationBySubtype != null && destinationSubtypesCycle != null){
            return new WaypointsGeneratorRandom(randomSeed, locationBySubtype, destinationSubtypesCycle, source);
        }
        else if (waypoints != null){
            return new WaypointsGeneratorFixed(waypoints);
        }
        else{
            throw new IllegalStateException("WaypointsGeneratorFactory must be initialized with either waypoints or randomSeed, locationBySubtype, and destinationSubtypesCycle");
        }

    }
}
