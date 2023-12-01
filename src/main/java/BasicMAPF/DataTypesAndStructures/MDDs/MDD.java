package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.VertexConflict;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MDD {
    private MDDNode start;
    private MDDNode goal;
    /**
     * Allows random access to MDD levels. The first index is the depth.
     * The second index is sorted on the natural ordering of the nodes.
     * Unmodifiable.
     */
    private List<List<MDDNode>> levels;

    public MDD(MDDSearchNode goal){
        initialize(goal);
    }

    /**
     * copy constructor (deep).
     * @param other an MDD to copy.
     */
    public MDD(MDD other){
        // todo why do we need a copy constructor when everything is immutable?
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

        // copy levels
        if (other.levels != null){
            this.levels = new ArrayList<>(other.levels.size());
            for (List<MDDNode> level : other.levels) {
                List<MDDNode> levelCopy = new ArrayList<>(level.size());
                for (MDDNode node : level) {
                    levelCopy.add(copies.get(node));
                }
                this.levels.add(Collections.unmodifiableList(levelCopy));
            }
            this.levels = Collections.unmodifiableList(this.levels);
        }
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

    public MDDNode getGoal() { // todo rename to target?
        return goal;
    }

    public Solution getPossibleSolution() {
        Solution solution = new Solution();
        List<Move> moves = new ArrayList<>();

        MDDNode current = start;
        while (!current.equals(goal)) {
            MDDNode next = current.getNeighbors().get(0); //It doesn't matter which son it was - we take a single path.

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

    public Agent getAgent(){
        return start.getAgent();
    }

    /**
     * Returns the level of the MDD, sorted on the natural ordering of the nodes.
     * Runtime on the first call:
     * ~O(d*n*log(n)) where d is the depth of the MDD and n is the number of nodes in each level.
     * Runtime on subsequent calls: O(1)
     * @param depth the depth of the level to return.
     * @return the level of the MDD, sorted on the natural ordering of the nodes.
     */
    public List<MDDNode> getLevel(int depth){
        if (depth < 0 || depth > getDepth())
            throw new IllegalArgumentException("depth must be between 0 and " + getDepth() + " inclusive (got " + depth + ")");
        // todo only initialize up to depth?
        if (levels == null) initializeLevels();
        return levels.get(depth);
    }

    private void initializeLevels() {
        levels = new ArrayList<>(getDepth() + 1);
        for (int i = 0; i <= getDepth(); i++) {
            levels.add(new ArrayList<>());
        }
        Queue<MDDNode> open = new ArrayDeque<>();
        Set<MDDNode> touched = new HashSet<>();
        open.add(start);
        while (!open.isEmpty()){
            MDDNode current = open.remove();
            levels.get(current.getDepth()).add(current);
            for (MDDNode neighbor : current.getNeighbors()){
                if (touched.contains(neighbor)) continue;
                open.add(neighbor);
                touched.add(neighbor);
            }
        }
        for (int i = 0; i < levels.size(); i++) {
            List<MDDNode> level = levels.get(i);
            Collections.sort(level);
            levels.set(i, Collections.unmodifiableList(level));
        }
        levels = Collections.unmodifiableList(levels);
    }

    public List<A_Conflict> conflictsWithMDDAtDepth(@NotNull MDD other, int depth, boolean stopAtFirstConflict){
        if (depth < 1)
            throw new IllegalArgumentException("depth must be at least 1 (got " + depth + ")");
        // look for vertex conflicts
        List<MDDNode> localLevel = depth <= this.getDepth() ? this.getLevel(depth) : Collections.singletonList(this.getGoal()); // wrong depth inside goal node, but shouldn't matter
        List<MDDNode> otherLevel = depth <= other.getDepth() ? other.getLevel(depth) : Collections.singletonList(other.getGoal()); // wrong depth inside goal node, but shouldn't matter
        List<A_Conflict> vertexConflicts = getLevelVertexConflicts(depth, localLevel, otherLevel, stopAtFirstConflict);
        if (stopAtFirstConflict && ! vertexConflicts.isEmpty()) return Collections.singletonList(vertexConflicts.get(0));

        // look for swapping conflicts
        if (depth <= this.getDepth() && depth <= other.getDepth()){ // no swapping conflicts if after goal time
            List<A_Conflict> swappingConflicts = getLevelSwappingConflicts(depth, localLevel, this.getLevel(depth - 1), other.getLevel(depth - 1), stopAtFirstConflict);
            if (stopAtFirstConflict && ! swappingConflicts.isEmpty()) return Collections.singletonList(swappingConflicts.get(0));
            if (vertexConflicts.isEmpty() && swappingConflicts.isEmpty()) return Collections.emptyList();
            else {
                List<A_Conflict> result = new ArrayList<>();
                result.addAll(vertexConflicts);
                result.addAll(swappingConflicts);
                return result;
            }
        }
        else
            return vertexConflicts;
    }

    @NotNull
    private List<A_Conflict> getLevelVertexConflicts(int depth, List<MDDNode> localLevel, List<MDDNode> otherLevel,
                                                         boolean stopAtFirstConflict) {
        // initialize the indices using binary search.
        // should improve runtime so long as the distribution given by the MDD comparator is informative for distinguishing between MDDs
        // (e.g., 2D coordinates of MDDs that are distant in Euclidean space)
        int localIndex = Collections.binarySearch(localLevel, otherLevel.get(0));
        if (localIndex < 0) localIndex = -localIndex - 1;
        if (localIndex >= localLevel.size()) return Collections.emptyList(); // no overlap

        int otherIndex = Collections.binarySearch(otherLevel, localLevel.get(0));
        if (otherIndex < 0) otherIndex = -otherIndex - 1;
        if (otherIndex >= otherLevel.size()) return Collections.emptyList(); // no overlap

        // scan for conflicts
        List<A_Conflict> conflicts = stopAtFirstConflict ? Collections.emptyList() : new ArrayList<>();
        while (localIndex < localLevel.size() && otherIndex < otherLevel.size()){
            MDDNode localNode = localLevel.get(localIndex);
            MDDNode otherNode = otherLevel.get(otherIndex);
            int compare = localNode.compareTo(otherNode);
            if (compare == 0){
                // found a vertex conflict
                VertexConflict conflict = new VertexConflict(localNode.getAgent(), otherNode.getAgent(), depth, localNode.getLocation());
                if (stopAtFirstConflict)
                    return Collections.singletonList(conflict);
                else {
                    conflicts.add(conflict);
                    // increment both, since the lists are sorted and with unique elements.
                    // if either overflow, then the loop will end.
                    localIndex++;
                    otherIndex++;
                }
            }
            else if (compare < 0){
                localIndex++;
            }
            else{
                otherIndex++;
            }
        }

        return conflicts;
    }

    @NotNull
    private List<A_Conflict> getLevelSwappingConflicts(int depth, List<MDDNode> localLevel, List<MDDNode> localLevelMinusOneDepth,
                                                             List<MDDNode> otherLevelMinusOneDepth, boolean stopAtFirstConflict) {
        // initialize the indices using binary search.
        // should improve runtime so long as the distribution given by the MDD comparator is informative for distinguishing between MDDs
        // (e.g., 2D coordinates of MDDs that are distant in Euclidean space)
        int localIndex = Collections.binarySearch(localLevel, otherLevelMinusOneDepth.get(0));
        if (localIndex < 0) localIndex = -localIndex - 1;
        if (localIndex >= localLevel.size()) return Collections.emptyList(); // no overlap

        int otherIndex = Collections.binarySearch(otherLevelMinusOneDepth, localLevel.get(0));
        if (otherIndex < 0) otherIndex = -otherIndex - 1;
        if (otherIndex >= otherLevelMinusOneDepth.size()) return Collections.emptyList(); // no overlap

        // scan for conflicts
        List<A_Conflict> conflicts = stopAtFirstConflict ? Collections.emptyList() : new ArrayList<>();
        while (localIndex < localLevel.size() && otherIndex < otherLevelMinusOneDepth.size()){
            MDDNode localNode = localLevel.get(localIndex);
            MDDNode otherCameFromNode = otherLevelMinusOneDepth.get(otherIndex);
            int compare = localNode.compareTo(otherCameFromNode);
            if (compare == 0){ // other MDD came from a location this MDD went to
                // check if there are swapping conflicts
                for (MDDNode otherWentToNode : otherCameFromNode.getNeighbors()) { // todo sort when constructing MDD so we can use binary search?
                    if (otherWentToNode.sameLocation(otherCameFromNode)) continue; // never a swapping conflict if staying in place
                    int localCameFromNodeIndex = Collections.binarySearch(localLevelMinusOneDepth, otherWentToNode);
                    if (localCameFromNodeIndex >= 0){ // found the node that other went to, in local previous level
                        MDDNode localCameFromNode = localLevelMinusOneDepth.get(localCameFromNodeIndex);
                        for (MDDNode localCameFromNodeNeighbor : localCameFromNode.getNeighbors()) { // todo sort when constructing MDD so we can use binary search?
                            if (localCameFromNodeNeighbor.sameLocation(localNode)){
                                // found a swapping conflict
                                SwappingConflict conflict = new SwappingConflict(localNode.getAgent(), otherWentToNode.getAgent(), depth, localNode.getLocation(), otherWentToNode.getLocation());
                                if (stopAtFirstConflict)
                                    return Collections.singletonList(conflict);
                                else
                                    conflicts.add(conflict);
                            }
                        }
                    }
                }
                // increment both, since the lists are sorted and with unique elements.
                // if either overflow, then the loop will end.
                localIndex++;
                otherIndex++;
            }
            else if (compare < 0){
                localIndex++;
            }
            else{
                otherIndex++;
            }
        }

        return conflicts;
    }

    public MDD constrainedView(Constraint newConstraint) {
        throw new NotImplementedException("todo"); //todo
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MDD{start=").append(start.getLocation().getCoordinate()).append(", goal=").append(goal.getLocation().getCoordinate()).append("}, levels (lacks edge information)=\n");
        for (int i = 0; i < levels.size(); i++) {
            sb.append(i).append(": ");
            for (MDDNode node : levels.get(i)) {
                sb.append(node.getLocation().getCoordinate()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // todo equals and hashcode(cached)
}
