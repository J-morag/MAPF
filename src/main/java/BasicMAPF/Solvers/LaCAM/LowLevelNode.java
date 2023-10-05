package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

public class LowLevelNode {
    public LowLevelNode parent;
    public Agent who;
    public I_Location where;
    public int depth;

    public LowLevelNode(LowLevelNode parent, Agent who, I_Location where) {
        this.parent = parent;
        this.who = who;
        this.where = where;
        this.depth = parent.depth + 1;
    }
}
