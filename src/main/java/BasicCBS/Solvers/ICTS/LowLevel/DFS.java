package BasicCBS.Solvers.ICTS.LowLevel;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

import java.util.Stack;

public class DFS extends AStarMDDBuilder {
    private Stack<Node> openList;
    /**
     * Constructor for the AStar searcher
     *
     * @param highLevelSearcher
     * @param heuristic         - the heuristics table that will enable us to get a more accurate heuristic
     */
    public DFS(ICTS_Solver highLevelSearcher, I_Location source, I_Location target, Agent agent, DistanceTableAStarHeuristicICTS heuristic) {
        super(highLevelSearcher, source, target, agent, heuristic);
    }

    @Override
    protected void initOpenList() {
        openList = new Stack<>();
    }

    @Override
    protected void addToOpen(Node node) {
        if(node.getF() > maxDepthOfSolution)
            return;
        if(contentOfOpen.containsKey(node)){
            //Do not add this node twice to the open list, just add it's parents to the already "inOpen" node.
            Node inOpen = contentOfOpen.get(node);
            inOpen.addParents(node.getParents());
        }
        else if(closeList.containsKey(node)){
            Node inClosed = closeList.get(node);
            inClosed.addParents(node.getParents());
        }
        else{
            generatedNodesNum++;
            openList.push(node);
            contentOfOpen.put(node, node);
        }
    }

    @Override
    protected Node pollFromOpen() {
        Node next = openList.pop();
        contentOfOpen.remove(next);
        return next;
    }

    @Override
    protected boolean isOpenEmpty() {
        return openList.isEmpty();
    }

    @Override
    protected void clearOpenList() {
        openList.clear();
    }
}
