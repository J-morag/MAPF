package BasicMAPF.DataTypesAndStructures;

import java.util.concurrent.TimeUnit;

public class Timeout {
    public long timeoutTimestampMS;

    public Timeout(long timeoutTimestampMS) {
        this.timeoutTimestampMS = timeoutTimestampMS;
    }

    public Timeout(long startTimeMS, long maximumRuntimeMS) {
        timeoutTimestampMS = getTimeoutTimestampMS(startTimeMS, maximumRuntimeMS);
    }

    public boolean isTimeoutExceeded() {
        return isTimeoutExceeded(timeoutTimestampMS);
    }

    public static boolean isTimeoutExceeded(long timeoutTimestampMS) {
        return getCurrentTimeMS_NSAccuracy() > timeoutTimestampMS;
    }

    public static boolean isTimeoutExceeded(long startTimeMS, long maximumRuntimeMS) {
        return getCurrentTimeMS_NSAccuracy() > getTimeoutTimestampMS(startTimeMS, maximumRuntimeMS);
    }

    public static long getTimeoutTimestampMS(long startTimeMS, long maximumRuntimeMS) {
        return startTimeMS + maximumRuntimeMS;
    }

    public static long getCurrentTimeMS_NSAccuracy() {
        return TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public static long elapsedMSSince_NSAccuracy(long since) {
        return getCurrentTimeMS_NSAccuracy() - since;
    }
}
