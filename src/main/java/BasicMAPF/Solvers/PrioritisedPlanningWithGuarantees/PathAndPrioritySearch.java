package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.DataTypesAndStructures.MDDs.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.DataTypesAndStructures.TimeInterval;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.GoalConstraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import org.apache.commons.collections4.map.HashedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static Environment.Config.DEBUG;
import static Environment.Config.INFO;

public class PathAndPrioritySearch extends A_Solver {
    public static final String GENERATED_VIRTUAL_NODES_STR = "GeneratedVirtualNodes";
    public static final String GENERATED_FULL_NODES_STR = "GeneratedFullNodes";
    public static final String GENERATED_NODE_INSTANCES_STR = "GeneratedNodeInstances";
    public static final String CLOSED_DUPLICATE_NODES_SKIPPED_STR = "ClosedDuplicateNodesSkipped";
    public static final String OPEN_DUPLICATE_NODES_SKIPPED_STR = "OpenDuplicateNodesSkipped";

    public static final boolean REUSE_PARENT_NODE_DATA_STRUCTURES = true;

    /* = Constants = */

    /* = Class Instance Fields = */

    /**
     * A queue of open {@link PaPSNode nodes/states}. Also referred to as OPEN.
     */
    public final I_OpenList<PaPSNode> openList;
    public final I_MDDSearcherFactory searcherFactory;
    /**
     * Determines how to sort {@link #openList OPEN}.
     */
    private final Comparator<PaPSNode> nodeComparator;
    private final boolean useSimpleMDDCache;
    private final int MDDCacheDepthDeltaMax;
    private final I_PaPSHeuristic pcsHeuristic;
    private final PaPSRootGenerator rootGenerator;

    /* = Run Fields = */

    private MDDManager mddManager;
    protected MAPF_Instance currentInstance;
    protected I_Map currentMap;
    protected RunParameters currentRunParameters;
    /**
     * A {@link SingleAgentGAndH heuristic} for the low level solver.
     */
    private SingleAgentGAndH singleAgentHeuristic;
    /**
     * Initial constraints given to the solver to work with.
     */
    private I_ConstraintSet initialConstraints;
    private int runningNodeID;
    private int cLowerBound;
    private int cUpperBound;
    private PaPSNode bestGoalNodeGenerated;
    /**
     * Number of fully generated nodes - where all agents are added or a conflict is found.
     */
    private int fullyGeneratedNodes;
    private final boolean useDuplicateDetection;
    /**
     * If set to false, will not split on agents, resulting in PCS-like behavior
     */
    private final boolean noAgentsSplit;
    private HashSet<PaPSNode> closedList;
    private int closedDuplicateNodesSkipped;
    private int openDuplicateNodesSkipped;

    PathAndPrioritySearch(@Nullable I_OpenList<PaPSNode> openList, @Nullable Comparator<PaPSNode> nodeComparator,
                          @Nullable I_MDDSearcherFactory searcherFactory, @Nullable Boolean useSimpleMDDCache,
                          @Nullable Integer MDDCacheDepthDeltaMax, @Nullable I_PaPSHeuristic pcsHeuristic,
                          @Nullable PaPSRootGenerator rootGenerator, @Nullable Boolean useDuplicateDetection,
                          @Nullable Boolean noAgentsSplit) {
        this.nodeComparator = Objects.requireNonNullElse(nodeComparator, PaPSCompTieBreakSmallerMDDs.DEFAULT_INSTANCE);
        this.openList = Objects.requireNonNullElseGet(openList, () -> this.nodeComparator instanceof BucketingComparator<PaPSNode> bucketing ?
                new BucketingOpenList<>(bucketing) : new OpenListTree<>(this.nodeComparator));
        this.searcherFactory = Objects.requireNonNullElseGet(searcherFactory, AStarFactory::new);

        this.useSimpleMDDCache = Objects.requireNonNullElse(useSimpleMDDCache, true);
        this.MDDCacheDepthDeltaMax = Objects.requireNonNullElse(MDDCacheDepthDeltaMax, 1);
        this.pcsHeuristic = Objects.requireNonNullElseGet(pcsHeuristic, PaPSHeuristicSIPP::new);
        this.rootGenerator = Objects.requireNonNullElse(rootGenerator, new DefaultPaPSRootGenerator());
        this.useDuplicateDetection = Objects.requireNonNullElse(useDuplicateDetection, true);
        this.noAgentsSplit = Objects.requireNonNullElse(noAgentsSplit, false);
        if (this.noAgentsSplit && rootGenerator instanceof NaivePaPSUnifiedOpenPCSRG) {
            throw new IllegalArgumentException(NaivePaPSUnifiedOpenPCSRG.class.getSimpleName() + " is not compatible with PaPS because PaPS disregards the order (treats it as a set)");
        }

        super.name = "Priority Constrained Search";
    }

