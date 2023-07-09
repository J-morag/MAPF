package BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.DataTypesAndStructures.Solution;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class StochasticIndexBasedPartialSolutionsStrategy implements PartialSolutionsStrategy {

    private final int seed;
    private final double weight;
    private Random random;

    public StochasticIndexBasedPartialSolutionsStrategy(@Nullable Double weight, @Nullable Integer defaultSeedForRandom) {
        this.weight = Objects.requireNonNullElse(weight, 1.0);
        if (this.weight > 1.0 || this.weight < 0){
            throw new IllegalArgumentException("weight must be between 0 and 1");
        }
        this.seed = Objects.requireNonNullElse(defaultSeedForRandom, 42);
        setRandomNumberGenerator(new Random(this.seed));
    }

    @Override
    public boolean moveToNextPrPIteration(MAPF_Instance problemInstance, int attemptNumber, Solution solutionSoFar, Agent agentWeJustPlanned, int agentWeJustPlannedIndex, boolean failedToPlanForCurrentAgent, boolean alreadyFoundFullSolution) {
        double agentsLeftProportion = (problemInstance.agents.size() - agentWeJustPlannedIndex) / (double)problemInstance.agents.size();
        double threshold = agentsLeftProportion * weight;
        boolean move = random.nextDouble() < threshold;
        return failedToPlanForCurrentAgent && move;
    }

    @Override
    public void resetState(@Nullable Random newRandom) {
        setRandomNumberGenerator(newRandom);
    }

    private void setRandomNumberGenerator(@Nullable Random newRandom) {
        if (newRandom != null){
            this.random = newRandom;
        }
        this.random = new Random(this.seed);
    }

}
