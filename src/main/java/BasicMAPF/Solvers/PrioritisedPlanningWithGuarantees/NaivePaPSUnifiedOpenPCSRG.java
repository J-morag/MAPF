package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;

import java.util.Collection;
import java.util.List;

import static BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.NaivePaPS.getAllOrderPermutations;
import static Environment.Config.INFO;

public class NaivePaPSUnifiedOpenPCSRG implements PaPSRootGenerator {

    @Override
    public void generateRoot(MAPF_Instance instance, RunParameters runParameters, PathAndPrioritySearch pcs, Timeout timeout) {
        Collection<List<Agent>> permutations = getAllOrderPermutations(instance.agents);
        if (INFO >= 2) System.out.println("NaiveOPSUnifiedOpenPCSRG: Number of permutations: " + permutations.size());
        for (List<Agent> ordering : permutations) {
            if (timeout.isTimeoutExceeded()) {
                return;
            }
            if (INFO >= 2) System.out.println("NaiveOPSUnifiedOpenPCSRG: Generating root for ordering: " + ordering);
            pcs.generateRoot(ordering.toArray(new Agent[0]));
        }
    }

}
