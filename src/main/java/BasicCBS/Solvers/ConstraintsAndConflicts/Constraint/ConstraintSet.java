package BasicCBS.Solvers.ConstraintsAndConflicts.Constraint;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.Move;

import java.util.*;

/**
 * A set of {@link Constraint}s.
 * Adding and removing constraints is O(1). Checking if the set {@link #rejects(Move)} or {@link #accepts(Move)} is O(n)
 * in the worst case. However, there will usually be very few unique constraints for every pair of [time,location], with
 * many unique pairs of [time,location]. Therefore, it is on average O(1).
 */
public class ConstraintSet{

    /**
     * Basically a dictionary from [time,location] to agents who can't go there at that time, and locations from which
     * they can't go there at that time.
     */
    private final Map<I_ConstraintGroupingKey, Set<Constraint>> constraints = new HashMap<>();

    public ConstraintSet() {
    }

    public ConstraintSet(ConstraintSet toCopy){
        if(toCopy == null) {throw new IllegalArgumentException();}
        this.addAll(toCopy);
    }

    public ConstraintSet(Collection<? extends Constraint> seedConstraints) {
        if(seedConstraints == null) {throw new IllegalArgumentException();}
        this.addAll(seedConstraints);
    }

    /*  = Set Interface =  */

    //removed, because the size of constraints field isn't the number of constraints in the set. if we need this, add size field to class.
//    public int size() {
//        return constraints.size();
//    }

    public boolean isEmpty() {
        return constraints.isEmpty();
    }

    public void add(Constraint constraint){
        I_ConstraintGroupingKey dummy = createDummy(constraint);

        this.constraints.computeIfAbsent(dummy, k -> new HashSet<>());

        add(this.constraints.get(dummy), constraint);
    }

    protected void add(Set<Constraint> constraintSet, Constraint constraint){
        constraintSet.add(constraint);
    }

