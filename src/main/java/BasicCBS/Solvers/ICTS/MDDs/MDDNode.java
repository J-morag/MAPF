package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;

import java.util.ArrayList;
import java.util.List;

public class MDDNode {
    private List<MDDNode> neighbors;
    private I_Location location;
    private int depth;
    private Agent agent;

    public MDDNode(MDDSearchNode current) {
        neighbors = new ArrayList<>(5); // 5 because typically we can move in 4 directions, or stay
        this.location = current.getLocation();
        this.depth = current.getG();
        this.agent = current.getAgent();
    }

    /**
     * copy constructor (deep). Doesn't copy the contents of {@link #neighbors}! Only creates an empty list.
     * @param other an MDDNode to copy.
     */
    public MDDNode(MDDNode other){
        neighbors = new ArrayList<>(5); // 5 because typically we can move in 4 directions, or stay
        this.location = other.location;
        this.depth = other.depth;
        this.agent = other.agent;
    }

    public void addNeighbor(MDDNode neighbor){
        neighbors.add(neighbor);
    }

    public List<MDDNode> getNeighbors() {
        return neighbors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MDDNode)) return false;

        MDDNode mddNode = (MDDNode) o;

        if (depth != mddNode.depth) return false;
        if (!location.equals(mddNode.location)) return false;
        return agent.equals(mddNode.agent);
    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + depth;
        result = 31 * result + agent.hashCode();
        return result;
    }

    public boolean sameLocation(MDDNode other){
        return this.getLocation().equals(other.getLocation());
    }

    @Override
    public String toString() {
        return "MDDNode{" +
                "location=" + location +
                ", depth=" + depth +
                ", agent=" + agent +
                '}';
    }

    public I_Location getLocation() {
        return location;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public Agent getAgent() {
        return agent;
    }
}
