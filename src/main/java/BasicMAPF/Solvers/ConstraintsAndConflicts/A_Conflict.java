package BasicMAPF.Solvers.ConstraintsAndConflicts;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.DataTypesAndStructures.Move;
import Environment.Config;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Checks if the target move has a conflict with the given move. Specifically excludes a vertex conflict (where both moves have the same time).
     * @param lastMove @NotNull - a last move of staying at the target location.
     * @param move @NotNull - a move to check for a conflict with.
     * @return true if the last move has a target (goal) conflict with the given move.
     */
    public static boolean lastMoveHasTargetConflictWith(@NotNull Move lastMove, @NotNull Move move){
        if (Config.WARNING >= 1 && lastMove.timeNow == move.timeNow){
            System.err.println("Suspicious call to A_Conflict.hasTargetConflict with lastMove.timeNow == move.timeNow. Only checks for target conflicts. lastMove: " + lastMove + ", move: " + move);
        }
        return lastMove.timeNow < move.timeNow
                && lastMove.currLocation.equals(move.currLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof A_Conflict)) return false;

        A_Conflict that = (A_Conflict) o;

        if (time != that.time) return false;
        if (!agent1.equals(that.agent1)) return false;
        if (!agent2.equals(that.agent2)) return false;
        return location.equals(that.location);
    }

    @Override
    public int hashCode() {
        int result = agent1.hashCode();
        result = 31 * result + agent2.hashCode();
        result = 31 * result + time;
        result = 31 * result + location.hashCode();
        return result;
    }
}
