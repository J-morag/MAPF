package BasicMAPF.DataTypesAndStructures;

public record TimeInterval(int start, int end) {
    public static final TimeInterval DEFAULT_INTERVAL = new TimeInterval(0, Integer.MAX_VALUE);
}
