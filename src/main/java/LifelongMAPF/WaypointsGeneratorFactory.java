package LifelongMAPF;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;

public class WaypointsGeneratorFactory {

    private final Integer randomSeed;
    private final I_Coordinate[] waypoints;

    public WaypointsGeneratorFactory(int randomSeed) {
        this.randomSeed = randomSeed;
        this.waypoints = null;
    }
    public WaypointsGeneratorFactory(I_Coordinate[] waypoints) {
        this.waypoints = waypoints;
        this.randomSeed = null;
    }


    public WaypointsGenerator create() {
        if (randomSeed != null){
            return new WaypointsGeneratorRandom(randomSeed);
        }
        else if (waypoints != null){
            return new WaypointsGeneratorFixed(waypoints);
        }
        else{
            throw new IllegalStateException("WaypointsGeneratorFactory must be initialized with either randomSeed or waypoints");
        }

    }
}
