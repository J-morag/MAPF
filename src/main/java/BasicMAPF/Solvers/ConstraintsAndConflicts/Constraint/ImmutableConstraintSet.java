package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An immutable view of a {@link ConstraintSet}.
 */
public class ImmutableConstraintSet extends ConstraintSet {
    // TODO put ConstraintSet behind an interface, and make this implement it...
    //  inheriting like this is inefficient and more importantly error-prone

    private final ConstraintSet constraintSet;

    public ImmutableConstraintSet(@NotNull ConstraintSet toCopy) {
        this.constraintSet = toCopy;
    }

    @Override
    public void setSharedGoals(boolean sharedGoals) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void setSharedSources(boolean sharedSources) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void addAll(Collection<? extends Constraint> constraints) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void addAll(ConstraintSet other) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void remove(Constraint constraint) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void removeAll(Collection<? extends Constraint> constraints) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void trimToTimeRange(int minTime, int maxTime) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    public void add(Constraint constraint) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    @Override
    protected void add(Set<Constraint> constraintSet, Constraint constraint) {
        throw new UnsupportedOperationException("ImmutableConstraintSet is immutable");
    }

    // delegate all other methods to the constraintSet

    @Override
    public boolean isSharedGoals() {
        return constraintSet.isSharedGoals();
    }

    @Override
    public boolean isSharedSources() {
        return constraintSet.isSharedSources();
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
    public boolean accepts(Move move) {
        return constraintSet.accepts(move);
    }

    @Override
    public boolean rejects(Move move) {
        return constraintSet.rejects(move);
    }

    @Override
    protected boolean rejects(Set<Constraint> constraints, Move move) {
        return constraintSet.rejects(constraints, move);
    }

    @Override
    public int firstRejectionTime(Move finalMove, boolean checkOtherAgentsLastMoves) {
        return constraintSet.firstRejectionTime(finalMove, checkOtherAgentsLastMoves);
    }

    @Override
    public int lastRejectionTime(Move finalMove, boolean checkOtherAgentsLastMoves) {
        return constraintSet.lastRejectionTime(finalMove, checkOtherAgentsLastMoves);
    }

    @Override
    public boolean acceptsForever(Move finalMove, boolean checkOtherAgentsLastMoves) {
        return constraintSet.acceptsForever(finalMove, checkOtherAgentsLastMoves);
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
    protected I_ConstraintGroupingKey createDummy(Constraint constraint) {
        return constraintSet.createDummy(constraint);
    }

    @Override
    protected I_ConstraintGroupingKey createDummy(Move move) {
        return constraintSet.createDummy(move);
    }

    @Override
    public int lastRejectAt(I_Location target, Agent agent) {
        return constraintSet.lastRejectAt(target, agent);
    }

    @Override
    public List<Constraint> allConstraintsForPlan(SingleAgentPlan singleAgentPlan) {
        return constraintSet.allConstraintsForPlan(singleAgentPlan);
    }

    @Override
    public List<Constraint> allConstraintsForSolution(Solution solution) {
        return constraintSet.allConstraintsForSolution(solution);
    }

    @Override
    public boolean equals(Object o) {
        return constraintSet.equals(o);
    }

    @Override
    public int hashCode() {
        return constraintSet.hashCode();
    }
}
