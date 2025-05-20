package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public class VisitedTargetAStarGoalCondition implements I_AStarGoalCondition {
    @Override
    public boolean isAGoal(@NotNull SingleAgentAStar_Solver.AStarState state) {
        return isAGoal(state.move, state.visitedTarget);
    }

    @Override
    public boolean isAGoal(@NotNull Move move, boolean visitedTarget) {
        return visitedTarget;
    }
}