package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.Solution;

import java.util.HashSet;
import java.util.Set;

public class AgentsGroup {
    private Set<Agent> agents;
    private Solution solution;
    private Set<AgentsGroup> conflicts;


    public AgentsGroup(Set<Agent> agents, Solution solution) {
        this.agents = agents;
        this.solution = solution;
        this.conflicts = new HashSet<>();
    }

    public Set<Agent> getAgents() {
        return agents;
    }

    public Solution getSolution() {
        return solution;
    }

    public Set<AgentsGroup> getConflicts() {
        return conflicts;
    }

    public boolean isConflictedWith(AgentsGroup other){
        return conflicts.contains(other);
    }

    public boolean hasConflicts(){
        return !conflicts.isEmpty();
    }

    public void addConflict(AgentsGroup other){
        if(conflicts.contains(other))
            return;
        Set<AgentsGroup> prevConflicts = new HashSet<>(conflicts);
        conflicts.add(other);
        for (AgentsGroup conflictedGroup : prevConflicts) {
            conflictedGroup.addConflict(other);
        }
        other.addConflict(this);
    }
}
