package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Solvers.AStar.DistanceTableAStarHeuristic;

import java.util.List;
import java.util.Map;

public class DistanceTableAStarHeuristicICTS extends DistanceTableAStarHeuristic {
    public DistanceTableAStarHeuristicICTS(List<? extends Agent> agents, I_Map map) {
        super(agents, map);
    }

    public void setH(MDDSearchNode node) {
        Map<I_Location, Integer> relevantDictionary = getDistanceDictionaries().get(node.getAgent());
        node.setH(relevantDictionary.get(node.getLocation()));
    }
}
