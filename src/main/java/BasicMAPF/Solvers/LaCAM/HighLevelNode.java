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

    public HighLevelNode(HashMap<Integer, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HighLevelNode parent) {
        this.configuration = configuration;
        this.tree = new LinkedList<>();
        this.tree.add(root);
        this.order = order;
        this.parent = parent;
    }
}
