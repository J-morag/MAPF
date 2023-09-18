package LifelongMAPF.AgentSelectors;

import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.WaypointsGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public abstract class A_LifelongAgentSelector implements I_LifelongAgentSelector {

    public final PeriodicSelector periodicSelector;


    protected A_LifelongAgentSelector(PeriodicSelector periodicSelector) {
        this.periodicSelector = Objects.requireNonNullElseGet(periodicSelector, () -> new PeriodicSelector(1));
    }

    protected A_LifelongAgentSelector() {
        this(null);
    }


    @Override
    public boolean timeToPlan(int farthestCommittedTime) {
        return periodicSelector.timeMeetsOrExceedsPeriod(farthestCommittedTime);
    }
    @Override
    public int getPlanningFrequency() {
        return periodicSelector.replanningPeriod;
    }

    @Override
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<LifelongAgent, WaypointsGenerator> agentsWaypointsGenerators, Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents) {
        return selectAgentsIfTimeToPlan(lifelongInstance, currentSolutionStartingFromCurrentTime,  lifelongAgentsToTimelyOfflineAgents,  agentsWaitingToStart, agentsWaypointsGenerators,  agentsActiveDestination, failedAgents);
    }

    @NotNull
    private AgentSelectionPredicate selectAgentsIfTimeToPlan(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime,
                                                             Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart,
                                                             Map<LifelongAgent, WaypointsGenerator> agentsWaypointsGenerators,
                                                             Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents) {
        if (timeToPlan(currentSolutionStartingFromCurrentTime.getStartTime())) {
            return new AgentSelectionPredicate(selectAgents(lifelongInstance, currentSolutionStartingFromCurrentTime,  lifelongAgentsToTimelyOfflineAgents,  agentsWaitingToStart,  agentsWaypointsGenerators,  agentsActiveDestination, failedAgents));
        }
        else {
            return getNoAgentsPredicate();
        }
    }

    protected abstract Set<Agent> selectAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime,
                                               Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart,
                                               Map<LifelongAgent, WaypointsGenerator> agentsWaypointsGenerators,
                                               Map<LifelongAgent, I_Coordinate> agentsActiveDestination, Set<Agent> failedAgents);

    @NotNull
    private static AgentSelectionPredicate getNoAgentsPredicate() {
        return new AgentSelectionPredicate(null);
    }
}
