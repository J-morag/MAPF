package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

public interface I_AStarGoalCondition{
    boolean isAGoal(SingleAgentAStar_Solver.AStarState state);
}
