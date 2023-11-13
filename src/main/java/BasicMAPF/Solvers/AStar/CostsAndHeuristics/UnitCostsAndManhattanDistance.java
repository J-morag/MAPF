package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

public class UnitCostsAndManhattanDistance implements SingleAgentGAndH {
    private final I_Coordinate target;

    public UnitCostsAndManhattanDistance(I_Coordinate target) {
        this.target = target;
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        return getHToTargetFromLocation(target, state.move.currLocation);
    }

    @Override
    public float getHToTargetFromLocation(I_Coordinate target, I_Location currLocation) {
        return currLocation.getCoordinate().distance(target);
    }

    @Override
    public boolean isConsistent() {
        return true;
    }
}