package TransientMAPF;


public record TransientMAPFSettings(boolean isTransientMAPF, boolean avoidOtherAgentsTargets, boolean avoidSeparatingVertices, boolean resolveAfterGoalConflictsLocally) {
    public TransientMAPFSettings {
        if (! isTransientMAPF && (avoidOtherAgentsTargets || avoidSeparatingVertices || resolveAfterGoalConflictsLocally)) {
            throw new IllegalArgumentException("useBlacklist, avoidSeparatingVertices, and resolveAfterGoalConflictsLocally can only be true if isTransientMAPF");
        }
    }
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false, false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true, false, false);
}
