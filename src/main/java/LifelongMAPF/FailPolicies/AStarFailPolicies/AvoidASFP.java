package LifelongMAPF.FailPolicies.AStarFailPolicies;

public class AvoidASFP extends GoASFP {
    public AvoidASFP(int d) {
        super(d);
    }

    @Override
    protected int getCost(GoState curr, double edgeDistanceFromSourceDelta) {
        return curr.cost + (edgeDistanceFromSourceDelta < 0 ? 1 : (edgeDistanceFromSourceDelta == 0 ? d + 1 : (d + 1) * (d + 1)));
    }
}
