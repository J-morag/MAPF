package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.PartialSolutionsStrategy;
import BasicMAPF.DataTypesAndStructures.RunParameters;

import java.util.Set;

/**
 * {@link RunParameters} for {@link PrioritisedPlanning_Solver}.
 */
public class RunParameters_PP extends RunParameters {
    public PartialSolutionsStrategy partialSolutionsStrategy;
    /**
     * Collect failed agents here
     */
    public Set<Agent> failedAgents;
    public RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable;

    public RunParameters_PP(RunParameters runParameters) {
        super(runParameters);
        this.preferredPriorityOrder = null;
    }

    public RunParameters_PP(RunParameters runParameters, Agent[] preferredPriorityOrder) {
        super(runParameters);
        this.preferredPriorityOrder = preferredPriorityOrder;
    }
}
