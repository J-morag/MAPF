package BasicCBS.Solvers.ICTS.HighLevel;

import BasicCBS.Instances.Agent;

public class ICT_NodeSumOfCostsComparator extends ICT_NodeComparator {
    @Override
    protected int costFunction(ICT_Node node) {
        int sum = 0;
        for (Agent agent : node.getAgentCost().keySet()) {
            sum += node.getCost(agent)
                    * agent.priority; // will be 1 by default (no priorities variant)
        }
        return sum;
    }
}
