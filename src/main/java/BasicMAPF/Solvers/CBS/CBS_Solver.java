package BasicMAPF.Solvers.CBS;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAndBlacklistAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.CorridorConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.SingleUseConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.UnmodifiableConstraintSet;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.RunParameters_SAAStar;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.*;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * The Conflict Based Search (CBS) Multi Agent Path Finding (MAPF) algorithm.
 */
public class CBS_Solver extends A_Solver {

    /*  = Fields =  */

    /*  = Fields related to the MAPF instance =  */

    private MAPF_Instance instance;

    /*  = Fields related to the run =  */

    /**
     * A {@link SingleAgentGAndH heuristic} for the low level solver.
     */
    private SingleAgentGAndH singleAgentGAndH;
    /**
     * Initial constraints given to the solver to work with.
     */
    private I_ConstraintSet initialConstraints;
    /**
     * Reused for each {@link CBS_Node}.
     */
    private I_ConstraintSet currentConstraints;

    /*  = Fields related to the class instance =  */

    /**
     * A queue of open {@link CBS_Node nodes/states}. Also referred to as OPEN.
     */
    public final I_OpenList<CBS_Node> openList;
    /**
     * @see OpenListManagementMode
     */
    private final OpenListManagementMode openListManagementMode;
    /**
     * A {@link I_Solver solver}, to be used for solving single-{@link Agent agent} sub-problems.
     */
    private final I_Solver lowLevelSolver;
    /**
     * Cost may be more complicated than a simple SOC (Sum of Individual Costs), so retrieve it through this method.
     */
    private final I_SolutionCostFunction costFunction;
    /**
     * Determines how to sort {@link #openList OPEN}.
     */
    private final Comparator<? super CBS_Node> CBSNodeComparator;
    /**
     * Whether to use corridor reasoning.
     * @see <a href="jiaoyangli.me/files/2020-ICAPS.pdf#page=1&zoom=180,-78,792">New Techniques for Pairwise Symmetry Breaking in Multi-Agent Path Finding</a>
     */
    private final boolean corridorReasoning;
    /**
     * if true, agents can have shared goals, so they can stay at their goal together (only last move onwards).
     */
    private final boolean sharedGoals;

    /**
     * If true, agents staying at their source (since the start) will not conflict 
     */
    private final boolean sharedSources;

    private final TransientMAPFSettings transientMAPFSettings;

    /*  = Constructors =  */

    /**
     * Parameterised constructor.
     * @param lowLevelSolver this {@link I_Solver solver} will be used to solve single agent sub-problems.
     * @param openList this will be used as the {@link I_OpenList open list} in the solver. This instance will be reused
     *                 by calling {@link I_OpenList#clear()} after every run.
     * @param openListManagementMode @see {@link OpenListManagementMode}.
     * @param costFunction a cost function for solutions.
     * @param cbsNodeComparator determines how to sort {@link #openList OPEN}.
     * @param useCorridorReasoning whether to use corridor reasoning.
     */
    CBS_Solver(@Nullable I_Solver lowLevelSolver, @Nullable I_OpenList<CBS_Node> openList, @Nullable OpenListManagementMode openListManagementMode,
                      @Nullable I_SolutionCostFunction costFunction, @Nullable Comparator<? super CBS_Node> cbsNodeComparator, @Nullable Boolean useCorridorReasoning,
                      @Nullable Boolean sharedGoals, @Nullable Boolean sharedSources, @Nullable TransientMAPFSettings transientMAPFSettings) {
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.openList = Objects.requireNonNullElseGet(openList, OpenListHeap::new);
        this.openListManagementMode = openListManagementMode != null ? openListManagementMode : OpenListManagementMode.AUTOMATIC;
        this.corridorReasoning = Objects.requireNonNullElse(useCorridorReasoning, false);
        clearOPEN();
        // if a specific cost function is not provided, use standard SOC (Sum of Individual Costs)
        this.costFunction = Objects.requireNonNullElseGet(costFunction, SumOfCosts::new);
        this.CBSNodeComparator = cbsNodeComparator != null ? cbsNodeComparator : new CBSNodeComparatorForcedTotalOrdering();
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);

