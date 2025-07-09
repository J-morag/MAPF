package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Metrics.InstanceReport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PathDelays {
    public static final String NAME = "PathDelays";
    public static final PathDelays instance = new PathDelays();

    public String getPathDelaysString(Solution solution, InstanceReport report) {
        StringBuilder sb = new StringBuilder();
        ArrayList<SingleAgentPlan> plans = new ArrayList<>(solution.size());
        Map<Integer, Integer> freespaceCosts = parseFreespaceString(report);

        // Collect all plans
        for (SingleAgentPlan plan : solution) {
            plans.add(plan);
        }

        // Sort by agent ID for consistent representation
        plans.sort(Comparator.comparingInt(o -> o.agent.iD));

        // Calculate delays and build the string
        for (SingleAgentPlan plan : plans) {
            int agentID = plan.agent.iD;
            int pathCost = plan.size();
            int freespaceCost = freespaceCosts.get(agentID);
            int delay = pathCost - freespaceCost;

            sb.append(agentID).append(":").append(delay).append(",");
        }

        return sb.toString();
    }

    private Map<Integer, Integer> parseFreespaceString(InstanceReport report) {
        Map<Integer, Integer> result = new HashMap<>();
        String freespaceString = report.getStringValue("FreespaceCosts");

        if (freespaceString == null || freespaceString.isEmpty()) {
            return result;
        }

        // Parse the string format "agentID:cost,agentID:cost,..."
        String[] entries = freespaceString.split(",");
        for (String entry : entries) {
            if (entry.isEmpty()) continue;

            String[] parts = entry.split(":");
            if (parts.length == 2) {
                try {
                    int agentID = Integer.parseInt(parts[0]);
                    int cost = Integer.parseInt(parts[1]);
                    result.put(agentID, cost);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid freespace cost format: " + entry, e);
                }
            }
        }

        return result;
    }
}

