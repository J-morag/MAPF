package BasicMAPF.Solvers.ICTS.HighLevel;

import BasicMAPF.Instances.Agent;

public class ICT_NodeSumOfCostsComparator extends ICT_NodeComparator {
    @Override
    protected int costFunction(ICT_Node node) {
        int sum = 0;
        for (Agent agent : node.getAgentCost().keySet()) {
            sum += node.getCost(agent);
        }
        return sum;
    }
}
