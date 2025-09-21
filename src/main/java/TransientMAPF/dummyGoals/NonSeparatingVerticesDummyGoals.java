package TransientMAPF.dummyGoals;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import TransientMAPF.SeparatingVerticesFinder;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class NonSeparatingVerticesDummyGoals implements I_DummyGoalsHeuristics{
    @Override
    public HashMap<Agent, I_Location> createDummyGoalsMapping(MAPF_Instance instance, @Nullable SingleAgentGAndH heuristic) {
        HashMap<Agent, I_Location> agentLocationMap = new HashMap<>();

        if (!(instance.map instanceof I_ExplicitMap map)) {
            throw new IllegalArgumentException("Dummy goals supports only maps of type I_ExplicitMap, got: " + instance.map.getClass().getSimpleName());
        }

        // Partition locations into non-SV and SV pools
        Set<I_Location> separating = SeparatingVerticesFinder.findSeparatingVertices(map);
        List<I_Location> nonSepPool = new ArrayList<>();
        List<I_Location> sepPool    = new ArrayList<>();

        for (I_Location loc : map.getAllLocations()) {
            if (separating.contains(loc)) sepPool.add(loc);
            else nonSepPool.add(loc);
        }

        Random rnd = new Random();
        Collections.shuffle(nonSepPool, rnd);
        Collections.shuffle(sepPool, rnd);

        Iterator<I_Location> nonSepIt = nonSepPool.iterator();

        for (Agent agent : instance.agents) {
            I_Location chosen;

            // Prefer a non-separating location
            if (nonSepIt.hasNext()) {
                chosen = nonSepIt.next();
            } else {
                // Fallback: separating vertex
                if (sepPool.isEmpty()) {
                    throw new IllegalStateException(
                            "Not enough distinct locations (including separating vertices) for all agents.");
                }
                chosen = sepPool.remove(sepPool.size() - 1);
            }

            agentLocationMap.put(agent, chosen);
        }
        return agentLocationMap;
    }
}
