package BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Solution;

public class DeepPartialSolutionsStrategy implements PartialSolutionsStrategy {
    @Override
    public boolean moveToNextPrPIteration(MAPF_Instance problemInstance, int attemptNumber, Solution solutionSoFar,
                                          Agent agentWeJustPlanned, int agentWeJustPlannedIndex,
                                          boolean failedToPlanForCurrentAgent, boolean alreadyFoundFullSolution) {
        return false;
    }
}
