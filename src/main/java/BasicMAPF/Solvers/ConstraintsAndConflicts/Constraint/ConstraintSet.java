package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;
import org.jetbrains.annotations.NotNull;

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
    protected final Map<I_ConstraintGroupingKey, Set<Constraint>> constraints = new HashMap<>();

    /**
     * Goal constraints. Locations in this collection are reserved starting from the constraint's time, indefinitely.
     */
    protected final Map<I_Location, GoalConstraint> goalConstraints = new HashMap<>();

    protected int lastConstraintTime = -1;

    /**
     * If set to true, agents who share the same goal may occupy their goal vertex at the same time.
     */
    public boolean sharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not constrain agents with the same source
     */
    public boolean sharedSources;

    public ConstraintSet() {
        this(null, null);
    }

    public ConstraintSet(Boolean sharedGoals, Boolean sharedSources) {
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
    }

    public ConstraintSet(ConstraintSet toCopy){
        this(toCopy, toCopy.lastConstraintTime);
    }

    public ConstraintSet(Collection<? extends Constraint> seedConstraints) {
        this();
        if(seedConstraints == null) {throw new IllegalArgumentException();}
        this.addAll(seedConstraints);
    }

    public ConstraintSet(@NotNull ConstraintSet toCopy, int upToAndIncludingTime) {
        this.sharedGoals = toCopy.sharedGoals;
        this.sharedSources = toCopy.sharedSources;
        this.addAll(toCopy, upToAndIncludingTime);
        this.lastConstraintTime = Math.min(upToAndIncludingTime, toCopy.lastConstraintTime);
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
     * @return the time of the last constraint. If it is a goal constraint (infinite), return the time when it starts
     */
    public int getLastConstraintTime(){
        return this.lastConstraintTime;
    }

    public void add(Constraint constraint){
        if(constraint instanceof  RangeConstraint){
            // add an individual constraint for each of the times covered by the range constraint
            RangeConstraint rangeConstraint = (RangeConstraint) constraint;

            for (int time = rangeConstraint.lowerBound; time <= rangeConstraint.upperBound; time++) {
                this.add(rangeConstraint.getConstraint(time));
            }
            this.lastConstraintTime = Math.max(this.lastConstraintTime, rangeConstraint.upperBound);
        }
        else if (constraint instanceof GoalConstraint){
            // can only have existing goal constraint if this.sharedGoal == true
            GoalConstraint existingGoalConstraintAtLocation = this.goalConstraints.get(constraint.location);
            if (existingGoalConstraintAtLocation == null || existingGoalConstraintAtLocation.time > constraint.time){
                this.goalConstraints.put(constraint.location, (GoalConstraint) constraint);
            }
            this.lastConstraintTime = Math.max(this.lastConstraintTime, constraint.time);
        }
        else{ // regular constraint
            I_ConstraintGroupingKey dummy = createDummy(constraint);
            this.constraints.computeIfAbsent(dummy, k -> new HashSet<>());
            add(this.constraints.get(dummy), constraint);
            this.lastConstraintTime = Math.max(this.lastConstraintTime, constraint.time);
        }

    }

    protected void add(Set<Constraint> constraintSet, Constraint constraint){
        constraintSet.add(constraint);
    }

    public void addAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons : constraints) {
            this.add(cons);
        }
    }

    public void addAll(ConstraintSet other) {
        addAll(other, Integer.MAX_VALUE);
    }

    private void addAll(ConstraintSet other, int upToTime) {
        for (I_ConstraintGroupingKey cw : other.constraints.keySet()) {
            for (Constraint cons : other.constraints.get(cw)) {
                if (cons.time <= upToTime) {
                    this.add(cons);
                }
            }
        }
        for (I_Location loc : other.goalConstraints.keySet()){
            if (other.goalConstraints.get(loc).time <= upToTime){
                this.add(other.goalConstraints.get(loc));
            }
        }
    }

    public void remove(Constraint constraint){
        if(constraint instanceof  RangeConstraint){
            // there is a problem with removing range constraints, since they may overlap.
            throw new UnsupportedOperationException();
        }
        else if (constraint instanceof  GoalConstraint){
            while (this.goalConstraints.values().remove(constraint));
        }
        else { // regular constraint
            I_ConstraintGroupingKey dummy = createDummy(constraint);

            if (this.constraints.containsKey(dummy)) {
                Set<Constraint> constraints = this.constraints.get(dummy);
                constraints.remove(constraint);
                if (constraints.isEmpty()) {
                    // if we've emptied the constraints, there is no more reason to keep an entry.
                    this.constraints.remove(dummy);
                }
            }
        }
    }

    /**
     *
     * @param constraints
     * @return true if this caused the set to change.
     */
    public void removeAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons : constraints) {
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
        boolean rejects = false;
        if (constraints.containsKey(dummy)){
            rejects = rejects(constraints.get(dummy), move);
        }
        if (!rejects && goalConstraints.containsKey(move.currLocation)){
            rejects = sharedGoals ? goalConstraints.get(move.currLocation).rejectsWithSharedGoals(move) :
                    goalConstraints.get(move.currLocation).rejects(move);
        }
        return rejects;
    }

    protected boolean rejects(Set<Constraint> constraints, Move move){
        for (Constraint constraint : constraints){
            if(constraint.rejects(move))
                return true;
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
        // TODO faster implementation. Probably with TreeSet.ceiling() and sorting keys by primary=location secondary=time
        // traverses the entire data structure. expensive.
        for (I_ConstraintGroupingKey cw : constraints.keySet()) {
            // if found constraint for this location, sometime in the future. Should be rare.
            if(cw.relevantInTheFuture(finalMove)){
                for (Constraint constraint : constraints.get(cw)) {
                    // make an artificial "stay" move for the relevant time.
                    // In practice, this should happen very rarely, so not very expensive.
                    int constraintTime = cw.getTime();
                    if(constraint.rejects(new Move(finalMove.agent, constraintTime, finalMove.currLocation, finalMove.currLocation))
                            && constraintTime < firstRejectionTime){
                        firstRejectionTime = constraintTime;
                    }
                }
            }
        }
        // #goalConstraints is irrelevant, since if there are no shared goals, there won't be two agents trying to get
        // to the same goal, and if there are shared goals then it's not a conflict

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
        for (Move move : moves) {
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
        for (Move move : moves) {
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
        this.constraints.keySet().removeIf(cw -> ((TimeLocation)cw).time < minTime || ((TimeLocation)cw).time >= maxTime);
    }

    protected I_ConstraintGroupingKey createDummy(Constraint constraint){
        return new TimeLocation(constraint);
    }

    protected I_ConstraintGroupingKey createDummy(Move move){
        return new TimeLocation(move);
    }


    /**
     * Find the last time when the agent is prevented from being at its goal.
     *
     * This method can be expensive in large sets, as it traverses all of {@link #constraints}.
     * @param target the agent's target.
     * @param agent the agent.
     * @return the first time when a constraint would eventually reject a "stay" move at the given move's location; -1 if never rejected.
     */
    public int lastRejectAt(I_Location target, Agent agent) {
        int lastRejectionTime = Integer.MIN_VALUE;
        Move fakeFinalMove = new Move(agent, 1, target, target);
        // traverses the entire data structure. expensive.
        for (I_ConstraintGroupingKey cw :
                constraints.keySet()) {
            //found constraint for this location, sometime in the future. Should be rare.
            if(cw.relevantInTheFuture(fakeFinalMove)){
                for (Constraint constraint :
                        constraints.get(cw)) {
                    // make an artificial "stay" move for the relevant time.
                    // In practice, this should happen very rarely, so not very expensive.
                    int constraintTime = ((TimeLocation)cw).time;
                    if(constraint.rejects(new Move(agent, constraintTime, target, target))
                            && constraintTime > lastRejectionTime){
                        lastRejectionTime = constraintTime;
                    }
                }
            }
        }

        return lastRejectionTime == Integer.MIN_VALUE ? -1 : lastRejectionTime;
    }

    /*  = translating moves and plans into constraints =*/

//    public List<Constraint> vertexConstraintsForPlan(SingleAgentPlan planForAgent) {
//        List<Constraint> constraints = new LinkedList<>();
//        for (Move move :
//                planForAgent) {
//            constraints.add(vertexConstraintsForMove(move));
//        }
//        return constraints;
//    }

    public static Constraint vertexConstraintsForMove(Move move){
        return new Constraint(null, move.timeNow, move.currLocation);
    }

    public static Constraint stayAtSourceConstraintsForMove(Move move){
        return new StayAtSourceConstraint(move.timeNow, move.currLocation);
    }

//    public List<Constraint> swappingConstraintsForPlan(SingleAgentPlan planForAgent) {
//        List<Constraint> constraints = new LinkedList<>();
//        for (Move move :
//                planForAgent) {
//            constraints.add(swappingConstraintsForMove(move));
//        }
//        return constraints;
//    }

    public static Constraint swappingConstraintsForMove(Move move){
        return new Constraint(null, move.timeNow,
                /*the constraint is in opposite direction of the move*/ move.currLocation, move.prevLocation);
    }

    public static Constraint goalConstraintForMove(Move move){
        return new GoalConstraint(move.timeNow, move.currLocation);
    }

    /**
     * Creates constraints to protect a {@link SingleAgentPlan plan}.
     * @param singleAgentPlan a plan to get constraints for.
     * @param horizonTime adds constraints up to and including this time. If constraints are dropped due to this, there will be no {@link GoalConstraint}.
     * @return all constraints to protect the plan.
     */
    public List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan, int horizonTime) {
        int firstMoveTime = singleAgentPlan.getFirstMoveTime();
        if (horizonTime < firstMoveTime){
            throw new IllegalArgumentException("horizon must include at least one move");
        }
        List<Constraint> constraints = new LinkedList<>();
        boolean stayingAtSourceSinceStart = true;
        // protect the agent's plan
        for (int t = firstMoveTime; t <= Math.min(singleAgentPlan.getEndTime(), horizonTime); t++) {
            Move move = singleAgentPlan.moveAt(t);
            boolean isStayMove = move.prevLocation.equals(move.currLocation);
            stayingAtSourceSinceStart &= isStayMove;

            if (!isStayMove){
                constraints.add(swappingConstraintsForMove(move));
            }
            if (move.timeNow != singleAgentPlan.getEndTime()){
                if (sharedSources && stayingAtSourceSinceStart){
                    // with shared sources, replace the vertex constraints for stay at source constraints when appropriate
                    constraints.add(stayAtSourceConstraintsForMove(move));
                }
                else {
                    constraints.add(vertexConstraintsForMove(move));
                }
            }
            else{ // for the last move don't save a vertex constraint, instead save a goal constraint
                constraints.add(goalConstraintForMove(singleAgentPlan.moveAt(singleAgentPlan.getEndTime())));
            }
        }
        return constraints;
    }

    /**
     * Creates constraints to protect a {@link SingleAgentPlan plan}.
     * @param singleAgentPlan a plan to get constraints for.
     * @return all constraints to protect the plan.
     */
    public List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan) {
        return allConstraintsForPlan(singleAgentPlan, Integer.MAX_VALUE);
    }

    /**
     * Creates constraints to protect a {@link Solution}.
     @param solution to get constraints for.
     @return all constraints to protect the solution.
     */
    public List<Constraint> allConstraintsForSolution(Solution solution) {
        List<Constraint> constraints = new LinkedList<>();
        for (SingleAgentPlan p :
                solution) {
            constraints.addAll(this.allConstraintsForPlan(p));
        }
        return constraints;
    }

    /* = from Object = */

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