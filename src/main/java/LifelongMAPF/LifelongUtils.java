package LifelongMAPF;

public class LifelongUtils {
    /**
     * For a given problem start time and horizon, return the absolute time when the horizon ends. Safe from integer overflow.
     * @return the absolute time when the horizon ends (the last time step that is in the horizon).
     */
    public static int horizonAsAbsoluteTime(int problemStartTime, int horizon) {
        return problemStartTime + horizon >= problemStartTime ? problemStartTime + horizon : Integer.MAX_VALUE;
    }
}
