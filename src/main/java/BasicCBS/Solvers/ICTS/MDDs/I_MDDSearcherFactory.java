package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

public interface I_MDDSearcherFactory {
    A_MDDSearcher createSearcher(ICTS_Solver highLevelSolver, I_Location source, I_Location target, Agent agent,
                                 DistanceTableAStarHeuristicICTS heuristic);

    void setDefaultDisappearAtGoal(boolean disappearAtGoal);
}
