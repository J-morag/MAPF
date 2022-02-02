package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;

import java.util.Collection;

public interface ConflictSelectionStrategy {
    A_Conflict selectConflict(Collection<? extends A_Conflict> conflicts);

}
