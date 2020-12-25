package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.LowLevel.Node;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;

import java.util.*;

public class MDD {
    private MDDNode start;
    private MDDNode goal;

    public MDD(Node goal){
        initialize(goal);
    }

    private void initialize(Node goal){
        MDDNode mddGoal = new MDDNode(goal);
        Agent agent = goal.getAgent();

        Queue<MDDNode> currentLevel = new LinkedList<>();
        currentLevel.add(mddGoal);
        this.goal = mddGoal;

        while (true) {
            if(currentLevel.size() == 1 && currentLevel.peek().getValue().getG() == 0) {
                //We are at the start state, so we can finish the building of the MDD
                break;
            }
            HashMap<Node, MDDNode> previousLevel = new HashMap<>();
            while (!currentLevel.isEmpty()) {
                MDDNode current = currentLevel.poll();
                Node currentValue = current.getValue();
                List<Node> currentParents = currentValue.getParents();
                for (Node parent : currentParents) {
                    MDDNode mddParent;
                    if(previousLevel.containsKey(parent)){
                        mddParent = previousLevel.get(parent);
                    }
                    else{
                        mddParent = new MDDNode(parent);
                        previousLevel.put(parent, mddParent);
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

            Move move = new Move(current.getValue().getAgent(), next.getValue().getG(), current.getValue().getLocation(), next.getValue().getLocation());
            moves.add(move); //insert the move to the moves

            current = next;
        }

        SingleAgentPlan plan = new SingleAgentPlan(start.getValue().getAgent(), moves);
        solution.putPlan(plan);

        return solution;
    }

    public int getDepth() {
        return goal.getValue().getG();
    }
}
