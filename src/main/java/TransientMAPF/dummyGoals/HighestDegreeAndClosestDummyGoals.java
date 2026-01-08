package TransientMAPF.dummyGoals;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

public class HighestDegreeAndClosestDummyGoals implements I_DummyGoalsHeuristics{
    @Override
    public HashMap<Agent, I_Location> createDummyGoalsMapping(MAPF_Instance instance, @Nullable SingleAgentGAndH heuristic) {
        HashMap<Agent, I_Location> agentLocationMap = new HashMap<>();

        if (!(instance.map instanceof I_ExplicitMap map)) {
            throw new IllegalArgumentException("Dummy goals supports only maps of type I_ExplicitMap, got: " + instance.map.getClass().getSimpleName());
        }

        if (heuristic == null) {
            heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, instance.map);
        }

        ArrayList<I_Location> allLocations = new ArrayList<>(map.getAllLocations());

        // Group locations by degree directly
        TreeMap<Integer, List<I_Location>> degreeBuckets = new TreeMap<>(Collections.reverseOrder());

        for (I_Location location : allLocations) {
            int degree = location.outgoingEdges().size();
            degreeBuckets.computeIfAbsent(degree, k -> new ArrayList<>()).add(location);
        }

        List<I_Location> candidates = new ArrayList<>();
        for (List<I_Location> group : degreeBuckets.values()) {
            candidates.addAll(group);
            if (candidates.size() >= instance.agents.size()) {
                break;
            }
        }

        Set<I_Location> used = new HashSet<>();
        for (Agent agent : instance.agents) {
            I_Location agentsTarget = map.getMapLocation(agent.target);
            I_Location best = null;
            int bestDist = Integer.MAX_VALUE;

            for (I_Location candidate : candidates) {
                if (used.contains(candidate)) continue;
                int dist = heuristic.getHToTargetFromLocation(agentsTarget.getCoordinate(), candidate);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = candidate;
                }
            }

            if (best == null) {
                throw new RuntimeException("No reachable dummy goal for agent " + agent);
            }

            agentLocationMap.put(agent, best);
            used.add(best);
        }

        return agentLocationMap;
    }
}
