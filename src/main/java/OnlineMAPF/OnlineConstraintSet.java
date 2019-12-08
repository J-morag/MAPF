package OnlineMAPF;

import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.Move;

import java.util.Collection;

public class OnlineConstraintSet extends ConstraintSet {

    public OnlineConstraintSet() {
    }

    public OnlineConstraintSet(ConstraintSet toCopy) {
        super(toCopy);
    }

    public OnlineConstraintSet(Collection<? extends Constraint> seedConstraints) {
        super(seedConstraints);
    }

    /**
     * Agents disappear at goal, so the only way to reject a move is with a constraint on its time (not later).
     * @param finalMove a move to occupy a location indefinitely.
     * @return -1
     */
    @Override
    public int rejectsEventually(Move finalMove) {
        return -1;
    }
}
