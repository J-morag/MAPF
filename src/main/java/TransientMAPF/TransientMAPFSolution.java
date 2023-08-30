package TransientMAPF;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;

import java.util.Map;

/**
 * A solution for Transient MAPF, where the agents are only required to pass through their targets, and not to stay
 * there forever, like in <a href="https://www.sciencedirect.com/science/article/pii/S0004370222000923">PIBT</a>
 */
public class TransientMAPFSolution extends Solution {
    public TransientMAPFSolution(Map<Agent, SingleAgentPlan> agentPlans) {
        super(agentPlans);
    }

    public TransientMAPFSolution(Iterable<? extends SingleAgentPlan> plans) {
        super(plans);
    }

    public TransientMAPFSolution() {
    }

    @Override
    protected boolean isAchievesTarget(MAPF_Instance instance, SingleAgentPlan plan) {
        return plan.containsTarget();
    }

}
