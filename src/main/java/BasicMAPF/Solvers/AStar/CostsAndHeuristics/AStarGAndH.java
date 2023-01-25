package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.Move;

public interface AStarGAndH {
    /**
     * @param state a state in the AStar search tree.
     * @return a heristic for the distance from the state to a goal state.
     */
    float getH(SingleAgentAStar_Solver.AStarState state);

    /**
     * @param move a move
     * @return the cost of a {@link Move}.
     */
    default int cost(Move move){
        return 1;
    }

    /**
     * @return whether this is a consistent heuristic
     */
    boolean isConsistent();
}
