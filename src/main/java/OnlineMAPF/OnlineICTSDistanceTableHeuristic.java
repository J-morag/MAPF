package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Solvers.ICTS.LowLevel.DistanceTableAStarHeuristicICTS;
import BasicCBS.Solvers.ICTS.LowLevel.Node;

import java.util.List;
import java.util.Map;

public class OnlineICTSDistanceTableHeuristic extends DistanceTableAStarHeuristicICTS {

    public OnlineICTSDistanceTableHeuristic(List<? extends Agent> agents, I_Map map) {
        super(agents, map);
    }

    @Override
    public void setH(Node node) {
        Map<I_Location, Integer> relevantDictionary = getDistanceDictionaries().get(node.getAgent());
        int h = node.getLocation() instanceof PrivateGarage ? 1 + relevantDictionary.get(node.getLocation().getNeighbors().get(0)) :
                relevantDictionary.get(node.getLocation());
        node.setH(h);
    }

    @Override
    public float getHForAgentAndCurrentLocation(Agent agent, I_Location currLocation) {
        return currLocation instanceof PrivateGarage ? 1 + super.getHForAgentAndCurrentLocation(agent, currLocation.getNeighbors().get(0)):
                super.getHForAgentAndCurrentLocation(agent, currLocation);
    }
}
