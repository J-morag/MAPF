package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.DataTypesAndStructures.Move;

public interface SingleAgentGAndH {
    /**
     * @param state a state in the AStar search tree.
     * @return a heuristic for the distance from the state to a goal state.
     */
    float getH(SingleAgentAStar_Solver.AStarState state);

    float getHToTargetFromLocation(I_Coordinate target, I_Location currLocation);

    /**
     * @param move a move
     * @param isAfterTarget a boolean indicates whether the agent reached his goal or not
     * @return the cost of a {@link Move}.
     */
    default int cost(Move move, boolean isAfterTarget){
        return cost(move);
    }

    /**
     * @param move a move
     * @return the cost of a {@link Move}.
     */
    default int cost(Move move){ return 1; }

    /**
     * @return whether this is a consistent heuristic
     */
    boolean isConsistent();
}
