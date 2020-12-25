package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;

import java.util.*;

public abstract class IndependenceDetection_MergedMDDFactory extends SearchBased_MergedMDDFactory {
    @Override
    public Solution create(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver) {

        List<AgentsGroup> agentsCollidingGroups = new ArrayList<>();
        for (Agent agent : agentMDDs.keySet()) {
            Set<Agent> set = new HashSet<>();
            set.add(agent);
            Solution solution = agentMDDs.get(agent).getPossibleSolution();
            AgentsGroup group = new AgentsGroup(set, solution);

            agentsCollidingGroups.add(group);
        }
        boolean conflicts = true;
        while (conflicts) {
            List<AgentsGroup> conflictingGroupsOneMemberFromEachGroup = new ArrayList<>();
            conflicts = false;
            for (int i = 0; i < agentsCollidingGroups.size(); i++) {
                AgentsGroup groupI = agentsCollidingGroups.get(i);
                for (int j = i + 1; j < agentsCollidingGroups.size(); j++) {
                    AgentsGroup groupJ = agentsCollidingGroups.get(j);
                    if (conflicts && groupI.isConflictedWith(groupJ)) //no need to check them if they already conflict with each other
                        continue;
                    Solution mergedSolution = new Solution(groupI.getSolution());
                    for (SingleAgentPlan plan : groupJ.getSolution()) {
                        mergedSolution.putPlan(plan);
                    }
                    if (!mergedSolution.isValidSolution()) {
                        conflicts = true;
                        if (!groupI.hasConflicts() && !groupJ.hasConflicts())
                            conflictingGroupsOneMemberFromEachGroup.add(groupI); // add only when the groups are not already in the conflicts...
                        groupI.addConflict(groupJ);
                    }
                }
            }

            if (conflicts) {
                for (AgentsGroup group : conflictingGroupsOneMemberFromEachGroup) {
                    Set<AgentsGroup> allInConflict = group.getConflicts();
                    allInConflict.add(group);
                    Map<Agent, MDD> currentMergingGroup = new HashMap<>();
                    Set<Agent> mergedAgents = new HashSet<>();
                    for (AgentsGroup curr : allInConflict) {
                        mergedAgents.addAll(curr.getAgents());
                        for (Agent agent : curr.getAgents()) {
                            currentMergingGroup.put(agent, agentMDDs.get(agent));
                        }
                    }
                    Solution mergedGroupSolution = super.create(currentMergingGroup, highLevelSolver);
                    if(mergedGroupSolution == null)
                        return null; //if a group don't have a solution, then there is surely no solution.
                    AgentsGroup mergedGroup = new AgentsGroup(mergedAgents, mergedGroupSolution);
                    agentsCollidingGroups.removeAll(allInConflict);
                    agentsCollidingGroups.add(mergedGroup);
                }
            }
        }
        //If we got here, then there is a solution for each group that does'nt collide with the other groups.
        Solution totalSolution = new Solution();
        for (AgentsGroup group : agentsCollidingGroups){
            for(SingleAgentPlan plan : group.getSolution()){
                totalSolution.putPlan(plan);
            }
        }
        if(!totalSolution.isValidSolution()) {
            try {
                throw new Exception("The total solution must be valid if we got here...");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        return totalSolution;
    }
}
