package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.DataTypesAndStructures.Solution;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.WaypointGenerators.WaypointsGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    protected Set<Agent> selectAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<LifelongAgent, WaypointsGenerator> agentsWaypointsGenerators, Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents) {
        return new HashSet<>(lifelongInstance.agents);
    }
}
