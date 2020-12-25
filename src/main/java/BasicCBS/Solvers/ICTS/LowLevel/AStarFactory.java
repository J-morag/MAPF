package BasicCBS.Solvers.ICTS.LowLevel;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

public class AStarFactory implements I_LowLevelSearcherFactory {
    @Override
    public A_LowLevelSearcher createSearcher(ICTS_Solver highLevelSolver, MAPF_Instance instance, DistanceTableAStarHeuristicICTS heuristic) {
        return new AStar(highLevelSolver, instance, heuristic);
    }
}
