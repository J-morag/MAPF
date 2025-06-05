package BasicMAPF.Solvers.PathAndPrioritySearch;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.DataTypesAndStructures.MDDs.MDD;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public record PaPSNode(@NotNull ArrayList<MDD> MDDs, int g, int h, int[] hArr, @NotNull ConstraintSet constraints,
                      A_Conflict conflict, int uniqueID, Agent[] priorityOrderedAgents, Map<Agent, Integer> agentIndices) {
    public int getF() {
        return g + h;
    }
    public MDD getLeastPriorityMDD(){
        return MDDs.get(MDDs.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaPSNode paPSNode)) return false;
        // if either is a fully generated node, then say they are not equal, because it probably matters what the "current" MDD is
        if (conflict != null || paPSNode.conflict != null) return false;

        // check set equality of the MDDs
        if (MDDs.size() != paPSNode.MDDs.size()) return false;
        if (g != paPSNode.g) return false;
        ArrayList<MDD> MDDsInCanonicalOrder = new ArrayList<>(MDDs);
        ArrayList<MDD> otherMDDsInCanonicalOrder = new ArrayList<>(paPSNode.MDDs);
        MDDsInCanonicalOrder.sort(Comparator.comparingInt(mdd -> mdd.getAgent().iD));
        otherMDDsInCanonicalOrder.sort(Comparator.comparingInt(mdd -> mdd.getAgent().iD));
        for (int i = 0; i < MDDsInCanonicalOrder.size(); i++) {
            if (!MDDsInCanonicalOrder.get(i).equals(otherMDDsInCanonicalOrder.get(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        // set hash: sum of element hashes
        int res = 0;
        for (MDD mdd : MDDs) {
            res += mdd.hashCode();
        }
        return res;
    }

    public int getSumMddSizes() {
        return MDDs.stream().mapToInt(MDD::numNodes).sum();
    }
}
