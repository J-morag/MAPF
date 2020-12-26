package BasicCBS.Solvers.ICTS.LowLevel;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

public interface I_LowLevelSearcherFactory {
    A_LowLevelSearcher createSearcher(ICTS_Solver highLevelSolver, I_Location source, I_Location target, Agent agent, DistanceTableAStarHeuristicICTS heuristic);
}
