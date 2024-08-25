package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.*;

public interface I_ConstraintSet {

    Map<I_Location, ArrayList<Constraint>> getLocationConstraintsTimeSorted();

    Map<I_Location, GoalConstraint> getGoalConstraints();

    boolean isSharedGoals();

    void setSharedGoals(boolean sharedGoals);

    boolean isSharedSources();

    void setSharedSources(boolean sharedSources);

    int getLastTimeToConsiderConstraints();

    void setLastTimeToConsiderConstraints(int lastTimeToConsiderConstraints);

    boolean isEmpty();

    /**
     * @return the time of the last constraint. If it is a goal constraint (infinite), return the time when it starts
     */
    int getLastConstraintStartTime();

    void add(Constraint constraint);

    void addAll(Collection<? extends Constraint> constraints);

    void addAll(ConstraintSet other);

    void addAll(I_ConstraintSet other);

    void remove(Constraint constraint);

    /**
     * @param constraints
     * @return true if this caused the set to change.
     */
    void removeAll(Collection<? extends Constraint> constraints);

    void clear();

    /**
     * @param move
     * @return the opposite of {@link #rejects(Move)}
     */
    boolean accepts(Move move);

    /**
     * Returns true iff any of the {@link Constraint}s that were {@link #add(Constraint) added} to this set conflict with
     * the given {@link Move}.
     *
     * @param move a {@link Move} to check if it is rejected or not.
     * @return true iff any of the {@link Constraint}s that were {@link #add(Constraint) added} to this set
     * conflict with the given {@link Move}.
     */
    boolean rejects(Move move);

    /**
     * Given a {@link Move} which an {@link Agent agent} makes to occupy a {@link I_Location location}
     * indefinitely starting after move's time, checks if there is a {@link Constraint} that would reject it eventually.
     *
     * @param finalMove                 a move to occupy a location indefinitely.
     * @return the *last* time when a constraint would eventually reject a "stay" move at the given move's location;
     * Specifically, would return {@link Integer#MAX_VALUE} if there is an infinite (target/goal) constraint on the location (not checked unless checkOtherAgentsLastMoves is true);
     * -1 if never rejected.
     */
    int lastRejectionTime(Move finalMove);

    /**
     * Given a {@link Move} which an {@link Agent agent} makes to occupy a {@link I_Location location}
     * indefinitely starting after move's time, checks if there is a {@link Constraint} that would reject it eventually.
     * <p>
     * In other words, we simulate this set being given an infinite number of "stay" moves after the given move.
     *
     * @param finalMove                 a move to occupy a location indefinitely.
     * @return the *first* time when a constraint would eventually reject a "stay" move at the given move's location; -1 if never rejected.
     */
    int firstRejectionTime(Move finalMove);

    /**
     * The opposite of {@link #firstRejectionTime(Move)}.
     *
     * @param finalMove a move to occupy a location indefinitely.
     * @return true if no constraint would eventually reject a "stay" move at the given move's location.
     */
    boolean acceptsForever(Move finalMove);

    /**
     * Returns true iff any of the {@link Constraint}s that were {@link #add(Constraint) added} to this set conflict with
     * the given {@link Move}.
     * <p>
     * Doesn't assume that the last move means stay at goal forever.
     *
     * @param moves a {@link Collection} of {@link Move}s to check if the are ejected or not.
     * @return true iff all of the given {@link Move}s conflict with any of the {@link Constraint}s that were
     * {@link #add(Constraint) added} to this set.
     * @see #acceptsForever(Move)
     */
    boolean rejectsAll(Collection<? extends Move> moves);

    /**
     * Returns true iff none of the {@link Constraint}s that were {@link #add(Constraint) added} to this set conflict with
     * the given {@link Move}.
     * <p>
     * Doesn't assume that the last move means stay at goal forever.
     *
     * @param moves
     * @return the opposite of {@link #rejectsAll(Collection)}.
     * @see #acceptsForever(Move)
     */
    boolean acceptsAll(Collection<? extends Move> moves);

    /**
     * Creates constraints to protect a {@link SingleAgentPlan plan}.
     *
     * @param singleAgentPlan a plan to get constraints for.
     * @return all constraints to protect the plan.
     */
    default List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan){
        return allConstraintsForPlan(singleAgentPlan, Integer.MAX_VALUE);
    }

    /**
     * Creates constraints to protect a {@link SingleAgentPlan plan}.
     *
     * @param singleAgentPlan a plan to get constraints for.
     * @return all constraints to protect the plan.
     */
    List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan, int horizonTime);

    /**
     * Creates constraints to protect a {@link Solution}.
     *
     * @param solution to get constraints for.
     * @return all constraints to protect the solution.
     */
    List<Constraint> allConstraintsForSolution(Solution solution);
}
