package TransientMAPF.dummyGoals;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class InitialLocationsDummyGoals implements I_DummyGoalsHeuristics{
    @Override
    public HashMap<Agent, I_Location> createDummyGoalsMapping(MAPF_Instance instance, @Nullable SingleAgentGAndH heuristic) {
        HashMap<Agent, I_Location> agentLocationMap = new HashMap<>();

        for (Agent agent : instance.agents) {
            agentLocationMap.put(agent, instance.map.getMapLocation(agent.source));
        }

        return agentLocationMap;
    }
}
