package LifelongMAPF.Triggers;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;

import java.util.Map;
import java.util.Queue;

public class ActiveButPlanEndedTrigger implements I_LifelongPlanningTrigger {
    @Override
    public int getNextPlanningTime(Solution latestSolution, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents) {
        int minGoalArrivalTime = Integer.MAX_VALUE;
        for (LifelongAgent lifelongAgent:
             lifelongAgentsToTimelyOfflineAgents.keySet()) {
            Agent timelyAgent = lifelongAgentsToTimelyOfflineAgents.get(lifelongAgent);
            SingleAgentPlan plan = latestSolution.getPlanFor(timelyAgent);

            // skip agents who are sitting at their final destination
            if (!isPlanEndsAtAgentFinalDestination(agentDestinationQueues, lifelongAgent, plan)){
                // find the time when the agent gets to its current goal
                // may arrive at goal before the end of the plan (and then move away and return)
                int agentMinGoalArrivalTime = plan.getEndTime();
                for (Move move: plan){
                    if (move.currLocation.getCoordinate().equals(plan.agent.target)){
                        agentMinGoalArrivalTime = move.timeNow;
                        break;
                    }
                }

                minGoalArrivalTime = Math.min(minGoalArrivalTime, agentMinGoalArrivalTime);
            }

        }
        return minGoalArrivalTime == Integer.MAX_VALUE ? -1 : minGoalArrivalTime;
    }

    public static boolean isPlanEndsAtAgentFinalDestination(Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, LifelongAgent lifelongAgent, SingleAgentPlan plan) {
        return agentDestinationQueues.get(plan.agent).isEmpty() &&
                plan.getLastMove().currLocation.getCoordinate()
                        .equals(lifelongAgent.waypoints.get(lifelongAgent.waypoints.size() - 1));
    }
}
