package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.DataTypesAndStructures.Move;
import org.jetbrains.annotations.NotNull;

/**
 * Like a vertex constraint, only it constrains indefinitely (as if the agent is sitting at its goal)
 */
public class GoalConstraint extends Constraint{
    public final Agent responsibleAgent;
    public GoalConstraint(Agent agent, int time, I_Location prevLocation, I_Location location, @NotNull Agent responsibleAgent) {
        this(agent, time, location, responsibleAgent);
        if (prevLocation != null){
            throw new UnsupportedOperationException("Goal constraints are an extension of vertex constraints, so #prevLocation should be null");
        }
        if (responsibleAgent.equals(agent)){
            throw new UnsupportedOperationException("responsibleAgent should be different from agent");
        }
    }

    public GoalConstraint(Agent agent, int time, I_Location location, @NotNull Agent responsibleAgent) {
        super(agent, time, location);
        this.responsibleAgent = responsibleAgent;
    }

    public GoalConstraint(int time, I_Location location, @NotNull Agent responsibleAgent) {
        super(time, location);
        this.responsibleAgent = responsibleAgent;
    }

    @Override
    public boolean accepts(Move move) {
        // add locking this location indefinitely
        return super.accepts(move) &&
                !(this.location.equals(move.currLocation) && this.time <= move.timeNow);
    }

    public boolean acceptsWithSharedGoals(Move move){
        boolean cannotConflictAtGoal = super.location.getCoordinate().equals(move.agent.target) && responsibleAgent.target.equals(move.agent.target);
        return this.accepts(move) || cannotConflictAtGoal;
    }

    public boolean rejectsWithSharedGoals(Move move){
        return !this.acceptsWithSharedGoals(move);
    }
}
