package BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.DataTypesAndStructures.Solution;

public class IndexBasedPartialSolutionsStrategy implements PartialSolutionsStrategy {

    private double cutoffPercent;

    public IndexBasedPartialSolutionsStrategy(double cutoffPercent) {
        this.cutoffPercent = cutoffPercent;
    }

    public double getCutoffPercent() {
        return cutoffPercent;
    }

    public void setCutoffPercent(double cutoffPercent) {
        this.cutoffPercent = cutoffPercent;
    }

    @Override
    public boolean moveToNextPrPIteration(MAPF_Instance problemInstance, int attemptNumber, Solution solutionSoFar, Agent agentWeJustPlanned, int agentWeJustPlannedIndex, boolean failedToPlanForCurrentAgent, boolean alreadyFoundFullSolution) {
        int numAgents = problemInstance.agents.size();
        return failedToPlanForCurrentAgent && agentWeJustPlannedIndex < numAgents * cutoffPercent;
    }
}
