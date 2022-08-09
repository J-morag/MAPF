package BasicMAPF.CostFunctions;

import BasicMAPF.Solvers.Solution;

public class SOCPCostFunction implements I_SolutionCostFunction{
    @Override
    public float solutionCost(Solution solution) {
        return solution.sumIndividualCostsWithPriorities();
    }

    @Override
    public String name() {
        return "SOCP";
    }
}
