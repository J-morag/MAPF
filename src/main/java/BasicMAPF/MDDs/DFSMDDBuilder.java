package BasicMAPF.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.Stack;

public class DFSMDDBuilder extends AStarMDDBuilder {
    private Stack<MDDSearchNode> openList;
    /**
     * Constructor for the AStar searcher
     *
     * @param heuristic         - the heuristics table that will enable us to get a more accurate heuristic
     */
    public DFSMDDBuilder(Timeout timeout, I_Location source, I_Location target, Agent agent, DistanceTableSingleAgentHeuristicMDD heuristic) {
        super(timeout, source, target, agent, heuristic);
    }
    /**
     * Constructor for the AStar searcher
     *
     * @param heuristic         - the heuristics table that will enable us to get a more accurate heuristic
     */
    public DFSMDDBuilder(Timeout timeout, I_Location source, I_Location target, Agent agent,
                         DistanceTableSingleAgentHeuristicMDD heuristic, boolean disappearAtGoal) {
        super(timeout, source, target, agent, heuristic, disappearAtGoal);
    }

    @Override
    protected void initOpenList() {
        openList = new Stack<>();
    }

    @Override
    protected void addToOpen(MDDSearchNode node) {
        if(node.getF() > maxDepthOfSolution)
            return;
        if(contentOfOpen.containsKey(node)){
            //Do not add this node twice to the open list, just add it's parents to the already "inOpen" node.
            MDDSearchNode inOpen = contentOfOpen.get(node);
            inOpen.addParents(node.getParents());
        }
        else if(closeList.containsKey(node)){
            MDDSearchNode inClosed = closeList.get(node);
            inClosed.addParents(node.getParents());
        }
        else{
            generatedNodesNum++;
            openList.push(node);
            contentOfOpen.put(node, node);
        }
    }

    @Override
    protected MDDSearchNode pollFromOpen() {
        MDDSearchNode next = openList.pop();
        contentOfOpen.remove(next);
        return next;
    }

    @Override
    protected boolean isOpenEmpty() {
        return openList.isEmpty();
    }

}
