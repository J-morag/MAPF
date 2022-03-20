package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Solution;

import java.util.Set;

public class MandatoryAgentsSubsetSelector implements I_LifelongAgentSelector {
    @Override
    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime) {
        return mustSelectAgents(lifelongInstance, latestSolution, farthestCommittedTime);
    }
}
