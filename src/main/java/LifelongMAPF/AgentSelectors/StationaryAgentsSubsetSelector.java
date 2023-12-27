package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.DataTypesAndStructures.Solution;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.WaypointGenerators.WaypointsGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StationaryAgentsSubsetSelector extends A_LifelongAgentSelector {

    final int maxGroupSize;

    public StationaryAgentsSubsetSelector(Integer maxGroupSize, PeriodicSelector periodicSelector) {
        super(periodicSelector);
        this.maxGroupSize = Objects.requireNonNullElse(maxGroupSize, Integer.MAX_VALUE);
    }

    public StationaryAgentsSubsetSelector(Integer maxGroupSize) {
        this(maxGroupSize, null);
    }

    public StationaryAgentsSubsetSelector(PeriodicSelector periodicSelector) {
        this(null, periodicSelector);
    }

    public StationaryAgentsSubsetSelector(){
        this(null, null);
    }

    @Override
    protected Set<Agent> selectAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<LifelongAgent, WaypointsGenerator> agentsWaypointsGenerators, Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents) {
        return getAllStationaryAgents(lifelongInstance, currentSolutionStartingFromCurrentTime, agentsWaitingToStart, maxGroupSize);
    }

    @NotNull
    public static Set<Agent> getAllStationaryAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime,
                                                    List<LifelongAgent> agentsWaitingToStart, int maxAgents) {
        Set<Agent> allAgentsThatWantAPath = new HashSet<>();
        for (Agent agent :
                agentsWaitingToStart) {
            if (allAgentsThatWantAPath.size() == maxAgents){
                return allAgentsThatWantAPath;
            }
            allAgentsThatWantAPath.add(agent);
        }
        // blocked agents or agents still at their previous target (could have been from fail policy)
        for (Agent agent :
                I_LifelongAgentSelector.agentsAtPreviousTarget(lifelongInstance, currentSolutionStartingFromCurrentTime)) {
            if (allAgentsThatWantAPath.size() == maxAgents){
                return allAgentsThatWantAPath;
            }
            allAgentsThatWantAPath.add(agent);
        }

        return allAgentsThatWantAPath;
    }
}
