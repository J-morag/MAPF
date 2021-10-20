package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Solvers.AStar.DistanceTableAStarHeuristic;

import java.util.List;

public class DistanceTableAStarHeuristicICTS extends DistanceTableAStarHeuristic {
    public DistanceTableAStarHeuristicICTS(List<? extends Agent> agents, I_Map map) {
        super(agents, map);
    }

    public void setH(MDDSearchNode node) {
        node.setH(getHToTargetFromLocation(node.getAgent().target, node.getLocation()));
    }

}
