package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class SumOfCosts implements I_SolutionCostFunction{

    public static final String NAME = "SOC";
    public static final SumOfCosts instance = new SumOfCosts();

    @Override
    public int solutionCost(Solution solution) {
        return solution.sumIndividualCosts();
    }

    @Override
    public String name() {return NAME;}
}
