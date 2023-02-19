package LifelongMAPF.SingleAgentFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StayOnceFailPolicy implements I_SingleAgentFailPolicy {

    @Override
    public @NotNull SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, I_ConflictAvoidanceTable softConstraints) {
        return new SingleAgentPlan(a, List.of(I_SingleAgentFailPolicy.getStayMove(farthestCommittedTime, a, agentLocation)));
    }
}
