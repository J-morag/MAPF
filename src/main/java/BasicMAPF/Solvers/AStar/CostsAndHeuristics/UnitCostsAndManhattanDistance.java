package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public class UnitCostsAndManhattanDistance implements SingleAgentGAndH {
    private final I_Coordinate target;

    public UnitCostsAndManhattanDistance(@NotNull I_Coordinate target) {
        this.target = target;
    }

    @Override
    public float getH(@NotNull SingleAgentAStar_Solver.AStarState state) {
        return getHToTargetFromLocation(target, state.move.currLocation);
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        return (int) currLocation.getCoordinate().distance(target);
    }

    @Override
    public boolean isConsistent() {
        return true;
    }
}