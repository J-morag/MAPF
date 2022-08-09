package BasicMAPF.CostFunctions;

import BasicMAPF.Solvers.Solution;

public interface I_SolutionCostFunction {

    float solutionCost(Solution solution);

    String name();

}
