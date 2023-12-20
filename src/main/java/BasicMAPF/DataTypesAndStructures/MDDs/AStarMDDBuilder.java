package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class AStarMDDBuilder extends A_MDDSearcher {

    private static final int PLAN_START_TIME = 0;
    private Queue<MDDSearchNode> openList;
    /**
     * The key will not be updated, although, the value will be the last version of this node.
     * Will contain everything in the open list, so we could modify them (add to their parents) while they are in the Priority Queue.
     */
    protected Map<MDDSearchNode, MDDSearchNode> contentOfOpen;
    protected Map<MDDSearchNode, MDDSearchNode> closeList;
    private final SingleAgentGAndH heuristic;
    protected int maxDepthOfSolution;
    protected ConstraintSet constraints;

    /**
     * Constructor for the AStar searcher
     *
     * @param heuristic - the heuristics table that will enable us to get a more accurate heuristic
     */
    public AStarMDDBuilder(@NotNull Timeout timeout, @NotNull I_Location source, @NotNull I_Location target,
                           @NotNull Agent agent, @NotNull SingleAgentGAndH heuristic) {
        super(timeout, source, target, agent);
        this.heuristic = heuristic;
    }

    protected void initOpenList(){
        openList = new PriorityQueue<>();
    }

    private void initializeSearch() {
        initOpenList();
        contentOfOpen = new HashMap<>();
        closeList = new HashMap<>();
        MDDSearchNode start = new MDDSearchNode(agent, super.getSource(), 0, heuristic.getHToTargetFromLocation(agent.target, super.getSource()));
        addToOpen(start);
    }

    protected void addToOpen(MDDSearchNode node){
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
            openList.add(node);
            contentOfOpen.put(node, node);
        }
    }

    protected MDDSearchNode pollFromOpen(){
        MDDSearchNode next = openList.poll();
        contentOfOpen.remove(next);
        return next;
    }

    private void addToClose(MDDSearchNode node) {
        closeList.put(node, node);
    }

    @Override
    public MDD continueSearching(int depthOfSolution) {
        this.maxDepthOfSolution = depthOfSolution;
        if (openList == null){
            initializeSearch();
        }

        MDDSearchNode goal = null;
        while(!isOpenEmpty()){
            if(timeout.isTimeoutExceeded())
                return null;
            MDDSearchNode current = pollFromOpen();
            expandedNodesNum++;
            if(depthOfSolution > -1 && current.getF() > depthOfSolution)
            {
                addToOpen(current);
                break;
            }
            if(isGoalState(current)){
                // todo cache result to avoid repeated calls or improve performance of this call in ConstraintSet
                int lastRejectionAtTargetTime = constraints == null ? -1 :
                        constraints.lastRejectionTime(new Move(current.getAgent(), nodeTime(current),
                                        current.getParents() == null ? current.getLocation() : current.getParents().get(0).getLocation(),
                                        current.getLocation()), false);
                if (lastRejectionAtTargetTime >= nodeTime(current)){
                    continue;
                }

                if (depthOfSolution == -1) {
                    depthOfSolution = current.getG();
                    this.maxDepthOfSolution = depthOfSolution;
                }
                if(current.getG() == depthOfSolution){
                    if(goal == null){
                        goal = current;
                        // Don't do continue here, because we want to add the sons of current to the open list for later
                    }
                    else{
                        //goal.addParents(current.getParents());
                        try {
                            throw new Exception("Should not enter here, because goal is already in closed list, so it already added the parents of the new solution to the goal");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // Don't do else here, because we want to add the sons of current to the open list for later even if it is a goal
            expand(current);
        }
        return goal == null ? null : new MDD(goal);
    }

    private int nodeTime(MDDSearchNode current) {
        return PLAN_START_TIME + current.getG();
    }

    @Override
    public MDD searchToFirstSolution(@Nullable ConstraintSet constraints) {
        if (openList != null)
            throw new IllegalStateException("should not search for first solution on a searcher that has already been used");
        this.constraints = constraints;
        MDD res = continueSearching(-1);
        this.constraints = null;
        return res;
    }

    protected void releaseMemory() {
        this.contentOfOpen = null;
        this.closeList = null;
        this.openList = null;
    }

    protected boolean isOpenEmpty() {
        return openList.isEmpty();
    }

    protected void expand(MDDSearchNode node){
        List<I_Location> neighborLocations = node.getNeighborLocations();
        for (I_Location location : neighborLocations) {
            int newG = node.getG() + 1;
            if (constraints != null &&
                    // todo profile Move creation impact
                    constraints.rejects(new Move(agent, PLAN_START_TIME + newG, node.getLocation(), location)))
                continue;
            MDDSearchNode neighbor = new MDDSearchNode(agent, location, newG, heuristic.getHToTargetFromLocation(agent.target, location));
            neighbor.addParent(node);
            addToOpen(neighbor);
        }
        addToClose(node);
    }

    protected boolean isGoalState(MDDSearchNode node) {
        return node.getLocation().getCoordinate().equals(target.getCoordinate());
    }

    protected static class DisappearAtGoalFilter implements Predicate<I_Location> {
        public I_Location target;

        @Override
        public boolean test(I_Location location) {
            return target.equals(location);
        }
    }
}
