package BasicCBS.Solvers.ICTS.LowLevel;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

public interface I_LowLevelSearcherFactory {
    A_LowLevelSearcher createSearcher(ICTS_Solver highLevelSolver, MAPF_Instance instance, DistanceTableAStarHeuristicICTS heuristic);
}
