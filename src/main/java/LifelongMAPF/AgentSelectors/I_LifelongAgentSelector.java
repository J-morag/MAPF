package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Select a subset of agents for which to plan at some point in time.
 * Selection should be made in a way that avoids creating impossible instances or invalid solutions.
 */
public interface I_LifelongAgentSelector {

    /**
     * @param lifelongInstance                       the lifelong instance
     * @param currentSolutionStartingFromCurrentTime the current solution being followed
     * @param lifelongAgentsToTimelyOfflineAgents    Map each lifelong agent to a suitable offline representation at time
     * @param agentsWaitingToStart
     * @param agentDestinationQueues
     * @param agentsActiveDestination
     * @return a subset of agents for which to plan at some point in time. could be lifelong or offline agents (and new or old), depending on implementation.
     */
    Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime,
                                                Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart,
                                                Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination);

    default boolean timeToPlan(int farthestCommittedTime){
        return true;
    }

    default int getPlanningFrequency(){
        return 1;
    }

    /**
     * @param lifelongInstance the lifelong instance
     * @param currentSolutionStartingFromCurrentTime the current solution being followed
     * @return agents with no path that need a path to their destination.
     */
    default Set<LifelongAgent> waitingForPathToNextGoalAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues){
        Set<LifelongAgent> agents = agentsAtPreviousTarget(lifelongInstance, currentSolutionStartingFromCurrentTime);
        agents.removeIf(agent -> ActiveButPlanEndedTrigger.isPlanEndsAtAgentFinalDestination(agentDestinationQueues, agent, currentSolutionStartingFromCurrentTime.getPlanFor(agent)));
        return agents;
    }

    /**
     * @param lifelongInstance the lifelong instance
     * @param currentSolutionStartingFromCurrentTime the current solution being followed
     * @return the agents that are idle - have reached last destination and are idle there.
     */
    default Set<LifelongAgent> idleFinishedAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues){
        // TODO extend to support agents moving freely after their last destination?
        Set<LifelongAgent> agents = agentsAtPreviousTarget(lifelongInstance, currentSolutionStartingFromCurrentTime);
        agents.removeIf(agent -> ! ActiveButPlanEndedTrigger.isPlanEndsAtAgentFinalDestination(agentDestinationQueues, agent, currentSolutionStartingFromCurrentTime.getPlanFor(agent)));
        return agents;
    }

    @NotNull
    static Set<LifelongAgent> agentsAtPreviousTarget(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime) {
        Set<LifelongAgent> res = new HashSet<>();
        for (Agent lifelongAgentAsAgent :
                lifelongInstance.agents) {
            LifelongAgent lifelongAgentAsLifelongAgent = (LifelongAgent)lifelongAgentAsAgent;
            SingleAgentPlan plan = currentSolutionStartingFromCurrentTime.getPlanFor(lifelongAgentAsLifelongAgent);

            if (plan.getFirstMove().currLocation.getCoordinate().equals(plan.getLastMove().currLocation.getCoordinate())) { // also covers blocked agents
                res.add(lifelongAgentAsLifelongAgent);
            }
        }
        return res;
    }

    class AgentSelectionPredicate implements Predicate<Agent>{
        private final Set<Agent> selectedAgents;

        public AgentSelectionPredicate(@Nullable Set<Agent> selectedAgents) {
            this.selectedAgents = selectedAgents;
        }

        @Override
        public boolean test(Agent agent) {
            return selectedAgents != null && selectedAgents.contains(agent);
        }
    }
}
