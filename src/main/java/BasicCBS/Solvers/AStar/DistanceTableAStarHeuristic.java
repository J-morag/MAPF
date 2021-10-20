package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.I_ExplicitMap;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link AStarHeuristic} that uses a pre-calculated dictionary of distances from possible goal locations to every
 * accessible {@link I_Location location} to provide a perfectly tight heuristic.
 */
public class DistanceTableAStarHeuristic implements AStarHeuristic {
    /**
     * Dictionary from target location, to its distance from any location on the map.
     */
    private final Map<I_Location, Map<I_Location, Integer>> distanceDictionaries;
    private final I_Map map;

    public Map<I_Location, Map<I_Location, Integer>> getDistanceDictionaries() {
        return distanceDictionaries;
    }

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map, to any location that is a target of any agent.
     * @param agents agents (targets) we need to include in the distance table.
     * @param map the map this heuristic will work on.
     */
    public DistanceTableAStarHeuristic(List<? extends Agent> agents, I_Map map) {
        this.map = map;
        this.distanceDictionaries = new HashMap<>();
        for (Agent agent : agents) {
            addAgentToHeuristic(agent);
        }
    }

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map to anywhere in the map.
     * This makes the heuristic essentially the "All Pairs Shortest Path" heuristic.
     * @param map the map this heuristic will work on.
     */
    public DistanceTableAStarHeuristic(I_ExplicitMap map) {
        this.map = map;
        this.distanceDictionaries = new HashMap<>();
        for (I_Location location : map.getAllLocations()) {
            addTargetToHeuristic(location);
        }
    }

    public void addAgentToHeuristic(Agent agent) {
        addTargetToHeuristic(map.getMapCell(agent.target));
    }

    public void addTargetToHeuristic(I_Location target) {
        if (!distanceDictionaries.containsKey(target)){
            //this map will be added to distanceDictionaries for every agent
            Map<I_Location, Integer> mapForAgent = new HashMap<>();
            this.distanceDictionaries.put(target, mapForAgent);

            //distance of a graphMapCell from itself
            this.distanceDictionaries.get(target).put(target, 0);

            //all the neighbors of a graphMapCell
            List<I_Location> neighbors = target.getNeighbors();
            LinkedList<I_Location> queue = new LinkedList<>(neighbors);

            int distance = 1;
            int count = queue.size();

            while (!(queue.isEmpty())) {
                I_Location i_location = queue.remove(0);

                //if a graphMapCell didn't get a distance yet
                if (!(this.distanceDictionaries.get(target).containsKey(i_location))) {
                    this.distanceDictionaries.get(target).put(i_location, distance);

                    //add all the neighbors of the current graphMapCell to  the queue
                    List<I_Location> neighborsCell = i_location.getNeighbors();
                    queue.addAll(neighborsCell);
                }

                count--;
                if (count == 0) { //full level/round of neighbors has finished
                    distance++;
                    count = queue.size(); //start new level with distance plus one
                }
            }
        }
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        return getHToTargetFromLocation(state.getMove().agent.target, state.getMove().currLocation);
    }

    public Integer getHToTargetFromLocation(I_Coordinate target, I_Location currLocation) {
        Map<I_Location, Integer> relevantDictionary = this.distanceDictionaries.get(map.getMapCell(target));
        return relevantDictionary.get(currLocation);
    }

}
