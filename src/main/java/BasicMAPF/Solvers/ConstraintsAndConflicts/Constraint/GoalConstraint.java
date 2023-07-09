package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.DataTypesAndStructures.Move;

/**
 * Like a vertex constraint, only it constrains indefinitely (as if the agent is sitting at its goal)
 */
public class GoalConstraint extends Constraint{
    public GoalConstraint(Agent agent, int time, I_Location prevLocation, I_Location location) {
        this(agent, time, location);
        if (prevLocation != null){
            throw new UnsupportedOperationException("Goal constraints are an extension of vertex constraints, so #prevLocation should be null");
        }
    }

    public GoalConstraint(int time, I_Location prevLocation, I_Location location) {
        this(time, location);
        if (prevLocation != null){
            throw new UnsupportedOperationException("Goal constraints are an extension of vertex constraints, so #prevLocation should be null");
        }
    }

    public GoalConstraint(Agent agent, int time, I_Location location) {
        super(agent, time, location);
    }

    public GoalConstraint(int time, I_Location location) {
        super(time, location);
    }

    @Override
    public boolean accepts(Move move) {
        // add locking this location indefinitely
        return super.accepts(move) &&
                !(this.location.equals(move.currLocation) && this.time <= move.timeNow);
    }

    public boolean acceptsWithSharedGoals(Move move){
        boolean cannotConflictAtGoal = super.location.getCoordinate().equals(move.agent.target);
        return this.accepts(move) || cannotConflictAtGoal;
    }

    public boolean rejectsWithSharedGoals(Move move){
        return !this.acceptsWithSharedGoals(move);
    }
}
