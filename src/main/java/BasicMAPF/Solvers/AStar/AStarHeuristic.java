package BasicMAPF.Solvers.AStar;

public interface AStarHeuristic {
    /**
     * @param state a state in the AStar search tree.
     * @return a heristic for the distance from the state to a goal state.
     */
    float getH(SingleAgentAStar_Solver.AStarState state);

    /**
     * @return whether this is a consistent heuristic
     */
    boolean isConsistent();
}
