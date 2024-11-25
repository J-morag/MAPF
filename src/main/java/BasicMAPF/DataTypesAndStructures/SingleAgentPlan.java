package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.VertexConflict;
import com.google.common.collect.Lists;

import java.util.*;
import java.util.function.Consumer;

/**
 * A plan for a single agent, which is a sequence of {@link Move}s.
 */
public class SingleAgentPlan implements Iterable<Move> {
    private List<Move> moves;
    public final Agent agent;
    private int firstVisitToTargetTime = -1;

    /**
     * @param moves a sequence of moves for the agent. Can be empty. All {@link Move}s must be moves for the same {@link Agent},
     *             and the contained {@link Move}'s {@link Move#timeNow} must form an ascending series with d=1.
     * @param agent the plan's agent.
     */
    public SingleAgentPlan(Agent agent, Iterable<Move> moves) {
        if(moves == null || agent == null) throw new IllegalArgumentException();
        this.agent = agent;
        setMoves(moves);
    }

    /**
     * Copy Constructor.
     * @param planToCopy  a {@link SingleAgentPlan}. @NotNull
     * @throws NullPointerException if #planToCopy is null.
     */
    public SingleAgentPlan(SingleAgentPlan planToCopy){
        this(planToCopy.agent, planToCopy.moves);
        this.firstVisitToTargetTime = planToCopy.firstVisitToTargetTime;
    }

    public SingleAgentPlan(Agent agent) {
        this(agent, new ArrayList<>());
    }

    private static boolean isValidNextMoveForAgent(List<Move> currentMoves, Move newMove, Agent agent){
        if (newMove == null){return false;}
        return agent.equals(newMove.agent) &&
                ( currentMoves.size() == 0 ||
                        newMove.timeNow - currentMoves.get(currentMoves.size()-1).timeNow == 1 );
    }

    private static boolean isValidMoveSequenceForAgent(List<Move> moves, Agent agent) {
        if(moves.isEmpty()){return true;}
        else{
            boolean result = true;
            Move prevMove = moves.get(0);
            for (Move move:
                    moves) {
                result &= (move==prevMove //for first iteration
                        || (move.timeNow-prevMove.timeNow == 1)); //ascending series with d=1
                prevMove = move;

                result &= move.agent.equals(agent); //all same agent
            }
            return result;
        }
    }

    /**
     * Add a single {@link Move} to the plan. The new move's {@link Move#timeNow} must be exactly 1 more than the
     * current latest move.
     * @param newMove a {@link Move} to add to the plan. The new move's {@link Move#timeNow} must be exactly 1 more than
     *               the current latest move.
     */
    public void addMove(Move newMove){
        if(isValidNextMoveForAgent(this.moves, newMove, this.agent)){
            this.moves.add(newMove);
            if (isMoveToTarget(newMove) && firstVisitToTargetTime == -1){
                firstVisitToTargetTime = newMove.timeNow;
            }
        }
        else {throw new IllegalArgumentException();}
    }

    private boolean isMoveToTarget(Move newMove) {
        return newMove.currLocation.getCoordinate().equals(agent.target);
    }

    /**
     * Appends a new sequence of moves to the current plan. The joint sequence must meet the same conditions as in
     * {@link #setMoves(Iterable)}.
     * @param newMoves a sequence of moves to append to the current plan.
     */
    public void addMoves(List<Move> newMoves){
        if(newMoves == null){throw new IllegalArgumentException();}
        for (Move move: newMoves) {
            addMove(move);
        }
    }

    /**
     * Clears the plan. Be careful when using this. The agent remains the same agent, and classes that use this plan
     * may behave unexpectedly if the plan they hold suddenly changes.
     */
    public void clearMoves(){
        this.moves.clear();
        this.firstVisitToTargetTime = -1;
    }

