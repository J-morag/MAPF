package LifelongMAPF.Triggers;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.Map;
import java.util.Queue;

public class DestinationAchievedTrigger implements I_LifelongPlanningTrigger {
    @Override
    public int getNextFarthestCommittedTime(Solution latestSolution, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues) {
        int minGoalArrivalTime = Integer.MAX_VALUE;
        for (SingleAgentPlan plan : latestSolution){
            if ( ! agentDestinationQueues.get(plan.agent).isEmpty()){
                minGoalArrivalTime = Math.min(minGoalArrivalTime, plan.getEndTime());
            }
        }
        return minGoalArrivalTime == Integer.MAX_VALUE ? -1 : minGoalArrivalTime;
    }
}
