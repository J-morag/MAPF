package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.MAPF_Instance;


public interface PaPSRootGenerator {
    void generateRoot(MAPF_Instance instance, RunParameters runParameters, PathAndPrioritySearch pcs, Timeout timeout);
}
