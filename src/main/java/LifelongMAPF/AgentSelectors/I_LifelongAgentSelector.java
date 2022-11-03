package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Select a subset of agents for which to plan at some point in time.
 * Selection should be made in a way that avoids creating impossible instances or invalid solutions.
 */
public interface I_LifelongAgentSelector {

    /**
     * @param lifelongInstance                    the lifelong instance
     * @param latestSolution                      the current solution being followed
     * @param farthestCommittedTime               will select agents that should be planned starting after this time.
     * @param lifelongAgentsToTimelyOfflineAgents Map each lifelong agent to a suitable offline representation at time
     * @param agentsWaitingToStart
     * @param agentDestinationQueues
     * @return a subset of agents for which to plan at some point in time.
     */
    Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues);

    /**
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @return agents with no path that need a path to their destination.
     */
    default Set<LifelongAgent> waitingForPathAgents(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues){
        Set<LifelongAgent> agents = agentsAtCurrentTarget(lifelongInstance, latestSolution, farthestCommittedTime);
        agents.removeIf(agent -> ActiveButPlanEndedTrigger.isPlanEndsAtAgentFinalDestination(agentDestinationQueues, agent, latestSolution.getPlanFor(agent)));
        return agents;
    }

    /**
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @return the agents that are idle - have reached last destination and are idle there.
     */
    default Set<LifelongAgent> idleFinishedAgents(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues){
        Set<LifelongAgent> agents = agentsAtCurrentTarget(lifelongInstance, latestSolution, farthestCommittedTime);
        agents.removeIf(agent -> ! ActiveButPlanEndedTrigger.isPlanEndsAtAgentFinalDestination(agentDestinationQueues, agent, latestSolution.getPlanFor(agent)));
        return agents;
    }

    @NotNull
    public static Set<LifelongAgent> agentsAtCurrentTarget(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime) {
        Set<LifelongAgent> res = new HashSet<>();
        for (Agent agent :
                lifelongInstance.agents) {
            LifelongAgent lifelongAgent = (LifelongAgent)agent;
            SingleAgentPlan plan = latestSolution.getPlanFor(agent);
            // get the time when the plan ends or the agent first arrives at its current goal
            int endTime = Integer.MAX_VALUE;
            for (Move move: plan){
                if (move.currLocation.getCoordinate().equals(plan.agent.target)){
                    endTime = move.timeNow;
                    break;
                }
            }
            endTime = Math.min(endTime, plan.getEndTime());
            if (endTime <= farthestCommittedTime) { // "==" would be enough?
                res.add(lifelongAgent);
            }
        }
        return res;
    }


}
