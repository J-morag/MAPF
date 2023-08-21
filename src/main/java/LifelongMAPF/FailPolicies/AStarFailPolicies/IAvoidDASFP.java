package LifelongMAPF.FailPolicies.AStarFailPolicies;

public class IAvoidDASFP extends IGoDASFP {
    public IAvoidDASFP(int d) {
        super(d);
    }

    @Override
    protected int getCost(IGoState curr, double edgeDistanceFromSourceDelta) {
        return curr.cost + (edgeDistanceFromSourceDelta < 0 ? 1 : (edgeDistanceFromSourceDelta == 0 ? d + 1 : (d + 1) * (d + 1)));
    }
}
