package LifelongMAPF.Triggers;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.DataTypesAndStructures.Solution;
import LifelongMAPF.LifelongAgent;

import java.util.Map;
import java.util.Queue;

/**
 * Dictates the time when next planning should occur according to some trigger.
 */
public interface I_LifelongPlanningTrigger {

    /**
     * @return the farthest committed time - at that time all locations are committed, and new ones should be planned for the times after it. or -1 if no next planning time (done).
     */
    int getNextPlanningTime(Solution latestSolution, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents);

}