        super.name = "CBS" + (this.transientMAPFSettings.isTransientMAPF() ? "t" : "");
    }

    /**
     * Default constructor.
     */
    CBS_Solver() {
        this(null, null, null, null, null, null, null, null, null);
    }

    /*  = initialization =  */

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        this.initialConstraints = Objects.requireNonNullElseGet(runParameters.constraints, ConstraintSet::new);
        this.currentConstraints = new ConstraintSet();
        this.generatedNodes = 0;
        this.expandedNodes = 0;
        this.instance = instance;

        // heuristic
        if (runParameters.singleAgentGAndH != null){
            this.singleAgentGAndH = runParameters.singleAgentGAndH;
        }
        else {
            if (this.lowLevelSolver instanceof SingleAgentAStar_Solver){
                this.singleAgentGAndH = new DistanceTableSingleAgentHeuristic(new ArrayList<>(instance.agents), instance.map);
            }
            if (this.singleAgentGAndH instanceof CachingDistanceTableHeuristic){
                ((CachingDistanceTableHeuristic)this.singleAgentGAndH).setCurrentMap(instance.map);
            }
            if (this.singleAgentGAndH != null && this.costFunction instanceof SumServiceTimes){
                this.singleAgentGAndH = new ServiceTimeGAndH(this.singleAgentGAndH);
            }
        }
    }

    /*  = algorithm =  */

    /**
     * Implements the CBS algorithm, as described in the original CBS article from Proceedings of the Twenty-Sixth AAAI
     * Conference on Artificial Intelligence.
     * @param instance {@inheritDoc}
     * @param parameters {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        initOpen(Objects.requireNonNullElseGet(parameters.constraints, ConstraintSet::new));
        CBS_Node goal = mainLoop();
        return solutionFromGoal(goal);
    }

    /**
     * Initialises the {@link #openList OPEN} and inserts the root.
     * @param initialConstraints a set of initial constraints on the agents.
     */
    private void initOpen(I_ConstraintSet initialConstraints) {
        if(this.openListManagementMode == OpenListManagementMode.AUTOMATIC ||
                this.openListManagementMode == OpenListManagementMode.AUTO_INIT_MANUAL_CLEAR){
            addToOpen(generateRoot(initialConstraints));
        }
    }

    /**
     * Creates a root node.
     */
    private CBS_Node generateRoot(I_ConstraintSet initialConstraints) {
        // init an empty solution
        Solution solution = transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution() : new Solution();
        // for every agent, add its plan to the solution
        for (Agent agent :
                this.instance.agents) {
            solution = solveSubproblem(agent, solution, initialConstraints);
            if (solution == null){
                // failed to solve for some agent
                return null;
            }
        }

        return new CBS_Node(solution, costFunction.solutionCost(solution));
    }

    /**
     * The main loop of the CBS algorithm. Expands and generates nodes.
     * @return the goal node, or null if a timeout occurs before it is found.
     */
    private CBS_Node mainLoop() {
        while(!openList.isEmpty() && !checkTimeout()){
            CBS_Node node = openList.poll();

            // verify solution (find conflicts)
            I_ConflictManager cat = getConflictManagerFor(node);
            node.setSelectedConflict(cat.selectConflict());

            if(isGoal(node)){
                return node;
            }
            else {
                expandNode(node);
            }
        }

        return null; //probably a timeout
    }

    /**
     * When a node is first generated, it is given the same {@link ConflictManager} as its parent. Only when that
     * node is later dequeued from {@link #openList}, will we update the table.
     * @param node a {@link CBS_Node node} that contains an out of date {@link ConflictManager}.
     * @return a {@link I_ConflictManager} for the solution in this node.
     */
    private I_ConflictManager getConflictManagerFor(CBS_Node node) {
        I_ConflictManager cat = this.corridorReasoning ?
                new CorridorConflictManager(buildConstraintSet(node,null), this.instance) :
                new ConflictManager(null, this.sharedGoals, this.sharedSources);
        for (SingleAgentPlan plan :
                node.getSolution()) {
            cat.addPlan(plan);
        }
        return cat;
    }

    private boolean isGoal(CBS_Node node) {
        // no conflicts -> found goal
        return node.selectedConflict == null;
    }

    /**
     * Expands a {@link CBS_Node}.
     * @param node a node to expand.
     */
    private void expandNode(CBS_Node node) {
        this.expandedNodes++;

        Constraint[] constraints = node.selectedConflict.getPreventingConstraints();
        // make copies of data structures for left child, while reusing the parent's data structures on the right child.
        node.leftChild = generateNode(node, constraints[0], true);
        node.rightChild = generateNode(node, constraints[1], false);

        addToOpen(node.leftChild);
        addToOpen(node.rightChild);
    }

    /**
     * Adds a node to {@link #openList OPEN}. If a duplicate node exists, keeps the one with less cost.
     * @param node a node to insert into {@link #openList OPEN}
     * @return true if {@link #openList OPEN} changed as a result of the call.
     */
    private boolean addToOpen(CBS_Node node) {
        if(node == null){
            // either the low level encountered a timeout (in which case we will also timeout very soon), or the low
            // level was unsolvable (in which case we prune this node and continue).
            return false;
        }
        return openList.add(node);
    }

    /**
     * Since the creation of a new {@link CBS_Node node} is somewhat complicated, it is handled in its own method.
     * @param parent the new node's parent
     * @param constraint the constraint that we want to add in this node, before re-solving the agent that is constrained.
     * @param copyDatastructures for one child, we may be able to reuse the parent's data structures, instead of copying them.
     * @return a new {@link CBS_Node}.
     */
    private CBS_Node generateNode(CBS_Node parent, Constraint constraint, boolean copyDatastructures) {
        Agent agent = constraint.agent;

        Solution solution = parent.solution;

        // replace with copies if required
        if(copyDatastructures) {
            solution =  transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution(solution) : new Solution(solution);
        }

        // modify for this node
        /*  replace the current plan for the agent with an empty plan, so that the low level won't try to continue the
            existing plan.
            Also we don't want to reuse (modify) SingleAgentPlan objects, as they are pointed to by other Solution objects, which
            we don't want to modify.
         */
        solution.putPlan(new SingleAgentPlan(agent));

        //the low-level should update the solution, so this is a reference to the same object as solution. We do this to
        //reuse Solution objects instead of creating extra ones.
        Solution agentSolution = solveSubproblem(agent, solution, buildConstraintSet(parent, constraint));
        if(agentSolution == null) {
            return null; //probably a timeout
        }
        // in case the low-level didn't update the Solution object it was given, this makes sure we preserve other agents'
        // plans, and add the re-planned agent's new plan.
        solution.putPlan(agentSolution.getPlanFor(agent));

        return new CBS_Node(solution, costFunction.solutionCost(solution), constraint, parent);
    }

    /**
     * When solving a new node, you want a set of constraints that apply to it. To save on memory, this set is created
     * on the spot, by climbing up the CT and collecting all the constraints that were added
     * @param parentNode the new node's parent.
     * @param newConstraint the constraint that this new node adds.
     * @return a {@link ConstraintSet} of all the constraints from parentNode to the root, plus newConstraint.
     */
    private I_ConstraintSet buildConstraintSet(CBS_Node parentNode, Constraint newConstraint) {
        // clear currentConstraints. we reuse this object every time.
        this.currentConstraints.clear();
        I_ConstraintSet constraintSet = this.currentConstraints;
        // start by adding all the constraints that we were asked to start the solver with (and are therefore not in the CT)
        constraintSet.addAll(initialConstraints);

        CBS_Node currentNode = parentNode;
        while (currentNode.addedConstraint != null){ // will skip the root (it has no constraints)
            constraintSet.add(currentNode.addedConstraint);
            currentNode = currentNode.parent;
        }
        if(newConstraint != null){
            constraintSet.add(newConstraint);
        }
        return constraintSet;
    }

    /**
     * Solves a single agent sub-problem.
     * @param agent
     * @param currentSolution
     * @param constraints
     * @return a solution to a single agent sub-problem. Typically the same object as currentSolution, after being modified.
     */
    private Solution solveSubproblem(Agent agent, Solution currentSolution, I_ConstraintSet constraints) {
        InstanceReport instanceReport = new InstanceReport();
        instanceReport.keepSolutionString = false;
        RunParameters subproblemParameters = getSubproblemParameters(currentSolution, constraints, instanceReport, agent);
        Solution subproblemSolution = this.lowLevelSolver.solve(this.instance.getSubproblemFor(agent), subproblemParameters);
        digestSubproblemReport(instanceReport);
        return subproblemSolution;
    }

    private RunParameters getSubproblemParameters(Solution currentSolution, I_ConstraintSet constraints, InstanceReport instanceReport, Agent agent) {
        // if there was already a timeout while solving a node, we will get a negative time left, which would be
        // interpreted as "use default timeout". In such a case we should instead give the solver 0 time to solve.
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        RunParameters subproblemParametes = new RunParametersBuilder().setTimeout(timeLeftToTimeout).setConstraints(new UnmodifiableConstraintSet(constraints)).
                setInstanceReport(instanceReport).setExistingSolution(currentSolution).setAStarGAndH(this.singleAgentGAndH).createRP();
        if(this.lowLevelSolver instanceof SingleAgentAStar_Solver){ // upgrades to a better heuristic
            RunParameters_SAAStar astarSubproblemParameters = new RunParameters_SAAStar(subproblemParametes);

            // TMAPF goal condition
            if (transientMAPFSettings.isTransientMAPF()){
                if (transientMAPFSettings.useBlacklist()) {
                    Set<I_Coordinate> targetsOfAgentsThatHaventPlannedYet = new HashSet<>();
                    for (Agent agentToBlack : this.instance.agents) {
                        if (!agent.equals(agentToBlack)) {
                            targetsOfAgentsThatHaventPlannedYet.add(agentToBlack.target);
                        }
                    }
                    astarSubproblemParameters.goalCondition = new VisitedTargetAndBlacklistAStarGoalCondition(targetsOfAgentsThatHaventPlannedYet);
                }
                else {
                    astarSubproblemParameters.goalCondition = new VisitedTargetAStarGoalCondition();
                }
            }

            SingleUseConflictAvoidanceTable cat = new SingleUseConflictAvoidanceTable(currentSolution, agent);
            cat.sharedGoals = this.sharedGoals;
            cat.sharedSources = this.sharedSources;
            astarSubproblemParameters.conflictAvoidanceTable = cat;
            subproblemParametes = astarSubproblemParameters;
        }
        return subproblemParametes;
    }

    /**
     * Extracts a solution from a goal {@link CBS_Node node}.
     * @param goal a {@link CBS_Node} that we consider to be a goal node.
     * @return a solution from a goal {@link CBS_Node node}.
     */
    private Solution solutionFromGoal(CBS_Node goal) {
        if(goal == null){
            return null;
        }
        else{
            return goal.solution;
        }
    }

    /**
     * Clears OPEN
     */
    private void clearOPEN() {
        if(this.openListManagementMode == OpenListManagementMode.AUTOMATIC ||
                this.openListManagementMode == OpenListManagementMode.MANUAL_INIT_AUTO_CLEAR){
            openList.clear();
        }
    }

    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        if(solution != null){
            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, costFunction.name());
            super.instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, costFunction.solutionCost(solution));
        }
    }

    @Override
    protected void releaseMemory() {
        clearOPEN();
        this.initialConstraints = null;
        this.currentConstraints = null;
        this.instance = null;
        this.singleAgentGAndH = null;
    }

    /*  = internal classes and interfaces =  */

    /**
     * A data type for representing a single node in the CBS search tree.
     * Try to keep most logic in {@link CBS_Solver}, avoiding methods in this class.
     */
    public class CBS_Node implements Comparable<CBS_Node>{

        /*  =  = fields =  */

        /**
         * The solution in this node. For every non-root node, this solution is after rerouting (solving low level) an
         * agent to overcome a conflict.
         * Holds references to the same {@link SingleAgentPlan plans} as in {@link #parent}, apart from the plan
         * of the re-routed agent.
         */
        private Solution solution;
        /**
         * The cost of the solution.
         */
        private float solutionCost;
        /**
         * The constraint that was added in this node (missing from {@link #parent}).
         */
        private Constraint addedConstraint;
        /**
         * A {@link A_Conflict conflict}, selected to be solved by new constraints in child nodes.
         */
        private A_Conflict selectedConflict;
        /**
         * Needed to enforce total ordering on nodes, which is needed to make node expansions fully deterministic. That
         * is to say, if all tie breaking methods still result in equality, tie break for using serialID.
         */
        private final int serialID = CBS_Solver.this.generatedNodes++; // take and increment

        /*  =  =  = CBS tree branches =  =  */

        /**
         * This node's parent node. This node's {@link #addedConstraint} solves parent's {@link #selectedConflict}.
         */
        private CBS_Node parent;
        /**
         * One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        private CBS_Node leftChild;
        /**
         * One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        private CBS_Node rightChild;

        /*  =  = constructors =  */

        /**
         * Root constructor.
         * @param solution an initial solution for all agents.
         * @param solutionCost the cost of the solution.
         */
        public CBS_Node(Solution solution, float solutionCost) {
            this.solution = solution;
            this.solutionCost = solutionCost;
            this.parent = null;
        }

        /**
         * Non-root constructor.
         */
        public CBS_Node(Solution solution, float solutionCost, Constraint addedConstraint, CBS_Node parent) {
            this.solution = solution;
            this.solutionCost = solutionCost;
            this.addedConstraint = addedConstraint;
            this.parent = parent;
        }

        /*  =  = when expanding a node =  */

        /**
         * Set the selected conflict. Typically done through delegation to {@link I_ConflictManager#selectConflict()}.
         * @param selectedConflict
         */
        public void setSelectedConflict(A_Conflict selectedConflict) {
            this.selectedConflict = selectedConflict;
        }

        /**
         * Set a reference to one of the generated child nodes when expanding this node.
         * @param leftChild One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        public void setLeftChild(CBS_Node leftChild) {
            this.leftChild = leftChild;
        }

        /**
         * Set a reference to one of the generated child nodes when expanding this node.
         * @param rightChild One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        public void setRightChild(CBS_Node rightChild) {
            this.rightChild = rightChild;
        }

        /*  =  = getters =  */

        public Solution getSolution() {
            return solution;
        }

        public float getSolutionCost() {
            return solutionCost;
        }

        public Constraint getAddedConstraint() {
            return addedConstraint;
        }

        public A_Conflict getSelectedConflict() {
            return selectedConflict;
        }

        public CBS_Node getParent() {
            return parent;
        }

        public CBS_Node getLeftChild() {
            return leftChild;
        }

        public CBS_Node getRightChild() {
            return rightChild;
        }

        @Override
        public int compareTo(CBS_Node o) {
            return Objects.compare(this, o, CBS_Solver.this.CBSNodeComparator);
        }

    }

    public static class CBSNodeComparatorForcedTotalOrdering implements Comparator<CBS_Node>{

        private static final Comparator<CBS_Node> costComparator = Comparator.comparing(CBS_Node::getSolutionCost);

        @Override
        public int compare(CBS_Node o1, CBS_Node o2) {
            if(Math.abs(o1.getSolutionCost() - o2.getSolutionCost()) < 0.1){ // floats are equal
                // If still equal, we tie break for smaller ID (older nodes) (arbitrary) to force a total ordering and remain deterministic
                return o2.serialID- o1.serialID;
            }
            else {
                return costComparator.compare(o1, o2);
            }
        }
    }


    /**
     * Modes for handling the initialization and clearing of {@link #openList OPEN}. The default mode of operation is
     * {@link #AUTOMATIC}.
     */
    public enum OpenListManagementMode{
        /**
         * Will handle OPEN automatically. This is the standard mode of operation. The solver will clear OPEN before and
         * after every run, and initialize OPEN at the start of every run with a single root {@link CBS_Node node}.
         */
        AUTOMATIC,
        /**
         * Will initialize OPEN automatically, but clearing it before or after a run will be controlled manually.
         * Note that this means the solver keeps part of its state after running. If you want to reuse the solver, you
         * have to manually handle the clearing of OPEN. If you keep references to many such solvers, this may adversely
         * affect available memory.
         */
        AUTO_INIT_MANUAL_CLEAR,
        /**
         * Will not initialize OPEN (assumes that it was already initialized), but will clear it after running.
         * It is not cleared before running. If it were to be cleared before running, manual initialization would be
         * impossible.
         */
        MANUAL_INIT_AUTO_CLEAR,
        /**
         * Will not initialize OPEN (assumes that it was already initialized).
         * Will not clear OPEN automatically.
         * Note that this means the solver keeps part of its state after running. If you want to reuse the solver, you
         * have to manually handle the clearing of OPEN. If you keep references to many such solvers, this may adversely
         * affect available memory.
         */
        MANUAL
    }
}
