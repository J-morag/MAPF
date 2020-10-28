package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Agent;
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
    // nicetohave avoid duplicates (when two agents have the same goal)

    /**
     * Dictionary from target location, to its distance from any location on the map.
     */
    private Map<I_Location, Map<I_Location, Integer>> distanceDictionaries;
    private I_Map map;

    public Map<I_Location, Map<I_Location, Integer>> getDistanceDictionaries() {
        return distanceDictionaries;
    }

    public DistanceTableAStarHeuristic(List<? extends Agent> agents, I_Map map) {
        this.map = map;
        this.distanceDictionaries = new HashMap<>();
        for (Agent agent : agents) {
            addAgentToHeuristic(agent);
        }
    }

    public void addAgentToHeuristic(Agent agent) {
        I_Location target = map.getMapCell(agent.target);
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

                //if a graphMapCell didn't got a distance yet
                if (!(this.distanceDictionaries.get(target).containsKey(i_location))) {
                    this.distanceDictionaries.get(target).put(i_location, distance);

                    //add all the neighbors of the current graphMapCell to  the queue
                    List<I_Location> neighborsCell = i_location.getNeighbors();
                    queue.addAll(neighborsCell);
                }

                count--;
                if (count == 0) { //full level/round of neighbors is finish
                    distance++;
                    count = queue.size(); //start new level with distance plus one
                }
            }
        }
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        return getHForAgentAndCurrentLocation(state.getMove().agent, state.getMove().currLocation);
    }

    // todo work only with locations
    public float getHForAgentAndCurrentLocation(Agent agent, I_Location currLocation){
        Map<I_Location, Integer> relevantDictionary = this.distanceDictionaries.get(map.getMapCell(agent.target));
        return relevantDictionary.get(currLocation);
    }

}
