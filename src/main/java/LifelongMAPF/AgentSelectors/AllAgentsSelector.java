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
 * A STATEFUL agent selector that selects all agents once (and only once) per every predetermined period length.
 */
public class AllAgentsSelector implements I_LifelongAgentSelector{
    public final PeriodicSelector periodicSelector;

    public AllAgentsSelector(PeriodicSelector periodicSelector) {
        this.periodicSelector = Objects.requireNonNullElse(periodicSelector, new PeriodicSelector(1));
    }

    public AllAgentsSelector() {
        this(null);
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

    @Override
    public boolean timeToPlan(int farthestCommittedTime) {
        return periodicSelector.timeMeetsOrExceedsPeriod(farthestCommittedTime);
    }

    @NotNull
    private static AgentSelectionPredicate getAllAgentsPredicate(MAPF_Instance lifelongInstance) {
        return new AgentSelectionPredicate(new HashSet<>(lifelongInstance.agents));
    }
}
