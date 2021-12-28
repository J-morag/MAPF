package BasicCBS.Solvers.ConstraintsAndConflicts.Constraint;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.Move;

public class GoalConstraint extends Constraint{
    public GoalConstraint(Agent agent, int time, I_Location prevLocation, I_Location location) {
        super(agent, time, prevLocation, location);
    }

    public GoalConstraint(int time, I_Location prevLocation, I_Location location) {
        super(time, prevLocation, location);
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
        boolean canConflictAtGoal = !super.location.getCoordinate().equals(move.agent.target);
        return this.accepts(move) || !canConflictAtGoal;
    }

    public boolean rejectsWithSharedGoals(Move move){
        return !this.acceptsWithSharedGoals(move);
    }
}
