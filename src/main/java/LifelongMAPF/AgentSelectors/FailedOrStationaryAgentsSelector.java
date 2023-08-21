package LifelongMAPF.AgentSelectors;

import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class FailedOrStationaryAgentsSelector extends StationaryAgentsSubsetSelector {
    // todo - add commit horizon... need to know when the original plan started

    public FailedOrStationaryAgentsSelector(Integer maxGroupSize, PeriodicSelector periodicSelector) {
        super(maxGroupSize, periodicSelector);
    }

    public FailedOrStationaryAgentsSelector(Integer maxGroupSize) {
        super(maxGroupSize);
    }

    public FailedOrStationaryAgentsSelector(PeriodicSelector periodicSelector) {
        super(periodicSelector);
    }

    public FailedOrStationaryAgentsSelector() {
    }

    @Override
    protected Set<Agent> selectAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents) {
        Set<Agent> selectedAgents = super.selectAgents(lifelongInstance, currentSolutionStartingFromCurrentTime, lifelongAgentsToTimelyOfflineAgents, agentsWaitingToStart, agentDestinationQueues, agentsActiveDestination, failedAgents);
        selectedAgents.addAll(failedAgents);
        return selectedAgents;
    }

}
