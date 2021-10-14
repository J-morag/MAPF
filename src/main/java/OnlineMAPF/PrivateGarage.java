package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;

import java.util.List;

/**
 * Represents a private location where an {@link OnlineAgent} can wait before entering the {@link BasicCBS.Instances.Maps.I_Map map}.
 * This is required, because without having such a location for each agent, the problem becomes incomplete, as agents
 * can appear right on top of other agents (in the same {@link BasicCBS.Instances.Maps.I_Location location}).
 * If the map is to be seen as a graph, then these garages are all vertices with an indegree of 0, and an outdegree of 1.
 */
public class PrivateGarage implements I_Location {

    /**
     * The only {@link I_Location cell} in the {@link BasicCBS.Instances.Maps.I_Map map} that the {@link Agent agent} can enter
     * the {@link BasicCBS.Instances.Maps.I_Map map} through.
     */
    public final I_Location mapEntryPoint;
    private final I_Coordinate coordinate;
    private final int ownerID;

    public PrivateGarage(int ownerID, I_Location mapEntryPoint) {
        if(mapEntryPoint == null) {throw new IllegalArgumentException("PrivateGarage: mapEntryPoint can't be null.");}
        this.ownerID = ownerID;
        this.mapEntryPoint = mapEntryPoint;
        this.coordinate = new GarageCoordinate();
    }

    @Override
    public Enum_MapCellType getType() {
        return Enum_MapCellType.EMPTY;
    }

    @Override
    public List<I_Location> getNeighbors() {
        return List.of(mapEntryPoint);
    }

    @Override
    public I_Coordinate getCoordinate() {
        return this.coordinate;
    }

    @Override
    public boolean isNeighbor(I_Location other) {
        return mapEntryPoint.equals(other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrivateGarage)) return false;

        PrivateGarage that = (PrivateGarage) o;

        return ownerID == that.ownerID;

    }

    @Override
    public int hashCode() {
        return ownerID;
    }

    private class GarageCoordinate implements I_Coordinate{

        public int getID(){return PrivateGarage.this.ownerID;}

        @Override
        public float distance(I_Coordinate other) {
            // has to jump to the entry point and then go from there to the goal.
            return 1 + PrivateGarage.this.mapEntryPoint.getCoordinate().distance(other) ;
        }

        @Override
        public String toString() {
//            return "(agent" + PrivateGarage.this.owner.iD + "'s garage)";
            return "(garage " + PrivateGarage.this.ownerID + ")";
        }

        @Override
        public int hashCode() {
            return getID();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GarageCoordinate)) return false;

            GarageCoordinate that = (GarageCoordinate) o;

            return this.getID() == that.getID();

        }
    }
}
