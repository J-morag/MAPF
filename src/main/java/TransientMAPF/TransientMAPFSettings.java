package TransientMAPF;

public record TransientMAPFSettings(boolean isTransientMAPF, boolean useBlacklist, boolean useSST) {
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true, true);
}
