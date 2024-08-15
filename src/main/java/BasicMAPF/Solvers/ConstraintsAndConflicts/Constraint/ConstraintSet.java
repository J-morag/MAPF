package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A set of {@link Constraint}s.
 * Adding and removing constraints is O(1). Checking if the set {@link #rejects(Move)} or {@link #accepts(Move)} is O(n)
 * in the worst case. However, there will usually be very few unique constraints for every pair of [time,location], with
 * many unique pairs of [time,location]. Therefore, it is on average O(1).
 */
public class ConstraintSet implements I_ConstraintSet {

    /**
     * Basically a dictionary from [time,location] to agents who can't go there at that time, and locations from which
     * they can't go there at that time.
     */
    protected final Map<I_ConstraintGroupingKey, Set<Constraint>> constraints = new HashMap<>(); // todo lists instead of sets?
//    protected final Map<I_Location, List<Constraint>> timeSortedConstraintsAtLocation = new HashMap<>(); // todo implement. Or only keep last and forgo ability to remove constraints??
    // todo use only locationConstraints instead of constraints field?
    protected final Map<I_Location, ArrayList<Constraint>> locationConstraints = new HashMap<>(); // todo ArrayMap for explicit maps?

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

    public ConstraintSet(I_ConstraintSet toCopy){
        if(toCopy == null) {throw new IllegalArgumentException();}
        this.sharedGoals = toCopy.isSharedGoals();
        this.sharedSources = toCopy.isSharedSources();
        this.addAll(toCopy);
        this.lastConstraintTime = toCopy.getLastConstraintStartTime();
    }
    public ConstraintSet(Collection<? extends Constraint> seedConstraints) {
        this();
        if(seedConstraints == null) {throw new IllegalArgumentException();}
        this.addAll(seedConstraints);
    }

    public ConstraintSet(@NotNull I_ConstraintSet toCopy, int upToAndIncludingTime) {
        this.sharedGoals = toCopy.isSharedGoals();
        this.sharedSources = toCopy.isSharedSources();
        this.addAll(toCopy, upToAndIncludingTime);
        this.lastConstraintTime = Math.min(upToAndIncludingTime, toCopy.getLastConstraintStartTime());
    }


    /*  = Set Interface =  */

    //removed, because the size of constraints field isn't the number of constraints in the set. if we need this, add size field to class.
//    public int size() {
//        return constraints.size();
//    }

    @Override
    public Set<Map.Entry<I_ConstraintGroupingKey, Set<Constraint>>> getEntrySet(){
        return Collections.unmodifiableSet(constraints.entrySet());
    }

    @Override
    public Map<I_Location, GoalConstraint> getGoalConstraints() {
        return Collections.unmodifiableMap(this.goalConstraints);
    }

    @Override
    public Map<I_Location, ArrayList<Constraint>> getLocationConstraints() {
        return Collections.unmodifiableMap(this.locationConstraints);
    }

    @Override
    public boolean isSharedGoals() {
        return sharedGoals;
    }

    @Override
    public void setSharedGoals(boolean sharedGoals) {
        this.sharedGoals = sharedGoals;
    }

    @Override
    public boolean isSharedSources() {
        return sharedSources;
    }

    @Override
    public void setSharedSources(boolean sharedSources) {
        this.sharedSources = sharedSources;
    }

    @Override
    public boolean isEmpty() {
        return constraints.isEmpty();
    }

    @Override
    public int getLastConstraintStartTime(){
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
            this.constraints.get(dummy).add(constraint);
            this.lastConstraintTime = Math.max(this.lastConstraintTime, constraint.time);

            ArrayList<Constraint> constraintsAtLocation = locationConstraints.computeIfAbsent(constraint.location, k -> new ArrayList<>());
            // insert sorted on constraint time (using binary search)
            int index = Collections.binarySearch(constraintsAtLocation, constraint, Comparator.comparingInt(c -> c.time));
            if (index < 0) index = -index - 1;
            constraintsAtLocation.add(index, constraint);
        }

    }

    @Override
    public void addAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons : constraints) {
            this.add(cons);
        }
    }

    @Override
    public void addAll(ConstraintSet other) {
        addAll(other, Integer.MAX_VALUE);
    }

    private void addAll(@NotNull I_ConstraintSet other, int upToTime) {
        for (Map.Entry<I_ConstraintGroupingKey, Set<Constraint>> entry : other.getEntrySet()) {
            for (Constraint cons : entry.getValue()) {
                if (cons.time <= upToTime) {
                    this.add(cons);
                }
            }
        }
        Map<I_Location, GoalConstraint> otherGoalConstraints = other.getGoalConstraints();
        for (I_Location loc : otherGoalConstraints.keySet()){
            if (otherGoalConstraints.get(loc).time <= upToTime){
                this.add(otherGoalConstraints.get(loc));
            }
        }
        for (Map.Entry<I_Location, ArrayList<Constraint>> entry : other.getLocationConstraints().entrySet()) {
            for (Constraint cons : entry.getValue()) {
                if (cons.time <= upToTime) {
                    this.add(cons);
                }
            }
        }
    }

    @Override
    public void addAll(I_ConstraintSet other) {
        for (Map.Entry<I_ConstraintGroupingKey, Set<Constraint>> entry : other.getEntrySet()) {
            for (Constraint constraint : entry.getValue()) {
                this.add(constraint);
            }
        }
        for (GoalConstraint goalConstraint : other.getGoalConstraints().values()) {
            this.add(goalConstraint);
        }
        for (Map.Entry<I_Location, ArrayList<Constraint>> entry : other.getLocationConstraints().entrySet()) {
            for (Constraint constraint : entry.getValue()) {
                this.add(constraint);
            }
        }
    }

    @Override
    public void remove(Constraint constraint){
        if(constraint instanceof  RangeConstraint){
            // there is a problem with removing range constraints, since they may overlap.
            throw new UnsupportedOperationException();
        }
        else if (constraint instanceof  GoalConstraint){
            while (this.goalConstraints.values().remove(constraint)); // todo check
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

            ArrayList<Constraint> constraintsAtLocation = this.locationConstraints.get(constraint.location);
            if (constraintsAtLocation != null) {
                constraintsAtLocation.remove(constraint);
                if (constraintsAtLocation.isEmpty()) {
                    this.locationConstraints.remove(constraint.location);
                }
            }
        }
    }

    @Override
    public void removeAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons : constraints) {
            this.remove(cons);
        }
    }

    @Override
    public void clear() {
        this.constraints.clear();
        this.goalConstraints.clear();
        this.locationConstraints.clear();
    }

    @Override
    public boolean accepts(Move move){
        return !rejects(move);
    }

    @Override
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
     * @param finalMove                 a move to occupy a location indefinitely.
     * @param checkOtherAgentsLastMoves if true, also check if the agent's goal is occupied indefinitely.
     *                                  Which should not happen if agents aren't allowed to have the same target.
     * @return the *last* time when a constraint would eventually reject a "stay" move at the given move's location;
     * Specifically, would return {@link Integer#MAX_VALUE} if there is an infinite (target/goal) constraint on the location (not checked unless checkOtherAgentsLastMoves is true);
     * -1 if never rejected.
     */
    @Override
    public int lastRejectionTime(Move finalMove, boolean checkOtherAgentsLastMoves){
        return firstOrLastRejectionTime(finalMove, checkOtherAgentsLastMoves, false);
    }

    /**
     * Given a {@link Move} which an {@link Agent agent} makes to occupy a {@link I_Location location}
     * indefinitely starting after move's time, checks if there is a {@link Constraint} that would reject it eventually.
     * <p>
     * In other words, we simulate this set being given an infinite number of "stay" moves after the given move.
     * <p>
     * This method can be expensive in large sets, as it traverses all of {@link #constraints}.
     *
     * @param finalMove                 a move to occupy a location indefinitely.
     * @param checkOtherAgentsLastMoves if true, also check if the agent's goal is occupied indefinitely.
     *                                  Which should not happen if agents aren't allowed to have the same target.
     * @return the *first* time when a constraint would eventually reject a "stay" move at the given move's location; -1 if never rejected.
     */
    @Override
    public int firstRejectionTime(Move finalMove, boolean checkOtherAgentsLastMoves){
        return firstOrLastRejectionTime(finalMove, checkOtherAgentsLastMoves, true);
    }

    protected int firstOrLastRejectionTime(Move finalMove, boolean checkOtherAgentsLastMoves, boolean first){
        int rejectionTime = first ? Integer.MAX_VALUE : -1;

        ArrayList<Constraint> constraintsSortedByTime = locationConstraints.get(finalMove.currLocation);
        if (constraintsSortedByTime != null){
            if (first){
                // todo skip to finalMove.time with binary search
                for (Constraint constraint : constraintsSortedByTime){
                    if (constraint.time >= finalMove.timeNow){
                        // todo dummy move to save object creations?
                        if (constraint.rejects(new Move(finalMove.agent, constraint.time, finalMove.currLocation, finalMove.currLocation))){
                            rejectionTime = constraint.time;
                            break;
                        }
                    }
                }
            }
            else { // last
                for (int i = constraintsSortedByTime.size() - 1; i >= 0; i--){
                    Constraint constraint = constraintsSortedByTime.get(i);
                    if (constraint.time < finalMove.timeNow){
                        break;
                    }
                    // todo dummy move to save object creations?
                    if (constraint.rejects(new Move(finalMove.agent, constraint.time, finalMove.currLocation, finalMove.currLocation))){
                        rejectionTime = Math.max(rejectionTime, constraint.time);
                        break;
                    }
                }
            }
        }

        // Unless explicitly requested, #goalConstraints is irrelevant, since in classic MAPF targets are unique.
        // So, there won't be two agents trying to end their plan at the same location.
        // Additionally, if there are shared goals then it's not a conflict
        if (checkOtherAgentsLastMoves && !sharedGoals){
            // TODO faster implementation. Probably with TreeSet.ceiling() and sorting keys by primary=location secondary=time
            for (I_Location loc : goalConstraints.keySet()){
                if (loc.equals(finalMove.currLocation)){ // any two constraints that are infinite in time will eventually conflict
                    GoalConstraint constraint = this.goalConstraints.get(loc);
                    if (first) rejectionTime = Math.min(rejectionTime, Math.max(constraint.time, finalMove.timeNow));
                    else return Integer.MAX_VALUE; // it rejects to infinity
                }
            }
        }

        return rejectionTime == Integer.MAX_VALUE ? -1 : rejectionTime;
    }

    @Override
    public boolean acceptsForever(Move finalMove, boolean checkOtherAgentsLastMoves) {
        return firstRejectionTime(finalMove, checkOtherAgentsLastMoves) == -1;
    }

    @Override
    public boolean rejectsAll(Collection<? extends Move> moves){
        boolean result = true;
        for (Move move : moves) {
            result &= this.rejects(move);
        }
        return result;
    }

    @Override
    public boolean acceptsAll(Collection<? extends Move> moves){
        boolean result = true;
        for (Move move : moves) {
            result &= this.accepts(move);
        }
        return result;
    }

    @Override
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
     * <p>
     * This method can be expensive in large sets, as it traverses all of {@link #constraints}.
     *
     * @param target the agent's target.
     * @param agent  the agent.
     * @return the first time when a constraint would eventually reject a "stay" move at the given move's location; -1 if never rejected.
     */
    @Override
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