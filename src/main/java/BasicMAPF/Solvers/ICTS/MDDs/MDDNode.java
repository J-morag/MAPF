package BasicMAPF.Solvers.ICTS.MDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MDDNode implements Comparable<MDDNode>{
    private final List<MDDNode> neighbors;
    private final I_Location location;
    private final int depth;
    private final Agent agent;

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
    public int hashCode() { // todo cache it?
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
                "location=" + location.getCoordinate() +
                ", depth=" + depth +
                '}';
    }

    public I_Location getLocation() {
        return location;
    }

    public int getDepth() {
        return depth;
    }

    public Agent getAgent() {
        return agent;
    }

    /**
     * Doesn't obey {@link #equals(Object)}
     * @param o the object to be compared.
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(@NotNull MDDNode o) {
        return this.location.compareTo(o.location);
    }
}
