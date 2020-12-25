package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.Solution;

import java.util.Map;

public interface I_MergedMDDFactory {
    Solution create(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver);
}