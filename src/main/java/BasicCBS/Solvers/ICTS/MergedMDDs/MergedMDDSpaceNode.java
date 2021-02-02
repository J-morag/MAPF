package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.MDDs.MDDNode;

import java.util.*;

/**
 * Search node for searching the search space of a merged MDD (MDD from MDDs of multiple agents) without explicitly
 * creating the merged MDD.
 */
public class MergedMDDSpaceNode {
    private static final Comparator<MDDNode> mddNodesOrderComparator = Comparator.comparingInt(mddNode -> mddNode.getAgent().iD);
//    private TreeSet<MDDNode> mddNodes = new TreeSet<>(mddNodesOrderComparator);
    private List<MDDNode> mddNodes;
    private MergedMDDSpaceNode parent;
    private int depth;
    private boolean disappearAtGoal;
    public List<FatherSonMDDNodePair> generatingMoves;

    public MergedMDDSpaceNode(MergedMDDSpaceNode parent, int depth, boolean disappearAtGoal) {
        this.parent = parent;
        this.depth = depth;
        this.disappearAtGoal = disappearAtGoal;
    }

    public MergedMDDSpaceNode(MergedMDDSpaceNode parent, int depth, boolean disappearAtGoal, List<MDDNode> mddNodes) {
        this.parent = parent;
        this.depth = depth;
        this.disappearAtGoal = disappearAtGoal;
        this.setMDDNodes(mddNodes);
    }

    public MergedMDDSpaceNode() {
        this.parent = null;
        this.depth = 0;
        this.disappearAtGoal = false;
    }

    public MergedMDDSpaceNode(MergedMDDSpaceNode parent, int depth) {
        this(parent, depth, false);
    }

    public MergedMDDSpaceNode(MergedMDDSpaceNode parent, int depth, List<MDDNode> mddNodes) {
        this(parent, depth, false, mddNodes);
    }

    public MergedMDDSpaceNode getParent() {
        return parent;
    }

    public void setMDDNodes(List<MDDNode> mddNodes){
        this.mddNodes = mddNodes;
        // we sort them so we can use them in more informed equals() and hashCode() functions.
        this.mddNodes.sort(mddNodesOrderComparator);
    }

    /**
     * Save the moves that generated this node. This is so we can delay the computationally expensive validation of the
     * combination of moves until we poll this node from open.
     * @param generatingMoves the moves that created this node from a previous node. can be null to remove (save heap space) after verifying this node.
     */
    public void setGeneratingMovesForLaterVerification(List<FatherSonMDDNodePair> generatingMoves){
        this.generatingMoves = generatingMoves;
    }

    public List<MDDNode> getMddNodes() {
        return mddNodes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MergedMDDSpaceNode)) return false;

        MergedMDDSpaceNode that = (MergedMDDSpaceNode) o;

        return mddNodes.equals(that.mddNodes);
    }

    @Override
    public int hashCode() {
        return mddNodes.hashCode();
    }

    public int getDepth() {
        return depth;
    }

    public Map<Agent, List<FatherSonMDDNodePair>> getFatherSonPairsMap() {
        Map<Agent, List<FatherSonMDDNodePair>> fatherSonPairs = new HashMap<>();

        for (MDDNode father : mddNodes) {
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

    public List<List<FatherSonMDDNodePair>> getFatherSonPairsLists() {
        List<List<FatherSonMDDNodePair>> fatherSonPairs = new LinkedList<>();

        for (MDDNode father : mddNodes) {
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
            fatherSonPairs.add(currentFatherSonPairs);
        }

        return fatherSonPairs;
    }

    @Override
    public String toString() {
        return "MergedMDDNode{" +
                "mddNodes=" + mddNodes +
                ", depth=" + depth +
                '}';
    }

}
