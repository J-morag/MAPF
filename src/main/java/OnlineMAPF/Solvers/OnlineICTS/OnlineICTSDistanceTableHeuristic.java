package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Solvers.ICTS.MDDs.DistanceTableAStarHeuristicICTS;
import OnlineMAPF.PrivateGarage;

import java.util.List;

public class OnlineICTSDistanceTableHeuristic extends DistanceTableAStarHeuristicICTS {

    public OnlineICTSDistanceTableHeuristic(List<? extends Agent> agents, I_Map map) {
        super(agents, map);
    }

    @Override
    public float getHForAgentAndCurrentLocation(Agent agent, I_Location currLocation) {
        return currLocation instanceof PrivateGarage ? 1 + super.getHForAgentAndCurrentLocation(agent, currLocation.getNeighbors().get(0)):
                super.getHForAgentAndCurrentLocation(agent, currLocation);
    }
}
