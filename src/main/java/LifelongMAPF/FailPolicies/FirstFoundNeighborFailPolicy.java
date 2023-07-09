package LifelongMAPF.FailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FirstFoundNeighborFailPolicy implements I_SingleAgentFailPolicy {

    public FirstFoundNeighborFailPolicy() {
        // TODO write warning to log. Meant as an example of an unsafe policy.
    }

    @Override
    public @NotNull SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, I_ConflictAvoidanceTable softConstraints) {
        return new SingleAgentPlan(a, List.of(new Move(a, farthestCommittedTime + 1, agentLocation, agentLocation.outgoingEdges().get(0))));
    }
}
