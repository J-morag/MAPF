package BasicCBS.Solvers.ICTS.LowLevel;

import BasicCBS.Solvers.ICTS.GeneralStuff.MDD;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

public abstract class A_LowLevelSearcher {
    protected int expandedNodesNum;
    protected int generatedNodesNum;
    protected ICTS_Solver highLevelSearcher;

    public A_LowLevelSearcher(ICTS_Solver highLevelSearcher) {
        expandedNodesNum = 0;
        generatedNodesNum = 0;
        this.highLevelSearcher = highLevelSearcher;
    }

    public int getExpandedNodesNum() {
        return expandedNodesNum;
    }

    public int getGeneratedNodesNum() {
        return generatedNodesNum;
    }

    /**
     * Searches for all the solutions in a wanted depth
     * Continuing the search from the last "checkpoint" that means that all of the open list and closed list is already saved in the searcher.
     * @param depthOfSolution - the depth of the wanted solutions
     * @return the goal state, which can easily be transferred to an MDD
     */
    public abstract MDD continueSearching(int depthOfSolution);
}
