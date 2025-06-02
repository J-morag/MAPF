package BasicMAPF.Solvers.PrioritisedPlanning;

import java.util.Objects;

/**
 * How to perform restarts.
 * DeterministicRescheduling is from: Andreychuk, Anton, and Konstantin Yakovlev. "Two techniques that enhance the performance of multi-robot prioritized path planning." arXiv preprint arXiv:1805.01270 (2018).
 * Set a number of initial restarts, and how to do them.
 * Set a method of performing restarts to try and find a solution for the contingency of no solution was found in the initial restarts.
 */
public class RestartsStrategy {

    public enum reorderingStrategy {
        randomRestarts, deterministicRescheduling, none
    }

    /**
     * How to perform restarts in the initial phase.
     */
    public final reorderingStrategy initialRestarts;

    /**
     * positive. Will try to do at least this number of attempts, unless interrupted.
     */
    public final int minAttempts;

    /**
     * How to perform restarts in the contingency that no solution is found in the initial phase.
     */
    public final reorderingStrategy contingencyRestarts;

    /**
     * Whether to randomize the A* search.
     */
    public boolean randomizeAStar;
    /**
     * @param initialRestarts     how to do initial restarts
     * @param minAttempts non-negative. Will try to do at least this number of attempts, unless interrupted.
     * @param contingencyRestarts how to do restarts if no solution found
     * @param randomizeAStar whether to randomize the A* search.
     */
    public RestartsStrategy(reorderingStrategy initialRestarts, Integer minAttempts, reorderingStrategy contingencyRestarts, Boolean randomizeAStar) {
        if (minAttempts != null && minAttempts < 1){
            throw new IllegalArgumentException("minAttempts must be non negative.");
        }

        this.initialRestarts = Objects.requireNonNullElse(initialRestarts, reorderingStrategy.none);
        this.minAttempts = Objects.requireNonNullElse(minAttempts, 1);
        this.contingencyRestarts = Objects.requireNonNullElse(contingencyRestarts, reorderingStrategy.none);
        this.randomizeAStar = Objects.requireNonNullElse(randomizeAStar, false);

        if ((this.initialRestarts == reorderingStrategy.none && !this.randomizeAStar && this.minAttempts > 1 ) ){
            throw new IllegalArgumentException("initial restarts kind and number must make sense together.");
        }

    }

    public RestartsStrategy() {
        this(null, null, null, null);
    }

    public boolean hasInitial(){
        // also enough to check just one because constructor enforces they make sense.
        return this.minAttempts > 1 && this.initialRestarts != reorderingStrategy.none;
    }

    public boolean hasContingency(){
        return this.contingencyRestarts != reorderingStrategy.none;
    }

    public boolean isNoRestarts(){
        return !(this.hasInitial() || this.hasContingency());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (minAttempts < Integer.MAX_VALUE){
            sb.append("min. ").append(minAttempts).append(" attempts");
        }
        if (hasInitial()){
            if (!sb.isEmpty()){
                sb.append(", ");
            }
            sb.append("initial reorderings: ").append(initialRestarts);
        }
        if (hasContingency()){
            if (!sb.isEmpty()){
                sb.append(", ");
            }
            sb.append("contingency reorderings: ").append(contingencyRestarts);
        }
        return sb.toString();
    }
}
