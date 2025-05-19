package TransientMAPF;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public record TransientMAPFSettings(boolean isTransientMAPF, boolean avoidOtherAgentsTargets, boolean avoidSeparatingVertices, boolean resolveAfterGoalConflictsLocally) {
    public TransientMAPFSettings {
        if (! isTransientMAPF && (avoidOtherAgentsTargets || avoidSeparatingVertices || resolveAfterGoalConflictsLocally)) {
            throw new IllegalArgumentException("useBlacklist, avoidSeparatingVertices, and resolveAfterGoalConflictsLocally can only be true if isTransientMAPF");
        }
    }
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false, false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true, false, false);
}
