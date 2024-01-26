package BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Metrics.Metrics;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class AdaptiveIndexPartialSolutionsStrategy implements PartialSolutionsStrategy {

    private final double increment;
    private final int seed;
    private final double initialCutoffPercent;
    private final IndexBasedPartialSolutionsStrategy indexBasedPartialSolutionsStrategy;
    private Random random;

    public AdaptiveIndexPartialSolutionsStrategy(@Nullable Double initialCutoffPercent, @Nullable Double increment, @Nullable Integer seedForEveryTimeStateIsReset) {
        this.initialCutoffPercent = Objects.requireNonNullElse(initialCutoffPercent, 0.5);
        this.indexBasedPartialSolutionsStrategy = new IndexBasedPartialSolutionsStrategy(this.initialCutoffPercent);
        setIndexBasedStrategy();
        this.increment = Objects.requireNonNullElse(increment, 0.1);
        this.seed = Objects.requireNonNullElse(seedForEveryTimeStateIsReset, 42);
        setRandomNumberGenerator(new Random(this.seed));
    }

    @Override
    public boolean moveToNextPrPIteration(MAPF_Instance problemInstance, int attemptNumber, Solution solutionSoFar, Agent agentWeJustPlanned, int agentWeJustPlannedIndex, boolean failedToPlanForCurrentAgent, boolean alreadyFoundFullSolution) {
        return failedToPlanForCurrentAgent &&
                indexBasedPartialSolutionsStrategy.moveToNextPrPIteration(problemInstance, attemptNumber, solutionSoFar, agentWeJustPlanned, agentWeJustPlannedIndex, failedToPlanForCurrentAgent, alreadyFoundFullSolution);
    }

    @Override
    public void updateAfterSolution(int totalNumAgents, int numSolvedAgents) {
        if (totalNumAgents < 1 || numSolvedAgents < 0 || numSolvedAgents > totalNumAgents){
            throw new IllegalArgumentException();
        }
        if ( numSolvedAgents != totalNumAgents){
            double failProportion = ((double)totalNumAgents - numSolvedAgents) / totalNumAgents;
            double step = increment * (random.nextBoolean() ? -1 : 1) * failProportion;
            double newCutoff = indexBasedPartialSolutionsStrategy.getCutoffPercent() + step;
            if (newCutoff > 0.0 && newCutoff < 1.0){
                indexBasedPartialSolutionsStrategy.setCutoffPercent(newCutoff);
            }
        }
    }

    @Override
    public void resetState(@Nullable Random newRandom) {
        Metrics.getMostRecentInstanceReport().putFloatValue("Adaptive Index reached cutoff", (float) this.indexBasedPartialSolutionsStrategy.getCutoffPercent());
        setIndexBasedStrategy();
        setRandomNumberGenerator(newRandom);
    }

    private void setRandomNumberGenerator(@Nullable Random newRandom) {
        if (newRandom != null){
            this.random = newRandom;
        }
        this.random = new Random(this.seed);
    }

    private void setIndexBasedStrategy() {
        this.indexBasedPartialSolutionsStrategy.setCutoffPercent(this.initialCutoffPercent);
    }
}
