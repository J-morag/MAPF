package BasicMAPF.Instances.Maps;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;

import java.util.List;

public interface I_Location {

    /**
     * Returns the type of the location.
     * @return the type of the location.
     */
    Enum_MapLocationType getType();

    /**
     * Returns an array that contains references to locations directly reachable from this. Should not include this.
     * The amount of neighbors varies by map and connectivity.
     * @return an array that contains references to locations directly reachable from this. Should not include this.
     */
    List<I_Location> outgoingEdges();

    /**
     * Returns an array of locations from which this location is directly reachable. Should not include this.
     * The amount of neighbors varies by map and connectivity.
     * @return an array of locations from which this location is directly reachable. Should not include this.
     */
    List<I_Location> incomingEdges();

    /**
     * returns the location's coordinate.
     * @return the location's coordinate.
     */
    I_Coordinate getCoordinate();

    /**
     * Get weights of the connections to this location's neighbors.
     * Should be uniformly indexed with the return value of {@link #outgoingEdges()}.
     * @return weights of the connections to this location's neighbors.
     */
    List<Integer> getOutgoingEdgesWeights();

    /**
     * Get weights of the connections from this location's neighbors.
     * Should be uniformly indexed with the return value of {@link #incomingEdges()}.
     * @return weights of the connections from this location's neighbors.
     */
    List<Integer> getIncomingEdgesWeights();

    /**
     * Return true iff other is a neighbor of this.
     * @param other another {@link I_Location}.
     * @return true iff other is a neighbor of this.
     */
    boolean isNeighbor(I_Location other);

}
