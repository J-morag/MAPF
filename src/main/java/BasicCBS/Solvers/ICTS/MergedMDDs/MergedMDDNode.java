package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.MDDs.MDDNode;

import java.util.*;

/**
 * This class is for merging two or more MDDNodes, when we want to perform a goal test on the ICT node
 */
public class MergedMDDNode {
    private Set<MergedMDDNode> neighbors;
    private Set<MDDNode> values;
    private Set<MergedMDDNode> parents;
    private int depth;
    private boolean disappearAtGoal;

    public MergedMDDNode(int depth, boolean disappearAtGoal) {
        neighbors = new HashSet<>();
        values = new HashSet<>();
        parents = new HashSet<>();
        this.depth = depth;
        this.disappearAtGoal = disappearAtGoal;
    }
    public MergedMDDNode(int depth) {
        this(depth, false);
    }

    public void addParent(MergedMDDNode parent){
        parents.add(parent);
    }

    public void addParents(Set<MergedMDDNode> otherParents){
        this.parents.addAll(otherParents);
    }

    public Set<MergedMDDNode> getParents() {
        return parents;
    }

    public void addNeighbor(MergedMDDNode neighbor){
        neighbors.add(neighbor);
    }

    public void addValue(MDDNode value){
        values.add(value);
    }

    public Set<MergedMDDNode> getNeighbors() {
        return neighbors;
    }

    public Set<MDDNode> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedMDDNode)) return false;

        MergedMDDNode that = (MergedMDDNode) o;

        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    public int getDepth() {
        return depth;
    }

    public Map<Agent, List<FatherSonMDDNodePair>> getFatherSonPairs() {
        Map<Agent, List<FatherSonMDDNodePair>> fatherSonPairs = new HashMap<>();

        for (MDDNode father : values) {
            List<MDDNode> currentChildren = father.getNeighbors();
            List<FatherSonMDDNodePair> currentFatherSonPairs = new LinkedList<>();
            for (MDDNode children : currentChildren) {
                FatherSonMDDNodePair fatherSonMDDNodePair = new FatherSonMDDNodePair(father, children, disappearAtGoal);
                currentFatherSonPairs.add(fatherSonMDDNodePair);
            }
            if(currentFatherSonPairs.isEmpty()){
                // we are at the deepest node of the MDD
                FatherSonMDDNodePair goalPair = new FatherSonMDDNodePair(father, father, disappearAtGoal);
                currentFatherSonPairs.add(goalPair);
            }
            fatherSonPairs.put(father.getAgent(), currentFatherSonPairs);
        }

        return fatherSonPairs;
    }

    @Override
    public String toString() {
        return "MergedMDDNode{" +
                "values=" + values +
                ", depth=" + depth +
                '}';
    }

    public void fixNeighbor(MergedMDDNode inOpen) {
        neighbors.remove(inOpen);
        neighbors.add(inOpen);
    }
}
