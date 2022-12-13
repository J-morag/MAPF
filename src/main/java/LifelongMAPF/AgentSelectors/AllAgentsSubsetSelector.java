package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Simply selects all agents, always.
 */
public class AllAgentsSubsetSelector implements I_LifelongAgentSelector {
    @Override
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        return new AgentSelectionPredicate(new HashSet<>(lifelongInstance.agents));
    }
}
