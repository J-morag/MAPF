package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Solvers.ICTS.MergedMDDs.DFS_MergedMDDSpaceSolver;

public class OnlineDFS_MergedMDDSpaceSolver extends DFS_MergedMDDSpaceSolver {

    public OnlineDFS_MergedMDDSpaceSolver() {
        this.disappearAtGoal = true;
    }
}
