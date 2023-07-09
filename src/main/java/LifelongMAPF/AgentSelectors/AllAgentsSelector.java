package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.DataTypesAndStructures.Solution;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * A STATEFUL agent selector that selects all agents once (and only once) per every predetermined period length.
 */
public class AllAgentsSelector extends A_LifelongAgentSelector{

    public AllAgentsSelector(PeriodicSelector periodicSelector) {
        super(periodicSelector);
    }

    public AllAgentsSelector() {
    }

    @Override
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        boolean isPlanningTime = timeToPlan(currentSolutionStartingFromCurrentTime.getStartTime());
        if (isPlanningTime){
            return getAllAgentsPredicate(lifelongInstance);
        }
        else {
            return new AgentSelectionPredicate(null);
        }
    }

    @NotNull
    private static AgentSelectionPredicate getAllAgentsPredicate(MAPF_Instance lifelongInstance) {
        return new AgentSelectionPredicate(new HashSet<>(lifelongInstance.agents));
    }
}
