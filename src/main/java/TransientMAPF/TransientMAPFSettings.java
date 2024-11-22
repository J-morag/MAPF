package TransientMAPF;


public record TransientMAPFSettings(boolean isTransientMAPF, boolean avoidOtherAgentsTargets, boolean avoidSeparatingVertices) {
    public TransientMAPFSettings {
        if (! isTransientMAPF && (avoidOtherAgentsTargets || avoidSeparatingVertices)) {
            throw new IllegalArgumentException("useBlacklist and avoidSeparatingVertices can only be true if isTransientMAPF");
        }
    }
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true, false);
}
