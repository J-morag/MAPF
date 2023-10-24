package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

public record VisitedAGoalAtSomePointInPlanGoalCondition(I_AStarGoalCondition goalCondition) implements I_AStarGoalCondition {
    @Override
    public boolean isAGoal(SingleAgentAStar_Solver.AStarState state) {
        return state.hasVisitedTargetLocationAncestor;
    }
}