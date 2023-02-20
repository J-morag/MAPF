package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.apache.commons.collections4.map.LRUMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A dynamic distance table, that will compute ad-hoc the distance table for any target on any map, and cache
 * results for subsequent uses. Must be set to the correct map using {@link #setCurrentMap(I_Map)} before querying
 * and when switching to another map!
 * IMPORTANT: Using this make run times unstable. The first time a new target on a new map is requested
 * is significantly more expensive than subsequent calls. Use with caution!
 */
public class CachingDistanceTableHeuristic implements AStarGAndH {
    private final static int DEFAULT_MAP_CACHE_SIZE = 1;
    private final static int DEFAULT_TARGET_CACHE_SIZE = 1000;
    private final int targetCacheSizePerMap;
    /**
     * Dictionary from {@link I_Map map} to a {@link DistanceTableAStarHeuristic distance table heuristic} for it.
     */
    private final Map<I_Map, DistanceTableAStarHeuristic> distanceTables;
    private I_Map currentMap;

    public CachingDistanceTableHeuristic() {
        this(DEFAULT_MAP_CACHE_SIZE, DEFAULT_TARGET_CACHE_SIZE);
    }

    public CachingDistanceTableHeuristic(int mapCacheSize, int targetCacheSizePerMap) {
        if (mapCacheSize < 1)
            throw new IllegalArgumentException("Map cache size must be at least 1.");
        this.distanceTables = new LRUMap<>(mapCacheSize);
        this.targetCacheSizePerMap = targetCacheSizePerMap;
    }

    public void setCurrentMap(I_Map map){
        this.currentMap = map;
        if (!this.distanceTables.containsKey(map)) {
            this.distanceTables.put(map, getNewDistanceTable(map));
        }
    }

    @NotNull
    private DistanceTableAStarHeuristic getNewDistanceTable(I_Map map) {
        return new DistanceTableAStarHeuristic(null, map, targetCacheSizePerMap, null);
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        DistanceTableAStarHeuristic dt = this.distanceTables.get(this.currentMap);
        if (dt == null){
            throw new IllegalStateException("Current map not set. Call setCurrentMap(I_Map) before querying.");
        }
        I_Location target = this.currentMap.getMapLocation(state.getMove().agent.target);
        if (dt.getDistanceDictionaries().containsKey(target)){
            return dt.getH(state);
        }
        else { // should only happen when encountering a new agent (target)
            dt.addTargetToHeuristic(target);
            return this.distanceTables.get(this.currentMap).getH(state);
        }
    }

    @Override
    public boolean isConsistent() {
        return true;
    }
}
