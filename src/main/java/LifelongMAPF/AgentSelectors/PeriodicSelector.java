package LifelongMAPF.AgentSelectors;

public class PeriodicSelector {

    public final int replanningPeriod;
    private int latestPlanningTime = 0;

    public PeriodicSelector(int replanningPeriod) {
        this.replanningPeriod = replanningPeriod;
    }

    public PeriodicSelector() {
        this(1);
    }

    public boolean timeMeetsOrExceedsPeriod(int currentTime) {
        if (currentTime != 0 &&
                currentTime < latestPlanningTime) {
            throw new IllegalStateException(String.format("Periodic Selector wasn't reset properly. Solution start time: %d, latest planning time: %d", currentTime, latestPlanningTime));
        }
        if (currentTime == 0 ||
                currentTime == latestPlanningTime || // for repeat queries
                currentTime - latestPlanningTime >= replanningPeriod) {
            latestPlanningTime = currentTime;
            return true;
        }
        return false;
    }
}
