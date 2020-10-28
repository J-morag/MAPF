package BasicCBS.Instances.Maps;

import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;

import java.util.List;

/**
 * A single cell in a {@link GraphMap}. Represents a unique location in the graph.
 * Immutable beyond first initialization (First with a constructor, and then with {@link #setNeighbors(GraphMapVertex[])}.
 */
public class GraphMapVertex implements I_Location {

    private static int IDCounter = 0;
    /**
     * Unique ID. This exists for creating good and deterministic hash codes.
     */
    private final int UniqueID = IDCounter++;

    /**
     * The type of the cell. The type could determine whether or not an agent can traverse or occupy a cell.
     */
    public final Enum_MapCellType cellType;
    public List<I_Location>  neighbors;
    public final I_Coordinate coordinate;

    GraphMapVertex(Enum_MapCellType cellType, I_Coordinate coordinate) {
        this.cellType = cellType;
        this.coordinate = coordinate;
        this.neighbors = null;
    }

    /**
     * Sets the cell's neighbors. All cells in the array should logically be non null.
     * Used during graph construction. Only the first call to this method on an instance affects the instance.
     * @param neighbors the cell's neighbors.
     * @throws NullPointerException if an element is null or the given array is null.
     */
    void setNeighbors(GraphMapVertex[] neighbors) {
        this.neighbors = (this.neighbors == null ? List.of(neighbors) : this.neighbors);
    }

    /**
     * Returns the type of the cell.
     * @return the type of the cell.
     */
    @Override
    public Enum_MapCellType getType() {
        return cellType;
    }

    /**
     * returns the cell's coordinate.
     * @return the cell's coordinate.
     */
    @Override
    public I_Coordinate getCoordinate() {
        return coordinate;
    }

    /**
     * Returns an UnmodifiableList of this cell's neighbors.
     * The amount of neighbors varies by map and connectivity.
     * Runs in O(1).
     * @return an UnmodifiableList of this cell's neighbors.
     */
    @Override
    public List<I_Location> getNeighbors() {
        return this.neighbors;
    }

    /**
     * Returns true iff other is contained in {@link #neighbors}. In particular, returns false if other==this.
     * @param other another {@link I_Location}.
     * @return true iff other is contained in {@link #neighbors}.
     */
    @Override
    public boolean isNeighbor(I_Location other) {
        boolean result = false;
        for (I_Location neighbor :
                neighbors) {
            result = result || (neighbor.equals(other));
        }
        return result;
    }

    @Override
    public String toString() {
        return "GraphMapCell{" +
                "coordinate=" + coordinate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GraphMapVertex)) return false;

        GraphMapVertex that = (GraphMapVertex) o;

        return UniqueID == that.UniqueID;

    }

    @Override
    public int hashCode() {
        return UniqueID;
    }
}
