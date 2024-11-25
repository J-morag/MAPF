package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.DataTypesAndStructures.ArrayMap;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.GraphMapVertex;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.apache.commons.collections4.map.LRUMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import BasicMAPF.DataTypesAndStructures.Move;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link SingleAgentGAndH} that uses a pre-calculated dictionary of distances from possible goal locations to every
 * accessible {@link I_Location location} to provide a perfectly tight heuristic.
 */
public class DistanceTableSingleAgentHeuristic implements SingleAgentGAndH {
    /**
     * Dictionary from target location, to its distance from any location on the map.
     * TODO performance - abstract this away and then determine concrete implementation based on coordinate type,
     *  with euclidean coordinate systems working with n-dimensional arrays instead of hash tables.
     */
    private final Map<I_Location, Map<I_Location, Integer>> distanceDictionaries;
    private final I_Map map;
    public final CongestionMap congestionMap;
    private final static int edgeCost = 1;

    private DistanceTableSingleAgentHeuristic(I_Map map, @Nullable CongestionMap congestionMap) {
        this.map = map;
        this.distanceDictionaries = new HashMap<>();
        this.congestionMap = congestionMap;
    }

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map, to any location that is a target of any agent.
     * @param agents agents (targets) we need to include in the distance table.
     * @param map the map this heuristic will work on.
     * @param lruCacheSize the size of the LRU cache used to store the distance dictionaries. If <= 0, no cache is used.
     * @param congestionMap will apply a congestion penalty if this isn't null.
     */
    public DistanceTableSingleAgentHeuristic(@Nullable List<? extends Agent> agents, I_Map map, int lruCacheSize, @Nullable CongestionMap congestionMap) {
        this.map = map;
        this.congestionMap = congestionMap;
        this.distanceDictionaries = getInitDistanceDictionaries(map, lruCacheSize);
        if (agents != null){
            if (lruCacheSize > 0 && lruCacheSize < agents.size())
                throw new IllegalArgumentException("LRU cache size must be at least as large as the number of agents.");
            for (Agent agent : agents) {
                addAgentToHeuristic(agent);
            }
        }
    }

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map, to any location that is a target of any agent.
     * @param agents agents (targets) we need to include in the distance table.
     * @param map the map this heuristic will work on.
     */
    public DistanceTableSingleAgentHeuristic(List<? extends Agent> agents, I_Map map) {
        this(agents, map, -1, null);
    }

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map to anywhere in the map.
     * This makes the heuristic essentially the "All Pairs Shortest Path" heuristic.
     * @param map the map this heuristic will work on.
     * @param lruCacheSize the size of the LRU cache used to store the distance dictionaries. If <= 0, no cache is used.
     * @param congestionMap will apply a congestion penalty if this isn't null.
     */
    public DistanceTableSingleAgentHeuristic(@NotNull I_ExplicitMap map, int lruCacheSize, @Nullable CongestionMap congestionMap) {
        this.map = map;
        this.congestionMap = congestionMap;
        if (lruCacheSize > 0 && lruCacheSize < map.getAllLocations().size())
            throw new IllegalArgumentException("lruCacheSize must be at least as large as the number of locations in the map (" + map.getAllLocations().size() + ")");
        this.distanceDictionaries = getInitDistanceDictionaries(map, lruCacheSize);
        for (I_Location location : map.getAllLocations()) {
            addTargetToHeuristic(location);
        }
    }

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map to anywhere in the map.
     * This makes the heuristic essentially the "All Pairs Shortest Path" heuristic.
     * @param map the map this heuristic will work on.
     */
    public DistanceTableSingleAgentHeuristic(I_ExplicitMap map) {
        this(map, -1, null);
    }

    private Map<I_Location, Map<I_Location, Integer>> getInitDistanceDictionaries(I_Map map, int lruCacheSize) {
        return lruCacheSize > 0 ? new LRUMap<>(lruCacheSize):
                map instanceof I_ExplicitMap explicitMap ? new ArrayMap<>(explicitMap.getNumMapLocations()) : new HashMap<>();
    }

    public Map<I_Location, Map<I_Location, Integer>> getDistanceDictionaries() {
        return distanceDictionaries;
    }

    public void addAgentToHeuristic(Agent agent) {
        I_Location location = map.getMapLocation(agent.target);
        if (location == null){
            throw new IllegalArgumentException(agent.target.getClass().getSimpleName() + " " + agent.target +
                    " doesn't correspond to any " + GraphMapVertex.class.getSimpleName());
        }
        addTargetToHeuristic(location);
    }

    public void addTargetToHeuristic(I_Location target) {
        if (!distanceDictionaries.containsKey(target)){
            //this map will be added to distanceDictionaries for every agent
            Map<I_Location, Integer> mapForAgent = map instanceof I_ExplicitMap explicitMap ?
                    new ArrayMap<>(explicitMap.getNumMapLocations()) : new HashMap<>();
            this.distanceDictionaries.put(target, mapForAgent);

            //distance of a vertex from itself
            this.distanceDictionaries.get(target).put(target, 0);

            // reverse edges, moving back from the target, traversing against the direction of the edges
            List<I_Location> neighbors = target.incomingEdges();
            LinkedList<I_Location> queue = new LinkedList<>(neighbors);

            int distance = 1;
            int count = queue.size();

            while (!(queue.isEmpty())) {
                I_Location i_location = queue.remove(0);

                //if a graphMapLocation didn't get a distance yet
                if (!(this.distanceDictionaries.get(target).containsKey(i_location))) {
                    this.distanceDictionaries.get(target).put(i_location, distance);

                    // traversing edges in reverse back from current vertex
                    List<I_Location> neighborsLocation = i_location.incomingEdges();
                    queue.addAll(neighborsLocation);
                }

                count--;
                if (count == 0) { //full level/round of neighbors has finished
                    distance += getEdgeCostWithCongestion(i_location);
                    count = queue.size(); //start new level with distance plus one
                }
            }
        }
    }

    @Override
    public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
        return getHToTargetFromLocation(state.getMove().agent.target, state.getMove().currLocation);
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        Map<I_Location, Integer> relevantDictionary = this.distanceDictionaries.get(map.getMapLocation(target));
        Integer h = relevantDictionary.get(currLocation);
        if (h != null){
            return h;
        }
        else if (map.isStronglyConnected()){
            throw new IllegalArgumentException("It seems the target is unreachable from the location, even though this is " +
                    "a strongly connected graph. Are target and location valid for this map?");
        }
        else {
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public int cost(Move move) {
        return getEdgeCostWithCongestion(move.currLocation);
    }

    private int getEdgeCostWithCongestion(I_Location to){
        return congestionMap == null ? edgeCost :
                edgeCost * (1 + (int)(congestionMap.congestionAt(to) * congestionMap.congestionMultiplier));
    }

    @Override
    public boolean isConsistent() {
        return true;
    }
}
