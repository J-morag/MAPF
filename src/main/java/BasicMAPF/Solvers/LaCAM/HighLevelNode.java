package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class HighLevelNode {
    public HashMap<Integer, I_Location> configuration;
    public Queue<LowLevelNode> tree;

    public ArrayList<Agent> order;
    public HighLevelNode parent;
    public HashMap<Integer, Boolean> reachedGoalsMap;

    public HighLevelNode(HashMap<Integer, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HighLevelNode parent, HashMap<Integer, Boolean> reachedGoalsMap) {
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

    }
}
