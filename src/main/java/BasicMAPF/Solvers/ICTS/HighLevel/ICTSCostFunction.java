package BasicMAPF.Solvers.ICTS.HighLevel;

import BasicMAPF.Solvers.Solution;

public interface ICTSCostFunction {
    float solutionCost(Solution solution, ICTS_Solver cbs);

}
