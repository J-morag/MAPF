package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.Solvers.Move;

/**
 * Allows solvers to avoid plans that create many conflicts.
 */
public interface I_ConflictAvoidanceTable {

    /**
     * Returns the number of agents who's plans conflict with this move
     * @param move a move that an agent wants to make
     * @return the number of agents who's plans conflict with this move
     */
    int numConflicts(Move move);

}
