package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public interface I_SolutionCostFunction {

    float solutionCost(Solution solution);

    String name();

}
