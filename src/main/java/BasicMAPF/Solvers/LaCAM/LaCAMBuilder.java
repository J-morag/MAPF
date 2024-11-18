package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import TransientMAPF.TransientMAPFSettings;

import java.util.Objects;

public class LaCAMBuilder {
    private I_SolutionCostFunction solutionCostFunction = null;
    private TransientMAPFSettings transientMAPFSettings = null;
    private Integer RHCR_Horizon = Integer.MAX_VALUE;

    private Boolean returnPartialSolutions = false;

    private Boolean ignoresStayAtSharedGoals = false;
    public LaCAMBuilder setSolutionCostFunction(I_SolutionCostFunction solutionCostFunction) {
        this.solutionCostFunction = solutionCostFunction;
        return this;
    }

    public LaCAMBuilder setTransientMAPFBehaviour(TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = transientMAPFSettings;
        return this;
    }

    public LaCAMBuilder setRHCRHorizon(Integer RHCR_Horizon) {
        this.RHCR_Horizon = RHCR_Horizon;
        return this;
    }

    public LaCAMBuilder setReturnPartialSolutions(Boolean returnPartialSolutions) {
        this.returnPartialSolutions = returnPartialSolutions;
        return this;
    }

    public LaCAMBuilder setIgnoresStayAtSharedGoals(Boolean ignoresStayAtSharedGoals) {
        this.ignoresStayAtSharedGoals = ignoresStayAtSharedGoals;
        return this;
    }

    public LaCAM_Solver createLaCAM() {
        return new LaCAM_Solver(solutionCostFunction, transientMAPFSettings, RHCR_Horizon, returnPartialSolutions, ignoresStayAtSharedGoals);
    }
}
