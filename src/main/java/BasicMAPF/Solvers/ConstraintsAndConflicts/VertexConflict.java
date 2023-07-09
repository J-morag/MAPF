package BasicMAPF.Solvers.ConstraintsAndConflicts;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.DataTypesAndStructures.Move;

import java.util.Objects;

/**
 * Represents a conflict between 2 {@link Agent}s, at a certain time, in a certain {@link I_Location location}.
 * This is known as a Vertex Conflict.
 */
public class VertexConflict extends A_Conflict {


    public VertexConflict(Agent agent1, Agent agent2, int time, I_Location location) {
        super(agent1, agent2, time, location);
    }


    public VertexConflict(Agent agent1, Agent agent2, TimeLocation timeLocation){
        super(agent1,agent2,timeLocation.time,timeLocation.location);
    }

    /**
     * returns an array of {@link Constraint}, each of which could prevent this conflict.
     * @return an array of {@link Constraint}, each of which could prevent this conflict.
     */
    public Constraint[] getPreventingConstraints(){
        return new Constraint[]{
                new Constraint(agent1,time, location),
                new Constraint(agent2,time, location)};
    }

    /**
     * assumes both moves have the same {@link Move#timeNow}.
     * @return true if these moves have a vertex conflict.
     */
    public static boolean haveConflicts(Move move1, Move move2){
        return move1.currLocation.equals(move2.currLocation);
    }

    /**
     * assumes both moves have the same {@link Move#timeNow}.
     * @return a vertex conflict between the agents, if the have one. else returns null
     */
    public static A_Conflict conflictBetween(Move move1, Move move2){
        if(VertexConflict.haveConflicts(move1, move2)){
            return new VertexConflict(move1.agent, move2.agent, move1.timeNow, move1.currLocation);
        }
        else return null;
    }


    /**
     * Override A_Conflict equals and hashcode, because we don't differ between:
     *     1. < agent1, agent2 >
     *     2. < agent2, agent1 >
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof A_Conflict)) return false;
        A_Conflict conflict = (A_Conflict) o;
        return time == conflict.time &&
                ((  Objects.equals(agent1, conflict.agent1) && Objects.equals(agent2, conflict.agent2)) ||
                (   Objects.equals(agent1, conflict.agent2) && Objects.equals(agent2, conflict.agent1))  ) &&
                    Objects.equals(location, conflict.location);
    }

    @Override
    public int hashCode() {
        int result = agent1.hashCode() + agent2.hashCode();
        result = 31 * result + time;
        result = 31 * result + location.hashCode();
        return result;
    }

}
