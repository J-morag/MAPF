package OnlineMAPF;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictSelectionStrategy;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.CorridorConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.ConstraintsAndConflicts.CorridorConflict;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.HashSet;

public class OnlineCorridorConflictManager extends CorridorConflictManager {

    public OnlineCorridorConflictManager(ConflictSelectionStrategy conflictSelectionStrategy, ConstraintSet constraints, MAPF_Instance instance) {
        super(conflictSelectionStrategy, constraints, instance);
        super.timeLocationTables.ignoreGoals = true;
    }

    public OnlineCorridorConflictManager(ConstraintSet constraints, MAPF_Instance instance) {
        super(constraints, instance);
        super.timeLocationTables.ignoreGoals = true;
    }

    public OnlineCorridorConflictManager(ConflictManager other, ConstraintSet constraints, MAPF_Instance instance) {
        super(other, constraints, instance);
        super.timeLocationTables.ignoreGoals = true;
    }


    @Override
    protected void checkAddVertexConflictsWithGoal(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan) {
        // do nothing
    }

    @Override
    protected I_Location addLastVertexOneDirection(HashSet<I_Location> corridorVertices, I_Location lastNeighbor, I_Location prevLocation) {
        // we don't want to consider private garages as a part of corridors
        if(lastNeighbor instanceof PrivateGarage){
            return prevLocation;
        }
        else {
            return super.addLastVertexOneDirection(corridorVertices, lastNeighbor, prevLocation);
        }
    }

    @Override
    protected CorridorConflict getCorridorConflict(A_Conflict conflict, HashSet<I_Location> corridorVertices, I_Location beginning, I_Location end) {
        return new CorridorConflict(conflict.agent1, conflict.agent2, conflict.time, beginning, end, corridorVertices,
                constraints, instance, this.agent_plan.get(conflict.agent1), this.agent_plan.get(conflict.agent2),
                ((OnlineAgent)conflict.agent1).arrivalTime, ((OnlineAgent)conflict.agent2).arrivalTime);
    }
}
