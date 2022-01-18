package BasicCBS.Solvers.ConstraintsAndConflicts.Constraint;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.Move;

/**
 * Identical to a vertex constraint, except it doesn't constrain any of the other agents that share the source location.
 */
public class StayAtSourceConstraint extends Constraint {

    public StayAtSourceConstraint(Agent agent, int time, I_Location prevLocation, I_Location location) {
        this(agent, time, location);
        if (prevLocation != null){
            throw new UnsupportedOperationException("Stay at source constraints are an extension of vertex constraints, so #prevLocation should be null");
        }
    }

    public StayAtSourceConstraint(int time, I_Location prevLocation, I_Location location) {
        this(time, location);
        if (prevLocation != null){
            throw new UnsupportedOperationException("Stay at source constraints are an extension of vertex constraints, so #prevLocation should be null");
        }
    }

    public StayAtSourceConstraint(Agent agent, int time, I_Location location) {
        super(agent, time, location);
    }

    public StayAtSourceConstraint(int time, I_Location location) {
        super(time, location);
    }

    public boolean acceptsWithSharedStarts(Move move){
        boolean sameSourceAgents = super.location.getCoordinate().equals(move.agent.source);
        return this.accepts(move) || sameSourceAgents;
    }

    public boolean rejectsWithSharedStarts(Move move){
        return !this.acceptsWithSharedStarts(move);
    }

}
