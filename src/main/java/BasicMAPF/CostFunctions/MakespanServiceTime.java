package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class MakespanServiceTime implements I_SolutionCostFunction {
    // todo add tests where this is used

    public static final String NAME = "MKST";
    public static final MakespanServiceTime instance = new MakespanServiceTime();

    @Override
    public int solutionCost(Solution solution) {
        return solution.makespanServiceTime();
    }

    @Override
    public String name() {
        return NAME;
    }
}
