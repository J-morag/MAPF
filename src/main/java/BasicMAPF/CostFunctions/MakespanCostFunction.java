package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class MakespanCostFunction implements I_SolutionCostFunction{

    @Override
    public float solutionCost(Solution solution) {
        return solution.makespan();
    }

    @Override
    public String name() {
        return "Makespan";
    }
}
