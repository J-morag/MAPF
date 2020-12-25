package BasicCBS.Solvers.ICTS.HighLevel;

import BasicCBS.Instances.Agent;

import java.util.*;

public class ICT_Node {
    private Map<Agent, Integer> agentCost;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ICT_Node ict_node = (ICT_Node) o;
        return agentCost.equals(ict_node.agentCost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentCost);
    }

    public ICT_Node(Map<Agent, Integer> agentCost) {
        this.agentCost = agentCost;
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
