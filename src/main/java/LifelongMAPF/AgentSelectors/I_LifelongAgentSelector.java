package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;

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
    Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents);

    /**
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @return the agents that must be selected, i.e. agents with no path: need path to next goal, or reached last goal.
     */
    default Set<Agent> mustSelectAgents(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime){
        if (latestSolution == null){
            return new HashSet<>(lifelongInstance.agents);
        }
        else{
            Set<Agent> res = new HashSet<>();
            for (SingleAgentPlan plan : latestSolution){
                // get the time when the plan ends or the agent first arrives at its current goal
                int endTime = Integer.MAX_VALUE;
                for (Move move: plan){
                    if (move.currLocation.getCoordinate().equals(plan.agent.target)){
                        endTime = move.timeNow;
                        break;
                    }
                }
                endTime = Math.min(endTime, plan.getEndTime());
                if (endTime <= farthestCommittedTime
//                        || !lifelongAgentsToTimelyOfflineAgents.get(plan.agent).target.equals(plan.agent.target) TODO not sure what this did. is it OK to remove?
                ) {
                    res.add(plan.agent);
                }
            }
            return res;
        }
    }
}
