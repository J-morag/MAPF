package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class SOCWithPriorities implements I_SolutionCostFunction{
    public static final String NAME = "SOCP";
    public static final SOCWithPriorities instance = new SOCWithPriorities();

    @Override
    public int solutionCost(Solution solution) {
        return solution.sumIndividualCostsWithPriorities();
    }

    @Override
    public String name() {
        return NAME;
    }
}
