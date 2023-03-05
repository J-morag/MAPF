package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class StationaryAgentsSubsetSelector implements I_LifelongAgentSelector {

    final int maxGroupSize;
    public final PeriodicSelector periodicSelector;

    public StationaryAgentsSubsetSelector(Integer maxGroupSize, PeriodicSelector periodicSelector) {
        this.maxGroupSize = Objects.requireNonNullElse(maxGroupSize, Integer.MAX_VALUE);
        this.periodicSelector = Objects.requireNonNullElse(periodicSelector, new PeriodicSelector(1));
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
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime,
                                                       Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart,
                                                       Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        if (timeToPlan(currentSolutionStartingFromCurrentTime.getStartTime())) {
            return new AgentSelectionPredicate(getAllStationaryAgents(lifelongInstance, currentSolutionStartingFromCurrentTime, agentsWaitingToStart, maxGroupSize));
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
        // blocked agents or agents at their previous destination or agents at their last destination
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
