package BasicMAPF.Solvers.ICTS.MDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.DistanceTableAStarHeuristic;

import java.util.List;

public class DistanceTableAStarHeuristicICTS extends DistanceTableAStarHeuristic {
    public DistanceTableAStarHeuristicICTS(List<? extends Agent> agents, I_Map map) {
        super(agents, map);
    }

    public void setH(MDDSearchNode node) {
        node.setH(getHToTargetFromLocation(node.getAgent().target, node.getLocation()));
    }

}
