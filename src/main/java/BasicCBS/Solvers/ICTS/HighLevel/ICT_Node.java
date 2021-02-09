package BasicCBS.Solvers.ICTS.HighLevel;

import BasicCBS.Instances.Agent;

import java.util.*;

public class ICT_Node {
    public Map<Agent, Integer> agentCosts;
    /**
     * just the costs, sorted by the id of the relevant agent. For a more informed HashCode function. Important!
     */
    int[] costs;

    public ICT_Node(Map<Agent, Integer> agentCosts) {
        this.agentCosts = agentCosts;
        this.costs = new int[agentCosts.size()];
        ArrayList<Agent> sortedAgents = new ArrayList<>(agentCosts.keySet());
        sortedAgents.sort(Comparator.comparingInt(a -> a.iD));
        for (int i = 0; i < sortedAgents.size(); i++) {
            this.costs[i] = agentCosts.get(sortedAgents.get(i));
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
        return agentCosts;
    }

    public void setAgentCost(Map<Agent, Integer> agentCosts) {
        this.agentCosts = agentCosts;
        this.costs = new int[agentCosts.size()];
        ArrayList<Agent> sortedAgents = new ArrayList<>(agentCosts.keySet());
        sortedAgents.sort(Comparator.comparingInt(a -> a.iD));
        for (int i = 0; i < sortedAgents.size(); i++) {
            this.costs[i] = agentCosts.get(sortedAgents.get(i));
        }
    }

    public int getCost(Agent agent){
        return agentCosts.get(agent);
    }

    public List<ICT_Node> getChildren(){
        List<ICT_Node> children = new LinkedList<>();
        for(Agent agent : agentCosts.keySet()){
            Map<Agent, Integer> current = new HashMap<>(agentCosts);
            int currCost = agentCosts.get(agent);
            current.put(agent, currCost + 1);
            ICT_Node child = new ICT_Node(current);
            children.add(child);
        }
        return children;
    }
}
