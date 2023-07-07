package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NaiveConflictDetection implements I_ConflictManager {

    private final HashMap<Agent, SingleAgentPlan> plans = new HashMap<>();

    @Override
    public void addPlan(SingleAgentPlan singleAgentPlan) {
        this.plans.put(singleAgentPlan.agent, singleAgentPlan);
    }

    /**
     * Returns the minimum time conflict.
     * @return {@inheritDoc}
     */
    @Override
    public A_Conflict selectConflict() {
        List<SingleAgentPlan> l_plans = new ArrayList<>(this.plans.values());
        A_Conflict minTimeConflict = null;
        for (int i = 0; i < l_plans.size(); i++) {
            SingleAgentPlan plan1 = l_plans.get(i);
            for (int j = i+1; j < l_plans.size(); j++) {
                SingleAgentPlan plan2 = l_plans.get(j);
                A_Conflict firstConflict = plan1.firstConflict(plan2);
                if(minTimeConflict == null ||
                        (firstConflict != null && firstConflict.time < minTimeConflict.time)){
                    minTimeConflict = firstConflict;
                }
            }
        }
        return minTimeConflict;
    }

    @Override
    public I_ConflictManager copy() {
        NaiveConflictDetection copy = new NaiveConflictDetection();
        for (SingleAgentPlan plan :
                this.plans.values()) {
            copy.addPlan(plan);
        }
        return copy;
    }
}
