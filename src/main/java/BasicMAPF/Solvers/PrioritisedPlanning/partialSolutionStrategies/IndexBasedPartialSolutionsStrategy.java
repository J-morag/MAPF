package BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Solution;

public class IndexBasedPartialSolutionsStrategy implements PartialSolutionsStrategy {

    private final double cutoffPercent;

    public IndexBasedPartialSolutionsStrategy(double cutoffPercent) {
        this.cutoffPercent = cutoffPercent;
    }

    @Override
    public boolean moveToNextPrPIteration(MAPF_Instance problemInstance, int attemptNumber, Solution solutionSoFar, Agent agentWeJustPlanned, int agentWeJustPlannedIndex, boolean failedToPlanForCurrentAgent, boolean alreadyFoundFullSolution) {
        int numAgents = problemInstance.agents.size();
        return agentWeJustPlannedIndex < numAgents * cutoffPercent;
    }
}
