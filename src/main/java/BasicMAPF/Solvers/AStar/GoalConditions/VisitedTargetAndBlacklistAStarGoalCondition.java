package BasicMAPF.Solvers.AStar.GoalConditions;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class VisitedTargetAndBlacklistAStarGoalCondition extends VisitedTargetAStarGoalCondition {
    private final Set<I_Coordinate> blacklist;

    public VisitedTargetAndBlacklistAStarGoalCondition(Set<I_Coordinate> blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public boolean isAGoal(SingleAgentAStar_Solver.@NotNull AStarState state) {
        return super.isAGoal(state) && !blacklist.contains(state.move.currLocation.getCoordinate());
    }
}
