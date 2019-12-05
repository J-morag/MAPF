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
    private final Map<ConstraintWrapper, ConstraintWrapper> constraints = new HashMap<>();

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

    /**
     *
     * @param constraint
     * @return true if this caused the set to change.
     */
    public boolean add(Constraint constraint){
        // using this instead of ConstraintWrapper(Constraint) because this doesn't create an unnecessary Set<Constraint>s
        // for every dummy we create.
        ConstraintWrapper dummy = new ConstraintWrapper(constraint.location, constraint.time);

        if(!this.constraints.containsKey(dummy)){
            this.constraints.put(dummy, dummy);
        }

        return this.constraints.get(dummy).add(constraint);
    }

    /**
     *
     * @param constraints
     * @return true if this caused the set to change.
     */
    public boolean addAll(Collection<? extends Constraint> constraints) {
        boolean changed = false;
        for (Constraint cons :
                constraints) {
            changed |= this.add(cons);
        }
        return changed;
    }

    /**
     *
     * @param other a constraint set whose constraints we would like to copy.
     * @return true if this caused the set to change.
     */
    public boolean addAll(ConstraintSet other) {
        boolean changed = false;
        for (ConstraintWrapper cw :
                other.constraints.keySet()) {
            for (Constraint cons :
                    cw.relevantConstraints) {
                changed |= this.add(cons);
            }
        }
        return changed;
    }

    /**
     *
     * @param constraint
     * @return true if this caused the set to change.
     */
    public boolean remove(Constraint constraint){
        // using this instead of ConstraintWrapper(Constraint) because this doesn't create an unnecessary Set<Constraint>s
        // for every dummy we create.
        ConstraintWrapper dummy = new ConstraintWrapper(constraint.location, constraint.time);

        if(!this.constraints.containsKey(dummy)){
            return false;
        }
        else{
            ConstraintWrapper constraintWrapper = this.constraints.get(dummy);
            boolean changed = constraintWrapper.remove(constraint);
            if(constraintWrapper.isEmpty()){
                // if we've emptied the constraint wrapper, there is no more reason to keep it.
                this.constraints.remove(constraintWrapper);
            }
            return changed;
        }
    }

    /**
     *
     * @param constraints
     * @return true if this caused the set to change.
     */
    public boolean removeAll(Collection<? extends Constraint> constraints) {
        boolean changed = false;
        for (Constraint cons :
                constraints) {
            changed |= this.remove(cons);
        }
        return changed;
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
        ConstraintWrapper dummy = new ConstraintWrapper(move.currLocation, move.timeNow);
        if(!constraints.containsKey(dummy)) {return false;}
        else {
            return constraints.get(dummy).rejects(move);
        }
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
        for (ConstraintWrapper cw :
                constraints.keySet()) {
            //found constraint for this location, sometime in the future. Should be rare.
            if(cw.time > finalMove.timeNow && cw.location.equals(finalMove.currLocation)){
                for (Constraint constraint :
                        cw.relevantConstraints) {
                    // make an artificial "stay" move for the relevant time.
                    // In practice, this should happen very rarely, so not very expensive.
                    if(constraint.rejects(new Move(finalMove.agent, cw.time, finalMove.currLocation, finalMove.currLocation))
                            && cw.time < firstRejectionTime){
                        firstRejectionTime = cw.time;
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

    /**
     * Removes constraints for times that are not in the given range.
     * @param minTime the minimum time (inclusive).
     * @param maxTime the maximum time (exclusive).
     */
    public void trimToTimeRange(int minTime, int maxTime){
        this.constraints.keySet().removeIf(constraintWrapper -> constraintWrapper.time < minTime || constraintWrapper.time >= maxTime);
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

    /**
     * replaces the constraint with a simple wrapper that is quick to find in a set.
     */
    private static class ConstraintWrapper{
        private I_Location location;
        private int time;
        private Set<Constraint> relevantConstraints;

        public ConstraintWrapper(I_Location location, int time) {
            this.location = location;
            this.time = time;
        }

        public ConstraintWrapper(Constraint constraint) {
            this(constraint.location, constraint.time);
            this.add(constraint);
        }

        public ConstraintWrapper(ConstraintWrapper toCopy){
            this.location = toCopy.location;
            this.time = toCopy.time;
            this.relevantConstraints = toCopy.relevantConstraints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConstraintWrapper that = (ConstraintWrapper) o;

            if (time != that.time) return false;
            return location.equals(that.location);

        }

        @Override
        public int hashCode() {
            int result = location.hashCode();
            result = 31 * result + time;
            return result;
        }

        /**
         *
         * @param constraint
         * @return true if this caused a change in the wrapper.
         */
        public boolean remove(Constraint constraint){
            if(constraint == null || this.relevantConstraints == null || !this.relevantConstraints.contains(constraint)){
                return false;
            }
            else{
                return this.relevantConstraints.remove(constraint);
            }
        }

        /**
         *
         * @param constraint a {@link Constraint} with the same time and location as this wrapper.
         * @return true if this caused a change in the wrapper.
         */
        public boolean add(Constraint constraint){
            if(constraint.time != this.time || constraint.location != this.location){return false;}
            if (this.relevantConstraints == null) {
                this.relevantConstraints = new HashSet<>();
            }
            return this.relevantConstraints.add(constraint);
        }

        public boolean rejects(Move move){
            for (Constraint constraint : this.relevantConstraints){
                if(constraint.rejects(move)) return true;
            }
            return false;
        }

        public boolean accepts(Move move){
            return !this.rejects(move);
        }

        public boolean isEmpty(){
            return this.relevantConstraints == null || this.relevantConstraints.isEmpty();
        }
    }
}
