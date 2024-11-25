package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

/**
 * A class relevant to LaCAM solver.
 * Low-Level node is a search tree of each High-Level node in LaCAM.
 * The node represents a constraint, meaning that a specific agent need to be in a specific location.
 */
public class LowLevelNode {
    public LowLevelNode parent;
    public Agent who;
    public I_Location where;
    public int depth;

    public LowLevelNode(LowLevelNode parent, Agent who, I_Location where) {
        this.parent = parent;
        this.who = who;
        this.where = where;
        if (parent == null) {
            this.depth = 0;
        }
        else {
            this.depth = parent.depth + 1;
        }
    }
}
