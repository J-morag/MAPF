package BasicCBS.Solvers.ConstraintsAndConflicts;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.Move;

import java.util.Objects;

/**
 * Represents a conflict between 2 {@link Agent}s which are swapping their {@link I_Location locations} at a certain time.
 * This is known as s Swapping Conflict or an Edge Conflict.
 *
 * The order of agents is unimportant, but the destinations must correctly correspond to their agents - {@link #location}
 * for {@link #agent1}'s destination, and {@link #agent2_destination} for {@link #agent2}'s destination.
 * An equivalent conflict would have both agents and destinations reversed.
 */
public class SwappingConflict extends A_Conflict{
    /**
     * In this class, agent2_destination will represent the second agent's destination, which is the same as the first
     * agent's previous location. The first agent's destination will be represented by the super class's
     * {@link #location} field.
     */
    public final I_Location agent2_destination;

    public SwappingConflict(Agent agent1, Agent agent2, int time, I_Location agent1_destination, I_Location agent2_destination) {
        super(agent1, agent2, time, agent1_destination);
        this.agent2_destination = agent2_destination;
    }


    /**
     * returns an array of {@link Constraint}, each of which could prevent this conflict.
     * @return an array of {@link Constraint}, each of which could prevent this conflict.
     */
    @Override
    public Constraint[] getPreventingConstraints() {
        return new Constraint[]{
                /*
                 the order of locations:
                 agent1 will be prevented from moving from its previous location (agent2's destination) to its destination.
                 */
                new Constraint(agent1, time, agent2_destination, location),
                /*
                 the order of locations:
                 agent2 will be prevented from moving from its previous location (agent1's destination) to its destination.
                 */
                new Constraint(agent2, time, location, agent2_destination)};
    }

    /**
     * assumes both moves have the same {@link Move#timeNow}.
     * @return true if these moves have a swapping conflict.
     */
    public static boolean haveConflicts(Move move1, Move move2){
        return move1.prevLocation.equals(move2.currLocation)
                && move2.prevLocation.equals(move1.currLocation);
    }

    /**
     * assumes both moves have the same {@link Move#timeNow}.
     * @return true if these moves have a swapping conflict.
     */
    public static A_Conflict conflictBetween(Move move1, Move move2){
        if(SwappingConflict.haveConflicts(move1, move2)){
            return new SwappingConflict(move1.agent, move2.agent, move1.timeNow, move1.currLocation, move2.currLocation);
        }
        else{
            return null;
        }
    }

    /**
     * Two {@link SwappingConflict}s are equal if all of their fields are equal, or if they are a mirror image, meaning
     * both their agent order and destination order are reversed.
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SwappingConflict)) return false;
        SwappingConflict that = (SwappingConflict) o;
        return time == that.time &&
                ( // all equals
                    Objects.equals(agent1, that.agent1) &&
                    Objects.equals(agent2, that.agent2) &&
                    Objects.equals(location, that.location) &&
                    Objects.equals(agent2_destination, that.agent2_destination)
                )
                ||
                ( // mirror image
                    Objects.equals(agent1, that.agent2) &&
                    Objects.equals(agent2, that.agent1) &&
                    Objects.equals(location, that.agent2_destination) &&
                    Objects.equals(agent2_destination, that.location)
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, (Objects.hash(agent2, agent2_destination) + Objects.hash(agent1, location)) );
    }
}
