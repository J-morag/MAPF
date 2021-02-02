package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.MDDs.MDD;
import BasicCBS.Solvers.ICTS.MDDs.MDDNode;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import com.google.common.collect.Lists;

import java.util.*;

/**
 * Searches the search space of a merged MDD without explicitly creating it, finding any valid solution from that space,
 * using Depth First Search.
 */
public class DFS_MergedMDDSpaceSolver implements I_MergedMDDSolver {

    private int goalDepth;
    private Stack<MergedMDDSpaceNode> openList;
    private Set<MergedMDDSpaceNode> closedList;

    protected void initializeSearch(Map<Agent, MDD> agentMDDs){
        this.openList = new Stack<>();
        this.closedList = new HashSet<>();
        addToOpen(this.getRoot(agentMDDs));
    }

    private MergedMDDSpaceNode getRoot(Map<Agent, MDD> agentMDDs) {
        MergedMDDSpaceNode root = new MergedMDDSpaceNode(null, 0);
        ArrayList<MDDNode> rootContents = new ArrayList<>(agentMDDs.size());
        for (MDD agentMDD : agentMDDs.values()){
            rootContents.add(agentMDD.getStart());
        }
        root.setMDDNodes(rootContents);
        return root;
    }

    @Override
    public Solution findJointSolution(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver) {
        initializeSearch(agentMDDs);
        goalDepth = 0;
        for (MDD mdd : agentMDDs.values()) {
            int currentDepth = mdd.getDepth();
            if (currentDepth > goalDepth)
                goalDepth = currentDepth;
        }

        // search the state space of the merged MDD (merger of all individual MDDs together)
        while (!openList.isEmpty() && !highLevelSolver.reachedTimeout()) {
            MergedMDDSpaceNode current = this.openList.pop();
            if (!closedList.contains(current) && isValidNode(current)){
                if (isGoal(current)) {
                    return this.getSolution(current);
                }
                expand(current);
            }
        }

        return null;
    }

    private boolean isValidNode(MergedMDDSpaceNode current) {
        boolean res = current.getParent() == null // root node
                || isValidCombination(current.generatingMoves);
        current.setGeneratingMovesForLaterVerification(null);
        return res;
    }

//    protected void combinationUtil(List<List<FatherSonMDDNodePair>> agentFatherSonPairs, List<FatherSonMDDNodePair> currentCombination,
//                                   List<MergedMDDSpaceNode> children, int index, int mddNodeDepth, MergedMDDSpaceNode parent) {
//        // Current combination is ready to be checked. check if it is a valid combination
//        if (index == agentFatherSonPairs.size()) {
//            if (isValidCombination(currentCombination)) {
//                MergedMDDSpaceNode current = getMergedMDDNode(parent, mddNodeDepth);
//                ArrayList<MDDNode> mddNodes = new ArrayList<>(currentCombination.size());
//                for (FatherSonMDDNodePair move : currentCombination){
//                    mddNodes.add(move.getSon());
//                }
//                current.setMDDNodes(mddNodes);
//                children.add(current);
//            }
//            return;
//        }
//
//        // Current combination is not yet ready to be checked.
//        for (FatherSonMDDNodePair possiblePair : agentFatherSonPairs.get(index)) {
//            List<FatherSonMDDNodePair> nextCombination = new ArrayList<>(currentCombination);
//            nextCombination.add(possiblePair);
//            combinationUtil(agentFatherSonPairs, nextCombination, children, index + 1, mddNodeDepth, parent);
//        }
//    }

    protected MergedMDDSpaceNode getMergedMDDNode(MergedMDDSpaceNode parent, int mddNodeDepth, List<MDDNode> mddNodes) {
        return new MergedMDDSpaceNode(parent, mddNodeDepth, mddNodes);
    }

    protected MergedMDDSpaceNode getMergedMDDNode(MergedMDDSpaceNode parent, int mddNodeDepth) {
        return new MergedMDDSpaceNode(parent, mddNodeDepth);
    }

