package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public interface I_AStarGoalCondition{
    boolean isAGoal(@NotNull SingleAgentAStar_Solver.AStarState state);
}
