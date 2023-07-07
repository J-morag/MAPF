package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class SSTCostFunction implements  I_SolutionCostFunction{
    @Override
    public float solutionCost(Solution solution) {
        return solution.sumServiceTimes();
    }

    @Override
    public String name() {
        return "SST";
    }
}
