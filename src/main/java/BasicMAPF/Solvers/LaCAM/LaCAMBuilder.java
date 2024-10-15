package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import TransientMAPF.TransientMAPFSettings;

public class LaCAMBuilder {
    private I_SolutionCostFunction solutionCostFunction = null;
    private TransientMAPFSettings transientMAPFSettings = null;

    public LaCAMBuilder setSolutionCostFunction(I_SolutionCostFunction solutionCostFunction) {
        this.solutionCostFunction = solutionCostFunction;
        return this;
    }

    public LaCAMBuilder setTransientMAPFBehaviour(TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = transientMAPFSettings;
        return this;
    }

    public LaCAM_Solver createLaCAM() {
        return new LaCAM_Solver(solutionCostFunction, transientMAPFSettings);
    }
}
