package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.NaiveConflictDetection;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.MDDs.MDD;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import com.google.common.collect.Maps;

import java.util.*;

public class IndependenceDetection_MergedMDDSolver implements I_MergedMDDSolver {

    private int expandedLowLevelNodes;
    private int generatedLowLevelNodes;
    /**
     * An MDD merger to delegate to when groups are to be merged.
     */
    final private I_MergedMDDSolver delegatedMergedMDDFactory;

    public IndependenceDetection_MergedMDDSolver(I_MergedMDDSolver delegatedMergedMDDFactory) {
        this.delegatedMergedMDDFactory = delegatedMergedMDDFactory;
    }

    @Override
    public Solution findJointSolution(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver) {
        this.expandedLowLevelNodes = 0;
        this.generatedLowLevelNodes = 0;

        Map<Agent, AgentsGroup> agentToAgentsGroup = new HashMap<>();
        Set<AgentsGroup> agentGroups = new HashSet<>();
        for (Agent agent : agentMDDs.keySet()) {
            // find an arbitrary solution for each agent and put each agent in a singleton group
            Set<Agent> set = new HashSet<>();
            set.add(agent);
            Solution solution = getPossibleSolution(agentMDDs, agent);
            AgentsGroup group = getAgentsGroup(set, solution);
            agentToAgentsGroup.put(agent, group);

            agentGroups.add(group);
        }
        // will break by returning, when either there are no conflicts or we can't find a joint solution for some group
        while (true){
            I_ConflictManager conflictManager = getConflictManager(agentGroups);
            A_Conflict arbitraryConflict = conflictManager.selectConflict();
            if (arbitraryConflict == null){
                // no conflicts - found the goal!
                return mergeSolutions(agentGroups);
            }
            else{
                // these have to be distinct if we have no bugs
                AgentsGroup group1 = agentToAgentsGroup.get(arbitraryConflict.agent1);
                AgentsGroup group2 = agentToAgentsGroup.get(arbitraryConflict.agent2);
                // get a joint solution
                Map<Agent, MDD> filteredMDDMap = Maps.filterKeys(agentMDDs, a -> group1.getAgents().contains(a) || group2.getAgents().contains(a));
                Solution mergedGroupSolution = getSolution(delegatedMergedMDDFactory.findJointSolution(filteredMDDMap, highLevelSolver));
                // if a sub-group can't be solved at these costs, there is no solution at these costs
                if (mergedGroupSolution == null){
                    return null;
                }
                else{
                    // remove the old groups
                    agentGroups.remove(group1);
                    agentGroups.remove(group2);
                    // add a merged group
                    AgentsGroup mergedGroup = getAgentsGroup(new HashSet<>(filteredMDDMap.keySet()), mergedGroupSolution);
                    agentGroups.add(mergedGroup);
                    for (Agent a : group1.getAgents()) agentToAgentsGroup.put(a, mergedGroup);
                    for (Agent a : group2.getAgents()) agentToAgentsGroup.put(a, mergedGroup);
                }
            }
        }
    }

    private Solution mergeSolutions(Set<AgentsGroup> agentGroups) {
        Solution mergedSolution = getSolution();
        for (AgentsGroup ag : agentGroups){
            for (SingleAgentPlan plan : ag.getSolution()){
                mergedSolution.putPlan(plan);

            }
        }
        return mergedSolution;
    }

    protected I_ConflictManager getConflictManager(Set<AgentsGroup> agentGroups) {
        I_ConflictManager conflictManager = new NaiveConflictDetection(true);
        for (AgentsGroup ag : agentGroups){
            for (SingleAgentPlan plan : ag.getSolution()){
                conflictManager.addPlan(plan);
            }
        }
        return conflictManager;
    }

    protected Solution getPossibleSolution(Map<Agent, MDD> agentMDDs, Agent agent) {
        return agentMDDs.get(agent).getPossibleSolution();
    }

    protected AgentsGroup getAgentsGroup(Set<Agent> agents, Solution solution) {
        return new AgentsGroup(agents, solution);
    }

    protected Solution getSolution(AgentsGroup ag){
        return new Solution(ag.getSolution());
    }

    protected Solution getSolution(){
        return new Solution();
    }

    protected Solution getSolution(Solution solution){
        return solution;
    }

    @Override
    public int getExpandedLowLevelNodesNum() {
        return this.expandedLowLevelNodes;
    }

    @Override
    public int getGeneratedLowLevelNodesNum() {
        return this.generatedLowLevelNodes;
    }

}
