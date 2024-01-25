package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.*;

public class HighLevelNode {
    public HashMap<Integer, I_Location> configuration;
    public Queue<LowLevelNode> tree;

    public ArrayList<Agent> order;
    public HighLevelNode parent;
    public HashMap<Integer, Boolean> reachedGoalsMap;

    public Set<HighLevelNode> neighbors;

    public float g;
    public float h;
    public float f;

    public HighLevelNode(HashMap<Integer, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HighLevelNode parent, HashMap<Integer, Boolean> reachedGoalsMap, float g, float h) {
        this.configuration = configuration;
        this.tree = new LinkedList<>();
        this.tree.add(root);
        this.order = order;
        this.parent = parent;
        if (reachedGoalsMap == null) {
            this.reachedGoalsMap = new HashMap<>();
            for (Integer agentID : this.configuration.keySet()) {
                this.reachedGoalsMap.put(agentID, false);
            }
        }
        else {
            this.reachedGoalsMap = reachedGoalsMap;
        }

        this.neighbors = new HashSet<>();
        this.g = g;
        this.h = h;
        this.f = this.g + this.h;

    }
}
