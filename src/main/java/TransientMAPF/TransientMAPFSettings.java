package TransientMAPF;

import BasicMAPF.Instances.Maps.I_Location;

import java.util.Comparator;
import java.util.Set;

public record TransientMAPFSettings(boolean isTransientMAPF, boolean avoidOtherAgentsTargets, boolean avoidSeparatingVertices) {
    public TransientMAPFSettings {
        if (! isTransientMAPF && (avoidOtherAgentsTargets || avoidSeparatingVertices)) {
            throw new IllegalArgumentException("useBlacklist and avoidSeparatingVertices can only be true if isTransientMAPF");
        }
    }
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true, false);

    public static Comparator<I_Location> createSeparatingVerticesComparator(Set<I_Location> separatingVerticesSet) {
        return (loc1, loc2) -> {
            boolean isLoc1SV = separatingVerticesSet.contains(loc1);
            boolean isLoc2SV = separatingVerticesSet.contains(loc2);
            if (!isLoc1SV && isLoc2SV) return -1;
            if (isLoc1SV && !isLoc2SV) return 1;
            return 0;
        };
    }
}
