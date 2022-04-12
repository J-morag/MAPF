package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.HashSet;
import java.util.Map;
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
     * @param lifelongAgentsToTimelyOfflineAgents Map each lifelong agent to a suitable offline representation at time
     * @return a subset of agents for which to plan at some point in time.
     */
    Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime, Map<Agent, Agent> lifelongAgentsToTimelyOfflineAgents);

    /**
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @return the agents that must be selected, i.e. agents with no path: need path to next goal, or reached last goal.
     */
    default Set<Agent> mustSelectAgents(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime, Map<Agent, Agent> lifelongAgentsToTimelyOfflineAgents){
        if (latestSolution == null){
            return new HashSet<>(lifelongInstance.agents);
        }
        else{
            Set<Agent> res = new HashSet<>();
            for (SingleAgentPlan plan : latestSolution){
                if (plan.getEndTime() <= farthestCommittedTime || !lifelongAgentsToTimelyOfflineAgents.get(plan.agent).target.equals(plan.agent.target)) {
                    res.add(plan.agent);
                }
            }
            return res;
        }
    }
}
