package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomDestroyHeuristic implements I_DestroyHeuristic{

    @Override
    public List<Agent> selectNeighborhood(Solution currentSolution, int neighborhoodSize, Random rnd, I_Map map) {
        List<Agent> shuffledAgentsList = new ArrayList<>(currentSolution.size());
        for (SingleAgentPlan plan :
                currentSolution) {
            shuffledAgentsList.add(plan.agent);
        }
        Collections.shuffle(shuffledAgentsList, rnd);
        return shuffledAgentsList.subList(0, neighborhoodSize);
    }

    @Override
    public void clear() {

    }
}
