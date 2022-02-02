package BasicMAPF.Instances.Maps;

import java.util.Collection;

/**
 * Interface for an explicit map. Meaning the map exists explicitly in memory, and can be iterated over.
 * This is in contrast for example, to an infinite map, where iteration should not be attempted.
 *
 * Space complexity:
 * This requires the entire graph to be built at initialization. For very large, sparse maps, this may pose a space
 * complexity challenge. Example: A 1000x1000x1000 map with just one agent, whose source and target are adjacent.
 */
public interface I_ExplicitMap extends I_Map{
    Collection<? extends I_Location> getAllLocations();
}
