package GraphMapPackage;

import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import LargeAgents_CBS.Instances.Maps.Enum_direction;

import java.util.*;


public class GraphMapVertex_LargeAgents extends GraphMapVertex {

    public Map<I_Location, Enum_direction> neighbors;

    GraphMapVertex_LargeAgents(Enum_MapCellType cellType, I_Coordinate coordinate) {
        super(cellType, coordinate);
        this.neighbors = new HashMap<>();
    }

    void setNeighbors(Map<I_Location, Enum_direction> neighbors) {
        // done - set neighbors
        this.neighbors = new HashMap<>();
        for(Map.Entry<I_Location, Enum_direction> entry: neighbors.entrySet()){
            this.neighbors.put(entry.getKey(),entry.getValue());
        }
    }


    public Enum_direction getDirection(I_Location location){
        return this.neighbors.get(location);
    }

    public Collection<Enum_direction> getDirectionCollection(){
        return this.neighbors.values();
    }

    public I_Location getLocationByDirection(Enum_direction direction){
        for (Map.Entry<I_Location, Enum_direction> entry : this.neighbors.entrySet()) {
            if( entry.getValue() == direction ){
                return entry.getKey();
            }
        }
        return null;
    }


    @Override
    public Enum_MapCellType getType() {
        return this.cellType;
    }

    @Override
    public List<I_Location> getNeighbors() {
        return new ArrayList<>(this.neighbors.keySet());
    }

    @Override
    public I_Coordinate getCoordinate() {
        return this.coordinate;
    }

    @Override
    public boolean isNeighbor(I_Location other) {
        boolean result = false;

        // Done - iterate over map
        result = result || this.neighbors.containsKey(other);

        return result;
    }

}
