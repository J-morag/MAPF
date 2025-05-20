package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.DataTypesAndStructures.Move;

/**
 * Allows solvers to avoid plans that create many conflicts.
 */
public interface I_ConflictAvoidanceTable {

    /**
     * Returns the number of agents who's plans conflict with this move
     *
     * @param move        a move that an agent wants to make
     * @param isALastMove whether this move is the last move of the agent, meaning the agent is set to stay there forever
     * @return the number of agents who's plans conflict with this move
     */
    int numConflicts(Move move, boolean isALastMove);

    int getLastOccupancyTime();

    int getNumberOfEdgeConflicts(Move move);
}
