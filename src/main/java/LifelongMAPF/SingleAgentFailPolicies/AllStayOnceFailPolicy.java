package LifelongMAPF.SingleAgentFailPolicies;

import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

public class AllStayOnceFailPolicy extends StayOnceFailPolicy {

    public Solution stopAll(Iterable<? extends SingleAgentPlan> currentPlans) {
        Solution solution = new Solution();
        for (SingleAgentPlan plan : currentPlans) {
            solution.putPlan(super.getFailPolicyPlan(plan.getPlanStartTime(), plan.agent, plan.getFirstMove().prevLocation, null));
        }
        return solution;
    }
}
