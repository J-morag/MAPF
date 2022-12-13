package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class AllStationaryAgentsSubsetSelector implements I_LifelongAgentSelector {

    int maxGroupSize;

    public AllStationaryAgentsSubsetSelector(Integer maxGroupSize) {
        this.maxGroupSize = Objects.requireNonNullElse(maxGroupSize, Integer.MAX_VALUE);
    }

    public AllStationaryAgentsSubsetSelector(){
        this(null);
    }

    @Override
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime,
                                                       Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart,
                                                       Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        Set<Agent> allStationaryAgents = getAllStationaryAgents(lifelongInstance, currentSolutionStartingFromCurrentTime, agentsWaitingToStart, maxGroupSize);
        return new AgentSelectionPredicate(allStationaryAgents);
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