    /**
     * Replaces the current plan with a copy of the given sequence of moves.
     * Can be empty. All {@link Move}s must be moves for the same {@link Agent}, and the contained {@link Move}'s
     * {@link Move#timeNow} must form an ascending series with d=1. Must start at {@link #agent}s source.
     * @param newMoves a sequence of moves for the agent.
     */
    public void setMoves(Iterable<Move> newMoves){
        if(newMoves == null) throw new IllegalArgumentException();
        ArrayList<Move> localMovesCopy = Lists.newArrayList(newMoves);
        if(!isValidMoveSequenceForAgent(localMovesCopy, agent)) throw new IllegalArgumentException("invalid move sequence for agent");
        this.moves = localMovesCopy;
        for (Move move: this.moves) {
            if (isMoveToTarget(move)){
                this.firstVisitToTargetTime = move.timeNow;
                break;
            }
        }
    }

    /**
     * Return the move in the plan where {@link Move#timeNow} equals the given time.
     * O(1).
     * @param time the time of the move in the plan.
     * @return the move in the plan where {@link Move#timeNow} equals the given time, or null if there is no move for
     * that time.
     */
    public Move moveAt(int time){
        if(moves.isEmpty())
            return null;
        int requestedIndex = time - getFirstMoveTime();
        Move res = requestedIndex >= moves.size() || requestedIndex < 0 ? null : moves.get(requestedIndex);
        return res;
    }

    /**
     * @return the start time of the plan, which is 1 less than the time of the first move. returns -1 if plan is empty.
     */
    public int getPlanStartTime(){
        return moves.isEmpty() ? -1 : moves.get(0).timeNow - 1;
        // since the first move represents one timestep after the start, the start time is timeNow of the first move -1
    }

    /**
     * @return the {@link Move#timeNow time} of the first move in the plan.
     */
    public int getFirstMoveTime(){
        return moves.isEmpty() ? -1 : moves.get(0).timeNow;
    }

    /**
     * @return the end time of the plan, which is the time of the last move. returns -1 if plan is empty.
     */
    public int getEndTime(){
        return moves.isEmpty() ? -1 : moves.get(moves.size()-1).timeNow;
    }

    /**
     * @return the first {@link Move} in the plan, or null if it is empty.
     */
    public Move getFirstMove(){return moves.isEmpty() ? null : moves.get(0);}

    /**
     * @return the last {@link Move} in the plan, or null if the plan is empty.
     */
    public Move getLastMove(){return moves.isEmpty() ? null : moves.get(moves.size() - 1);}

    public boolean containsTarget() {
        return firstVisitToTargetTime != -1;
    }

    public int firstVisitToTargetTime(){
        return firstVisitToTargetTime;
    }

    public boolean endsOnTarget() {
        return this.firstVisitToTargetTime == this.getEndTime() || // faster
                isMoveToTarget(this.getLastMove());
    }

    /**
     * Returns the total time of the plan, which is the difference between end and start times. It is the same as the number of moves in the plan.
     * @return the total time of the plan. Return 0 if plan is empty.
     */
    public int size(){return moves.size();}

    /**
     * The cost of the plan is the size of the plan, which is the number of operators ({@link Move moves}) made.
     * When an agent starts at its goal it makes a single stay move which is its entire plan. This move is unnecessary,
     * so such a plan will have a cost of 0.
     * @return the cost of the plan.
     */
    public int getCost() {
        if(size() == 1 && moves.get(0).prevLocation.equals(moves.get(0).currLocation)){
            return 0;
        }
        else{
            return size();
        }
    }

    /**
     * Returns the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     * @param other another {@link SingleAgentPlan}.
     * @return the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     */
    public A_Conflict firstConflict(SingleAgentPlan other){
        return this.firstConflict(other, false, false, null);
    }

    /**
     * Returns the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     * @param other another {@link SingleAgentPlan}.
     * @param timeHorizon the maximum time to check for conflicts. If null, checks all the way to the end of the plan.
     * @return the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     */
    public A_Conflict firstConflict(SingleAgentPlan other, int timeHorizon){
        return this.firstConflict(other, false, false, timeHorizon);
    }