    public void addAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons :
                constraints) {
            this.add(cons);
        }
    }

    public void addAll(ConstraintSet other) {
        for (I_ConstraintGroupingKey cw :
                other.constraints.keySet()) {
            for (Constraint cons :
                    other.constraints.get(cw)) {
                this.add(cons);
            }
        }
    }

    public void remove(Constraint constraint){
        // using this instead of ConstraintWrapper(Constraint) because this doesn't create an unnecessary Set<Constraint>s
        // for every dummy we create.
        I_ConstraintGroupingKey dummy = createDummy(constraint);

        if(this.constraints.containsKey(dummy)){
            Set<Constraint> constraints = this.constraints.get(dummy);
            constraints.remove(constraint);
            if(constraints.isEmpty()){
                // if we've emptied the constraints, there is no more reason to keep an entry.
                this.constraints.remove(dummy);
            }
        }
    }

    /**
     *
     * @param constraints
     * @return true if this caused the set to change.
     */
    public void removeAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons :
                constraints) {
            this.remove(cons);
        }
    }

    public void clear() {
        this.constraints.clear();
    }

    /**
     *
     * @param move
     * @return the opposite of {@link #rejects(Move)}
     */
    public boolean accepts(Move move){
        return !rejects(move);
    }

    /**
     * Returns true iff any of the {@link Constraint}s that were {@link #add(Constraint) added} to this set conflict with
     * the given {@link Move}.
     * @param move a {@link Move} to check if it is rejected or not.
     * @return true iff any of the {@link Constraint}s that were {@link #add(Constraint) added} to this set
     *          conflict with the given {@link Move}.
     */
    public boolean rejects(Move move){
        I_ConstraintGroupingKey dummy = createDummy(move);
        if(!constraints.containsKey(dummy)) {return false;}
        else {
            return rejects(constraints.get(dummy), move);
        }
    }

    protected boolean rejects(Set<Constraint> constraints, Move move){
        for (Constraint constraint : constraints){
            if(constraint.rejects(move)) return true;
        }
        return false;
    }

    /**
     * Given a {@link Move} which an {@link Agent agent} makes to occupy a {@link I_Location location}
     * indefinitely starting after move's time, checks if there is a {@link Constraint} that would reject it eventually.
     *
     * In other words, we simulate this set being given an infinite number of "stay" moves after the given move.
     *
     * This method can be expensive in large sets, as it traverses all of {@link #constraints}.
     * @param finalMove a move to occupy a location indefinitely.
     * @return the first time when a constraint would eventually reject a "stay" move at the given move's location; -1 if never rejected.
     */
    public int rejectsEventually(Move finalMove){
        int firstRejectionTime = Integer.MAX_VALUE;
        // traverses the entire data structure. expensive.
        for (I_ConstraintGroupingKey cw :
                constraints.keySet()) {
            //found constraint for this location, sometime in the future. Should be rare.
            if(cw.relevantInTheFuture(finalMove)){
                for (Constraint constraint :
                        constraints.get(cw)) {
                    // make an artificial "stay" move for the relevant time.
                    // In practice, this should happen very rarely, so not very expensive.
                    int constraintTime = ((TimeLocation)cw).time;
                    if(constraint.rejects(new Move(finalMove.agent, constraintTime, finalMove.currLocation, finalMove.currLocation))
                            && constraintTime < firstRejectionTime){
                        firstRejectionTime = constraintTime;
                    }
                }
            }
        }

        return firstRejectionTime == Integer.MAX_VALUE ? -1 : firstRejectionTime;
    }

    /**
     * The opposite of {@link #rejectsEventually(Move)}.
     * @param finalMove a move to occupy a location indefinitely.
     * @return true if no constraint would eventually reject a "stay" move at the given move's location.
     */
    public boolean acceptsForever(Move finalMove){
        return rejectsEventually(finalMove) == -1;
    }

    /**
     * Returns true iff any of the {@link Constraint}s that were {@link #add(Constraint) added} to this set conflict with
     * the given {@link Move}.
     *
     * Doesn't assume that the last move means stay at goal forever.
     * @see #acceptsForever(Move)
     * @param moves a {@link Collection} of {@link Move}s to check if the are ejected or not.
     * @return true iff all of the given {@link Move}s conflict with any of the {@link Constraint}s that were
     *          {@link #add(Constraint) added} to this set.
     */
    public boolean rejectsAll(Collection<? extends Move> moves){
        boolean result = true;
        for (Move move :
                moves) {
            result &= this.rejects(move);
        }
        return result;
    }

    /**
     * Returns true iff none of the {@link Constraint}s that were {@link #add(Constraint) added} to this set conflict with
     * the given {@link Move}.
     *
     * Doesn't assume that the last move means stay at goal forever.
     * @see #acceptsForever(Move)
     * @param moves
     * @return the opposite of {@link #rejectsAll(Collection)}.
     */
    public boolean acceptsAll(Collection<? extends Move> moves){
        boolean result = true;
        for (Move move :
                moves) {
            result &= this.accepts(move);
        }
        return result;
    }

    protected I_ConstraintGroupingKey createDummy(Constraint constraint) {
        return new TimeLocation(constraint);
    }

    protected I_ConstraintGroupingKey createDummy(Move move) {
        return new TimeLocation(move);
    }

    /**
     * Removes constraints for times that are not in the given range.
     * @param minTime the minimum time (inclusive).
     * @param maxTime the maximum time (exclusive).
     */
    public void trimToTimeRange(int minTime, int maxTime){
        this.constraints.keySet().removeIf(cw -> ((TimeLocation)cw).time < minTime || ((TimeLocation)cw).time >= maxTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstraintSet)) return false;

        ConstraintSet that = (ConstraintSet) o;

        return constraints.equals(that.constraints);

    }

    @Override
    public int hashCode() {
        return constraints.hashCode();
    }

}
