package BasicCBS.Solvers.ICTS.HighLevel;

import BasicCBS.Instances.Agent;

public class ICT_NodeMakespanComparator extends ICT_NodeComparator{
    @Override
    protected int costFunction(ICT_Node node) {
        int max = Integer.MIN_VALUE;
        for (Agent agent : node.getAgentCost().keySet()) {
            int currCost = node.getCost(agent);
            if(currCost > max){
                max = currCost;
            }
        }
        return max;
    }
}
