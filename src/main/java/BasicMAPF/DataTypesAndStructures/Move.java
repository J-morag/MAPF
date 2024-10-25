package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

/**
 * A single move for a single agent. Immutable.
 */
public class Move {
    /**
     * The {@link Agent} making the move.
     * Ignored in {@link #equals(Object)} and {@link #hashCode()}. This allows this class to be used as-is in
     * {@link java.util.Set}s for efficiently finding conflicts between different {@link Agent}'s {@link Move}s. It also
     * makes more sense when comparing two {@link Move}s made by different {@link Agent}s, as one would want to ask the
     * question "are these two agents making the same move?" when calling {@link #equals(Object)}.
     */
    public final Agent agent;
    /**
     * The time at the end of the move.
     * If an agent was at v0 at t0, and it moved to v1 at t1, then timeNow for that move equals t1.
     */
    public final int timeNow;
    /**
     * The agent's location before the move. Can equal {@link #currLocation}.
     */
    public final I_Location prevLocation;
    /**
     * The {@link #agent}'s location at the end of the move
     */
    public final I_Location currLocation;

    public boolean isStayAtSource = false;

    public Move(Agent agent, int timeNow, I_Location prevLocation, I_Location currLocation) {
        if(agent == null || timeNow<1 || prevLocation == null || currLocation == null){
            throw new IllegalArgumentException("Illegal argument in Move constructor: agent="+agent+", timeNow="+timeNow+", prevLocation="+prevLocation+", currLocation="+currLocation);
        }
        this.agent = agent;
        this.timeNow = timeNow;
        this.prevLocation = prevLocation;
        this.currLocation = currLocation;
    }

    @Override
    public String toString() {
        return this.timeNow + ":" + this.prevLocation.getCoordinate() + "->" + this.currLocation.getCoordinate();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move move)) return false;

        return timeNow == move.timeNow && prevLocation.equals(move.prevLocation) && currLocation.equals(move.currLocation);
    }

    /**
     * Ignores the {@link #agent} field.
     */
    @Override
    public int hashCode() {
        int result = timeNow;
        result = 31 * result + prevLocation.hashCode();
        result = 31 * result + currLocation.hashCode();
        return result;
    }
}
