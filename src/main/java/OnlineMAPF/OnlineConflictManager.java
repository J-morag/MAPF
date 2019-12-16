package OnlineMAPF;

import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicCBS.Solvers.SingleAgentPlan;

public class OnlineConflictManager extends ConflictManager {
    @Override
    protected void checkAddVertexConflictsWithGoal(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan) {
        // do nothing, since agents should now disappear at goal.
    }
}
