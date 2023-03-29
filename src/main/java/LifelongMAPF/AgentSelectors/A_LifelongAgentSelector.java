package LifelongMAPF.AgentSelectors;

import java.util.Objects;

public abstract class A_LifelongAgentSelector implements I_LifelongAgentSelector {

    public final PeriodicSelector periodicSelector;


    protected A_LifelongAgentSelector(PeriodicSelector periodicSelector) {
        this.periodicSelector = Objects.requireNonNullElse(periodicSelector, new PeriodicSelector(1));
    }

    protected A_LifelongAgentSelector() {
        this(null);
    }


    @Override
    public boolean timeToPlan(int farthestCommittedTime) {
        return periodicSelector.timeMeetsOrExceedsPeriod(farthestCommittedTime);
    }
    @Override
    public int getPlanningFrequency() {
        return periodicSelector.replanningPeriod;
    }
}
