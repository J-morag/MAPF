package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.HashSet;
import java.util.Set;

/**
 * Simply selects all agents, always.
 */
public class AllAgentsSubsetSelector implements I_LifelongAgentSelector {

    @Override
    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime) {
        if (latestSolution == null){
            return new HashSet<>(lifelongInstance.agents);
        }
        Set<Agent> res = new HashSet<>();
        for (SingleAgentPlan plan : latestSolution){
            res.add(plan.agent);
        }
        return res;
    }
}
