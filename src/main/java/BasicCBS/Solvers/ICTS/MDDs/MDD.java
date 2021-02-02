package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;

import java.util.*;

public class MDD {
    private MDDNode start;
    private MDDNode goal;

    public MDD(MDDSearchNode goal){
        initialize(goal);
    }

    private void initialize(MDDSearchNode goal){
        MDDNode mddGoal = new MDDNode(goal);
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
                        mddParent = new MDDNode(parent);
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
}
