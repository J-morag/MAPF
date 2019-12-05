package BasicCBS.Solvers.ConstraintsAndConflicts;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.Move;

import java.util.Objects;

public abstract class A_Conflict {
    public final Agent agent1;
    public final Agent agent2;
    public final int time;
    public final I_Location location;

    public A_Conflict(Agent agent1, Agent agent2, int time, I_Location location) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.time = time;
        this.location = location;
    }

    /**
     * @return an array of constraints, each of which could prevent this conflict
     */
    public abstract Constraint[] getPreventingConstraints();

    /**
     * Checks that both moves have the same time.
     * @param move1 @NotNull
     * @param move2 @NotNull
     * @return true if these moves have a vertex conflict or a swapping conflict.
     */
    public static boolean haveConflicts(Move move1, Move move2){
        if(move1 == null || move2 == null){throw new IllegalArgumentException("can't compare null moves");}

        return move1.timeNow == move2.timeNow
                && (SwappingConflict.haveConflicts(move1, move2) || VertexConflict.haveConflicts(move1, move2));
    }

    /**
     * Returns a conflict between the moves if there is one, else it returns null.
     * @param move1 @NotNull
     * @param move2 @NotNull
     * @return a conflict between the moves if there is one, else it returns null.
     */
    public static A_Conflict conflictBetween(Move move1, Move move2){
        if(move1 == null || move2 == null){throw new IllegalArgumentException("can't compare null moves");}
        if(move1.timeNow == move2.timeNow){
            A_Conflict conflict = VertexConflict.conflictBetween(move1, move2);
            if(conflict != null) return conflict;
            else {
                conflict = SwappingConflict.conflictBetween(move1, move2);
                return conflict;
            }
        }
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof A_Conflict)) return false;
        A_Conflict conflict = (A_Conflict) o;
        return  time == conflict.time &&
                agent1.equals(conflict.agent1) &&
                agent2.equals(conflict.agent2) &&
                location.equals(conflict.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agent1, agent2, time, location);
    }
}
