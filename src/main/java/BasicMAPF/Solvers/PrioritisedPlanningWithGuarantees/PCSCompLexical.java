package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import Environment.Config;

import java.util.Arrays;
import java.util.Comparator;

public class PCSCompLexical implements Comparator<PaPSNode> {

    public static final PCSCompLexical DEFAULT_INSTANCE = new PCSCompLexical();

    @Override
    public int compare(PaPSNode o1, PaPSNode o2) {
        if (Config.DEBUG >=2 && !Arrays.equals(o1.priorityOrderedAgents(), o2.priorityOrderedAgents())){
            throw new IllegalArgumentException("PCSCompLexical: The PCSNodes have different agent orderings");
        }
        int res;
        for (int i = 0; i < o1.priorityOrderedAgents().length; i++) {
            int o1agentCost = o1.MDDs().size() > i ? o1.MDDs().get(i).getDepth() : o1.hArr()[i - o1.MDDs().size()];
            int o2agentCost = o2.MDDs().size() > i ? o2.MDDs().get(i).getDepth() : o2.hArr()[i - o2.MDDs().size()];
            res = Integer.compare(o1agentCost, o2agentCost);
            if (res != 0) {
                return res;
            }
        }
        res = Integer.compare(o2.MDDs().size(), o1.MDDs().size()); // reversed to prefer more MDDs
        if (res != 0) {
            return res;
        }
        res = Integer.compare(o1.getSumMddSizes(), o2.getSumMddSizes());
        if (res != 0){
            return res;
        }
        return Integer.compare(o1.uniqueID(), o2.uniqueID());
    }
}
