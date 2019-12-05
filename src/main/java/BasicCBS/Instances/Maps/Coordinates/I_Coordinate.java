package BasicCBS.Instances.Maps.Coordinates;

/**
 * Represents a unique location in Euclidean space.
 */
public interface I_Coordinate {

    /**
     * A distance measure from one coordinate to another.
     * e.g. euclidean distance or cosine similarity in euclidean space, manhattanDistance in grids...
     * This distance may be used as a heuristic, so it is best to consider its most likely use-case, and make sure that
     * it will function as an admissible and consistent heuristic function.
     * If the concept of distance is inapplicable to the domain of this coordinate, implement this method by returning 0;
     * @param other
     * @return
     */
    float distance(I_Coordinate other);

}
