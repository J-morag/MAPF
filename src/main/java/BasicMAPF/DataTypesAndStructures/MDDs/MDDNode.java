package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MDDNode implements Comparable<MDDNode>{
    /**
     * The default capacity of the neighbors list is 5, because typically we can move in 4 directions, or stay.
     */
    private static final int DEFAULT_NEIGHBORS_CAPACITY = 5;

    private final List<MDDNode> neighbors;
    private final I_Location location;
    private final int depth;
    private final Agent agent;

    public MDDNode(MDDSearchNode current) {
        this(current.getLocation(), current.getG(), current.getAgent(), DEFAULT_NEIGHBORS_CAPACITY);
    }

    /**
     * copy constructor (deep). Doesn't copy the contents of {@link #neighbors}! Only creates an empty list.
     * @param other an MDDNode to copy.
     */
    public MDDNode(MDDNode other){
        this(other.location, other.depth, other.agent);
    }

    public MDDNode(I_Location location, int depth, Agent agent) {
        this(location, depth, agent, DEFAULT_NEIGHBORS_CAPACITY);
    }

    public MDDNode(I_Location location, int depth, Agent agent, int neighborsCapacity) {
        neighbors = new ArrayList<>(neighborsCapacity);
        this.location = location;
        this.depth = depth;
        this.agent = agent;
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
