package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AllStationaryAgentsSubsetSelector implements I_LifelongAgentSelector {
    @Override
    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues) {
        Set<Agent> allAgentsThatWantAPath = new HashSet<>();
        allAgentsThatWantAPath.addAll(I_LifelongAgentSelector.agentsAtCurrentTarget(lifelongInstance, latestSolution,farthestCommittedTime));
        allAgentsThatWantAPath.addAll(agentsWaitingToStart);
        return allAgentsThatWantAPath;
    }
}
