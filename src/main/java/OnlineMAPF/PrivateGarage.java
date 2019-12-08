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

    /**
     * The agent waits outside of the graph, yet the only coordinate that can represent the garage would be that of its
     * {@link #mapEntryPoint}.
     * @return the coordinate of the garage's {@link #mapEntryPoint}.
     */
    @Override
    public I_Coordinate getCoordinate() {
        return this.coordinate;
    }

    @Override
    public boolean isNeighbor(I_Location other) {
        return mapEntryPoint.equals(other);
    }

    // does not override equals() and hashCode(). since every agent has a unique and private garage, address equality is good.

    private class GarageCoordinate implements I_Coordinate{

        @Override
        public float distance(I_Coordinate other) {
            return other == PrivateGarage.this.mapEntryPoint.getCoordinate() ? 0 : -1;
        }

        @Override
        public String toString() {
//            return "(agent" + PrivateGarage.this.owner.iD + "'s garage)";
            return "(garage " + PrivateGarage.this.ownerID + ")";
        }

        // address equality is good enough, since these are supposed to be unique

    }
}
