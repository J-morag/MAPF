package LifelongMAPF.AgentSelectors;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.WaypointGenerators.WaypointsGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NoTargetInPlanAgentsSelector extends A_LifelongAgentSelector {

    final int maxGroupSize;

    public NoTargetInPlanAgentsSelector(Integer maxGroupSize, PeriodicSelector periodicSelector) {
        super(periodicSelector);
        this.maxGroupSize = Objects.requireNonNullElse(maxGroupSize, Integer.MAX_VALUE);
    }

    public NoTargetInPlanAgentsSelector(PeriodicSelector periodicSelector) {
        this(null, periodicSelector);
    }

    @Override
    protected Set<Agent> selectAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<LifelongAgent, WaypointsGenerator> agentsWaypointsGenerators, Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents) {
        return agentsThatDontHaveTheirTargetInTheirPlans(lifelongInstance, currentSolutionStartingFromCurrentTime, agentsWaitingToStart, maxGroupSize, agentsActiveDestination);
    }

    @NotNull
    public static Set<Agent> agentsThatDontHaveTheirTargetInTheirPlans(MAPF_Instance lifelongInstance,
                                                                       @NotNull Solution currentSolutionStartingFromCurrentTime,
                                                                       List<LifelongAgent> agentsWaitingToStart, int maxAgents,
                                                                       Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        Set<Agent> selectedAgents = new HashSet<>();
        for (Agent agent :
                agentsWaitingToStart) {
            if (selectedAgents.size() == maxAgents){
                return selectedAgents;
            }
            selectedAgents.add(agent);
        }
        // agents whose target is not in their plan
        for (SingleAgentPlan plan :
                currentSolutionStartingFromCurrentTime) {
            if (selectedAgents.size() == maxAgents){
                return selectedAgents;
            } else if (!plan.containsTarget()) {
                selectedAgents.add(plan.agent);
            }
        }
        return selectedAgents;
    }
}
