package LifelongMAPF.AgentSelectors;

import BasicMAPF.Solvers.Solution;
import org.jetbrains.annotations.NotNull;

public class PeriodicSelector {

    public final int replanningPeriod;
    private int latestPlanningTime = 0;

    public PeriodicSelector(int replanningPeriod) {
        this.replanningPeriod = replanningPeriod;
    }

    public PeriodicSelector() {
        this(1);
    }

    public boolean timeMeetsOrExceedsPeriod(@NotNull Solution currentSolutionStartingFromCurrentTime) {
        if (currentSolutionStartingFromCurrentTime.getStartTime() != 0 &&
                currentSolutionStartingFromCurrentTime.getStartTime() <= latestPlanningTime) {
            throw new IllegalStateException(String.format("Periodic Selector wasn't reset properly. Solution start time: %d, latest planning time: %d", currentSolutionStartingFromCurrentTime.getStartTime(), latestPlanningTime));
        }
        if (currentSolutionStartingFromCurrentTime.getStartTime() == 0 ||
                currentSolutionStartingFromCurrentTime.getStartTime() - latestPlanningTime >= replanningPeriod) {
            latestPlanningTime = currentSolutionStartingFromCurrentTime.getStartTime();
            return true;
        }
        return false;
    }
}
