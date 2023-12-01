package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.MDDs.MDDNode;

import java.util.*;

/**
 * This class is for merging two or more MDDNodes, when we want to perform a goal test on the ICT node
 */
public class MergedMDDNode {
    private static final Comparator<MDDNode> mddNodesOrderComparator = Comparator.comparingInt(mddNode -> mddNode.getAgent().iD);
    private List<MDDNode> mddNodes;
    private List<MergedMDDNode> parents;
    private int depth;

    /**
     * @param depth depth in the merged mdd
     * @param numAgents the number of agents represented by the merged mdd. used for more efficient initialization of lists.
     */
    public MergedMDDNode(int depth, int numAgents) {
        mddNodes = new ArrayList<>(numAgents);
        parents = new ArrayList<>(numAgents);
        this.depth = depth;
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

    public void setMDDNodes(List<MDDNode> mddNodes){
        this.mddNodes = mddNodes;
        // we sort them so we can use them in more informed equals() and hashCode() functions.
        this.mddNodes.sort(mddNodesOrderComparator);
    }

    public List<MDDNode> getMddNodes() {
        return mddNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedMDDNode)) return false;

        MergedMDDNode that = (MergedMDDNode) o;

        if (depth != that.depth) return false;
        return mddNodes.equals(that.mddNodes);
    }

    @Override
    public int hashCode() {
        int result = mddNodes.hashCode();
        result = 31 * result + depth;
        return result;
    }

    public int getDepth() {
        return depth;
    }

    public Map<Agent, List<FatherSonMDDNodePair>> getFatherSonPairs() {
        Map<Agent, List<FatherSonMDDNodePair>> fatherSonPairs = new HashMap<>();

        for (MDDNode father : mddNodes) {
            List<MDDNode> currentChildren = father.getNeighbors();
            List<FatherSonMDDNodePair> currentFatherSonPairs = new LinkedList<>();
            for (MDDNode children : currentChildren) {
                FatherSonMDDNodePair fatherSonMDDNodePair = new FatherSonMDDNodePair(father, children);
                currentFatherSonPairs.add(fatherSonMDDNodePair);
            }
            if(currentFatherSonPairs.isEmpty()){
                FatherSonMDDNodePair goalPair = new FatherSonMDDNodePair(father, father);
                currentFatherSonPairs.add(goalPair);
            }
            fatherSonPairs.put(father.getAgent(), currentFatherSonPairs);
        }

        return fatherSonPairs;
    }

    /**
     * Each list in this list is all moves that a single agent can make on their MDD from the mdd node that we have for
     * that agent in this current merged MDD node
     * @return
     */
    public List<List<FatherSonMDDNodePair>> getFatherSonPairsLists() {
        List<List<FatherSonMDDNodePair>> fatherSonPairs = new ArrayList<>(mddNodes.size());

        for (MDDNode father : mddNodes) {
            List<MDDNode> currentChildren = father.getNeighbors();
            List<FatherSonMDDNodePair> currentFatherSonPairs = new ArrayList<>(currentChildren.size());
            for (MDDNode children : currentChildren) {
                FatherSonMDDNodePair fatherSonMDDNodePair = new FatherSonMDDNodePair(father, children);
                currentFatherSonPairs.add(fatherSonMDDNodePair);
            }
            if(currentFatherSonPairs.isEmpty()){
                // we are at the deepest node of the MDD
                FatherSonMDDNodePair goalPair = new FatherSonMDDNodePair(father, father);
                currentFatherSonPairs.add(goalPair);
            }
            fatherSonPairs.add(currentFatherSonPairs);
        }

        return fatherSonPairs;
    }

    @Override
    public String toString() {
        return "MergedMDDNode{" +
                "values=" + mddNodes +
                ", depth=" + depth +
                '}';
    }
}