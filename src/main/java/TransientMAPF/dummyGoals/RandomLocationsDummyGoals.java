package TransientMAPF.dummyGoals;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RandomLocationsDummyGoals implements I_DummyGoalsHeuristics{
    @Override
    public HashMap<Agent, I_Location> createDummyGoalsMapping(MAPF_Instance instance, @Nullable SingleAgentGAndH heuristic) {
        HashMap<Agent, I_Location> agentLocationMap = new HashMap<>();

        if (!(instance.map instanceof I_ExplicitMap map)) {
            throw new IllegalArgumentException("Dummy goals supports only maps of type I_ExplicitMap, got: " + instance.map.getClass().getSimpleName());
        }

        List<I_Location> allLocations = new ArrayList<>(map.getAllLocations());
        Collections.shuffle(allLocations);

        int i = 0;
        for (Agent agent : instance.agents) {
            agentLocationMap.put(agent, allLocations.get(i++));
        }

        return agentLocationMap;
    }
}
