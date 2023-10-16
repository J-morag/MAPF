package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.Instances.Agent;
import BasicMAPF.DataTypesAndStructures.RunParameters;

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

    public RunParameters_PP(RunParameters runParameters, Agent[] preferredPriorityOrder) {
        super(runParameters);
        this.preferredPriorityOrder = preferredPriorityOrder;
    }
}
