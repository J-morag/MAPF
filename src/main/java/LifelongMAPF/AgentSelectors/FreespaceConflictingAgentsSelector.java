package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import LifelongMAPF.LifelongAgent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Computes an individual optimal free-space path for each agent without a path, selects agents whose current path
 * conflicts with any of the individual optimal paths.
 */
public class FreespaceConflictingAgentsSelector implements I_LifelongAgentSelector {

    SingleAgentAStar_Solver singleAgentSolver;
    int maxGroupSize;

    public FreespaceConflictingAgentsSelector(SingleAgentAStar_Solver singleAgentSolver, Integer maxGroupSize) {
        this.singleAgentSolver = Objects.requireNonNullElse(singleAgentSolver, new SingleAgentAStar_Solver());
        this.maxGroupSize = Objects.requireNonNullElse(maxGroupSize, Integer.MAX_VALUE);
    }

    public FreespaceConflictingAgentsSelector(){
        this(null, null);
    }


    @Override
    public Predicate<Agent> getAgentSelectionPredicate(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues, Map<LifelongAgent, I_Coordinate> agentsActiveDestination) {
        Set<Agent> selectedAgents = new HashSet<>(getStationaryAgentsAndTheirFreespaceConflictingAgents(lifelongInstance, currentSolutionStartingFromCurrentTime, lifelongAgentsToTimelyOfflineAgents, agentsWaitingToStart));

        return new AgentSelectionPredicate(selectedAgents);
    }

    @NotNull
    private Set<LifelongAgent> getStationaryAgentsAndTheirFreespaceConflictingAgents(MAPF_Instance lifelongInstance, @NotNull Solution currentSolutionStartingFromCurrentTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart) {
        // TODO fill iteratively with the blocking agents (when max group is limited)? Assuming it's better to have a small seed and their blockers than a big seeds and 0 blockers because we reached max group size.
        Set<Agent> stationaryAgents = AllStationaryAgentsSubsetSelector.getAllStationaryAgents(lifelongInstance, currentSolutionStartingFromCurrentTime, agentsWaitingToStart, maxGroupSize);
        Set<LifelongAgent> selectedAgents = new HashSet<>(lifelongAgentsToTimelyOfflineAgents.keySet());
        selectedAgents.retainAll(stationaryAgents);

        int startTime = currentSolutionStartingFromCurrentTime.getStartTime();
        if (selectedAgents.size() < maxGroupSize){
            List<SingleAgentPlan> freespaceIndividualPlans = getFreespaceIndividualPlans(lifelongInstance, startTime, lifelongAgentsToTimelyOfflineAgents, selectedAgents);
            Set<LifelongAgent> agentsWithPlan = new HashSet<>(lifelongAgentsToTimelyOfflineAgents.keySet());
            agentsWithPlan.removeAll(selectedAgents);
            Set<LifelongAgent> conflictingAgents = getConflictingAgents(currentSolutionStartingFromCurrentTime, freespaceIndividualPlans, agentsWithPlan);
            for (LifelongAgent conflictingAgent :
                    conflictingAgents) {
                if (selectedAgents.size() == maxGroupSize) {
                    break;
                }
                else {
                    selectedAgents.add(conflictingAgent);
                }
            }
        }
        return selectedAgents;
    }


    @NotNull
    private static Set<LifelongAgent> getConflictingAgents(@NotNull Solution latestSolution, List<SingleAgentPlan> freespaceIndividualPlans, Set<LifelongAgent> agentsWithPlan) {
        Set<LifelongAgent> selectedAgents = new HashSet<>();
        for (LifelongAgent agentWithPlan: agentsWithPlan){
            for (SingleAgentPlan freespacePlan: freespaceIndividualPlans){
                if (latestSolution.getPlanFor(agentWithPlan).conflictsWith(freespacePlan)){
                    selectedAgents.add(agentWithPlan);
                    break;
                }
            }
        }
        return selectedAgents;
    }

    @NotNull
    private List<SingleAgentPlan> getFreespaceIndividualPlans(MAPF_Instance lifelongInstance, int startTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, Set<LifelongAgent> agentsNeedingPlan) {
        List<SingleAgentPlan> freespaceIndividualPlans = new ArrayList<>();
        // compute an individual optimal free-space path for each agent without a path
        for (LifelongAgent agent: agentsNeedingPlan){
            InstanceReport disposableIR = new InstanceReport();
            RunParameters rp = new RunParameters(null, disposableIR,null, startTime); // TODO timeout?
            MAPF_Instance singleAgentInstance = new MAPF_Instance("individual optimal for " + agent.iD, lifelongInstance.map, new Agent[]{lifelongAgentsToTimelyOfflineAgents.get(agent)});
            SingleAgentPlan planForAgent = singleAgentSolver.solve(singleAgentInstance, rp).getPlanFor(agent);
            freespaceIndividualPlans.add(planForAgent);
        }
        return freespaceIndividualPlans;
    }
}
