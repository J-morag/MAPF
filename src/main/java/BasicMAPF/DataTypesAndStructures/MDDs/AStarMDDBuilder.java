package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class AStarMDDBuilder extends A_MDDSearcher {

    private static final int PLAN_START_TIME = 0;
    private static final int VERBOSE = 1;
    private final SingleAgentGAndH heuristic;
    private Queue<MDDSearchNode> openList;
    /**
     * The key will not be updated, although, the value will be the last version of this node.
     * Will contain everything in the open list, so we could modify them (add to their parents) while they are in the Priority Queue.
     */
    protected Map<MDDSearchNode, MDDSearchNode> contentOfOpen;
    protected Map<MDDSearchNode, MDDSearchNode> closeList;
    protected int maxDepthOfSolution;
    protected I_ConstraintSet constraints;

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
        MDDSearchNode start = new MDDSearchNode(agent, super.getSource(),
                0, heuristic.getHToTargetFromLocation(agent.target, super.getSource()), PLAN_START_TIME);
        addToOpen(start);
    }

    protected void addToOpen(MDDSearchNode node){
        MDDSearchNode inOpen;
        MDDSearchNode inClosed;
        if((inOpen = contentOfOpen.get(node)) != null){
            // Do not add this node twice to the open list, just add its parents to the already "inOpen" node.
            if (node.getG() < inOpen.getG()){ // solely for when using the time ceiling because of searching for a minimal MDD under infinite constraints
                openList.remove(inOpen); // todo might be inefficient because it's O(n)
                contentOfOpen.remove(inOpen);
                openList.add(node);
                contentOfOpen.put(node, node);
            } else if (node.getG() == inOpen.getG()) {
                inOpen.addParents(node.getParents()); // without time ceiling, only this is needed (unconditionally)
            }
        }
        else if((inClosed = closeList.get(node)) != null){
            if (node.getG() < inClosed.getG()){
                throw new IllegalStateException("Should not enter here, because we should not get a new node if it is in closed list with a lower g");
            } else if (node.getG() == inClosed.getG()) {
                inClosed.addParents(node.getParents());
            }
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
        MDDSearchNode goal = continueSearchingDontBuildMDD(depthOfSolution);
        return MDDFromGoalNode(goal);
    }

    private MDDSearchNode continueSearchingDontBuildMDD(int depthOfSolution){
        this.maxDepthOfSolution = depthOfSolution;
        int lastRejectionAtTargetTime = constraints == null ? -1 :
                constraints.lastRejectionTime(new Move(agent, 1, target, target));
        int lastConstraintsChangeTime = constraints == null ? 0 :
                constraints.getLastConstraintStartTime();
        lastConstraintsChangeTime = Math.max(lastConstraintsChangeTime, 0);
        boolean findMinMode = depthOfSolution == -1;

        if (openList == null){
            initializeSearch();
        }
        MDDSearchNode goal = null;
        while(!isOpenEmpty()){
            if(timeout.isTimeoutExceeded())
                return null;
            MDDSearchNode current = pollFromOpen();
            if (VERBOSE >= 3) System.out.println(this.getClass().getSimpleName() + "current is " + current.getLocation() + " at time " + current.getT() + " with g=" + current.getG() + " and h=" + current.getH());
            if(depthOfSolution > -1 && current.getF() > depthOfSolution)
            {
                addToOpen(current);
                break;
            }
            if(isGoalState(current)){
                if (depthOfSolution == -1 && current.getT() > lastRejectionAtTargetTime) {
                    depthOfSolution = current.getG();
                }
                if(current.getG() == depthOfSolution){
                    if(goal == null){
                        goal = current;
                        // Don't do continue here, because we want to add the sons of current to the open list for later
                    }
                    else{
                        try {
                            throw new Exception("Should not enter here, because goal is already in closed list, so it already added the parents of the new solution to the goal");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            // Don't do else here, because we want to add the sons of current to the open list for later even if it is a goal
            expand(current, lastConstraintsChangeTime, findMinMode, depthOfSolution > -1 ? depthOfSolution  : null);
        }
        return goal;
    }

    @Override
    public MDD searchToFirstSolution(@Nullable I_ConstraintSet constraints) {
        if (openList != null)
            throw new IllegalStateException("should not search for first solution on a searcher that has already been used");
        this.constraints = constraints;
        MDDSearchNode goal = continueSearchingDontBuildMDD(-1);
        this.releaseMemory();
        return MDDFromGoalNode(goal);
    }

    @Nullable
    private MDD MDDFromGoalNode(MDDSearchNode goal) {
        return goal == null ? null : new MDD(goal);
    }

    protected void releaseMemory() {
        this.contentOfOpen = null;
        this.closeList = null;
        this.openList = null;
        this.maxDepthOfSolution = 0;
    }

    protected boolean isOpenEmpty() {
        return openList.isEmpty();
    }

    protected void expand(MDDSearchNode parent, int lastConstraintsChangeTime, boolean findMinMode, Integer gMax){
        expandedNodesNum++;
        if (VERBOSE >= 3) System.out.println(this.getClass().getSimpleName() + "expanding " + parent.getLocation() + " at time " + parent.getT() + " with g=" + parent.getG() + " and h=" + parent.getH());

        List<I_Location> neighborLocations = parent.getNeighborLocations();
        for (I_Location location : neighborLocations) {
            int newG = parent.getG() + 1;
            if (findMinMode && gMax != null && newG > gMax) continue;
            int newH = heuristic.getHToTargetFromLocation(agent.target, location);
            int newTime = parent.getT() + 1;
            if (findMinMode && newTime > lastConstraintsChangeTime){
                if (location.equals(parent.getLocation())) continue; // no stay when after last constraint time
                newTime = lastConstraintsChangeTime + 1; // keeps same time as the parent
            }
            if (constraints != null &&
                    // todo profile Move creation impact
                    constraints.rejects(new Move(agent, newTime, parent.getLocation(), location)))
                continue;
            MDDSearchNode neighbor = new MDDSearchNode(agent, location, newG, newH, newTime);
            neighbor.addParent(parent);
            addToOpen(neighbor);
        }
        addToClose(parent);
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
