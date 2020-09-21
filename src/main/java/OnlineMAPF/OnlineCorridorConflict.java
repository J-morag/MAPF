package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.ConstraintsAndConflicts.CorridorConflict;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.HashSet;

public class OnlineCorridorConflict extends CorridorConflict {
    public OnlineCorridorConflict(Agent agent1, Agent agent2, int time, I_Location begin, I_Location end, HashSet<I_Location> corridorVertices, ConstraintSet constraints, MAPF_Instance instance, SingleAgentPlan agent1CurrentPlan, SingleAgentPlan agent2CurrentPlan, int agent1StartTime, int agent2StartTime) {
        super(agent1, agent2, time, begin, end, corridorVertices, constraints, instance, agent1CurrentPlan, agent2CurrentPlan, agent1StartTime, agent2StartTime);
    }

    @Override
    protected I_Location getAgentSource(Agent agent, MAPF_Instance instance) {
        return ((OnlineAgent)agent).getPrivateGarage(instance.map.getMapCell(agent.source));
    }
}
