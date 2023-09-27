package LifelongMAPF.FailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StayFailPolicy implements I_SingleAgentFailPolicy {

    @Override
    public @NotNull SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, @Nullable I_ConflictAvoidanceTable softConstraints) {
        return getStayOncePlan(farthestCommittedTime, a, agentLocation, softConstraints);
    }

    public static SingleAgentPlan getStayOncePlan (int farthestCommittedTime, Agent a, I_Location agentLocation, @Nullable I_ConflictAvoidanceTable softConstraints) {
        return new SingleAgentPlan(a, List.of(I_SingleAgentFailPolicy.getStayMove(farthestCommittedTime, a, agentLocation)));
    }
}
