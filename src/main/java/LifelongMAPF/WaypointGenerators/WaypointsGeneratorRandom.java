package LifelongMAPF.WaypointGenerators;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WaypointsGeneratorRandom implements WaypointsGenerator {

    private final Random rand;
    private final Map<String, List<? extends I_Location>> locationBySubtype;
    private final List<String> destinationSubtypesCycle;
    private final I_Location source;
    private final List<I_Coordinate> waypoints = new ArrayList<>();
    private final int startingIndex;

    public WaypointsGeneratorRandom(int randomSeed, Map<String, List<? extends I_Location>> locationBySubtype,
                                    List<String> destinationSubtypesCycle, I_Location source) {
        this.rand = new Random(randomSeed);
        this.locationBySubtype = locationBySubtype;
        this.destinationSubtypesCycle = destinationSubtypesCycle;
        this.source = source;
        this.startingIndex = getStartingIndex(source, rand);
    }

    @Override
    public List<I_Coordinate> waypointsSequence(){
        return Collections.unmodifiableList(waypoints);
    }

    @Override
    public I_Coordinate waypointAtIndex(int i){
        return waypoints.get(i);
    }

    @Override
    @Nullable public I_Coordinate nextWaypoint(){
        return nextWaypoint(null);
    }

    @Override
    @Nullable public I_Coordinate nextWaypoint(@Nullable Set<I_Location> waypointsToAvoid){
        if (waypoints.isEmpty()){
            this.waypoints.add(source.getCoordinate());
            return source.getCoordinate();
        }
        String desiredSubtype = destinationSubtypesCycle.get((waypoints.size() + startingIndex) % destinationSubtypesCycle.size());
        List<? extends I_Location> locationsWithDesiredSubtype = locationBySubtype.get(desiredSubtype);
        I_Location nextDestination = getRandomFromList(locationsWithDesiredSubtype);
        List<? extends I_Location> desiredLocationsAndNotAvoided = null;
        while (Iterables.getLast(waypoints).equals(nextDestination.getCoordinate()) || // don't want repeated destinations
                // try to avoid overcapacity destinations. If no desired undercapacity destination exists, stick with whatever we got
                (waypointsToAvoid != null && waypointsToAvoid.contains(nextDestination) &&
                        !(desiredLocationsAndNotAvoided =
                                desiredLocationsAndNotAvoided(waypointsToAvoid, desiredSubtype, locationsWithDesiredSubtype))
                                .isEmpty())
        ){
            nextDestination = desiredLocationsAndNotAvoided == null || desiredLocationsAndNotAvoided.isEmpty() ?
                    getRandomFromList(locationsWithDesiredSubtype) :
                    getRandomFromList(desiredLocationsAndNotAvoided);
        }
        waypoints.add(nextDestination.getCoordinate());

        return nextDestination.getCoordinate();
    }

    private I_Location getRandomFromList(List<? extends I_Location> locationsWithDesiredSubtype) {
        return locationsWithDesiredSubtype.get(rand.nextInt(locationsWithDesiredSubtype.size()));
    }

    private static List<? extends I_Location> desiredLocationsAndNotAvoided(@NotNull Set<I_Location> waypointsToAvoid, String desiredSubtype, List<? extends I_Location> locationsWithDesiredSubtype) {
        List<? extends I_Location> desiredLocationsNotOverCapacity = new LinkedList<>(locationsWithDesiredSubtype);
        desiredLocationsNotOverCapacity.removeAll(waypointsToAvoid);
        return desiredLocationsNotOverCapacity;
    }

    private int getStartingIndex(I_Location sourceLocation, Random random) {
        @Nullable List<String> sourceSubtypes = sourceLocation.getSubtypes();
        if (sourceSubtypes == null || sourceSubtypes.isEmpty()){
            return 0;
        }
        else {
            List<Integer> matchingIndices = new ArrayList<>();
            for (int i = 0; i < destinationSubtypesCycle.size(); i++) {
                if (sourceSubtypes.contains(destinationSubtypesCycle.get(i))){
                    matchingIndices.add(i);
                }
            }
            return matchingIndices.get(random.nextInt(matchingIndices.size()));
        }
    }
}
