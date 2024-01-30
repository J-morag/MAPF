package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.DataTypesAndStructures.Move;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link SingleAgentGAndH} that uses a pre-calculated dictionary of distances from possible goal locations to every
 * accessible {@link I_Location location} to provide a perfectly tight heuristic.
 */
public class ServiceTimeGAndH implements SingleAgentGAndH {

    private final DistanceTableSingleAgentHeuristic heuristic;

    /**
     * Constructor. Create a dictionary of real distances from anywhere in the map, to any location that is a target of any agent.
     * @param agents agents (targets) we need to include in the distance table.
     * @param map the map this heuristic will work on.
     */
    public ServiceTimeGAndH(@Nullable List<? extends Agent> agents, I_Map map, @Nullable SingleAgentGAndH heuristic) {
        this.heuristic = new DistanceTableSingleAgentHeuristic(agents, map);
    }


    @Override
    public float getHToTargetFromLocation(I_Coordinate target, I_Location currLocation) {
        return heuristic.getHToTargetFromLocation(target, currLocation);
    }


    @Override
    public boolean isConsistent() {
        return heuristic.isConsistent();
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        if (state.getPrev() != null && state.getPrev().visitedTarget) {
            return 0;
        }
        return getHToTargetFromLocation(state.getMove().agent.target, state.getMove().currLocation);
    }

    /**
     * @inheritDoc
     */
    public int cost(Move move, boolean isAfterTargetExcludingFirstMoveToTarget){
        if (isAfterTargetExcludingFirstMoveToTarget) {
            return 0;
        }
        return cost(move);
    }
}
