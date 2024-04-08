package TransientMAPF;

public record TransientMAPFSettings(boolean isTransientMAPF, boolean useBlacklist) {
    public TransientMAPFSettings {
        if (! isTransientMAPF && useBlacklist) {
            throw new IllegalArgumentException("useBlacklist can only be true if isTransientMAPF");
        }
    }
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true);
}
