package BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Solution;

public interface PartialSolutionsStrategy {

    boolean moveToNextPrPIteration(MAPF_Instance problemInstance, int attemptNumber, Solution solutionSoFar,
                                   Agent agentWeJustPlanned, int agentWeJustPlannedIndex,
                                   boolean failedToPlanForCurrentAgent);

    default boolean allowed(){
        return true;
    }

}