package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

public class SIPPSHeuristic implements SingleAgentGAndH{
    private final SingleAgentGAndH wrappedHeuristic;
    private final int lowerBoundOnTravelTime;

    public SIPPSHeuristic(SingleAgentGAndH heuristic, int lowerBoundOnTravelTime) {
        this.wrappedHeuristic = heuristic;
        this.lowerBoundOnTravelTime = lowerBoundOnTravelTime;
    }

    @Override
    public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
        float hVal = wrappedHeuristic.getH(state);
        int conflicts = state.getConflicts();
        if (conflicts == 0) {
            int g = state.getG();
            return Math.max(hVal, lowerBoundOnTravelTime - g);
        }
        return hVal;
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        throw new UnsupportedOperationException("Requires a parent state to get conflicts from.");
    }

    @Override
    public boolean isConsistent() {
        return true; // probably
    }

    @Override
    public boolean isTransient() {
        return this.wrappedHeuristic.isTransient();
    }
}
