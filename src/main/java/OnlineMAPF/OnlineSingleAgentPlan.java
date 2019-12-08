package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.List;

public class OnlineSingleAgentPlan extends SingleAgentPlan {
    public OnlineSingleAgentPlan(Agent agent, List<Move> moves) {
        super(agent, moves);
    }

    public OnlineSingleAgentPlan(SingleAgentPlan planToCopy) {
        super(planToCopy);
    }

    public OnlineSingleAgentPlan(Agent agent) {
        super(agent);
    }

    /**
     * No conflicts at goal with online plans, since agents don't stay at goal.
     * @param other another plan.
     * @param maxTime the maximum time at which both plans have moves.
     * @return {@inheritDoc}
     */
    @Override
    protected A_Conflict firstConflictAtGoal(SingleAgentPlan other, int maxTime) {
        return null;
    }
}
