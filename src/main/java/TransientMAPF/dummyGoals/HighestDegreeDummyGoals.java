package TransientMAPF.dummyGoals;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class HighestDegreeDummyGoals implements I_DummyGoalsHeuristics{
    @Override
    public HashMap<Agent, I_Location> createDummyGoalsMapping(MAPF_Instance instance, @Nullable SingleAgentGAndH heuristic) {
        HashMap<Agent, I_Location> agentLocationMap = new HashMap<>();
        if (!(instance.map instanceof I_ExplicitMap map)) {
            throw new IllegalArgumentException("Dummy goals supports only maps of type I_ExplicitMap, got: " + instance.map.getClass().getSimpleName());
        }

        ArrayList<I_Location> allLocations = new ArrayList<>(map.getAllLocations());

        // Group locations by degree directly
        TreeMap<Integer, List<I_Location>> degreeBuckets = new TreeMap<>(Collections.reverseOrder());

        for (I_Location location : allLocations) {
            int degree = location.outgoingEdges().size();
            degreeBuckets.computeIfAbsent(degree, k -> new ArrayList<>()).add(location);
        }

        // Collect candidates until we have enough
        List<I_Location> candidates = new ArrayList<>();
        for (List<I_Location> group : degreeBuckets.values()) {
            candidates.addAll(group);
            if (candidates.size() >= instance.agents.size()) {
                break;
            }
        }

        // Shuffle and assign
        Collections.shuffle(candidates);
        int i = 0;
        for (Agent agent : instance.agents) {
            agentLocationMap.put(agent, candidates.get(i++));
        }

        return agentLocationMap;
    }
}