    protected static boolean isValidCombination(List<FatherSonMDDNodePair> currentCombination) {
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
                if (currentCombination.get(i).colliding(currentCombination.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

//    protected static boolean isValidCombination(List<FatherSonMDDNodePair> currentCombination, Set<I_Location> allParentLocations) {
//        HashMap<I_Location, FatherSonMDDNodePair> childLocations = new HashMap<>(currentCombination.size());
//        for (FatherSonMDDNodePair pair : currentCombination){
//            childLocations.put(pair.getSon().getLocation(), pair);
//        }
//        if (childLocations.size() < currentCombination.size()){
//            // there is a vertex conflict somewhere
//            return false;
//        }
//        for (FatherSonMDDNodePair pair : currentCombination){
//            if (allParentLocations.contains(pair.getSon().getLocation())
//                    && childLocations.containsKey(pair.getFather().getLocation())){
//                // suspected swapping conflict
//                FatherSonMDDNodePair otherPair = childLocations.get(pair.getFather().getLocation());
//                if (otherPair.getFather().sameLocation(pair.getSon())){
//                    return false;
//                }
//            }
//        }
//
//        return true;
//    }

    protected void expand(MergedMDDSpaceNode current) {
        List<MergedMDDSpaceNode> child = getChildren(current);
        for (MergedMDDSpaceNode neighbor : child) {
            addToOpen(neighbor);
        }
        addToClosed(current);
    }

    private void addToOpen(MergedMDDSpaceNode mergedMDDSpaceNode) {
//        if (!closedList.contains(virtualMergedMDDNode)){
            openList.push(mergedMDDSpaceNode);
//        }
    }


    protected List<MergedMDDSpaceNode> getChildren(MergedMDDSpaceNode current) {
        // get cartesian product (all combinations of moves from each mdd in the mergedMDD node).
        List<List<FatherSonMDDNodePair>> fatherSonPairLists = current.getFatherSonPairsLists();
        // sort these is advance, so that we will get lists that are already sorted by agent (cartesianProduct maintains
        // order), so that we won't have to re-sort in each call to MergedMDDSpaceNode.setMDDNodes .
        fatherSonPairLists.sort(Comparator.comparingInt(list -> list.get(0).getFather().getAgent().iD));
        // copy to new list because cartesianProduct returns UnmodifiableList
        List<List<FatherSonMDDNodePair>> fatherSonPairCartesianProduct = new ArrayList<>(Lists.cartesianProduct(fatherSonPairLists));
        // validation postponed to when a node is polled from open
//        fatherSonPairCartesianProduct.removeIf(l -> !isValidCombination(l));
        // turn into new merged mdd search-space nodes
        List<MergedMDDSpaceNode> children = new ArrayList<>(fatherSonPairCartesianProduct.size());
        for (List<FatherSonMDDNodePair> fatherSonPairCombination : fatherSonPairCartesianProduct){
            ArrayList<MDDNode> mddNodes = new ArrayList<>(fatherSonPairCombination.size());
            for (FatherSonMDDNodePair move : fatherSonPairCombination){
                mddNodes.add(move.getSon());
            }
            MergedMDDSpaceNode child = getMergedMDDNode(current, current.getDepth() + 1, mddNodes);
            child.setGeneratingMovesForLaterVerification(fatherSonPairCombination);
            children.add(child);
        }
        return children;
    }

    protected boolean isGoal(MergedMDDSpaceNode current) {
        return current.getDepth() == goalDepth;
    }

    protected void addToClosed(MergedMDDSpaceNode node){
        closedList.add(node);
    }

    private Solution getSolution(MergedMDDSpaceNode goal) {
        Solution solution = new Solution();
        Map<Agent, List<Move>> agentMoves = new HashMap<>();
        for (MDDNode node : goal.getMddNodes()) {
            Agent agent = node.getAgent();
            agentMoves.put(agent, new ArrayList<>());
        }

        MergedMDDSpaceNode current = goal;
        while (current.getParent() != null) {
            MergedMDDSpaceNode parent = current.getParent();
            for (MDDNode currentAgentMDDNode : current.getMddNodes()){
                Agent agent = currentAgentMDDNode.getAgent();
                MDDNode parentAgentMDDNode = null;
                // look for the same agent parent merged MDD node
                for (MDDNode parentMDDNode : parent.getMddNodes()){
                    if (parentMDDNode.getAgent().equals(agent)){
                        parentAgentMDDNode = parentMDDNode;
                        break;
                    }
                }
                if (!currentAgentMDDNode.equals(parentAgentMDDNode)) {
                    //Only consider moves that changed something.
                    //this equal calculation uses the g value,
                    //so we will distinguish between being at state s at time g and at time g + 1
                    //(staying in the same location as part of the solution, vs repeating last mdd node for the merged MDD)
                    Move move = new Move(agent, currentAgentMDDNode.getDepth(), parentAgentMDDNode.getLocation(),
                            currentAgentMDDNode.getLocation());
                    agentMoves.get(agent).add(move); //insert the move to the agent's moves
                }
            }
            current = parent;
        }

        for(Agent agent : agentMoves.keySet()){
            List<Move> moves = agentMoves.get(agent);
            Collections.reverse(moves);
            SingleAgentPlan plan = new SingleAgentPlan(agent, moves);
            solution.putPlan(plan);
        }

        return solution;
    }
}
