package BasicMAPF.MDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;

import java.util.List;

public class DistanceTableSingleAgentHeuristicMDD extends DistanceTableSingleAgentHeuristic {
    public DistanceTableSingleAgentHeuristicMDD(List<? extends Agent> agents, I_Map map) {
        super(agents, map);
    }

    public void setH(MDDSearchNode node) {
        node.setH(getHToTargetFromLocation(node.getAgent().target, node.getLocation()));
    }

}
