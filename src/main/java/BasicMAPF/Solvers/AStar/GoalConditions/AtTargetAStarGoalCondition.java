package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public record AtTargetAStarGoalCondition(I_Coordinate targetCoor) implements I_AStarGoalCondition {

    @Override
    public boolean isAGoal(@NotNull SingleAgentAStar_Solver.AStarState state) {
        return isAGoal(state.move, state.visitedTarget);
    }

    @Override
    public boolean isAGoal(@NotNull Move move, boolean visitedTarget) {
        return move.currLocation.getCoordinate().equals(this.targetCoor);
    }
}
