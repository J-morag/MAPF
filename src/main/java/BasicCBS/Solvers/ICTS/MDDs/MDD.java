package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;

import java.util.*;

public class MDD {
    private MDDNode start;
    private MDDNode goal;
    /**
     * an offset on the depth of mdd nodes. this is so we can take a node from an mdd and use it the the new root of the
     * mdd, at O(1) runtime.
     */
    public int depthOffset;

    public MDD(MDDSearchNode goal){
        this.depthOffset = 0;
        initialize(goal);
    }

    /**
     * copy constructor (deep).
     * @param other an MDD to copy.
     */
    public MDD(MDD other){
        this.depthOffset = other.depthOffset;
        // iterate over the mdd in BFS order
        // we don't need a closed list since this is a DAG
        // open will only contain nodes from other
        Queue<MDDNode> open = new ArrayDeque<>();
        // copies will only contain nodes from this (copies)
        Map<MDDNode, MDDNode> copies = new HashMap<>();
        this.start = new MDDNode(other.getStart());
        open.add(other.getStart());
        copies.put(this.start, this.start);
        while (!open.isEmpty()){
            MDDNode originalCurrentMddNode = open.remove();
            // a copy has to exist already
            MDDNode copyCurrentMddNode = copies.get(originalCurrentMddNode);
            List<MDDNode> originalChildren = originalCurrentMddNode.getNeighbors();

            for (MDDNode originalChild : originalChildren){
                MDDNode childCopy;
                // never saw this child before
                if(!copies.containsKey(originalChild)) {
                    // so we will have to expand it later
                    open.add(originalChild);
                    // copy the child. should only happen once because we check the contents of copies.
                    childCopy = new MDDNode(originalChild);
                    copies.put(childCopy, childCopy);
                }
                else{
                    // this child has already been seen. we just have to get the copy we've already made
                    childCopy = copies.get(originalChild);
                }
                copyCurrentMddNode.addNeighbor(childCopy);
            }
        }
        this.goal = copies.get(other.goal);
    }

    /**
     * Cut this MDD to the provided start node.
     * @param start a start node for this mdd. should currently be somewhere is this MDD. is not checked.
     * @return this.
     */
    public MDD changeStartNode(MDDNode start){
        this.start = start;
        this.depthOffset = depthOffset - this.start.getDepth();
        return this;
//        if (zeroDepth) {
//            int depthDelta = start.getDepth();
//            // iterate over the mdd in BFS order
//            // we don't need a closed list since this is a DAG
//            // open will only contain nodes from other
//            Queue<MDDNode> open = new ArrayDeque<>();
//            // copies will only contain nodes from this (copies)
//            Set<MDDNode> contentsOfOpen = new HashSet<>();
//            open.add(start);
//            contentsOfOpen.add(this.start);
//            while (!open.isEmpty()){
//                MDDNode current = open.remove();
//                // update depth
//                current.setDepth(current.getDepth() - depthDelta);
//                List<MDDNode> children = current.getNeighbors();
//
//                for (MDDNode child : children){
//                    // never saw this child before
//                    if(!contentsOfOpen.contains(child)) {
//                        // so we will have to expand it later
//                        open.add(child);
//                        contentsOfOpen.add(child);
//                    }
//                }
//            }
//        }
    }

    private void initialize(MDDSearchNode goal){
        MDDNode mddGoal = new MDDNode(goal, this);
        Queue<MDDNode> currentLevel = new LinkedList<>();
        Map<MDDNode, MDDSearchNode> mddNodesToSearchNodes = new HashMap<>();
        currentLevel.add(mddGoal);
        mddNodesToSearchNodes.put(mddGoal, goal);
        this.goal = mddGoal;

        while (true) {
            if(currentLevel.size() == 1 && currentLevel.peek().getDepth() == 0) {
                //We are at the start state, so we can finish the building of the MDD
                break;
            }
            HashMap<MDDSearchNode, MDDNode> previousLevel = new HashMap<>();
            while (!currentLevel.isEmpty()) {
                MDDNode current = currentLevel.poll();
                MDDSearchNode currentValue = mddNodesToSearchNodes.get(current);
                List<MDDSearchNode> currentParents = currentValue.getParents();
                for (MDDSearchNode parent : currentParents) {
                    MDDNode mddParent;
                    if(previousLevel.containsKey(parent)){
                        mddParent = previousLevel.get(parent);
                    }
                    else{
                        mddParent = new MDDNode(parent, this);
                        previousLevel.put(parent, mddParent);
                        mddNodesToSearchNodes.put(mddParent, parent);
                    }
                    mddParent.addNeighbor(current);
                }
            }
            currentLevel.addAll(previousLevel.values());
        }
        this.start = currentLevel.poll();
    }

    public MDDNode getStart() {
        return start;
    }

    public MDDNode getGoal() {
        return goal;
    }

    public Solution getPossibleSolution() {
        Solution solution = new Solution();
        List<Move> moves = new ArrayList<>();

        MDDNode current = start;
        while (!current.equals(goal)) {
            MDDNode next = current.getNeighbors().get(0); //It doesn't matter which son it was, we take a single path.

            Move move = new Move(current.getAgent(), next.getDepth(), current.getLocation(), next.getLocation());
            moves.add(move); //insert the move to the moves

            current = next;
        }

        SingleAgentPlan plan = new SingleAgentPlan(start.getAgent(), moves);
        solution.putPlan(plan);

        return solution;
    }

    public int getDepth() {
        return goal.getDepth();
    }

    public int getDepthOffset() {
        return depthOffset;
    }
}
