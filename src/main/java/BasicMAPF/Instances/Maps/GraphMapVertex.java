package BasicMAPF.Instances.Maps;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.apache.commons.collections4.list.UnmodifiableList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A single location(/vertex) in a {@link GraphMap}. Represents a unique location in the graph.
 * Immutable beyond first initialization (First with a constructor, and then with {@link #setNeighbors(GraphMapVertex[])}.
 */
public class GraphMapVertex implements I_Location {

    /**
     * The type of the location. The type could determine whether or not an agent can traverse or occupy a location.
     */
    public final Enum_MapLocationType locationType;
    public List<I_Location> outgoingEdges;
    private List<I_Location> incomingEdges = new ArrayList<>();
    /**
     * weights for the edges connecting this location to its neighbors. indexed uniformly with {@link #outgoingEdges}.
     * If weights were not provided, contains a uniform edge cost of 1.
     */
    public List<Integer> outgoingEdgeWeights;
    /**
     * weights for the edges connecting neighbors to this location. indexed uniformly with {@link #outgoingEdges}.
     * If weights were not provided, contains a uniform edge cost of 1.
     */
    public List<Integer> incomingEdgeWeights = new ArrayList<>();

    public final I_Coordinate coordinate;

    public final List<String> locationSubtypes;

    GraphMapVertex(Enum_MapLocationType locationType, I_Coordinate coordinate, @Nullable List<String> locationSubtypes) {
        this.locationType = locationType;
        this.coordinate = coordinate;
        this.outgoingEdges = null;
        this.locationSubtypes = locationSubtypes == null ? null : List.copyOf(locationSubtypes);
    }
    GraphMapVertex(Enum_MapLocationType locationType, I_Coordinate coordinate) {
        this(locationType, coordinate, null);
    }

    /**
     * Sets the location's neighbors. All locations in the array should logically be non null.
     * Used during graph construction. Only the first call to this method on an instance affects the instance.
     * Also sets this as a reverse edge on each of the neighbors.
     * @param outgoingEdges the location's neighbors.
     * @throws NullPointerException if an element is null or the given array is null.
     */
    void setNeighbors(GraphMapVertex[] outgoingEdges) {
        Integer[] edgeWeights = new Integer[outgoingEdges.length];
        Arrays.fill(edgeWeights, 1);
        setNeighbors(outgoingEdges, edgeWeights);
    }

    /**
     * Sets the location's neighbors. All locations in the array should logically be non null.
     * Used during graph construction. Only the first call to this method on an instance affects the instance.
     * Also sets this as a reverse edge on each of the neighbors.
     * Also sets weights for the edges to the neighbors.
     * @param outgoingEdges the location's neighbors. must be indexed uniformly with edgeWeights.
     * @param edgeWeights weights of the connections to the neighbors. must be indexed uniformly with neighbors.
     * @throws NullPointerException if an element is null or the given array is null.
     */
    void setNeighbors(GraphMapVertex[] outgoingEdges, Integer[] edgeWeights) {
        // unmodifiable list
        if (outgoingEdges.length != edgeWeights.length){
            throw new IllegalArgumentException("neighbors and edgeWeights must be indexed uniformly");
        }
        this.outgoingEdges = (this.outgoingEdges == null ? List.of(outgoingEdges) : this.outgoingEdges);
        this.outgoingEdgeWeights = (this.outgoingEdgeWeights == null ? List.of(edgeWeights) : this.outgoingEdgeWeights);
        // set reverse edges
        for (int i = 0; i < outgoingEdges.length; i++) {
            GraphMapVertex neighbor = outgoingEdges[i];
            neighbor.incomingEdges.add(this);
            neighbor.incomingEdgeWeights.add(edgeWeights[i]);
        }
    }

    /**
     * Returns the type of the location.
     * @return the type of the location.
     */
    @Override
    public Enum_MapLocationType getType() {
        return locationType;
    }

    /**
     * returns the location's coordinate.
     * @return the location's coordinate.
     */
    @Override
    public I_Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * Returns an UnmodifiableList of this location's neighbors.
     * The amount of neighbors varies by map and connectivity.
     * Runs in O(1).
     * @return an UnmodifiableList of this location's neighbors.
     */
    @Override
    public List<I_Location> outgoingEdges() {
        return this.outgoingEdges;
    }

    /**
     * {@inheritDoc}
     * Also locks the internal list of incoming agents from further modification.
     * @return {@inheritDoc}
     */
    @Override
    public List<I_Location> incomingEdges() {
        if (! (this.incomingEdges instanceof UnmodifiableList) ){
            this.incomingEdges = List.of(this.incomingEdges.toArray(I_Location[]::new));
        }
        return this.incomingEdges;
    }

    /**
     * Returns an UnmodifiableList of the weights of this location's edges.
     * The amount of neighbors varies by map and connectivity.
     * Runs in O(1).
     * Indexed uniformly with the list of neighbors returned by {@link #outgoingEdges()}.
     * @return an UnmodifiableList of the weights of this location's edges.
     */
    @Override
    public List<Integer> getOutgoingEdgesWeights() {
        return this.outgoingEdgeWeights;
    }

    @Override
    public List<Integer> getIncomingEdgesWeights() {
        return null;
    }

    @Override
    public @Nullable List<String> getSubtypes() {
        return locationSubtypes;
    }

    /**
     * Returns true iff other is contained in {@link #outgoingEdges}. In particular, returns false if other==this.
     * @param other another {@link I_Location}.
     * @return true iff other is contained in {@link #outgoingEdges}.
     */
    @Override
    public boolean isNeighbor(I_Location other) {
        boolean result = false;
        for (I_Location neighbor :
                outgoingEdges) {
            result = result || (neighbor.equals(other));
        }
        return result;
    }

    @Override
    public String toString() {
        return "GraphMapLocation{" +
                "coordinate=" + coordinate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphMapVertex that)) return false;

        return coordinate.equals(that.coordinate);
    }

    @Override
    public int hashCode() {
        return coordinate.hashCode();
    }
}
