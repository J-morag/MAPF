package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CongestionMap {
    private Map<I_Location, Set<Agent>> agentsAtLocations = new HashMap<>();

    public final double congestionMultiplier;

    public CongestionMap(@Nullable  Iterable<SingleAgentPlan> plans, @Nullable Double congestionMultiplier) {
        this.congestionMultiplier = Objects.requireNonNullElse(congestionMultiplier, 1.0);
        if (plans != null){
            this.registerPlans(plans);
        }
    }

    public void registerMove(Move move){
        Set<Agent> agents = agentsAtLocations.computeIfAbsent(move.currLocation, l -> new HashSet<>());
        agents.add(move.agent);
    }

    public void registerPlan(SingleAgentPlan plan){
        for (Move move:
             plan) {
            registerMove(move);
        }
    }

    public void registerPlans(Iterable<SingleAgentPlan> plans){
        for (SingleAgentPlan plan :
                plans) {
            registerPlan(plan);
        }
    }

    public int congestionAt(I_Location loc){
        Set<Agent> agentsAtLoc = agentsAtLocations.get(loc);
        return agentsAtLoc != null ? agentsAtLoc.size() : 0;
    }
}
