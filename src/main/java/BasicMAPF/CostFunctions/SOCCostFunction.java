package BasicMAPF.CostFunctions;

import BasicMAPF.Solvers.Solution;

public class SOCCostFunction implements I_SolutionCostFunction{
    @Override
    public float solutionCost(Solution solution) {
        return solution.sumIndividualCosts();
    }

    @Override
    public String name() {
        return "SOC";
    }
}
