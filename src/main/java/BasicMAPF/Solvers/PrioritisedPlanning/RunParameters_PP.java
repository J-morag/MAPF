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
    /**
     * The {@link PrioritisedPlanning_Solver} will use this as the priority of the {@link Agent}s, with lower index
     * {@link Agent}s being treated as having higher priority. In practise this means they will be planned for first,
     * and then avoided when planning for higher index {@link Agent}s.
     * If the {@link PrioritisedPlanning_Solver} is given an {@link BasicMAPF.Instances.MAPF_Instance} which contains agents not in
     * this collection, they will all be treated as having lower priority, and their internal order will be determined
     * arbitrarily. If this collection contains {@link Agent}s that are not in the {@link BasicMAPF.Instances.MAPF_Instance},
     * they will be ignored.
     */
    public final Agent[] preferredPriorityOrder;
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
