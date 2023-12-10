package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class Makespan implements I_SolutionCostFunction{

    public static final String NAME = "Makespan";
    public static final Makespan instance = new Makespan();

    @Override
    public int solutionCost(Solution solution) {
        return solution.makespan();
    }

    @Override
    public String name() {
        return NAME;
    }
}
