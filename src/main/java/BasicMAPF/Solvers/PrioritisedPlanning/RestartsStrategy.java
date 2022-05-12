package BasicMAPF.Solvers.PrioritisedPlanning;

import java.util.Objects;

/**
 * How to perform restarts.
 * DeterministicRescheduling is from: Andreychuk, Anton, and Konstantin Yakovlev. "Two techniques that enhance the performance of multi-robot prioritized path planning." arXiv preprint arXiv:1805.01270 (2018).
 * Set a number of initial restarts, and how to do them.
 * Set a method of performing restarts to try and find a solution for the contingency of no solution was found in the initial restarts.
 */
public class RestartsStrategy {

    public enum RestartsKind{
        randomRestarts, deterministicRescheduling, none
    }

    /**
     * How to perform restarts in the initial phase.
     */
    public final RestartsKind initialRestarts;

    /**
     * How many random restarts to perform initially. Will reorder the agents and re-plan this many times. will return the best solution found.
     * Total number of initial runs will be this + 1, or less if a timeout occurs (may still return a valid solution when that happens).
     */
    public final int numInitialRestarts;

    /**
     * How to perform restarts in the contingency that no solution is found in the initial phase.
     */
    public final RestartsKind contingencyRestarts;

    /**
     * @param initialRestarts how to do initial restarts
     * @param numInitialRestarts non negative
     * @param contingencyRestarts how to do restarts if no solution found
     */
    public RestartsStrategy(RestartsKind initialRestarts, Integer numInitialRestarts, RestartsKind contingencyRestarts) {
        if (numInitialRestarts != null && numInitialRestarts < 0){
            throw new IllegalArgumentException("numInitialRestarts must be non negative.");
        }

        this.initialRestarts = Objects.requireNonNullElse(initialRestarts, RestartsKind.none);
        this.numInitialRestarts = Objects.requireNonNullElse(numInitialRestarts, 0);
        this.contingencyRestarts = Objects.requireNonNullElse(contingencyRestarts, RestartsKind.none);

        if ((this.initialRestarts == RestartsKind.none && this.numInitialRestarts > 0) ||
                (this.initialRestarts != RestartsKind.none && this.numInitialRestarts == 0)){
            throw new IllegalArgumentException("initial restarts kind and number must make sense together.");
        }

    }
    /**
     * @param initialRestarts how to do initial restarts
     * @param numInitialRestarts non negative
     */
    public RestartsStrategy(RestartsKind initialRestarts, Integer numInitialRestarts) {
        this(initialRestarts, numInitialRestarts, null);
    }

    public RestartsStrategy() {
        this(null, null, null);
    }

    public boolean hasInitial(){
        // also enough to check just one because constructor enforces they make sense.
        return this.numInitialRestarts > 0 && this.initialRestarts != RestartsKind.none;
    }

    public boolean hasContingency(){
        return this.contingencyRestarts != RestartsKind.none;
    }

    public boolean isNoRestarts(){
        return !(this.hasInitial() || this.hasContingency());
    }

    @Override
    public String toString() {
        return  initialRestarts +
                " x" + numInitialRestarts +
                (contingencyRestarts != RestartsKind.none ? " + " + contingencyRestarts : "")
                ;
    }
}
