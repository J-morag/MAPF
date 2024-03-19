package TransientMAPF;

public record TransientMAPFSettings(boolean isTransientMAPF, boolean useBlacklist) {
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true);
}
