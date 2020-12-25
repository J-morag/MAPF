package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Solvers.ICTS.LowLevel.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class MDDNode {
    private List<MDDNode> neighbors;
    private Node value;

    public MDDNode(Node current) {
        neighbors = new LinkedList<>();
        value = current;
    }

    public void addNeighbor(MDDNode neighbor){
        neighbors.add(neighbor);
    }

    public List<MDDNode> getNeighbors() {
        return neighbors;
    }

    public Node getValue(){
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MDDNode mddNode = (MDDNode) o;
        return value.equals(mddNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public boolean sameLocation(MDDNode other){
        return this.value.getLocation().equals(other.value.getLocation());
    }

    @Override
    public String toString() {
        return "MDDNode{" +
                "value=" + value +
                '}';
    }
}
