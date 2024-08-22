package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An immutable view of a {@link ConstraintSet}.
 */
public class UnmodifiableConstraintSet implements I_ConstraintSet {
    private final I_ConstraintSet constraintSet;

    public UnmodifiableConstraintSet(@NotNull I_ConstraintSet toCopy) {
        this.constraintSet = toCopy;
    }

    @Override
    public Map<I_Location, ArrayList<Constraint>> getLocationConstraintsTimeSorted() {
        return this.constraintSet.getLocationConstraintsTimeSorted();
    }

    @Override
    public Map<I_Location, GoalConstraint> getGoalConstraints() {
        return constraintSet.getGoalConstraints();
    }

    @Override
    public boolean isSharedGoals() {
        return constraintSet.isSharedGoals();
    }

    @Override
    public void setSharedGoals(boolean sharedGoals) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public boolean isSharedSources() {
        return constraintSet.isSharedSources();
    }

    @Override
    public void setSharedSources(boolean sharedSources) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void setLastTimeToConsiderConstraints(int lastTimeToConsiderConstraints) {
        constraintSet.setLastTimeToConsiderConstraints(lastTimeToConsiderConstraints);
    }

    @Override
    public int getLastTimeToConsiderConstraints() {
        return constraintSet.getLastTimeToConsiderConstraints();
    }

    @Override
    public boolean isEmpty() {
        return constraintSet.isEmpty();
    }

    @Override
    public int getLastConstraintStartTime() {
        return constraintSet.getLastConstraintStartTime();
    }

    @Override
    public void add(Constraint constraint) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void addAll(Collection<? extends Constraint> constraints) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void addAll(ConstraintSet other) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void addAll(I_ConstraintSet other) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void remove(Constraint constraint) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void removeAll(Collection<? extends Constraint> constraints) {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(this.getClass().getSimpleName() + " is immutable");
    }

    @Override
    public boolean accepts(Move move) {
        return constraintSet.accepts(move);
    }

    @Override
    public boolean rejects(Move move) {
        return constraintSet.rejects(move);
    }

    @Override
    public int lastRejectionTime(Move finalMove) {
        return constraintSet.lastRejectionTime(finalMove);
    }

    @Override
    public int firstRejectionTime(Move finalMove) {
        return constraintSet.firstRejectionTime(finalMove);
    }

    @Override
    public boolean acceptsForever(Move finalMove) {
        return constraintSet.acceptsForever(finalMove);
    }

    @Override
    public boolean rejectsAll(Collection<? extends Move> moves) {
        return constraintSet.rejectsAll(moves);
    }

    @Override
    public boolean acceptsAll(Collection<? extends Move> moves) {
        return constraintSet.acceptsAll(moves);
    }

    @Override
    public List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan) {
        return constraintSet.allConstraintsForPlan(singleAgentPlan);
    }

    @Override
    public List<Constraint> allConstraintsForSolution(Solution solution) {
        return constraintSet.allConstraintsForSolution(solution);
    }
    
    
}
