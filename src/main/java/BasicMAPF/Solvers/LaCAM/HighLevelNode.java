package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.*;

public class HighLevelNode {
    public HashMap<Agent, I_Location> configuration;
    public Queue<LowLevelNode> tree;

    public ArrayList<Agent> order;
    public HashMap<Agent, Float> priorities;
    public HighLevelNode parent;
    public HashMap<Agent, Boolean> reachedGoalsMap;

    public Set<HighLevelNode> neighbors;

    public float g;
    public float h;
    public float f;

    public HighLevelNode(HashMap<Agent, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HashMap<Agent, Float> priorities, HighLevelNode parent, float g, float h) {
        this.configuration = configuration;
        this.tree = new LinkedList<>();
        this.tree.add(root);
        this.order = order;
        this.priorities = priorities;
        this.parent = parent;
        if (parent == null) {
            this.reachedGoalsMap = new HashMap<>();
            for (Agent agent : this.configuration.keySet()) {
                this.reachedGoalsMap.put(agent, false);
            }
        }
        else {
            this.reachedGoalsMap = parent.reachedGoalsMap;
        }

        this.neighbors = new HashSet<>();
        this.g = g;
        this.h = h;
        this.f = this.g + this.h;
    }
}
