package BasicCBS.Instances.Maps;

/**
 * Represents the type of {@link I_Location location}.
 * The type could determine whether an agent can traverse or occupy a location.
 *
 * The basic implementation has EMPTY to denote traversable location, and WALL to denote impassable locations. Other
 * types may be added to represent more complex domains.
 */
public enum Enum_MapLocationType {
    /**
     * Standard empty, traversable, location.
     */
    EMPTY,
    /**
     * Traversable, but agents can't wait/stay/stop at the location.
     */
    NO_STOP,
    /**
     * Traversable by some agents but impassable for other (undefined).
     */
    TREE,
    /**
     * Impassable location.
     */
    WALL
}
