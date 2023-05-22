package LifelongMAPF.FailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class wasteTimeFailPolicy implements I_SingleAgentFailPolicy {
    @Override
    public @NotNull SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, @Nullable I_ConflictAvoidanceTable softConstraints) {
        throw new NotImplementedException();
    }
}
