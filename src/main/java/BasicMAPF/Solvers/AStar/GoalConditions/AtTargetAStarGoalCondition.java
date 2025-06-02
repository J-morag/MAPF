package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public record AtTargetAStarGoalCondition(I_Coordinate targetCoor) implements I_AStarGoalCondition {

    @Override
    public boolean isAGoal(@NotNull SingleAgentAStar_Solver.AStarState state) {
        return state.move.currLocation.getCoordinate().equals(this.targetCoor);
    }
}
