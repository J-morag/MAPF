package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

/**
 * This class is used to set the h value manually.
 */
public class ManualSingleAgentGAndH implements SingleAgentGAndH {
    public final float hValue;

    public ManualSingleAgentGAndH(float hValue) {
        this.hValue = hValue;
    }

    @Override
    public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
        return hValue;
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        return (int)hValue;
    }

    @Override
    public boolean isConsistent() {
        // depends on how it is set manually from outside
        return true;
    }
}
