package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;

import java.util.HashMap;
import java.util.Map;

public class OnlineSolution extends Solution{

    public final Map<Integer, Solution> solutionsAtTimes;

    public OnlineSolution(Map<Integer, Solution> solutionsAtTimes) {
        //make unified solution for super
        super(mergeSolutions(solutionsAtTimes));
        this.solutionsAtTimes = solutionsAtTimes;
    }

    private static Map<Agent, SingleAgentPlan> mergeSolutions(Map<Integer, Solution> solutionsAtTimes) {
        // fixme - this currently only works for Prioritised Planning, since it assumes no overlap between solutions/agents in solutionsAtTimes
        Map<Agent, SingleAgentPlan> merged = new HashMap<>();
        for (Solution s :
                solutionsAtTimes.values()) {
            for (SingleAgentPlan plan :
                    s) {
                merged.put(plan.agent, plan);
            }
        }
        return merged;
    }


}
