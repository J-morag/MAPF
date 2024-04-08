package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.DataTypesAndStructures.Move;
import org.jetbrains.annotations.NotNull;

public interface SingleAgentGAndH {
    /**
     * @param state a state in the AStar search tree.
     * @return a heuristic for the distance from the state to a goal state.
     */
    float getH(@NotNull SingleAgentAStar_Solver.AStarState state);

    int getHToTargetFromLocation(@NotNull I_Coordinate target,@NotNull I_Location currLocation);

    /**
     * @param move a move
     * @param isAfterTargetExcludingFirstMoveToTarget if the agent has already visited its target,
     *                                           previously (not reaching it for the first time now)
     * @return the cost of a {@link Move}.
     */
    default int cost(Move move, boolean isAfterTargetExcludingFirstMoveToTarget){
        return cost(move);
    }

    /**
     * @param move a move
     * @return the cost of a {@link Move}.
     */
    default int cost(Move move){
        return 1; }

    /**
     * @return whether this is a consistent heuristic
     */
    boolean isConsistent();
}
