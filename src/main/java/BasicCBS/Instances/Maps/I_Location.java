package BasicCBS.Instances.Maps;

import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;

import java.util.List;

public interface I_Location {

    /**
     * Returns the type of the cell.
     * @return the type of the cell.
     */
    Enum_MapCellType getType();

    /**
     * Returns an array that contains references to this cell's neighbors. Should not include this.
     * The amount of neighbors varies by map and connectivity.
     * @return an array that contains references to this cell's neighbors. Should not include this.
     */
    List<I_Location> getNeighbors();

    /**
     * returns the cell's coordinate.
     * @return the cell's coordinate.
     */
    I_Coordinate getCoordinate();

    /**
     * Return true iff other is a neighbor of this.
     * @param other another {@link I_Location}.
     * @return true iff other is a neighbor of this.
     */
    boolean isNeighbor(I_Location other);

}
