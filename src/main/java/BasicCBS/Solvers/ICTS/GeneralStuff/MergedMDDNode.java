package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Instances.Agent;

import java.util.*;

/**
 * This class is for merging two or more MDDNodes, when we want to perform a goal test on the ICT node
 */
public class MergedMDDNode {
    private List<MergedMDDNode> neighbors;
    private Map<Agent, MDDNode> values;
    private List<MergedMDDNode> parents;
    private int depth;
    private boolean disappearAtGoal;

    public MergedMDDNode(int depth, boolean disappearAtGoal) {
        neighbors = new LinkedList<>();
        values = new HashMap<>();
        parents = new LinkedList<>();
        this.depth = depth;
        this.disappearAtGoal = disappearAtGoal;
    }
    public MergedMDDNode(int depth) {
        this(depth, false);
    }

    public void addParent(MergedMDDNode parent){
        parents.add(parent);
    }

    public void addParents(List<MergedMDDNode> otherParents){
        this.parents.addAll(otherParents);
    }

    public List<MergedMDDNode> getParents() {
        return parents;
    }

    public void addNeighbor(MergedMDDNode neighbor){
        neighbors.add(neighbor);
    }

    public void addValue(MDDNode value){
        values.put(value.getValue().getAgent(), value);
    }

    public MDDNode getValue(Agent agent){
        return values.get(agent);
    }

    public List<MergedMDDNode> getNeighbors() {
        return neighbors;
    }

    public Map<Agent, MDDNode> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergedMDDNode that = (MergedMDDNode) o;
        return values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    public int getDepth() {
        return depth;
    }

    public Map<Agent, List<FatherSonMDDNodePair>> getFatherSonPairs() {
        Map<Agent, List<FatherSonMDDNodePair>> fatherSonPairs = new HashMap<>();

        for (Agent agent : values.keySet()) {
            MDDNode father = values.get(agent);
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
            fatherSonPairs.put(agent, currentFatherSonPairs);
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
