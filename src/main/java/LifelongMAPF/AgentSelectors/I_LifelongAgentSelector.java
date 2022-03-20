package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.HashSet;
import java.util.Set;

/**
 * Select a subset of agents for which to plan at some point in time.
 * Selection should be made in a way that avoids creating impossible instances or invalid solutions.
 */
public interface I_LifelongAgentSelector {

    /**
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @return a subset of agents for which to plan at some point in time.
     */
    Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime);

    /**
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @return the agents that must be selected
     */
    default Set<Agent> mustSelectAgents(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime){
        if (latestSolution == null){
            return new HashSet<>(lifelongInstance.agents);
        }
        else{
            Set<Agent> res = new HashSet<>();
            for (SingleAgentPlan plan : latestSolution){
                if (plan.getEndTime() <= farthestCommittedTime) res.add(plan.agent);
            }
            return res;
        }
    }
}
