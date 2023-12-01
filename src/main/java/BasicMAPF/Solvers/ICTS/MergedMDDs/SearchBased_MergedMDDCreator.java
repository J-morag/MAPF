package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.DataTypesAndStructures.MDDs.MDD;
import BasicMAPF.DataTypesAndStructures.MDDs.MDDNode;
import BasicMAPF.DataTypesAndStructures.Solution;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import java.util.*;

public abstract class SearchBased_MergedMDDCreator implements I_MergedMDDSolver, I_MergedMDDCreator {
    private int goalDepth;
    private final boolean DISAPPEAR_AT_GOAL = false;
    private int expandedLowLevelNodes;
    private int generatedLowLevelNodes;
    protected Map<MergedMDDNode, MergedMDDNode> contentOfOpen;
    private static final Comparator<List<FatherSonMDDNodePair>> fatherSonPairListsAgentIdComparator = Comparator.comparingInt(list -> list.get(0).getFather().getAgent().iD);
    private static final boolean debug = false;

    protected void initializeSearch(){
        this.expandedLowLevelNodes = 0;
        this.generatedLowLevelNodes = 0;
        this.contentOfOpen = new HashMap<>();
    }

    protected abstract boolean isOpenEmpty();

    protected abstract void addToClosed(MergedMDDNode node);

    @Override
    public Solution findJointSolution(Map<Agent, MDD> agentMDDs, Timeout timout) {
        MergedMDD mergedMDD = this.createMergedMDD(agentMDDs, timout);
        return mergedMDD != null ? mergedMDD.getSolution() : null;
    }

    private MergedMDDNode getRoot(Map<Agent, MDD> agentMDDs) {
        MergedMDDNode root = new MergedMDDNode(0,  agentMDDs.size());
        ArrayList<MDDNode> rootContents = new ArrayList<>(agentMDDs.size());
        for (MDD agentMDD : agentMDDs.values()){
            rootContents.add(agentMDD.getStart());
        }
        root.setMDDNodes(rootContents);
        return root;
    }

    @Override
    public MergedMDD createMergedMDD(Map<Agent, MDD> agentMDDs, Timeout timout) {
        initializeSearch();

        MergedMDD mergedMDD = new MergedMDD();

        MergedMDDNode root = getRoot(agentMDDs);
        mergedMDD.setStart(root);
        addToOpen(root);

        goalDepth = 0;

        for (Agent agent : agentMDDs.keySet()) {
            MDD mdd = agentMDDs.get(agent);
//            start.addValue(mdd.getStart());

            int currentDepth = mdd.getDepth();
            if (currentDepth > goalDepth) {
                goalDepth = currentDepth;
            }
        }

        // create the joint MDD
        while (!isOpenEmpty() && !timout.isTimeoutExceeded()) {
            MergedMDDNode current = pollFromOpen();
//            if (expandedLowLevelNodes % 100000 == 0 ) System.out.println(expandedLowLevelNodes);
            if (isGoal(current)) {
                mergedMDD.setGoal(current);
                releaseMemory();
                return mergedMDD;
            }
            expand(current);
        }

        releaseMemory();
        return null;
    }

    protected void releaseMemory(){
        contentOfOpen = null;
    }

    protected boolean isValidCombination(List<FatherSonMDDNodePair> currentCombination) {
        for (int i = 0; i < currentCombination.size(); i++) {
            FatherSonMDDNodePair currentI = currentCombination.get(i);
            for (int j = i + 1; j < currentCombination.size(); j++) {
                FatherSonMDDNodePair currentJ = currentCombination.get(j);
                if (currentI.equals(currentJ)) {
                    try {
                        throw new Exception("currentI and currentJ can't be equals. if they are, we have an error...");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (currentCombination.get(i).colliding(currentCombination.get(j), DISAPPEAR_AT_GOAL)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected MergedMDDNode getMergedMDDNode(int mddNodeDepth, int numAgents) {
        return new MergedMDDNode(mddNodeDepth, numAgents);
    }

    protected void expand(MergedMDDNode current) {
        this.expandedLowLevelNodes++;
        List<MergedMDDNode> neighbors = getChildren(current);
        for (MergedMDDNode neighbor : neighbors) {
            this.generatedLowLevelNodes++;
            addToOpen(neighbor);
        }
        addToClosed(current);
    }

    protected abstract void addToOpen(MergedMDDNode node);

    protected abstract MergedMDDNode pollFromOpen();

    protected List<MergedMDDNode> getChildren(MergedMDDNode current) {
        // get cartesian product (all combinations of moves from each mdd in the mergedMDD node).
        List<List<FatherSonMDDNodePair>> fatherSonPairLists = current.getFatherSonPairsLists();
        // sort these is advance, so that we will get lists that are already sorted by agent (cartesianProduct maintains
        // order), so that we won't have to re-sort in each call to MergedMDDNode.setMDDNodes .
        if (debug && !Ordering.from(fatherSonPairListsAgentIdComparator).isOrdered(fatherSonPairLists)){
            try {
                throw new Exception("since all actions are deterministic and on lists, and the lists are initially " +
                        "ordered, fatherSonPairLists should already be ordered.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        fatherSonPairLists.sort(fatherSonPairListsAgentIdComparaor);
        // copy to new list because cartesianProduct returns UnmodifiableList
        List<List<FatherSonMDDNodePair>> fatherSonPairCartesianProduct = new ArrayList<>(Lists.cartesianProduct(fatherSonPairLists));
        // validation postponed to after checking if a node is in open already (and therefore valid)
//        fatherSonPairCartesianProduct.removeIf(l -> !isValidCombination(l));

        // turn into new merged mdd nodes
        List<MergedMDDNode> children = new ArrayList<>(fatherSonPairCartesianProduct.size());
        for (List<FatherSonMDDNodePair> fatherSonPairCombination : fatherSonPairCartesianProduct){
            ArrayList<MDDNode> mddNodes = new ArrayList<>(fatherSonPairCombination.size());
            MergedMDDNode child = getMergedMDDNode(current.getDepth() + 1, fatherSonPairCombination.size());
            for (FatherSonMDDNodePair move : fatherSonPairCombination){
                mddNodes.add(move.getSon());
            }
            child.setMDDNodes(mddNodes);
            // try to avoid repeat validations if at all possible, since they are expensive
            if(contentOfOpen.containsKey(child) || isValidCombination(fatherSonPairCombination)){
                child.addParent(current);
                children.add(child);
            }
        }
        return children;
    }

    protected boolean isGoal(MergedMDDNode current) {
        return current.getDepth() == goalDepth;
    }

    @Override
    public int getExpandedLowLevelNodesNum() {
        return this.expandedLowLevelNodes;
    }

    @Override
    public int getGeneratedLowLevelNodesNum() {
        return this.generatedLowLevelNodes;
    }

    protected abstract int openSize();
}
