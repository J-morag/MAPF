package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;

import java.util.List;
import java.util.Random;

public interface I_DestroyHeuristic {

    List<Agent> selectNeighborhood(Solution currentSolution, int neighborhoodSize, Random rnd, I_Map map, SingleAgentGAndH heuristic);

    void clear();

}
