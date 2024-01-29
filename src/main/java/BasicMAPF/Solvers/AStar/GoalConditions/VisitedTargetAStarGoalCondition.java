package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public class VisitedTargetAStarGoalCondition implements I_AStarGoalCondition {
    @Override
    public boolean isAGoal(@NotNull SingleAgentAStar_Solver.AStarState state) {
        return state.visitedTarget;
    }
}