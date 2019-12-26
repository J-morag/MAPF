package BasicCBS.Solvers.ConstraintsAndConflicts.Constraint;

import BasicCBS.Solvers.Move;

/**
 * replaces the constraint with a key that is quick to find in a set.
 */
public interface I_ConstraintGroupingKey {

    /**
     * Given some agent's final move (brings it to goal), return true if constraints under this key may be relevant
     * at some point in the future (after this move)
     * @param finalMove an agent's final move in its plan
     * @return true if constraints under this key may be relevant at some point in the future
     */
    boolean relevantInTheFuture(Move finalMove);
}
