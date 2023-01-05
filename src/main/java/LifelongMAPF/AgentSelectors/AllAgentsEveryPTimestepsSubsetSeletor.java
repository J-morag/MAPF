package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Predicate;

/**
 * A STATEFUL agent selector that selects all agents once (and only once) per every predetermined period length.
 */
public class AllAgentsEveryPTimestepsSubsetSeletor implements I_LifelongAgentSelector{

    private final AllAgentsSubsetSelector allAgentsSubsetSelector = new AllAgentsSubsetSelector();
    public final int replanningPeriod;
    private int latestPlanningTime = 0;

    public AllAgentsEveryPTimestepsSubsetSeletor(int replanningPeriod) {
        this.replanningPeriod = replanningPeriod;
    }

    @Override
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        if (currentSolutionStartingFromCurrentTime.getStartTime() == 0 ||
                currentSolutionStartingFromCurrentTime.getStartTime() - latestPlanningTime >= replanningPeriod){
            latestPlanningTime = currentSolutionStartingFromCurrentTime.getStartTime();
            return allAgentsSubsetSelector.getAgentSelectionPredicate(lifelongInstance, currentSolutionStartingFromCurrentTime, lifelongAgentsToTimelyOfflineAgents, agentsWaitingToStart, agentDestinationQueues, agentsActiveDestination);
        }
        else {
            return new AgentSelectionPredicate(new HashSet<>());
        }
    }
}
