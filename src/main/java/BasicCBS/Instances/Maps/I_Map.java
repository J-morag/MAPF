package BasicCBS.Instances.Maps;

import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;

public interface I_Map {

    /**    /**
     * Returns the {@link I_Location map cell} for the given {@link I_Coordinate}.
     * @param i_coordinate the {@link I_Coordinate} of the {@link I_Location map cell}.
     * @return the {@link I_Location map cell} for the given {@link I_Coordinate}.
     */
    I_Location getMapCell(I_Coordinate i_coordinate);

    /**
     * @param i_coordinate the {@link I_Coordinate} to check.
     * @return true if the given coordinate is valid for this map, meaning it is reachable in some way. Note that for
     * a coordinate which could exist given the dimensions of the map, but is not reachable, behaviour is undefined.
     */
    boolean isValidCoordinate(I_Coordinate i_coordinate);

}
