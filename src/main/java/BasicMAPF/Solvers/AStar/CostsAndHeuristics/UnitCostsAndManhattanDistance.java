package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

public class UnitCostsAndManhattanDistance implements AStarGAndH {
    private final I_Coordinate target;

    public UnitCostsAndManhattanDistance(I_Coordinate target) {
        this.target = target;
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        return state.move.currLocation.getCoordinate().distance(target);
    }
    @Override
    public boolean isConsistent() {
        return true;
    }
}