package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_ExplicitMap;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;

import java.util.*;

/**
 * A dynamic distance table, that will compute ad-hoc the distance table for any target on any map, and cache
 * results for subsequent uses. Must be set to the correct map using {@link #setCurrentMap(I_ExplicitMap)} before querying
 * and when switching to another map!
 * IMPORTANT: Using this make run times unstable. The first time a new target on a new map is requested
 * is significantly more expensive than subsequent calls. Use with caution!
 */
public class CachingDistanceTableHeuristic implements AStarHeuristic {
    /**
     * Dictionary from {@link I_Map map} to a {@link DistanceTableAStarHeuristic distance table heuristic} for it.
     */
    private final Map<I_ExplicitMap, DistanceTableAStarHeuristic> distanceTables = new HashMap<>();
    private I_ExplicitMap currentMap;

    public CachingDistanceTableHeuristic() {
    }

    public void setCurrentMap(I_ExplicitMap map){
        this.currentMap = map;
        if (!this.distanceTables.containsKey(map)) {
            this.distanceTables.put(map, new DistanceTableAStarHeuristic(new ArrayList<>(0), map));
        }
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        DistanceTableAStarHeuristic dt = this.distanceTables.get(this.currentMap);
        I_Location target = this.currentMap.getMapCell(state.getMove().agent.target);
        if (dt.getDistanceDictionaries().containsKey(target)){
            return dt.getH(state);
        }
        else { // should only happen when encountering a new agent (target)
            dt.addTargetToHeuristic(target);
            return this.distanceTables.get(this.currentMap).getH(state);
        }
    }

}