    /**
     * Returns the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     * @param other another {@link SingleAgentPlan}.
     * @param sharedGoalsEnabled if agents can share the same goal location
     * @param sharedSourcesEnabled if agents share the same source and so don't conflict if one of them has been staying there since the start
     * @return the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     */
    public A_Conflict firstConflict(SingleAgentPlan other, boolean sharedGoalsEnabled, boolean sharedSourcesEnabled){
        return firstConflict(other, sharedGoalsEnabled, sharedSourcesEnabled, null);
    }

    /**
     * Returns the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     * @param other another {@link SingleAgentPlan}.
     * @param sharedGoalsEnabled if agents can share the same goal location
     * @param sharedSourcesEnabled if agents share the same source and so don't conflict if one of them has been staying there since the start
     * @param timeHorizon the maximum time to check for conflicts. If null, will check until the end of the shortest plan.
     * @return the first (lowest time) conflict between this and the other plan. If they don't conflict, returns null.
     */
    public A_Conflict firstConflict(SingleAgentPlan other, boolean sharedGoalsEnabled, boolean sharedSourcesEnabled, Integer timeHorizon){
        // find lower and upper bound for time, and check only in that range
        //the min time to check is the max first move time
        int minTime = Math.max(this.getFirstMoveTime(), other.getFirstMoveTime());
        //the max time to check is the min last move time
        int maxTime = Math.min(this.getEndTime(), other.getEndTime());
        timeHorizon = timeHorizon == null ? Integer.MAX_VALUE: timeHorizon;
        // if they both get to their goals at the same time and share it, it can't have a conflict
        if (sharedGoalsEnabled && this.moveAt(this.getEndTime()).currLocation.
                equals(other.moveAt(other.getEndTime()).currLocation)
                && this.getEndTime() == other.getEndTime()){
            maxTime -= 1;
        }
        boolean localStayingAtSource = true;
        boolean otherStayingAtSource = true;

        for(int time = minTime; time <= Math.min(maxTime, timeHorizon); time++){
            Move localMove = this.moveAt(time);
            localStayingAtSource &= localMove.prevLocation.equals(localMove.currLocation);
            Move otherMoveAtTime = other.moveAt(time);
            otherStayingAtSource &= otherMoveAtTime.prevLocation.equals(otherMoveAtTime.currLocation);

            A_Conflict firstConflict = A_Conflict.conflictBetween(localMove, otherMoveAtTime);
            if(firstConflict != null && !(sharedSourcesEnabled && localStayingAtSource && otherStayingAtSource)){
                return firstConflict;
            }
        }

        // if we've made it all the way here, the plans don't conflict in their shared timespan.
        // now check if one plan ended and then the other plan had a move that conflicts with the first plan's last position (goal)
        return firstConflictAtGoal(other, maxTime, sharedGoalsEnabled, timeHorizon);
    }

    /**
     * Compares with another {@link SingleAgentPlan}, looking for vertex conflicts ({@link VertexConflict}) or
     * swapping conflicts ({@link SwappingConflict}). Runtime is O(the number of moves in this plan).
     * @return true if a conflict exists between the plans.
     */
    public boolean conflictsWith(SingleAgentPlan other){
        return this.conflictsWith(other, false, false);
    }

    /**
     * Compares with another {@link SingleAgentPlan}, looking for vertex conflicts ({@link VertexConflict}) or
     * swapping conflicts ({@link SwappingConflict}). Runtime is O(the number of moves in this plan).
     * @param canShareGoals if the agents can share goals
     * @param sharedSources if agents share the same source and so don't conflict if one of them has been staying there since the start
     * @return true if a conflict exists between the plans.
     */
    public boolean conflictsWith(SingleAgentPlan other, boolean canShareGoals, boolean sharedSources){
        return firstConflict(other, canShareGoals, sharedSources) != null;
    }

