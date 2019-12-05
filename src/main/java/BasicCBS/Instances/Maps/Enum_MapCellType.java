package BasicCBS.Instances.Maps;

/**
 * Represents the type of a {@link I_Location cell}.
 * The type could determine whether or not an agent can traverse or occupy a cell.
 *
 * The basic implementation has EMPTY to denote passable cells, and WALL to denote impassable cells. Other types my be
 * added to represent more complex domains.
 */
public enum Enum_MapCellType {
    EMPTY,
    TREE,
    WALL
}
