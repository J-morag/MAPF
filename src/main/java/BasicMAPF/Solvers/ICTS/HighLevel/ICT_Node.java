package BasicMAPF.Solvers.ICTS.HighLevel;

import BasicMAPF.Instances.Agent;

import java.util.*;

public class ICT_Node {
    public Map<Agent, Integer> agentCost;
    /**
     * just the costs, sorted by the id of the relevant agent. For a more informed HashCode function. Important!
     */
    int[] costs;

    public ICT_Node(Map<Agent, Integer> agentCost) {
        this.agentCost = agentCost;
        this.costs = new int[agentCost.size()];
        ArrayList<Agent> sortedAgents = new ArrayList<>(agentCost.keySet());
        sortedAgents.sort(Comparator.comparingInt(a -> a.iD));
        for (int i = 0; i < sortedAgents.size(); i++) {
            this.costs[i] = agentCost.get(sortedAgents.get(i));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ICT_Node)) return false;

        ICT_Node ict_node = (ICT_Node) o;

        return Arrays.equals(costs, ict_node.costs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(costs);
    }

    public Map<Agent, Integer> getAgentCost() {
        return agentCost;
    }

    public int getCost(Agent agent){
        return agentCost.get(agent);
    }

    public List<ICT_Node> getChildren(){
        List<ICT_Node> children = new LinkedList<>();
        for(Agent agent : agentCost.keySet()){
            Map<Agent, Integer> current = new HashMap<>(agentCost);
            int currCost = agentCost.get(agent);
            current.put(agent, currCost + 1);
            ICT_Node child = new ICT_Node(current);
            children.add(child);
        }
        return children;
    }
}
