package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.DataTypesAndStructures.MDDs.MDD;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public record PCSNode(@NotNull ArrayList<MDD> MDDs, int g, int h, @NotNull ConstraintSet constraints, A_Conflict conflict,
                      int uniqueID) {
    public int getF() {
        return g + h;
    }
    public MDD getLeastPriorityMDD(){
        return MDDs.get(MDDs.size() - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PCSNode pcsNode)) return false;

        return uniqueID == pcsNode.uniqueID;
    }

    @Override
    public int hashCode() { // todo is there and can we detect symmetries where MDD lists are equivalent?
        return uniqueID;
    }

    public int getSumMddSizes() {
        return MDDs.stream().mapToInt(MDD::numNodes).sum();
    }
}
