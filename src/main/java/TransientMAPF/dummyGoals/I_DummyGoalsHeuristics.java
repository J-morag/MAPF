package TransientMAPF.dummyGoals;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;


public interface I_DummyGoalsHeuristics {
    HashMap<Agent, I_Location> createDummyGoalsMapping(MAPF_Instance instance, @Nullable SingleAgentGAndH heuristic);
}
