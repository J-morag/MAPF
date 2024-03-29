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
 * A {@link SingleAgentGAndH} that wraps another implementation, adding that both cost and heuristic are zeroed after visiting the target.
 */
public class ServiceTimeGAndH implements SingleAgentGAndH {

    private final SingleAgentGAndH gAndH;

    /**
     * Constructor.
     * @param gAndH delegate to this {@link SingleAgentGAndH}
     */
    public ServiceTimeGAndH(@NotNull SingleAgentGAndH gAndH) {
        this.gAndH = gAndH;
    }


    @Override
    public int getHToTargetFromLocation(I_Coordinate target, I_Location currLocation) {
        return gAndH.getHToTargetFromLocation(target, currLocation);
    }


    @Override
    public boolean isConsistent() {
        return gAndH.isConsistent();
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
