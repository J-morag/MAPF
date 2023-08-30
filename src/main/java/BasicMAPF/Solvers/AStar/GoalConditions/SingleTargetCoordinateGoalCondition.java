package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

public record SingleTargetCoordinateGoalCondition(I_Coordinate targetCoor) implements I_AStarGoalCondition {
    @Override
    public boolean isAGoal(SingleAgentAStar_Solver.AStarState state) {
        return state.move.currLocation.getCoordinate().equals(this.targetCoor);
    }
}
