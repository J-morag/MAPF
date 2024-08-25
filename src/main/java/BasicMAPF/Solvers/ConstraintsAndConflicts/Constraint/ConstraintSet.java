package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.*;

/**
 * A set of {@link Constraint}s.
 * Adding and removing constraints is O(1). Checking if the set {@link #rejects(Move)} or {@link #accepts(Move)} is O(n)
 * in the worst case. However, there will usually be very few unique constraints for every pair of [time,location], with
 * many unique pairs of [time,location]. Therefore, it is on average O(1).
 */
public class ConstraintSet implements I_ConstraintSet {

    /**
     * Constraints are grouped by location, and then sorted (ascending) on their time.
     */
    protected final Map<I_Location, ArrayList<Constraint>> locationConstraintsTimeSorted = new HashMap<>(); // todo ArrayMap for explicit maps?

    /**
     * Goal constraints. Locations in this collection are reserved starting from the constraint's time, indefinitely.
     */
    protected final Map<I_Location, GoalConstraint> goalConstraints = new HashMap<>();

    protected int lastConstraintTime = -1;

    /**
     * If set to true, agents who share the same goal may occupy their goal vertex at the same time.
     */
    private boolean sharedGoals;
    /**
     * If true, agents staying at their source (since the start) will not constrain agents with the same source
     */
    private boolean sharedSources;
    /**
     * The effects of constraints that happen at a time greater than this value will be ignored.
     */
    private int lastTimeToConsiderConstraints = Integer.MAX_VALUE;

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
        this.lastTimeToConsiderConstraints = toCopy.getLastTimeToConsiderConstraints();
        this.addAll(toCopy);
        this.lastConstraintTime = toCopy.getLastConstraintStartTime();
    }
    public ConstraintSet(Collection<? extends Constraint> seedConstraints) {
        this();
        if(seedConstraints == null) {throw new IllegalArgumentException();}
        this.addAll(seedConstraints);
    }


    /*  = Set Interface =  */

    @Override
    public Map<I_Location, ArrayList<Constraint>> getLocationConstraintsTimeSorted() {
        return Collections.unmodifiableMap(this.locationConstraintsTimeSorted);
    }

    @Override
    public Map<I_Location, GoalConstraint> getGoalConstraints() {
        return Collections.unmodifiableMap(this.goalConstraints);
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
    public int getLastTimeToConsiderConstraints() {
        return lastTimeToConsiderConstraints;
    }

    @Override
    public void setLastTimeToConsiderConstraints(int lastTimeToConsiderConstraints) {
        this.lastTimeToConsiderConstraints = lastTimeToConsiderConstraints;
    }

    @Override
    public boolean isEmpty() {
        return this.locationConstraintsTimeSorted.isEmpty();
    }

    @Override
    public int getLastConstraintStartTime(){
        return lastTimeToConsiderConstraints < Integer.MAX_VALUE && !this.goalConstraints.isEmpty() ?
                Math.max(this.lastConstraintTime, this.lastTimeToConsiderConstraints) :
                this.lastConstraintTime;
    }

    public void add(Constraint constraint){
        if(constraint instanceof RangeConstraint rangeConstraint){
            // add an individual constraint for each of the times covered by the range constraint
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
        else{
            addRegularConstraint(constraint);
        }

    }

    private void addRegularConstraint(Constraint constraint) {
        ArrayList<Constraint> constraintsAtLocation = locationConstraintsTimeSorted.computeIfAbsent(constraint.location, k -> new ArrayList<>());
        // insert sorted on constraint time (using binary search)
        int index = Collections.binarySearch(constraintsAtLocation, constraint, Comparator.comparingInt(c -> c.time));
        if (index < 0) {
            index = -index - 1;
            constraintsAtLocation.add(index, constraint);
        }
        else {
            // detect duplicates
            boolean duplicate = false;
            for (int i = index; i < constraintsAtLocation.size(); i++) {
                if (constraintsAtLocation.get(i).equals(constraint)) {
                    duplicate = true;
                    break;
                }
            }
            for (int i = index - 1; i >= 0 && !duplicate; i--) {
                if (constraintsAtLocation.get(i).equals(constraint)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                constraintsAtLocation.add(index, constraint);
            }
        }
        this.lastConstraintTime = Math.max(this.lastConstraintTime, constraint.time);
    }

    @Override
    public void addAll(Collection<? extends Constraint> constraints) {
        for (Constraint cons : constraints) {
            this.add(cons);
        }
    }

    @Override
    public void addAll(ConstraintSet other) {
        for (I_Location loc : other.locationConstraintsTimeSorted.keySet()){
            for (Constraint cons : other.locationConstraintsTimeSorted.get(loc)){
                this.add(cons);
            }
        }
        for (I_Location loc : other.goalConstraints.keySet()){
            this.add(other.goalConstraints.get(loc));
        }
    }

    @Override
    public void addAll(I_ConstraintSet other) {
        for (ArrayList<Constraint> constraints : other.getLocationConstraintsTimeSorted().values()) {
            for (Constraint constraint : constraints) {
                this.add(constraint);
            }
        }
        for (GoalConstraint goalConstraint : other.getGoalConstraints().values()) {
            this.add(goalConstraint);
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
            ArrayList<Constraint> constraintsAtLocation = this.locationConstraintsTimeSorted.get(constraint.location);
            if (constraintsAtLocation != null) {
                constraintsAtLocation.remove(constraint);
                if (constraintsAtLocation.isEmpty()) {
                    this.locationConstraintsTimeSorted.remove(constraint.location);
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
        this.locationConstraintsTimeSorted.clear();
        this.goalConstraints.clear();
    }

    @Override
    public boolean accepts(Move move){
        return !rejects(move);
    }

    @Override
    public boolean rejects(Move move){
        if (move.timeNow > lastTimeToConsiderConstraints){
            return false;
        }

        List<Constraint> constraintsAtLocation;
        if ((constraintsAtLocation = locationConstraintsTimeSorted.get(move.currLocation)) != null){
            // binary search for the constraint at the move's time
            int index = Collections.binarySearch(constraintsAtLocation, getDummyConstraint(move), Comparator.comparingInt(c -> c.time));
            if (index >= 0){
                // check all constraints with the correct time
                for (int i = index; i < constraintsAtLocation.size(); i++){
                    if (constraintsAtLocation.get(i).time != move.timeNow){
                        break;
                    }
                    if (constraintsAtLocation.get(i).rejects(move)){
                        return true;
                    }
                }
                for (int i = index - 1; i >= 0; i--) {
                    if (constraintsAtLocation.get(i).time != move.timeNow) {
                        break;
                    }
                    if (constraintsAtLocation.get(i).rejects(move)) {
                        return true;
                    }
                }
            }
        }
        if (goalConstraints.containsKey(move.currLocation)){
            return sharedGoals ? goalConstraints.get(move.currLocation).rejectsWithSharedGoals(move) : goalConstraints.get(move.currLocation).rejects(move);
        }
        return false;
    }

    private Constraint getDummyConstraint(Move move) {
        // todo create a permanent dummy to avoid too many object creations?
        return new Constraint(null, move.timeNow, null, move.currLocation);
    }

    /**
     * Given a {@link Move} which an {@link Agent agent} makes to occupy a {@link I_Location location}
     * indefinitely starting after move's time, checks if there is a {@link Constraint} that would reject it eventually.
     *
     * @param finalMove                 a move to occupy a location indefinitely.
     * @return the *last* time when a constraint would eventually reject a "stay" move at the given move's location;
     * Specifically, would return {@link Integer#MAX_VALUE} if there is an infinite (target/goal) constraint on the location (not checked unless checkOtherAgentsLastMoves is true);
     * -1 if never rejected.
     */
    @Override
    public int lastRejectionTime(Move finalMove){
        return firstOrLastRejectionTime(finalMove, false);
    }

    /**
     * Given a {@link Move} which an {@link Agent agent} makes to occupy a {@link I_Location location}
     * indefinitely starting after move's time, checks if there is a {@link Constraint} that would reject it eventually.
     * <p>
     * In other words, we simulate this set being given an infinite number of "stay" moves after the given move.
     * <p>
     *
     * @param finalMove                 a move to occupy a location indefinitely.
     * @return the *first* time when a constraint would eventually reject a "stay" move at the given move's location; -1 if never rejected.
     */
    @Override
    public int firstRejectionTime(Move finalMove){
        return firstOrLastRejectionTime(finalMove, true);
    }

    protected int firstOrLastRejectionTime(Move finalMove, boolean first){
        if (finalMove.timeNow > lastTimeToConsiderConstraints){
            return -1;
        }

        int rejectionTime = first ? Integer.MAX_VALUE : -1;

        ArrayList<Constraint> constraintsSortedByTime = locationConstraintsTimeSorted.get(finalMove.currLocation);
        if (constraintsSortedByTime != null){
            if (first){
                // skip to finalMove.time with binary search
                int index = Collections.binarySearch(constraintsSortedByTime, getDummyConstraint(finalMove), Comparator.comparingInt(c -> c.time));
                if (index>= 0){
                    while (index > 0 && constraintsSortedByTime.get(index - 1).time == finalMove.timeNow)
                        index--;
                }
                else{
                    index = -index - 1;
                }
                for (; index < constraintsSortedByTime.size(); index++) {
                    Constraint constraint = constraintsSortedByTime.get(index);
                    if (constraint.time > lastTimeToConsiderConstraints){
                        break;
                    }
                    if (constraint.time >= finalMove.timeNow) {
                        // todo dummy move to save object creations?
                        if (constraint.rejects(new Move(finalMove.agent, constraint.time, finalMove.currLocation, finalMove.currLocation))) {
                            rejectionTime = constraint.time;
                            break;
                        }
                    }
                }
            }
            else { // last
                for (int i = constraintsSortedByTime.size() - 1; i >= 0; i--){
                    Constraint constraint = constraintsSortedByTime.get(i);
                    if (constraint.time > lastTimeToConsiderConstraints){
                        continue;
                    }
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

        GoalConstraint constraint = this.goalConstraints.get(finalMove.currLocation); // any two constraints that are infinite in time will eventually conflict
        if (constraint != null){ // in classic MAPF, agents can't share goals, so this should be false anyway. But we need to check it for TMAPF
            if (!sharedGoals || !constraint.location.getCoordinate().equals(finalMove.agent.target) || !constraint.responsibleAgent.target.equals(finalMove.agent.target)){
                if (constraint.time <= lastTimeToConsiderConstraints){ // already made sure that move time is smaller
                    if (first) rejectionTime = Math.min(rejectionTime, Math.max(constraint.time, finalMove.timeNow));
                    else return lastTimeToConsiderConstraints; // it rejects to infinity. (return here to avoid interpreting MAX_VALUE as no-conflict at the end)
                }
            }
        }

        return rejectionTime == Integer.MAX_VALUE ? -1 : rejectionTime;
    }

    @Override
    public boolean acceptsForever(Move finalMove) {
        return firstRejectionTime(finalMove) == -1;
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
    public List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan) {
        List<Constraint> constraints = new LinkedList<>();
        boolean stayingAtSourceSinceStart = true;
        // protect the agent's plan
        for (int t = singleAgentPlan.getFirstMoveTime(); t <= singleAgentPlan.getEndTime(); t++) {
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

    static Constraint vertexConstraintsForMove(Move move) {
        return new Constraint(null, move.timeNow, move.currLocation);
    }

    static Constraint stayAtSourceConstraintsForMove(Move move) {
        return new StayAtSourceConstraint(move.timeNow, move.currLocation);
    }

    static Constraint swappingConstraintsForMove(Move move) {
        return new Constraint(null, move.timeNow,
                /*the constraint is in opposite direction of the move*/ move.currLocation, move.prevLocation);
    }

    static Constraint goalConstraintForMove(Move move) {
        return new GoalConstraint(move.timeNow, move.currLocation, move.agent);
    }

    @Override
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
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstraintSet that)) return false;

        return lastConstraintTime == that.lastConstraintTime && sharedGoals == that.sharedGoals && sharedSources == that.sharedSources && locationConstraintsTimeSorted.equals(that.locationConstraintsTimeSorted) && goalConstraints.equals(that.goalConstraints);
    }

    @Override
    public int hashCode() {
        int result = locationConstraintsTimeSorted.hashCode();
        result = 31 * result + goalConstraints.hashCode();
        result = 31 * result + lastConstraintTime;
        result = 31 * result + Boolean.hashCode(sharedGoals);
        result = 31 * result + Boolean.hashCode(sharedSources);
        return result;
    }
}