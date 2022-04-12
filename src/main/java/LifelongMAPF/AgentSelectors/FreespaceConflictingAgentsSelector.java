package LifelongMAPF.AgentSelectors;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;
import Environment.Metrics.InstanceReport;

import java.util.*;

/**
 * Computes an individual optimal free-space path for each agent without a path, selects agents whose current path
 * conflicts with any of the individual optimal paths.
 */
public class FreespaceConflictingAgentsSelector implements I_LifelongAgentSelector {

    SingleAgentAStar_Solver singleAgentSolver = new SingleAgentAStar_Solver();

    /**
     *
     * @param lifelongInstance the lifelong instance
     * @param latestSolution the current solution being followed
     * @param farthestCommittedTime will select agents that should be planned starting after this time.
     * @param lifelongAgentsToTimelyOfflineAgents
     * @return
     */
    @Override
    public Set<Agent> selectAgentsSubset(MAPF_Instance lifelongInstance, Solution latestSolution, int farthestCommittedTime, Map<Agent, Agent> lifelongAgentsToTimelyOfflineAgents) {
        Set<Agent> agentsWithoutPlan = this.mustSelectAgents(lifelongInstance, latestSolution, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents);
        List<SingleAgentPlan> freespaceIndividualPlans = new ArrayList<>();
        // compute an individual optimal free-space path for each agent without a path
        for (Agent agent: agentsWithoutPlan){
            InstanceReport disposableIR = new InstanceReport();
            RunParameters rp = new RunParameters(null, disposableIR,null, farthestCommittedTime); // TODO timeout?
            MAPF_Instance singleAgentInstance = new MAPF_Instance("individual optimal for " + agent.iD, lifelongInstance.map, new Agent[]{lifelongAgentsToTimelyOfflineAgents.get(agent)});
            SingleAgentPlan planForAgent = singleAgentSolver.solve(singleAgentInstance, rp).getPlanFor(agent);
            freespaceIndividualPlans.add(planForAgent);
        }

        Set<Agent> agentsWithPlan = new HashSet<>(lifelongInstance.agents);
        agentsWithPlan.removeAll(agentsWithoutPlan);
        Set<Agent> selectedAgents = new HashSet<>();
        for (Agent agentWithPlan: agentsWithPlan){
            for (SingleAgentPlan freespacePlan: freespaceIndividualPlans){
                if (latestSolution.getPlanFor(agentWithPlan).conflictsWith(freespacePlan)){
                    selectedAgents.add(agentWithPlan);
                    break;
                }
            }
        }

        selectedAgents.addAll(agentsWithoutPlan);
        return selectedAgents;
    }
}
