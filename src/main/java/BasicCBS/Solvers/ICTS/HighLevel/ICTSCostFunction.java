package BasicCBS.Solvers.ICTS.HighLevel;

import BasicCBS.Solvers.Solution;

public interface ICTSCostFunction {
    float solutionCost(Solution solution, ICTS_Solver cbs);

}
