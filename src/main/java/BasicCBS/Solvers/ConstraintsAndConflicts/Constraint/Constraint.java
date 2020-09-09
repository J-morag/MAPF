package BasicCBS.Solvers.ConstraintsAndConflicts.Constraint;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.Move;

/**
 * A constraint on a {@link I_Location location}, at a specific time. It may or may not apply to all agents.
 * This class is useful for preventing a vertex conflict by setting {@link #prevLocation} to null.
 * This class is useful for preventing a swapping conflict by setting {@link #prevLocation} to a valid {@link I_Location}.
 */
public class Constraint {
    /**
     * The only agent this constraint applies to. If this is null, this constraint applies to all agents.
     */
    public final Agent agent;
    /**
     * The time the constraint applies to.
     */
    public final int time;
    /**
     * The location from which the constraint forbids entry to {@link #location}. @Nullable
     * This is meant to prevent swapping conflicts. If it is null, then this constraint prevents vertex conflicts.
     */
    public final I_Location prevLocation;
    /**
     * The location the constraint applies to.
     */
    public final I_Location location;


    /**
     * @param agent the specific agent that this constraint applies to. If null, the constraint applies to all agents.
     * @param time the time the constraint applies to.
     * @param prevLocation the location from which the constraint forbids entry to {@link #location}. @Nullable
     * @param location the location the constraint applies to.
     */
    public Constraint(Agent agent, int time, I_Location prevLocation, I_Location location) {
        if(time<0 || location == null) throw new IllegalArgumentException();
        this.agent = agent;
        this.time = time;
        this.prevLocation = prevLocation;
        this.location = location;
    }

    /**
     * Constructor. The constraint will prevent swapping conflicts and apply to all agents.
     * @param time the time the constraint applies to.
     * @param prevLocation the location from which the constraint forbids entry to {@link #location}. @Nullable
     * @param location the location the constraint applies to.
     */
    public Constraint(int time, I_Location prevLocation, I_Location location) {
        this(null, time, prevLocation, location);
    }

    /**
     * Constructor. The constraint will prevent vertex conflicts.
     * @param agent the specific agent that this constraint applies to. If null, the constraint applies to all agents.
     * @param time the time the constraint applies to.
     * @param location the location the constraint applies to.
     */
    public Constraint(Agent agent, int time, I_Location location) {
        this(agent, time,null, location);
    }

    /**
     * Constructor. The constraint will prevent vertex conflicts and will apply to all agents.
     * @param time the time the constraint applies to.
     * @param location the location the constraint applies to.
     */
    public Constraint(int time, I_Location location) {
        this(null, time,null, location);
    }

    /**
     * Returns true iff the given {@link Move} conflicts with this constraint. Checking if a move if accepted is faster,
     * than checking if it is rejected, since a single difference is enough to resolve the check. Therefore, this method
     * is implemented in full, whereas {@link #rejects(Move)} uses this method for its implementation.
     * @param move a move that might conflict with this constraint
     * @return true iff the given {@link Move} conflicts with this constraint.
     */
    public boolean accepts(Move move){
        if(move == null) throw new IllegalArgumentException();
        return this.location != move.currLocation || this.time != move.timeNow
                /*the constraint is limited to a specific agent, and that agent is different*/
                || (this.agent != null && !this.agent.equals(move.agent) )
                /*the previous location is not null, and different*/
                || (this.prevLocation != null &&  !move.prevLocation.equals(this.prevLocation) );
    }

    /**
     * Returns false iff the given {@link Move} conflicts with this constraint. This returns the opposite of
     * {@link #accepts(Move)}, and is only present for convenience.
     * @param move a move that might conflict with this constraint
     * @return false iff the given {@link Move} conflicts with this constraint.
     */
    public boolean rejects(Move move){
        return !this.accepts(move);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Constraint that = (Constraint) o;

        if (time != that.time) return false;
        if (agent != null ? !agent.equals(that.agent) : that.agent != null) return false;
        if (prevLocation != null ? !prevLocation.equals(that.prevLocation) : that.prevLocation != null) return false;
        return location.equals(that.location);

    }

    @Override
    public int hashCode() {
        int result = agent != null ? agent.hashCode() : 0;
        result = 31 * result + time;
        result = 31 * result + (prevLocation != null ? prevLocation.hashCode() : 0);
        result = 31 * result + location.hashCode();
        return result;
    }
}

