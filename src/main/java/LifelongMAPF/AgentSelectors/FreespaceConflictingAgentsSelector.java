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

    /**
     * @param lifelongInstance                    the lifelong instance
     * @param latestSolution                      the current solution being followed
     * @param farthestCommittedTime               will select agents that should be planned starting after this time.
     * @param lifelongAgentsToTimelyOfflineAgents
     * @param agentsWaitingToStart
     * @param agentDestinationQueues
     * @return
     */
    @Override
    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues) {
        Set<Agent> selectedAgents = new HashSet<>();

        for (int i = 0; i < agentsWaitingToStart.size() && i < maxGroupSize; i++) {
            selectedAgents.add(agentsWaitingToStart.get(i));
        }

        Set<LifelongAgent> agentsNeedingPlan = this.waitingForPathAgents(lifelongInstance, latestSolution, farthestCommittedTime, agentDestinationQueues);
        for (LifelongAgent agentNeedingPlan :
                agentsNeedingPlan) {
            if (selectedAgents.size() == maxGroupSize) {
                break;
            }
            else {
                selectedAgents.add(agentNeedingPlan); // TODO fill iteratively with the blocking agents?
            }
        }

        if (selectedAgents.size() < maxGroupSize){
            List<SingleAgentPlan> freespaceIndividualPlans = getFreespaceIndividualPlans(lifelongInstance, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents, agentsNeedingPlan);
            Set<Agent> agentsWithPlan = new HashSet<>(lifelongInstance.agents);
            agentsWithPlan.removeAll(agentsNeedingPlan);
            Set<Agent> conflictingAgents = getConflictingAgents(latestSolution, freespaceIndividualPlans, agentsWithPlan);
            for (Agent conflictingAgent :
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

//
//    /**
//     * @param lifelongInstance                    the lifelong instance
//     * @param latestSolution                      the current solution being followed
//     * @param farthestCommittedTime               will select agents that should be planned starting after this time.
//     * @param lifelongAgentsToTimelyOfflineAgents
//     * @param agentsWaitingToStart
//     * @param agentDestinationQueues
//     * @return
//     */
//    @Override
//    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, @NotNull Solution latestSolution, int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> agentsWaitingToStart, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues) {
//        Set<Agent> selectedAgents = new HashSet<>();
//
//        for (int i = 0; i < agentsWaitingToStart.size() && i < maxGroupSize; i++) {
//            selectedAgents.add(agentsWaitingToStart.get(i));
//        }
//
//        Set<LifelongAgent> agentsNeedingPlan = this.waitingForPathAgents(lifelongInstance, latestSolution, farthestCommittedTime, agentDestinationQueues);
//        for (LifelongAgent agentNeedingPlan :
//                agentsNeedingPlan) {
//            if (selectedAgents.size() == maxGroupSize) {
//                break;
//            }
//            else {
//                selectedAgents.add(agentNeedingPlan);
//            }
//        }
//
//        if (selectedAgents.size() < maxGroupSize){
//            List<SingleAgentPlan> freespaceIndividualPlans = getFreespaceIndividualPlans(lifelongInstance, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents, agentsNeedingPlan);
//            Set<Agent> agentsWithPlan = new HashSet<>(lifelongInstance.agents);
//            agentsWithPlan.removeAll(agentsNeedingPlan);
//            Set<Agent> conflictingAgents = getConflictingAgents(latestSolution, freespaceIndividualPlans, agentsWithPlan);
//            for (Agent conflictingAgent :
//                    conflictingAgents) {
//                if (selectedAgents.size() == maxGroupSize) {
//                    break;
//                }
//                else {
//                    selectedAgents.add(conflictingAgent);
//                }
//            }
//        }
//
//        return selectedAgents;
//    }

    @NotNull
    private static Set<Agent> getConflictingAgents(@NotNull Solution latestSolution, List<SingleAgentPlan> freespaceIndividualPlans, Set<Agent> agentsWithPlan) {
        Set<Agent> selectedAgents = new HashSet<>();
        for (Agent agentWithPlan: agentsWithPlan){
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
    private List<SingleAgentPlan> getFreespaceIndividualPlans(MAPF_Instance lifelongInstance, int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, Set<LifelongAgent> agentsNeedingPlan) {
        List<SingleAgentPlan> freespaceIndividualPlans = new ArrayList<>();
        // compute an individual optimal free-space path for each agent without a path
        for (LifelongAgent agent: agentsNeedingPlan){
            InstanceReport disposableIR = new InstanceReport();
            RunParameters rp = new RunParameters(null, disposableIR,null, farthestCommittedTime); // TODO timeout?
            MAPF_Instance singleAgentInstance = new MAPF_Instance("individual optimal for " + agent.iD, lifelongInstance.map, new Agent[]{lifelongAgentsToTimelyOfflineAgents.get(agent)});
            SingleAgentPlan planForAgent = singleAgentSolver.solve(singleAgentInstance, rp).getPlanFor(agent);
            freespaceIndividualPlans.add(planForAgent);
        }
        return freespaceIndividualPlans;
    }
}
