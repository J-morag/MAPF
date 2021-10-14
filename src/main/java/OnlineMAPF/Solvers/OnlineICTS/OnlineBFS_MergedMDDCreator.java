package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Solvers.ICTS.MergedMDDs.BreadthFirstSearch_MergedMDDCreator;

public class OnlineBFS_MergedMDDCreator extends BreadthFirstSearch_MergedMDDCreator {

    public OnlineBFS_MergedMDDCreator() {
        this.disappearAtGoal = true;
    }
}
