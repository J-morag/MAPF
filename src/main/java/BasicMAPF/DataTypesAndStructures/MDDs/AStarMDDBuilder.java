package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class AStarMDDBuilder extends A_MDDSearcher {

    private Queue<MDDSearchNode> openList;
    /**
     * The key will not be updated, although, the value will be the last version of this node.
     * Will contain everything in the open list, so we could modify them (add to their parents) while they are in the Priority Queue.
     */
    protected Map<MDDSearchNode, MDDSearchNode> contentOfOpen;
    protected Map<MDDSearchNode, MDDSearchNode> closeList;
    private final SingleAgentGAndH heuristic;
    protected int maxDepthOfSolution;
    private boolean disappearAtGoal = false;
    protected DisappearAtGoalFilter disappearAtGoalFilter = new DisappearAtGoalFilter();

    /**
     * Constructor for the AStar searcher
     *
     * @param heuristic - the heuristics table that will enable us to get a more accurate heuristic
     */
    public AStarMDDBuilder(@NotNull Timeout timeout, @NotNull I_Location source, @NotNull I_Location target, @NotNull Agent agent, @NotNull SingleAgentGAndH heuristic) {
        this(timeout, source, target, agent, heuristic, null);
    }

    /**
     * Constructor for the AStar searcher
     *
     * @param heuristic - the heuristics table that will enable us to get a more accurate heuristic
     */
    public AStarMDDBuilder(@NotNull Timeout timeout, @NotNull I_Location source, @NotNull I_Location target, @NotNull Agent agent, @NotNull SingleAgentGAndH heuristic,
                           @Nullable Boolean disappearAtGoal) {
        super(timeout, source, target, agent);
        this.heuristic = heuristic;
        this.disappearAtGoalFilter.target = target;
        this.disappearAtGoal = Objects.requireNonNullElse(disappearAtGoal, false);
    }

    protected void initOpenList(){
        openList = new PriorityQueue<>();
    }

    private void initializeSearch() {
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
        initOpenList();
        contentOfOpen = new HashMap<>();
        closeList = new HashMap<>();
        initializeSearch();

        MDDSearchNode goal = null;
        while(!isOpenEmpty()){
            if(timeout.isTimeoutExceeded())
                return null;
            MDDSearchNode current = pollFromOpen();
            expandedNodesNum++;
            if(current.getF() > depthOfSolution)
            {
                addToOpen(current);
                break;
            }
            if(isGoalState(current)){
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
                /*  it is logical, because of the DFS who extends AStar
                else{

                    try {
                        throw new Exception("It is not logical that we will receive a different goal in a different depth");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                */
            }
            // Don't do else here, because we want to add the sons of current to the open list for later
            expand(current);
        }
        releaseMemory();
        return goal == null ? null : new MDD(goal);
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
        if (disappearAtGoal && node.getG() + 1 < maxDepthOfSolution){
            // filter neighbors. Only allow generation of goal node if the goal node is at the target depth.
            neighborLocations.removeIf(disappearAtGoalFilter);
        }
        for (I_Location location : neighborLocations) {
            MDDSearchNode neighbor = new MDDSearchNode(agent, location, node.getG() + 1, heuristic.getHToTargetFromLocation(agent.target, location));
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
