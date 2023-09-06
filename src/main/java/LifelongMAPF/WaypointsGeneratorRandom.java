package LifelongMAPF;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class WaypointsGeneratorRandom implements WaypointsGenerator {

    private final Integer randomSeed;

    public WaypointsGeneratorRandom(int randomSeed) {
        this.randomSeed = randomSeed;
    }

    @Override
    public List<I_Coordinate> waypointsSequence(){
        throw new NotImplementedException();
    }

    @Override
    public I_Coordinate waypointAtIndex(int i){
        throw new NotImplementedException();
    }

    @Override
    @Nullable public I_Coordinate nextWaypoint(){
        return nextWaypoint(null);
    }

    @Override
    @Nullable public I_Coordinate nextWaypoint(@Nullable Set<I_Coordinate> waypointsToAvoid){
//        I_Coordinate waypoint = nextWaypointInternal();
//        if (waypoint == null){
//            return null;
//        }
//        while (waypointsToAvoid != null && waypointsToAvoid.contains(waypoint)){
//            waypoint = nextWaypointInternal();
//            if (waypoint == null){
//                return null;
//            }
//        }

        throw new NotImplementedException();
    }

    private I_Coordinate nextWaypointInternal() {
        return null;
    }
}
