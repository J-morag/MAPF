package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.VertexConflict;
import Environment.Config;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MDD {
    private MDDNode start;
    private MDDNode goal;
    private int numNodes;
    /**
     * Allows random access to MDD levels. The first index is the depth.
     * The second index is sorted on the natural ordering of the nodes.
     * Unmodifiable.
     */
    private List<List<MDDNode>> levels;
    private int levelsHash = -1;

    public MDD(@NotNull MDDSearchNode goal){
        initialize(goal);
        verifyIntegrity(null, null);
    }

    public MDD(@NotNull MDDNode start, @NotNull MDDNode goal){
        this.start = start;
        this.goal = goal;
        initializeUpToLevel(getDepth());
        this.numNodes = levels.stream().mapToInt(List::size).sum();
        verifyIntegrity(null, null);
    }

    /**
     * copy constructor (deep).
     * @param other an MDD to copy.
     */
    public MDD(@NotNull MDD other){
        // todo why do we need a copy constructor when everything is immutable?
        this(other, null);
        verifyIntegrity(null, null);
    }

    /**
     * copy constructor (deep) with constraints.
     * @param other an MDD to copy.
     */
    public MDD(@NotNull MDD other, @Nullable I_ConstraintSet constraints){
        // todo avoid code duplications with the other copy option
        // at each level collect all edges that point to nodes seen in the previous level -
        //  any edge/vertex we collect would be on a path from start to goal without passing constraints
        MDDNode goalCopy = new MDDNode(other.goal);
        Map<MDDNode, MDDNode> collectedNodes = new HashMap<>();
        collectedNodes.put(goalCopy, goalCopy);
        for (int d = other.getDepth()-1; d >= 0; d--) {
            List<MDDNode> level = other.getLevel(d);
            for (MDDNode node : level) {
                for (MDDNode neighbor : node.getNeighbors()) {
                    if (collectedNodes.containsKey(neighbor)){
                        if (constraints != null &&
                                constraints.rejects(new Move(neighbor.getAgent(), neighbor.getDepth(), // assumes time == depth
                                node.getLocation(), neighbor.getLocation()))){
                            continue;
                        }
                        MDDNode nodeCopy;
                        if (!collectedNodes.containsKey(node)){
                            nodeCopy = new MDDNode(node); // without the original neighbors
                            collectedNodes.put(nodeCopy, nodeCopy);
                        }
                        else nodeCopy = collectedNodes.get(node);
                        // todo reuse original if we end up with the same list of neighbors as the original?
                        nodeCopy.addNeighbor(collectedNodes.get(neighbor));
                    }
                }
            }
        }
        MDDNode newMDDStartNode = collectedNodes.get(other.start);
        if (newMDDStartNode == null || // constraints severed the MDD
                (constraints != null && constraintsRejectStayingAtTargetForever(constraints, goalCopy))){
            this.start = null;
            this.goal = null;
            this.levels = null;
            return;
        }
        else {
            this.start = newMDDStartNode;
            this.goal = goalCopy;
            this.numNodes = collectedNodes.size();
        }

        verifyIntegrity(constraints, null);
    }

    /**
     * copy (deep) with constraints (negative).
     */
    public MDD deepCopyWithConstraints(@NotNull I_ConstraintSet constraints){
        return new MDD(this, constraints);
    }

    /**
     * Copy with constraint.
     */
    public MDD shallowCopyWithConstraint(@NotNull Constraint constraint, boolean isPositiveConstraint) {
        if (!isPositiveConstraint){
            // todo optimize by removing the singleton creations?
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.add(constraint);
            return this.deepCopyWithConstraints(constraintSet);
        }
        else {
            int constraintEndDepth = constraint.time;// assumes depth := time
            // shallow copy! This already connects us to the rest of the MDD after the constraint, but is a shallow copy.
            MDDNode constraintEndNodeShallowCopy;
            try {
                 constraintEndNodeShallowCopy = getLevel(constraintEndDepth).get(
                         Collections.binarySearch(getLevel(constraintEndDepth),
                                new MDDNode(constraint.location, constraintEndDepth, constraint.agent)));
            } catch (IndexOutOfBoundsException e){
                throw new IllegalArgumentException("positive constraint " + constraint + " not found in MDD\n" + this);
            }
            int constraintStartDepth = constraint.prevLocation != null ? constraint.time-1 : constraint.time; // assumes depth := time
            MDDNode constraintStartNodeCopy = constraint.prevLocation != null ?
                    new MDDNode(constraint.prevLocation, constraintStartDepth, constraint.agent) : constraintEndNodeShallowCopy;
            if (Config.DEBUG >= 2 && Collections.binarySearch(getLevel(constraintStartDepth), constraintStartNodeCopy) < 0){
                throw new IllegalStateException("constraintStartNodeCopy" + constraintStartNodeCopy + " not found in level " + constraintStartDepth + " of " + this);
            }

            // at each level collect all edges that point to nodes seen in the previous level -
            //  any edge/vertex we collect would be on a path from start to the constraint
            Map<MDDNode, MDDNode> collectedNodes = new HashMap<>();
            collectedNodes.put(constraintStartNodeCopy, constraintStartNodeCopy);
            for (int d = constraintStartDepth-1; d >= 0; d--) {
                List<MDDNode> level = getLevel(d);
                for (MDDNode node : level) {
                    for (MDDNode neighbor : node.getNeighbors()) {
                        if (collectedNodes.containsKey(neighbor)){
                            MDDNode nodeCopy;
                            if (!collectedNodes.containsKey(node)){
                                nodeCopy = new MDDNode(node); // without the original neighbors
                                collectedNodes.put(nodeCopy, nodeCopy);
                            }
                            else nodeCopy = collectedNodes.get(node);
                            nodeCopy.addNeighbor(collectedNodes.get(neighbor));
                        }
                    }
                }
            }
            MDDNode newMDDStartNode = collectedNodes.get(start);
            // we now have an MDD up to the start of the constraint

            // now add the edge from the constraint to the MDD (only if it's an edge constraint)
            if (constraintStartNodeCopy != constraintEndNodeShallowCopy){
                constraintStartNodeCopy.addNeighbor(constraintEndNodeShallowCopy);
            }

            MDD mdd = new MDD(newMDDStartNode, this.goal); // again, goal is shallow copied here
            mdd.verifyIntegrity(null, constraint);
            return mdd;
        }
    }

    public MDD constrainedView(Constraint newConstraint) {
        throw new NotImplementedException("todo"); //todo
    }

    private boolean constraintsRejectStayingAtTargetForever(@NotNull I_ConstraintSet constraints, MDDNode goalCopy) {
        // todo? profile Move creation runtime?
        return constraints.firstRejectionTime(new Move(goalCopy.getAgent(), Math.max(goalCopy.getDepth(), 1), // assumes time == depth
                goalCopy.getLocation(), goalCopy.getLocation())) != -1;
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
        if (this.start.getDepth() != 0){
            throw new IllegalStateException("MDD start node has depth " + this.start.getDepth() + " instead of 0");
        }

        this.numNodes = mddNodesToSearchNodes.size();
    }

    public MDDNode getStart() {
        return start;
    }

    public MDDNode getGoal() { // todo rename to target?
        return goal;
    }

    public Solution getPossibleSolution() {
        return getPossibleSolutionFrom(start);
    }

    public Solution getPossibleSolutionFrom(MDDNode current) {
        Solution solution = new Solution();
        List<Move> moves = new ArrayList<>();

        while (current != goal) {
            MDDNode next = current.getNeighbors().get(0); //It doesn't matter which son it was - we take a single path.
            Move move = new Move(current.getAgent(), next.getDepth(), current.getLocation(), next.getLocation());
            moves.add(move);
            current = next;
        }
        if (start == goal){ // special case for starting at goal
            Move move = new Move(start.getAgent(), 1, start.getLocation(), start.getLocation());
            moves.add(move);
        }

        SingleAgentPlan plan = new SingleAgentPlan(start.getAgent(), moves);
        solution.putPlan(plan);

        return solution;
    }

    public int getDepth() {
        return this.goal != null ? this.goal.getDepth() : -1;
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
        initializeUpToLevel(depth);
        return levels.get(depth);
    }

    private void initializeUpToLevel(int depth) {
        if (levels == null){
            levels = new ArrayList<>(getDepth() + 1);
            levels.add(Collections.singletonList(start));
        }
        for (int i = levels.size(); i <= depth; i++) {
            Set<MDDNode> currentLevelSet = new HashSet<>();
            List<MDDNode> prevLevel = levels.get(i-1);
            for (MDDNode node : prevLevel) {
                currentLevelSet.addAll(node.getNeighbors());
            }
            List<MDDNode> currentLevel = new ArrayList<>(currentLevelSet);
            Collections.sort(currentLevel);
            levels.add(Collections.unmodifiableList(currentLevel));
            if (i == getDepth()){
                levels = Collections.unmodifiableList(levels);
                levelsHash = hashLevels();
            }
        }
    }

    public List<A_Conflict> conflictsWithMDDAtDepth(@NotNull MDD other, int depth, boolean stopAtFirstConflict){
        if (depth < 1)
            throw new IllegalArgumentException("depth must be at least 1 (got " + depth + ")");
        // look for vertex conflicts
        List<MDDNode> localLevel = depth <= this.getDepth() ? this.getLevel(depth) : Collections.singletonList(this.getGoal()); // wrong depth inside goal node, but shouldn't matter
        List<MDDNode> otherLevel = depth <= other.getDepth() ? other.getLevel(depth) : Collections.singletonList(other.getGoal()); // wrong depth inside goal node, but shouldn't matter
        if (otherLevel.isEmpty()) System.out.println("otherLevel is empty: " + other); // todo tmp
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

    public void verifyIntegrity(@Nullable I_ConstraintSet negativeConstraints, @Nullable Constraint positiveConstraint) {
        if (Config.DEBUG >= 2 && getDepth() != -1){
            for (int i = 0; i < getDepth(); i++) {
                List<MDDNode> level = getLevel(i);
                for (MDDNode node : level) {
                    // exists a path from each node to the goal
                    Solution possibleSolution = getPossibleSolutionFrom(node);
                    SingleAgentPlan plan = possibleSolution.getPlanFor(getAgent());
                    if (plan == null){
                        throw new IllegalStateException("MDD has no plan for agent " + getAgent() + ": " + this);
                    }
                    if (plan.getEndTime() != this.getDepth() && getDepth() > 0){
                        throw new IllegalStateException("MDD has depth " + this.getDepth() + " but plan for agent " + getAgent() + " has end time " + plan.getEndTime() + ": " + this);
                    }
                    if (! plan.getLastMove().currLocation.equals(this.getGoal().getLocation())){
                        throw new IllegalStateException("MDD goal is " + this.getGoal().getLocation() + " but plan for agent " + getAgent() + " ends at " + plan.getLastMove().currLocation + ": " + this);
                    }
                    if (Config.DEBUG >= 3){
                        // all neighbors are in the next level
                        for (MDDNode neighbor : node.getNeighbors()) {
                            if (Collections.binarySearch(getLevel(i+1), neighbor) < 0){
                                throw new IllegalStateException("MDD node " + node + " has neighbor " + neighbor + " but it is not in the next level: " + this);
                            }
                        }
                        // obeys negative constraints
                        if (negativeConstraints != null){
                            for (Move move : plan) {
                                if (negativeConstraints.rejects(move)){
                                    throw new IllegalStateException("MDD node " + node + " has move " + move + " that is rejected by negative constraints: " + this);
                                }
                            }
                        }
                        // obeys positive constraint
                        if (positiveConstraint != null){
                            if (i < positiveConstraint.time){
                                boolean found = false;
                                for (Move move : plan) {
                                    if (move.currLocation.equals(positiveConstraint.location) && move.timeNow == positiveConstraint.time &&
                                            (positiveConstraint.prevLocation == null || move.prevLocation.equals(positiveConstraint.prevLocation))){
                                        found = true;
                                        break;
                                    }
                                }
                                if (! found){
                                    throw new IllegalStateException("MDD node " + node + " does not have a move that obeys positive constraint " + positiveConstraint + ": " + this);
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MDD{start=").append(start.getLocation().getCoordinate()).append(", goal=").append(goal.getLocation().getCoordinate()).append("}, levels (lacks edge information)=\n");
        for (int i = 0; i < getDepth(); i++) {
            sb.append(i).append(": ");
            for (MDDNode node : getLevel(i)) {
                sb.append(node.getLocation().getCoordinate()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MDD mdd)) return false;

        return numNodes == mdd.numNodes && Objects.equals(start, mdd.start) && Objects.equals(goal, mdd.goal) &&
                (levelsHash == mdd.levelsHash || levelsHash == -1 || mdd.levelsHash == -1) &&
                levelsEquals(mdd);
    }

    @Override
    public int hashCode() { // todo cache the whole thing?
        if (levelsHash == -1){
            levelsHash = hashLevels();
        }
        int result = Objects.hashCode(start);
        result = 31 * result + Objects.hashCode(goal);
        result = 31 * result + numNodes;
        result = 31 * result + levelsHash;
        return result;
    }


//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if (!(o instanceof MDD mdd)) return false;
//
//        return numNodes == mdd.numNodes && start.equals(mdd.start) && goal.equals(mdd.goal) &&
//                (levelsHash == mdd.levelsHash || levelsHash == -1 || mdd.levelsHash == -1) && levelsEquals(mdd);
//    }
//
//    @Override
//    public int hashCode() {
//        if (levelsHash == -1){
//            levelsHash = hashLevels();
//        }
//        int result = start.hashCode();
//        result = 31 * result + goal.hashCode();
//        result = 31 * result + numNodes;
//        result = 31 * result + levelsHash;
//        return result;
//    }

    public boolean levelsEquals(MDD other){
        if (this == other) return true;
        if (other == null) return false;
        if (getDepth() != other.getDepth()) return false;
        for (int i = 0; i < getDepth(); i++) {
            List<MDDNode> level = getLevel(i);
            List<MDDNode> otherLevel = other.getLevel(i);
            if (level.size() != otherLevel.size()) return false;
            for (int j = 0; j < level.size(); j++) {
                if (! level.get(j).equals(otherLevel.get(j))) return false;
                // check edges
                List<MDDNode> neighbors = level.get(j).getNeighbors();
                List<MDDNode> otherNeighbors = otherLevel.get(j).getNeighbors();
                if (neighbors.size() != otherNeighbors.size()) return false;
                for (int k = 0; k < neighbors.size(); k++) {
                    if (! neighbors.get(k).equals(otherNeighbors.get(k))) return false;
                }
            }
        }
        return true;
    }

    private int hashLevels(){ // todo test me
        initializeUpToLevel(getDepth());
        int result = 0;
        for (int i = 0; i < getDepth(); i++) {
            List<MDDNode> level = getLevel(i);
            for (MDDNode mddNode : level) {
                result = 31 * result + mddNode.hashCode();
                List<MDDNode> neighbors = mddNode.getNeighbors();
                for (MDDNode neighbor : neighbors) {
                    result = 31 * result + neighbor.hashCode();
                }
            }
        }
        return result;
    }

    public boolean acceptedBy(I_ConstraintSet constraints) {
        for (int i = 0; i < getDepth(); i++) {
            List<MDDNode> level = getLevel(i);
            for (MDDNode node : level) {
                for (MDDNode neighbor : node.getNeighbors()) {
                    // todo? profile Move creation runtime?
                    if (constraints.rejects(new Move(neighbor.getAgent(), neighbor.getDepth(), // assumes time == depth
                            node.getLocation(), neighbor.getLocation()))){
                        return false;
                    }
                }
            }
        }
        return ! constraintsRejectStayingAtTargetForever(constraints, goal);
    }

    public int numNodes() {
        return numNodes;
    }
}