    /**
     * helper function for {@link #firstConflict(SingleAgentPlan)}.
     *
     * @param other              another plan.
     * @param maxTime            the maximum time at which both plans have moves.
     * @param sharedGoalsEnabled if the agents can share the same goal
     * @param timeHorizon
     * @return a conflict if one of the plans ends, and then the other plan makes a move that conflicts with the ended plan's agent staying at its goal. else null.
     */
    protected A_Conflict firstConflictAtGoal(SingleAgentPlan other, int maxTime, boolean sharedGoalsEnabled, Integer timeHorizon) {
        // if they share goals, the last move of the late ending plan can't be a conflict with the early ending plan.
        int sharedGoalsTimeOffset = sharedGoalsEnabled &&
                this.moveAt(this.getEndTime()).currLocation.equals(other.moveAt(other.getEndTime()).currLocation) ? -1 : 0;

        if(this.getEndTime() != other.getEndTime()){
            SingleAgentPlan lateEndingPlan = this.getEndTime() > maxTime ? this : other;
            SingleAgentPlan earlyEndingPlan = this.getEndTime() <= maxTime ? this : other;

            // if plans for "start at goal and stay there" are represented as an empty plan, we will have to make this check
            if (earlyEndingPlan.getEndTime() == -1){
                // can skip late ending plan's start location, since if they conflict on start locations it is an
                // impossible instance (so we shouldn't get one)
                for (int t = 1; t < Math.min(timeHorizon, lateEndingPlan.getEndTime() + sharedGoalsTimeOffset); t++) {
                    I_Location steppingIntoLocation = lateEndingPlan.moveAt(t).currLocation;
                    if (steppingIntoLocation.getCoordinate().equals(earlyEndingPlan.agent.target)){
                        Move stayMove = new Move(earlyEndingPlan.agent, t, steppingIntoLocation, steppingIntoLocation);
                        A_Conflict goalConflict = A_Conflict.conflictBetween(lateEndingPlan.moveAt(t), stayMove);
                        return goalConflict;
                    }
                }
            }
            else{
                // look for the late ending plan stepping into the agent from the early ending plan, sitting at its goal.
                I_Location goalLocation = earlyEndingPlan.moveAt(maxTime).currLocation;
                for (int time = maxTime+1; time <= Math.min(timeHorizon, lateEndingPlan.getEndTime() + sharedGoalsTimeOffset) && time >= lateEndingPlan.getFirstMoveTime(); time++) {
                    Move stayMove = new Move(earlyEndingPlan.agent, time, goalLocation, goalLocation);
                    A_Conflict goalConflict = A_Conflict.conflictBetween(lateEndingPlan.moveAt(time), stayMove);
                    if(goalConflict != null){
                        return goalConflict;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns a new plan that is a prefix of this plan. The new plan will have the same agent as this plan.
     * @param firstNMoves the number of moves from this plan to include in the new plan. Must be positive and at most
     *                    the size of this plan.
     * @return a new plan that is a prefix of this plan.
     */
    public SingleAgentPlan getPrefix(int firstNMoves) {
        if (firstNMoves <= 0) {
            throw new IllegalArgumentException("firstNMoves must be positive. got: " + firstNMoves + " for plan: " + this);
        }
        if (firstNMoves > this.size()) {
            throw new IllegalArgumentException("firstNMoves must be at most the size of the plan. got: " + firstNMoves + " for plan: " + this);
        }
        return new SingleAgentPlan(this.agent, this.moves.subList(0, firstNMoves));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Plan for agent ").append(agent.iD).append(':');
        sb.append(this.moves.isEmpty() ? " empty" : " 0:" + moves.get(0).prevLocation.getCoordinate());
        for (Move move: this) {
            sb.append(' ').append(move.timeNow).append(':').append(move.currLocation.getCoordinate());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SingleAgentPlan moves1 = (SingleAgentPlan) o;
        return moves.equals(moves1.moves) &&
                agent.equals(moves1.agent);
    }

    @Override
    public int hashCode() {
        int result = moves.hashCode();
        result = 31 * result + agent.hashCode();
        return result;
    }

    /*  = Iterable Interface =  */

    @Override
    public Iterator<Move> iterator() {
        return this.moves.iterator();
    }

    @Override
    public void forEach(Consumer<? super Move> action) {
        this.moves.forEach(action);
    }

    @Override
    public Spliterator<Move> spliterator() {
        return this.moves.spliterator();
    }
}
