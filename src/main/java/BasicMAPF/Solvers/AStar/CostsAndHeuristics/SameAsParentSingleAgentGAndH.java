package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

/**
 * This class just returns the same h value as the parent state.
 * Does not calculate h.
 * Used to avoid recalculating h when it is guaranteed to be the same.
 */
public class SameAsParentSingleAgentGAndH implements SingleAgentGAndH {
    public static final SameAsParentSingleAgentGAndH INSTANCE = new SameAsParentSingleAgentGAndH();

    @Override
    public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
        return state.h;
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        throw new UnsupportedOperationException("Requires a parent state to copy h from.");
    }

    @Override
    public boolean isConsistent() {
        // actually depends on what heuristic is used to calculate h for the parent
        return true;
    }
}
