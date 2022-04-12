package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Solution;

import java.util.Map;
import java.util.Set;

public class MandatoryAgentsSubsetSelector implements I_LifelongAgentSelector {
    @Override
    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime, Map<Agent, Agent> lifelongAgentsToTimelyOfflineAgents) {
        return mustSelectAgents(lifelongInstance, latestSolution, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents);
    }
}
