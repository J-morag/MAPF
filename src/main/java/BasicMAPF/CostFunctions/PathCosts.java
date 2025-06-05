package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;

import java.util.ArrayList;
import java.util.Comparator;

public class PathCosts {
    public static final String NAME = "PathLengths";
    public static final PathCosts instance = new PathCosts();

    public String getPathCostsString(Solution solution) {
        StringBuilder sb = new StringBuilder();
        ArrayList<SingleAgentPlan> plans = new ArrayList<>(solution.size());
        for (SingleAgentPlan plan : solution) {
            plans.add(plan);
        }
        plans.sort(Comparator.comparingInt(o -> o.agent.iD));
        for (SingleAgentPlan plan : plans) {
            sb.append(plan.agent.iD).append(":").append(plan.size()).append(",");
        }
        return sb.toString();
    }
}