package LifelongMAPF.SingleAgentFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;

public interface I_SingleAgentFailPolicy {
    @NotNull
    SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, I_ConflictAvoidanceTable softConstraints);

    @NotNull
    static Move getStayMove(int farthestCommittedTime, Agent a, I_Location agentLocation) {
        return new Move(a, farthestCommittedTime + 1, agentLocation, agentLocation);
    }

}