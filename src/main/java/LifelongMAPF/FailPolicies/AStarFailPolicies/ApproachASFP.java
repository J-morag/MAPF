package LifelongMAPF.FailPolicies.AStarFailPolicies;

public class ApproachASFP extends GoASFP {
    public ApproachASFP(int d) {
        super(d);
    }

    @Override
    protected int getCost(GoState curr, double edgeDistanceFromSourceDelta, double edgeDistanceFromTargetDelta) {
        return curr.cost + (edgeDistanceFromTargetDelta < 0 ? 1 : (edgeDistanceFromTargetDelta == 0 ? d + 1 : (d + 1) * (d + 1)));
    }
}