    /*  = initialization =  */

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        this.currentInstance = instance;
        this.currentMap = instance.map;
        this.currentRunParameters = runParameters;
        this.singleAgentHeuristic = Objects.requireNonNullElseGet(runParameters.singleAgentGAndH,
                () -> new DistanceTableSingleAgentHeuristic(instance.agents, this.currentInstance.map) );
        this.initialConstraints = Objects.requireNonNullElseGet(runParameters.constraints, ConstraintSet::new);
        this.runningNodeID = 0;
        this.cLowerBound = Integer.MIN_VALUE;
        this.cUpperBound = Integer.MAX_VALUE;
        this.bestGoalNodeGenerated = null;
        this.expandedNodes = 0;
        this.closedDuplicateNodesSkipped = 0;
        this.openDuplicateNodesSkipped = 0;
        this.mddManager = new MDDManager(searcherFactory, getTimeout(), singleAgentHeuristic);
        this.fullyGeneratedNodes = 0;
        if (useDuplicateDetection){
            closedList = new HashSet<>();
        }
    }

    /*  = algorithm =  */

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        rootGenerator.generateRoot(instance, parameters, this, getTimeout());
        PaPSNode goalNode = mainLoop();
        return solutionFromGoal(goalNode);
    }

    void addToOpen(@NotNull PaPSNode node) {
        if (useDuplicateDetection && closedList.contains(node)){
            if (INFO >= 2){
                System.out.println("PCS - Closed duplicate node skipped: " + node.hashCode() + " open list size " + openList.size() + " closed list size " + closedList.size());
            }
            closedDuplicateNodesSkipped++;
            return;
        }
        if (useDuplicateDetection && openList.contains(node)){
            if (INFO >= 2){
                System.out.println("PCS - Open duplicate node skipped: " + node.hashCode() + " open list size " + openList.size() + " closed list size " + closedList.size());
            }
            openDuplicateNodesSkipped++;
            return; // no need to keep the one with min g, because equality guarantees same MDDs => same g
        }
        openList.add(node);
    }

    private PaPSNode mainLoop() {
        while(!checkTimeout() && !openList.isEmpty()) {
            PaPSNode node = openList.poll();
            if (cLowerBound > node.getF() && ! (nodeComparator instanceof PCSCompLexical)){
                throw new IllegalStateException("Lower bound should not decrease during search unless the heuristic is inconsistent or there is a bug. " +
                        "Lower bound: " + cLowerBound + ", node f: " + node.getF() + ", node: " + node);
            }
            cLowerBound = node.getF();
            if (cLowerBound == cUpperBound){
                return bestGoalNodeGenerated;
            }
            if (useDuplicateDetection){
                if (closedList.contains(node)){
                    if (INFO >= 2){
                        System.out.println("PCS - Closed duplicate node skipped: " + node.hashCode() + " open list size " + openList.size() + " closed list size " + closedList.size());
                    }
                    closedDuplicateNodesSkipped++;
                    continue;
                }
                else addToClosedList(node);
            }

            expandedNodes++;
            if (isVirtualNode(node)) { // agent expand
                if (noAgentsSplit){
                    addNextAgent(node);
                }
                else {expandOnAllAgents(node);}
            }
            else // constraint expand
                expandOnConflict(node);
        }
        return null;
    }

    private boolean addToClosedList(PaPSNode node) {
        return closedList.add(node);
    }

    private static boolean isVirtualNode(PaPSNode node) {
        return node.conflict() == null && node.MDDs().size() < node.priorityOrderedAgents().length;
    }

    private static boolean isGoal(PaPSNode node) {
        return node.MDDs().size() == node.priorityOrderedAgents().length && node.conflict() == null;
    }

    private Solution solutionFromGoal(PaPSNode goalNode) {
        if (goalNode == null){
            return null;
        }
        Solution solution = new Solution();
        for (MDD mdd: goalNode.MDDs()) {
            Agent agent = mdd.getAgent();
            SingleAgentPlan plan = new SingleAgentPlan(agent);
            MDDNode prevNode = mdd.getStart();
            // any arbitrary walk down the MDD would give a plan, and any plan would be valid
             while (! prevNode.equals(mdd.getGoal())){
                 MDDNode nextNode = prevNode.getNeighbors().get(0);
                 plan.addMove(new Move(agent, nextNode.getDepth() // assumes depth := time
                         , prevNode.getLocation(), nextNode.getLocation()));
                 prevNode = nextNode;
             }
            solution.putPlan(plan);
        }
        return solution;
    }

    private void expandOnConflict(PaPSNode node) {
        Agent leastPriorityAgent = node.getLeastPriorityMDD().getAgent();
        Agent higherPriorityAgent = node.conflict().agent1.equals(leastPriorityAgent) ?
                node.conflict().agent2 : node.conflict().agent1;
        Constraint[] preventingConstraintsCBSStyle = node.conflict().getPreventingConstraints();
        if (preventingConstraintsCBSStyle.length != 2){
            throw new IllegalStateException("Expected 2 preventing constraints, got " + preventingConstraintsCBSStyle.length);
        }
        Constraint constraintOnHighPriorityAgent = preventingConstraintsCBSStyle[0].agent.equals(higherPriorityAgent) ?
                preventingConstraintsCBSStyle[0] : preventingConstraintsCBSStyle[1];


        // left node - positive constraint on the higher priority agent
        expandOnConflictOneSide(node, constraintOnHighPriorityAgent, true);
        // right node - negative constraint on the higher priority agent
        expandOnConflictOneSide(node, constraintOnHighPriorityAgent, false);
    }

    void generateRoot(@NotNull Agent[] priorityOrderedAgents){
        if (INFO >= 2) System.out.printf("Generating root node with ordering prefix %s\n", Arrays.toString(priorityOrderedAgents));
        ConstraintSet constraints = new ConstraintSet(initialConstraints);
        ArrayList<MDD> MDDs = new ArrayList<>();
        Map<Agent, Integer> agentIndices = getAgentIndices(priorityOrderedAgents);
        int[] hArr = getHarr(MDDs, constraints, priorityOrderedAgents);
        if (hArr == null){ // if using a smart heuristic that might detect a node already can't lead to a solution
            return;
        }
        int h = Arrays.stream(hArr).sum();
        generateNode(MDDs, constraints, priorityOrderedAgents, agentIndices, 0, h, hArr);
    }

    /**
     * @param parent the parent of this node
     * @param newConstraintOnHighPriorityAgent the new constraint on the high priority agent
     * @param isPositiveConstraint whether the new constraint is positive or negative
     */
    private void expandOnConflictOneSide(@NotNull PaPSNode parent, @NotNull Constraint newConstraintOnHighPriorityAgent,
                                         boolean isPositiveConstraint) {
        if (INFO >= 2) System.out.println("Generating node with constraint " + newConstraintOnHighPriorityAgent + " and isPositiveConstraint=" + isPositiveConstraint);

        ConstraintSet updatedConstraints = new ConstraintSet(parent.constraints()); // todo possible memory performance issue
        ArrayList<MDD> MDDs = new ArrayList<>(parent.MDDs());
        Map<Agent, Integer> agentIndices = parent.agentIndices();

        // handle high-priority agent first -
        // it might generate new constraints on lower priority agents because of new critical resources
        MDD constrainedHighPriorityMDD = parent.MDDs().get(agentIndices.get(newConstraintOnHighPriorityAgent.agent))
                .shallowCopyWithConstraint(newConstraintOnHighPriorityAgent, isPositiveConstraint);
        if (Config.DEBUG >= 1 && constrainedHighPriorityMDD == null){
            throw new IllegalStateException("Should not get a constraint on high priority MDD that severs it if all " +
                    "critical resources were preemptively added as constraints on the lower priority agents.");
        }
        MDDs.set(agentIndices.get(newConstraintOnHighPriorityAgent.agent), constrainedHighPriorityMDD);
        List<Constraint> criticalResourceConstraints = getCriticalResourcesAsNegativeConstraints(constrainedHighPriorityMDD);
        updatedConstraints.addAll(criticalResourceConstraints); // relies on ConstraintSet's set behavior to handle duplicates
        List<Constraint> addedConstraints = new ArrayList<>(criticalResourceConstraints);

        if (!addedConstraints.isEmpty()){
            // get an MDD (or view) that obeys the constraints, including the new constraints, of the least priority MDD
            MDD compliantMDD = parent.getLeastPriorityMDD().deepCopyWithConstraints(updatedConstraints);
            if (compliantMDD.getDepth() == -1){ // impossible at current depth, so deepen.
                compliantMDD = deeperMDD(parent.getLeastPriorityMDD().getAgent(), updatedConstraints,
                        currentMap.getMapLocation(parent.getLeastPriorityMDD().getAgent().source),
                        currentMap.getMapLocation(parent.getLeastPriorityMDD().getAgent().target),
                        parent.getLeastPriorityMDD().getDepth());
                if (compliantMDD == null){ // impossible at any depth, or timeout
                    return;
                }
            }
            MDDs.set(MDDs.size() - 1, compliantMDD);
        }

        // finished handling the direct consequences of the new constraints, now finish the node
        Agent[] priorityOrderedAgents = parent.priorityOrderedAgents();
        int g = getG(MDDs);
        int[] hArr = getHarr(MDDs, updatedConstraints, priorityOrderedAgents);
        if (hArr == null){ // if using a smart heuristic that might detect a node already can't lead to a solution
            return;
        }
        int h = Arrays.stream(hArr).sum();

        generateNode(MDDs, updatedConstraints, priorityOrderedAgents, agentIndices, g, h, hArr);
    }

    /**
     * Expand the node by adding each option of which agent to add next.
     * @param node the parent node
     */
    protected void expandOnAllAgents(PaPSNode node) {
        ArrayList<MDD> MDDs = node.MDDs();
        // done with this MDD - add all its critical resources as negative constraints
        List<Constraint> criticalResourceConstraints = MDDs.isEmpty() ? Collections.emptyList() :
                getCriticalResourcesAsNegativeConstraints(MDDs.get(MDDs.size() - 1));
        ConstraintSet updatedConstraints = node.constraints(); // can reuse the parent's set object once
        updatedConstraints.addAll(criticalResourceConstraints);

        Agent[] priorityOrderedAgents = node.priorityOrderedAgents();
        Map<Agent, Integer> agentIndices = node.agentIndices();

        int[] hArr = getHarr(MDDs, updatedConstraints, priorityOrderedAgents);
        if (hArr == null){ // if using a smart heuristic that might detect a node already can't lead to a solution
            return;
        }
        int hBeforeRemovingNextAgent = Arrays.stream(hArr).sum();

        // one per option for next agent
        for (int i = MDDs.size(); i < priorityOrderedAgents.length; i++) {
            Agent[] newPriorityOrderedAgents;
            Map<Agent, Integer> newAgentIndices;
            ConstraintSet newConstraints;

            // For the first child, where possible, reuse the parent's data structures
            if (i == MDDs.size() && REUSE_PARENT_NODE_DATA_STRUCTURES) {
                newPriorityOrderedAgents = priorityOrderedAgents;
                newAgentIndices = agentIndices;
                newConstraints = updatedConstraints;
            } else {
                newPriorityOrderedAgents = new Agent[priorityOrderedAgents.length];
                System.arraycopy(priorityOrderedAgents, 0, newPriorityOrderedAgents, 0, priorityOrderedAgents.length);
                newPriorityOrderedAgents[MDDs.size()] = priorityOrderedAgents[i];
                newPriorityOrderedAgents[i] = priorityOrderedAgents[MDDs.size()];

                newAgentIndices = new HashedMap<>(agentIndices); // todo replace with ArrayMap? Need to change Agent.hashCode for that.
                newAgentIndices.put(priorityOrderedAgents[MDDs.size()], i);
                newAgentIndices.put(priorityOrderedAgents[i], MDDs.size());

                newConstraints = new ConstraintSet(updatedConstraints);
            }

            if (INFO >= 2) System.out.printf("Reached %d agents ordering prefix %s\n",
                    MDDs.size() + 1, Arrays.toString(Arrays.copyOf(newPriorityOrderedAgents, MDDs.size() + 1)));

            ArrayList<MDD> newMDDs = new ArrayList<>(MDDs);

            // next least-priority agent
            Agent nextAgent = newPriorityOrderedAgents[MDDs.size()];
            MDD nextAgentMDD = getInitialMDDForAgent(nextAgent, updatedConstraints);
            if (nextAgentMDD == null){ // unsolvable at any depth under current constraints, or timeout
                return;
            }
            newMDDs.add(nextAgentMDD);

            // copy and reorder the already computed hArr, and remove the h value for the agent we've added
            int j = i - MDDs.size();
            int n = hArr.length;
            int[] newHArr = new int[n - 1];
            if (j == 0) {
                // If the element to remove is already at index 0, just copy indices 1..n-1.
                System.arraycopy(hArr, 1, newHArr, 0, n - 1);
            } else {
                // Copy the block from index 1 up to j (not including j)
                if (j > 1) {
                    System.arraycopy(hArr, 1, newHArr, 0, j - 1);
                }
                // Put the element from index 0 into the correct position
                newHArr[j - 1] = hArr[0];
                // Copy the block from index j+1 to the end into the appropriate position
                if (j < n - 1) {
                    System.arraycopy(hArr, j + 1, newHArr, j, n - 1 - j);
                }
            }

            int g = getG(newMDDs);
            if (DEBUG >= 1 && newMDDs.get(newMDDs.size()-1).getDepth() < node.hArr()[i - MDDs.size()]){
                throw new IllegalStateException("Inconsistent heuristic! MDD depth is less than the heuristic value. " +
                        "MDD depth: " + newMDDs.get(newMDDs.size()-1).getDepth() + ", heuristic value: " + node.hArr()[i - MDDs.size()] + ", agent: " + newMDDs.get(newMDDs.size()-1).getAgent() + ", MDDs: " + newMDDs);
            }
            generateNode(newMDDs, newConstraints, newPriorityOrderedAgents, newAgentIndices, g, hBeforeRemovingNextAgent - hArr[i - MDDs.size()], newHArr);
        }
    }

    /**
     * A linear expand operation that just creates one child node with the next agent according to the priority ordering.
     * @param node the parent node
     */
    private void addNextAgent(PaPSNode node) {
        ArrayList<MDD> MDDs = REUSE_PARENT_NODE_DATA_STRUCTURES ? node.MDDs() : new ArrayList<>(node.MDDs());
        // done with this MDD - add all its critical resources as negative constraints
        List<Constraint> criticalResourceConstraints = MDDs.isEmpty() ? Collections.emptyList() :
                getCriticalResourcesAsNegativeConstraints(MDDs.get(MDDs.size() - 1));
//        List<Constraint> addedConstraints = new ArrayList<>(criticalResourceConstraints); //for now, misses the already added constraints because we don't save them and don't use them for anything
        ConstraintSet updatedConstraints = REUSE_PARENT_NODE_DATA_STRUCTURES ? node.constraints() :
                new ConstraintSet(node.constraints());
        updatedConstraints.addAll(criticalResourceConstraints);
        Agent[] priorityOrderedAgents = REUSE_PARENT_NODE_DATA_STRUCTURES ? node.priorityOrderedAgents() :
                Arrays.copyOf(node.priorityOrderedAgents(), node.priorityOrderedAgents().length);

        // next least-priority agent
        Agent nextAgent = priorityOrderedAgents[MDDs.size()];
        MDD nextAgentMDD = getInitialMDDForAgent(nextAgent, updatedConstraints);
        if (nextAgentMDD == null){ // unsolvable at any depth under current constraints, or timeout
            return;
        }
        MDDs.add(nextAgentMDD);

        int[] hArr = getHarr(MDDs, updatedConstraints, priorityOrderedAgents);
        if (hArr == null){ // if using a smart heuristic that might detect a node already can't lead to a solution
            return;
        }
        int h = Arrays.stream(hArr).sum();

        int g = getG(MDDs);
        if (DEBUG >= 1 && MDDs.get(MDDs.size()-1).getDepth() < node.hArr()[0]){
            throw new IllegalStateException("Inconsistent heuristic! MDD depth is less than the heuristic value. " +
                    "MDD depth: " + MDDs.get(MDDs.size()-1).getDepth() + ", heuristic value: " + node.hArr()[0] + ", agent: " + MDDs.get(MDDs.size()-1).getAgent() + ", MDDs: " + MDDs);
        }
        generateNode(MDDs, updatedConstraints, priorityOrderedAgents,
                REUSE_PARENT_NODE_DATA_STRUCTURES ? node.agentIndices() : new HashMap<>(node.agentIndices()),
                g, h, hArr);
    }

    private void generateNode(ArrayList<MDD> MDDs, ConstraintSet updatedConstraints, Agent[] priorityOrderedAgents, Map<Agent, Integer> agentIndices, int g, int h, int[] hArr) {
        A_Conflict conflict = MDDs.isEmpty() ? null : earliestConflictWithLeastPriorityMDD(MDDs);
        if (conflict != null || MDDs.size() == priorityOrderedAgents.length){
            fullyGeneratedNodes++;
            if (INFO >= 2) System.out.printf("Generated node with %d agents, ordering prefix %s\n", MDDs.size() + 1, Arrays.toString(priorityOrderedAgents));
        }
        PaPSNode paPSNode = new PaPSNode(MDDs, g, h, hArr, updatedConstraints, conflict, runningNodeID++, priorityOrderedAgents, agentIndices);
        generatedNodes++;
        addToOpen(paPSNode);
        if (isGoal(paPSNode)){
            if (g < this.cUpperBound){
                this.cUpperBound = g;
                this.bestGoalNodeGenerated = paPSNode;
            }
        }
    }

    protected int[] getHarr(ArrayList<MDD> MDDs, ConstraintSet updatedConstraints, Agent[] priorityOrderedAgents) {
        Map<I_Location, List<TimeInterval>> safeIntervalsByLocation = null;
        if (pcsHeuristic instanceof PaPSHeuristicSIPP){
            safeIntervalsByLocation = updatedConstraints.vertexConstraintsToSortedSafeTimeIntervals(null, this.currentMap);
        }
        return pcsHeuristic.getH(priorityOrderedAgents, MDDs.size(), updatedConstraints, currentInstance, singleAgentHeuristic, safeIntervalsByLocation);
    }

    protected List<Constraint> getCriticalResourcesAsNegativeConstraints(MDD positivelyConstrainedHighPriorityMDD) {
        // todo optimize by not generating constraints that exist?
        // todo optimize by incorporating this with the MDD pruning process?
        List<Constraint> res = new ArrayList<>();
        for (int time = 0; time <= positivelyConstrainedHighPriorityMDD.getDepth(); time++) { // assumes depth := time
            List<MDDNode> level = positivelyConstrainedHighPriorityMDD.getLevel(time);
            if (level.size() == 1){
                MDDNode singularNode = level.get(0);
                // add vertex constraint (add even for depth 0, which should not have any effect)
                res.add(new Constraint(null, time, singularNode.getLocation()));
                // add edge constraint
                if (singularNode.getNeighbors().size() == 1){
                    MDDNode neighbor = singularNode.getNeighbors().get(0);
                    res.add(new Constraint(null, time+1, neighbor.getLocation(), singularNode.getLocation())); // reverse the edge to create the constraint
                }
            }
        }
        // target constraint
        res.add(new GoalConstraint(null, positivelyConstrainedHighPriorityMDD.getDepth()+1 // assumes depth := time, starts after reaching target
                , positivelyConstrainedHighPriorityMDD.getGoal().getLocation(), positivelyConstrainedHighPriorityMDD.getAgent()));
        return res;
    }

    private A_Conflict earliestConflictWithLeastPriorityMDD(ArrayList<MDD> MDDs) {
        MDD leastPriorityMDD = MDDs.get(MDDs.size() - 1);
        // todo have a set of the nodes in each MDD and use it to completely skip MDDs that have no suspicion of overlap with the least-priority MDD?
        int deepestDepth = 0;
        for (MDD mdd: MDDs) {
            if (mdd == null){
                throw new IllegalStateException("Should not have null MDDs in the MDD list: " + MDDs);
            }
            deepestDepth = Math.max(deepestDepth, mdd.getDepth());
        }

        int d =
//                // todo if the parent had the same least-priority agent and this MDD had only shrunk, skip levels that we already fully checked?
//                parent != null && parent.getLeastPriorityMDD().getAgent().equals(leastPriorityMDD.getAgent())
//                && parent.getLeastPriorityMDD().getDepth() == leastPriorityMDD.getDepth() ?
//                parent.conflict().time
//                :
                1;
        for (; d <= deepestDepth ; d++) {
            for (int i = 0; i < MDDs.size() - 1; // each MDD that precedes the least-priority MDD
                 i++) {
                if (leastPriorityMDD.getDepth() < d && MDDs.get(i).getDepth() < d){ // todo can we upgrade this to an "or" once we proactively add target constraints?
                    continue; // not critical - checking will be quick anyway
                }
                // check d level of both MDDs for vertex conflicts, and for swapping conflicts with level d-1
                List<A_Conflict> conflicts = leastPriorityMDD.conflictsWithMDDAtDepth(MDDs.get(i), d, true);
                if (! conflicts.isEmpty()){
                    return conflicts.get(0);
                }
            }
        }
        return null;
    }

    private MDD getMinMDD(Agent agent, @NotNull I_ConstraintSet constraints) {
        // todo canonize MDDs so that MDDs that happen to be equal don't take up space?
        return mddManager.getMinMDDNoReuse(currentMap.getMapLocation(agent.source), currentMap.getMapLocation(agent.target), agent, constraints);
    }

    protected MDD getInitialMDDForAgent(Agent newlyAddedAgent, @NotNull I_ConstraintSet constraints) {
        if (!useSimpleMDDCache){
            return getMinMDD(newlyAddedAgent, constraints);
        }
        // todo canonize MDDs so that MDDs that happen to be equal don't take up space?
        I_Location source = currentMap.getMapLocation(newlyAddedAgent.source);
        I_Location target = currentMap.getMapLocation(newlyAddedAgent.target);
        int minDepth = singleAgentHeuristic.getHToTargetFromLocation(newlyAddedAgent.target, source);
        // get MDD from a cache of unconstrained MDDs (generate if absent), check if it conflicts with an existing constraint.
        MDD unconstrainedMDD = getUnconstrainedMddAtDepth(newlyAddedAgent, source, target, minDepth);
        if (unconstrainedMDD == null){ // should only be because of timeout
            return null;
        }
        if (MDDCacheDepthDeltaMax == 1){
            mddManager.clearSearchers();
        }
        if (unconstrainedMDD.acceptedBy(constraints)){
            return unconstrainedMDD;
        }
        else {
            // if it conflicts, create a constrained copy and see if it's possible.
            MDD constrainedMDD = unconstrainedMDD.deepCopyWithConstraints(constraints);
            if (constrainedMDD.getDepth() != -1){ // possible at current depth
                return constrainedMDD;
            }
            else { // impossible at current depth
                return deeperMDD(newlyAddedAgent, constraints, source, target, minDepth);
            }
        }
    }

    private MDD getUnconstrainedMddAtDepth(Agent newlyAddedAgent, I_Location source, I_Location target, int requestedDepth) {
        MDD unconstrainedMDD = mddManager.getMDD(source, target, newlyAddedAgent, requestedDepth);
        if (checkTimeout()){
            return null;
        }
        else if (unconstrainedMDD == null){
            throw new IllegalStateException("Should not fail to build an MDD without constraints, assuming the requested depth was more than the minimal depth. " +
                    "\nrequestedDepth: " + requestedDepth + "\nnewlyAddedAgent:" + newlyAddedAgent + "\nsource:" + source +
                    "\ntarget:" + target + "\nminimalDepthFromHeuristic:" + singleAgentHeuristic.getHToTargetFromLocation(newlyAddedAgent.target, source));
        }
        return unconstrainedMDD;
    }

    private MDD deeperMDD(Agent agent, @NotNull I_ConstraintSet constraints, I_Location source, I_Location target, int currentImpossibleDepth) {
        if (!useSimpleMDDCache || MDDCacheDepthDeltaMax == 1){
            return getMinMDD(agent, constraints);
        }
        // get a +1 deeper unconstrained MDD from the cache (generate if absent), check if it still conflicts
        MDD unconstrainedMDD = getUnconstrainedMddAtDepth(agent, source, target, currentImpossibleDepth + 1);
        if (unconstrainedMDD == null){ // should only be because of timeout
            return null;
        }
        if (unconstrainedMDD.acceptedBy(constraints)){
            return unconstrainedMDD;
        }
        else { // If it conflicts, search for min MDD under constraints.
            return getMinMDD(agent, constraints);
        }
    }

    private int getG(ArrayList<MDD> mdds) {
        int g = 0;
        for (MDD mdd: mdds) {
            g += mdd.getDepth();
        }
        return g;
//        // start with parent's g, remove (parent's) least-priority agent's MDD depth, add that agent's new MDD depth and the new agents' MDD depth
//        int g = parent != null ? parent.g() - parent.getLeastPriorityMDD().getDepth() : 0;
//        for (int i = parent != null ? parent.MDDs().size() - 1 : 0; i < MDDs.size(); i++) {
//            g += MDDs.get(i).getDepth();
//        }
//        return g;
    }

    private @NotNull Map<Agent, Integer> getAgentIndices(Agent[] priorityOrderedAgents) {
        // todo use ArrayMap?
        Map<Agent, Integer> agentIndices = new HashMap<>();
        for (int i = 0; i < priorityOrderedAgents.length; i++) {
            agentIndices.put(priorityOrderedAgents[i], i);
        }
        return agentIndices;
    }

    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.totalLowLevelNodesExpanded = mddManager.getExpandedNodesNum();
        super.totalLowLevelNodesGenerated = mddManager.getGeneratedNodesNum();
        super.writeMetricsToReport(solution);
        super.instanceReport.putIntegerValue(CLOSED_DUPLICATE_NODES_SKIPPED_STR, closedDuplicateNodesSkipped);
        super.instanceReport.putIntegerValue(OPEN_DUPLICATE_NODES_SKIPPED_STR, openDuplicateNodesSkipped);

        // Number of generated nodes, counting every addition of an agent as a new node, but not counting fully generated
        // nodes (when a conflict is found or all agents are added).
        int generatedVirtualNodes = generatedNodes - fullyGeneratedNodes;
        super.instanceReport.putIntegerValue(GENERATED_VIRTUAL_NODES_STR, generatedVirtualNodes);

        super.instanceReport.putIntegerValue(GENERATED_FULL_NODES_STR, fullyGeneratedNodes);
        // depends on the specific times when we paused generating a node, and so created a concrete object
        super.instanceReport.putIntegerValue(GENERATED_NODE_INSTANCES_STR, runningNodeID);

        if(solution != null){
            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
            super.instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
        }
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.openList.clear();
        this.closedList = null;
        this.currentInstance = null;
        this.currentMap = null;
        this.initialConstraints = null;
        this.singleAgentHeuristic = null;
        this.mddManager = null;
        this.bestGoalNodeGenerated = null;
    }

    /* = subclasses = */

    public static class DEFAULT_PaPS_COMPARATOR implements BucketingComparator<PaPSNode> {
        @Override
        public int compare(PaPSNode o1, PaPSNode o2) {
            int res = bucketCompare(o1, o2); // f
            if (res != 0){
                return res;
            }
            res = Integer.compare(o2.g(), o1.g()); // reversed to prefer higher g
            if (res != 0){
                return res;
            }
            return Integer.compare(o1.uniqueID(), o2.uniqueID());
        }

        @Override
        public int getBucket(PaPSNode paPSNode) {
            return paPSNode.getF();
        }
    }
}
