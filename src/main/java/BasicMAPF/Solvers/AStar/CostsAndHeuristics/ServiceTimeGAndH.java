package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

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
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        return gAndH.getHToTargetFromLocation(target, currLocation);
    }


    @Override
    public boolean isConsistent() {
        return gAndH.isConsistent();
    }

    @Override
    public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
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

    public SingleAgentGAndH getWrappedHeuristic() {
        return this.gAndH;
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}
