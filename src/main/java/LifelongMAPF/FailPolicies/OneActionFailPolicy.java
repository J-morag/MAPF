package LifelongMAPF.FailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OneActionFailPolicy implements I_SingleAgentFailPolicy {

    private final boolean onlyMoveIfNoConflicts;

    public OneActionFailPolicy(boolean onlyMoveIfNoConflicts) {
        this.onlyMoveIfNoConflicts = onlyMoveIfNoConflicts;
    }

    @Override
    public @NotNull SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, @Nullable I_ConflictAvoidanceTable softConstraints) {
        Move stayMove = I_SingleAgentFailPolicy.getStayMove(farthestCommittedTime, a, agentLocation);
        if (softConstraints == null)
            // TODO log warning?
            return new SingleAgentPlan(a, List.of(stayMove));
        int minConflicts = softConstraints.numConflicts(stayMove, true);
        Move bestMove = stayMove;

        for (I_Location neighbor:
             agentLocation.outgoingEdges()) {
            Move move = new Move(a, farthestCommittedTime + 1, agentLocation, neighbor);
            int numConflicts = softConstraints.numConflicts(move, true);
            // ensures that stay is preferred over move for the same number of conflicts
            if (numConflicts < minConflicts && (!onlyMoveIfNoConflicts || numConflicts == 0)){
                minConflicts = numConflicts;
                bestMove = move;
            }
        }

        return new SingleAgentPlan(a, List.of(bestMove));
    }
}
