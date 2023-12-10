package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class SumServiceTimes implements I_SolutionCostFunction{
    public static final String NAME = "SST";
    public static final SumServiceTimes instance = new SumServiceTimes();

    @Override
    public int solutionCost(Solution solution) {
        return solution.sumServiceTimes();
    }

    @Override
    public String name() {
        return NAME;
    }
}
