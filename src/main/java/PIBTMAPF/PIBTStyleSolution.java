package PIBTMAPF;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;

import java.util.Map;

public class PIBTStyleSolution extends Solution {
    public PIBTStyleSolution(Map<Agent, SingleAgentPlan> agentPlans) {
        super(agentPlans);
    }

    public PIBTStyleSolution(Iterable<? extends SingleAgentPlan> plans) {
        super(plans);
    }

    public PIBTStyleSolution() {
    }

    @Override
    protected boolean isAchievesTarget(MAPF_Instance instance, SingleAgentPlan plan) {
        return plan.containsTarget();
    }

}
