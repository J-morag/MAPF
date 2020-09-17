package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.Set;

public class OnlineConflictManager extends ConflictManager {
    @Override
    protected void checkAddVertexConflictsWithGoal(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan) {
        // do nothing, since agents should now disappear at goal.
    }

    @Override
    protected void checkAddConflictsByTimeLocation(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan) {
        Set<Agent> agentsAtTimeLocation = this.timeLocationTables.getAgentsAtTimeLocation(timeLocation);
        super.addVertexConflicts(timeLocation, singleAgentPlan.agent, agentsAtTimeLocation);
        super.checkAddSwappingConflicts(timeLocation.time, singleAgentPlan);
    }
}
