package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.I_AStarGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.AtTargetAStarGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * An A* solver that only solves single agent problems. It assumes the first {@link Agent} from {@link MAPF_Instance instances}
 * that it is given is the agent to solve for.
 * By default, it uses {@link I_Coordinate#distance(I_Coordinate)} as a heuristic.
 */
public class SingleAgentAStar_Solver extends A_Solver {

    private static final Comparator<AStarState> equalStatesDiscriminator = new TieBreakingForLowerGAndLessConflicts();
    protected final Comparator<AStarState> stateComparator;
    public boolean agentsStayAtGoal = true;

    protected I_ConstraintSet constraints;
    protected I_OpenList<AStarState> openList;
    protected Set<AStarState> closed;
    protected Agent agent;
    protected I_Map map;
    protected SingleAgentPlan existingPlan;
    protected Solution existingSolution;
    protected I_ConflictAvoidanceTable conflictAvoidanceTable;
    public I_Coordinate sourceCoor;
    public I_Coordinate targetCoor;
    public I_AStarGoalCondition goalCondition;
    protected SingleAgentGAndH gAndH;
    private PseudoRandomUniformSamplingIntNoRepeat randomIDGenerator;
    /**
     * Not real-world time. The problem's start time.
     */
    protected int problemStartTime;
    protected int expandedNodes;
    private int generatedNodes;
    private int numRegeneratedNodes;
    /**
     * Maximum allowed f value ({@link SingleAgentPlan} cost). Will stop and return null if proved that it cannot be found.
     */
    protected float fBudget;

    public SingleAgentAStar_Solver() {this(null);}

    public SingleAgentAStar_Solver(Comparator<AStarState> stateComparator) {
        super.name = "AStar";
        this.stateComparator = Objects.requireNonNullElse(stateComparator, new TieBreakingForLessConflictsAndHigherG());
    }
    /*  = set up =  */

    protected void init(MAPF_Instance instance, RunParameters runParameters){
        super.init(instance, runParameters);
        this.openList = createEmptyOpenList();
        this.closed = new HashSet<>();
        this.constraints = runParameters.constraints == null ? new ConstraintSet(): runParameters.constraints;
        this.agent = instance.agents.get(0);
        this.map = instance.map;

        this.problemStartTime = runParameters.problemStartTime;
        if(runParameters.existingSolution != null){
            this.existingSolution = runParameters.existingSolution;
            if(runParameters.existingSolution.getPlanFor(this.agent) != null){
                this.existingPlan = runParameters.existingSolution.getPlanFor(this.agent);
                if (existingPlan.size() > 0){
                    this.problemStartTime = this.existingPlan.getEndTime();
                }
            }
            else {
                this.existingPlan = new SingleAgentPlan(this.agent);
                this.existingSolution.putPlan(this.existingPlan);
            }
        }
        else{
            // make a new, empty solution, with a new, empty, plan
            this.existingSolution = new Solution();
            this.existingPlan = new SingleAgentPlan(this.agent);
            this.existingSolution.putPlan(this.existingPlan);
        }

        if(runParameters instanceof RunParameters_SAAStar parameters
                && parameters.conflictAvoidanceTable != null){
            this.conflictAvoidanceTable = parameters.conflictAvoidanceTable;
        }
        // else keep the value that it has already been initialised with (above)

        if(runParameters instanceof RunParameters_SAAStar parameters){
            this.sourceCoor = parameters.sourceCoor != null ? parameters.sourceCoor : agent.source;
            this.targetCoor = parameters.targetCoor != null ? parameters.targetCoor : agent.target;
        }
        else{
            this.sourceCoor = agent.source;
            this.targetCoor = agent.target;
        }

        if(runParameters instanceof RunParameters_SAAStar parameters
                && parameters.goalCondition != null){
            this.goalCondition = parameters.goalCondition;
        }
        else{
            this.goalCondition = new AtTargetAStarGoalCondition(this.targetCoor);
        }
        this.gAndH = Objects.requireNonNullElseGet(runParameters.singleAgentGAndH, () -> new UnitCostsAndManhattanDistance(this.targetCoor));
        if (! this.gAndH.isConsistent()){
            throw new IllegalArgumentException("Support for inconsistent heuristics is not implemented.");
        }
        if (this.goalCondition instanceof VisitedTargetAStarGoalCondition ^ this.gAndH.isTransient()){
            throw new IllegalArgumentException("VisitedTargetAStarGoalCondition requires a transient heuristic and vice versa. Got a " +
                    this.gAndH.getClass().getSimpleName() + " heuristic and a " + this.goalCondition.getClass().getSimpleName() + " goal condition.");
        }

        // todo should make this more explicit. Getting an rng might not necessarily mean that we want to use it like this.
        this.randomIDGenerator = runParameters.randomNumberGenerator == null ? null:
                new PseudoRandomUniformSamplingIntNoRepeat(runParameters.randomNumberGenerator);

        if(runParameters instanceof RunParameters_SAAStar parameters){
            this.fBudget = parameters.fBudget;
        }
        else{
            this.fBudget = Float.POSITIVE_INFINITY;
        }

        this.expandedNodes = 0;
        this.generatedNodes = 0;
        this.numRegeneratedNodes = 0;
    }

    protected @NotNull I_OpenList<AStarState> createEmptyOpenList() {
        return new OpenListTree<>(stateComparator);
    }

    /*  = A* algorithm =  */

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        return solveAStar();
    }

    /**
     * Solves AStar for a single agent.
     * Assumes just 1 goal {@link I_Location location} - otherwise there may be problems when accounting for constraints
     * at goal, that come after reaching goal.
     * @return a solution that contains a plan for the {@link #agent} to its goal.
     */
    protected Solution solveAStar() {
        // if failed to init OPEN then the problem cannot be solved as defined (bad constraints? bad existing plan?)
        if (!initOpen()) return null;

        AStarState currentState;
        int lastRejectionTime = 0;
        Map<I_Location, Integer> lastRejectionTimes = new HashMap<>(); // todo ArrayMap?

        while ((currentState = openList.poll()) != null){ //dequeu in the while condition
            if(checkTimeout()) {return null;}
            // early stopping if we already exceed fBudget.
            if (currentState.getF() > fBudget) {return null;}
            closed.add(currentState);

            // nicetohave - change to early goal test
            if ((this.conflictAvoidanceTable != null && currentState.isALastMove) // we split the state into a visit and a "stay forever" state
                    || (this.conflictAvoidanceTable == null && isGoalState(currentState)) // didn't split, so check if this is a valid place to stay forever and finish
            )
            {
                // check to see if a rejecting constraint on the goal's location exists at some point in the future,
                // which would mean we can't finish the plan there and stay forever
                if (!agentsStayAtGoal){
                    lastRejectionTime = -1;
                }
                // For Transient MAPF paths. May try to stop forever in different locations, so caching is more complicated.
                else if (goalCondition instanceof VisitedTargetAStarGoalCondition){
                    Move currentMove = currentState.move;
                    lastRejectionTime = lastRejectionTimes.computeIfAbsent(currentState.move.currLocation,
                            k -> constraints.lastRejectionTime(currentMove));
                }
                else if (lastRejectionTime == 0) { // (classic MAPF) uninitialized (caching the result)
                    lastRejectionTime = constraints.lastRejectionTime(currentState.move);
                }

                if(lastRejectionTime < currentState.move.timeNow){ // no rejections
                    // update this.existingPlan which is contained in this.existingSolution
                    currentState.backTracePlan(this.existingPlan);
                    return this.existingSolution; // the goal is good, and we can return the plan.
                } else // we are rejected from the goal location at some point in the future.
                    if (!(this.conflictAvoidanceTable != null && currentState.isALastMove)) { // expanding a "last move" is meaningless
                    expand(currentState);
                }
            } else { //expand
                expand(currentState); //doesn't generate closed or duplicate states
            }
        }
        return null; //no goal state found (problem unsolvable)
    }

    /**
     * Initialises {@link #openList OPEN}.
     * OPEN is not initialised with a single root state as is common. This is because states in this solver represent
     * {@link Move moves} (classically - operators) rather than {@link I_Location map locations} (classically - states).
     * Instead, OPEN is initialised with all possible moves from the starting position.
     * @return true if OPEN was successfully initialised, else false.
     */
    protected boolean initOpen() {
        // if the existing plan isn't empty, we start from the last move of the existing plan.
        if(existingPlan.size() > 0){
            Move lastExistingMove = existingPlan.moveAt(existingPlan.getEndTime());
            // We assume that we cannot change the existing plan, so if it is rejected by constraints, we can't initialise OPEN.
            if(constraints.rejects(lastExistingMove)) {return false;}

            generate(existingPlan.moveAt(existingPlan.getEndTime()),null, 0, visitedTarget(null, existingPlan.containsTarget()));
        }
        else { // the existing plan is empty (no existing plan)

            I_Location sourceLocation = map.getMapLocation(this.sourceCoor);
            // can move to neighboring locations or stay, unless this is NO_STOP location, in which case can only move
            List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(sourceLocation.outgoingEdges());
            if (sourceLocation.getType() != Enum_MapLocationType.NO_STOP){
                neighborLocationsIncludingCurrent.add(sourceLocation);
            }

            for (I_Location destination: neighborLocationsIncludingCurrent) {
                Move possibleMove = new Move(agent, problemStartTime + 1, sourceLocation, destination);
                if (sourceLocation.equals(destination)){
                    possibleMove.isStayAtSource = true;
                }
                if (constraints.accepts(possibleMove)) { //move not prohibited by existing constraint
                    generate(possibleMove, null, getG(null, possibleMove), visitedTarget(null, isMoveToTarget(possibleMove)));
                }
            }

        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    protected void expand(@NotNull AStarState state) {
        expandedNodes++;
        // can move to neighboring locations or stay put
        List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(state.move.currLocation.outgoingEdges());
        // no point to do stay moves or search the time dimension after the time of last constraint.
        // this makes A* complete even when there are goal constraints (infinite constraints)
        boolean afterLastConstraint = (state.move.timeNow > constraints.getLastConstraintStartTime()) && // after the time of last constraint (according to constraints set)
                // if conflicts avoidance table exists, soft constraints should be supported too
                (this.conflictAvoidanceTable == null || (state.move.timeNow > conflictAvoidanceTable.getLastOccupancyTime())); // after the time of last conflicts according to conflictAvoidanceTable
        if (!afterLastConstraint &&
                !state.move.currLocation.getType().equals(Enum_MapLocationType.NO_STOP)) { // can't stay on NO_STOP
            neighborLocationsIncludingCurrent.add(state.move.currLocation);
        }

        for (I_Location destination : neighborLocationsIncludingCurrent) {
            // give all moves after last constraint time the same time so that they're equal. patch the plan later to correct times
            Move possibleMove = new Move(state.move.agent, !afterLastConstraint ? state.move.timeNow + 1 : state.move.timeNow,
                    state.move.currLocation, destination);
            if (possibleMove.prevLocation.equals(possibleMove.currLocation) && state.move.isStayAtSource) {
                possibleMove.isStayAtSource = true;
            }

            // move not prohibited by existing constraint
            if (constraints.accepts(possibleMove)) {
                generate(possibleMove, state, getG(state, possibleMove), visitedTarget(state, isMoveToTarget(possibleMove)));
            }
        }
    }

    protected void generate(Move move, AStarState prevState, int g, boolean visitedTarget) {
        AStarState newNode = new AStarState(move, prevState, g, this.gAndH,
                (prevState == null ? 0 : prevState.conflicts) + numConflicts(move, false),
                visitedTarget, false);
        addToOpenList(newNode);
        if (this.conflictAvoidanceTable != null && goalCondition.isAGoal(move, visitedTarget)){
            // This is a possible goal. We must check how many conflicts will happen if we stay
            // there forever, which is different from the number of conflicts if we just pass through.
            AStarState lastMoveCandidateChild = new AStarState(move, prevState, g, this.gAndH,
                    (prevState == null ? 0 : prevState.conflicts) + numConflicts(move, true),
                    visitedTarget, true);
            addToOpenList(lastMoveCandidateChild);
        }
    }

    protected void addToOpenList(@NotNull AStarState state) {
        AStarState existingState;
        if (closed.contains(state)) { // state visited already
            // TODO for inconsistent heuristics -
            //  if the new one has a lower f, remove the old one from closed and add the new one to open
        } else if (null != (existingState = openList.get(state))) { //an equal state is waiting in open
            //keep the one with min G
            state.keepTheStateWithMinG(state, existingState); //O(LOGn)
        } else { // it's a new state
            openList.add(state);
        }
    }

    boolean isMoveToTarget(Move possibleMove) {
        return possibleMove.currLocation.getCoordinate().equals(targetCoor);
    }
    protected boolean isGoalState(AStarState state) {
        return this.goalCondition.isAGoal(state);
    }

    public int getGeneratedNodes() {
        return this.generatedNodes;
    }

    public int getExpandedNodes() {
        return this.expandedNodes;
    }

    /*  = wind down =  */

    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);

        if(instanceReport != null){
            instanceReport.putIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel, this.expandedNodes);
            instanceReport.putIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel, this.generatedNodes);
            instanceReport.putIntegerValue(InstanceReport.StandardFields.regeneratedNodesLowLevel, this.numRegeneratedNodes);
        }
    }

    protected void releaseMemory() {
        super.releaseMemory();
        this.constraints = null;
        this.gAndH = null;
        this.randomIDGenerator = null;
        this.instanceReport = null;
        this.openList = null;
        this.closed = null;
        this.agent = null;
        this.map = null;
        this.existingSolution = null;
        this.existingPlan = null;
        this.conflictAvoidanceTable = null;
        this.sourceCoor = null;
        this.targetCoor = null;
        this.goalCondition = null;
    }

    /**
     * the number of conflicts that the given move would generate.
     * @param move the move to check for conflicts.
     * @param isALastMove True if the move is intended as the last move in a plan, meaning that the agent will stay at the
     *                    location indefinitely. For TMAPF. Irrelevant in classic MAPF, because these conflicts are used only for
     *                    tie-breaking (soft constraints), and each agent can only end at its unique target.
     * @return the number of conflicts that the given move would generate.
     */
    protected int numConflicts(Move move, boolean isALastMove){
        return conflictAvoidanceTable == null ? 0 : conflictAvoidanceTable.numConflicts(move, isALastMove);
    }

    private int getID() {
        return this.randomIDGenerator == null ? SingleAgentAStar_Solver.this.generatedNodes - 1: randomIDGenerator.next();
    }

    /*  = inner classes =  */

    public class AStarState implements Comparable<AStarState>{

        /**
         * Needed to enforce total ordering on nodes, which is needed to make node expansions fully deterministic. That
         * is to say, if all tie breaking methods still result in equality, tie break for using serialID.
         */
        protected final int id = getID();
        public final Move move;
        protected final AStarState prev;
        protected final int g;
        public final float h;
        private final int f;
        /**
         * counts the number of conflicts generated by this node and all its ancestors.
         */
        protected final int conflicts;
        public final boolean visitedTarget;
        public final boolean isALastMove;

        /**
         * Create a new A* state.
         * @param move          the move that this state represents.
         * @param prevState     the state that this state is coming from.
         * @param g             the cost of this state (cumulative).
         * @param hFunction    the heuristic function to use for this state.
         * @param conflicts     the number of conflicts (cumulative) with soft constraints that this state has.
         * @param visitedTarget whether the agent has visited its target location - either now or in the past.
         * @param isALastMove  whether this move is the last move of the agent, meaning the agent is set to stay there forever.
         *                     For TMAPF. Irrelevant in classic MAPF, because only the target vertex is considered for last move.
         */
        public AStarState(@NotNull Move move, AStarState prevState, int g, SingleAgentGAndH hFunction, int conflicts, boolean visitedTarget, boolean isALastMove) {
            SingleAgentAStar_Solver.this.generatedNodes++;
            this.move = move;
            this.prev = prevState;
            this.g = g;
            this.conflicts = conflicts;
            this.visitedTarget = visitedTarget;
            this.isALastMove = isALastMove;

            // must call this last, since it needs some other fields to be initialized already.
            // todo - should just change the interface to get the needed values rather than a partial instance of the class
            this.h = hFunction.getH(this);

            this.f = Math.round(this.g + this.h);
            if (Config.DEBUG >= 2 && Math.abs(this.g + this.h - this.f) > 0.1){
                throw new IllegalStateException("f value is not approximately an integer: " + this.f + " = " + this.g + " + " + this.h);
            }
        }

        /*  = getters =  */
        public Move getMove() {
            return move;
        }

        public AStarState getPrev() {
            return prev;
        }

        public int getG() {
            return g;
        }

        public int getConflicts() {
            return conflicts;
        }

        public int getF(){
            return f;
        }

        protected void keepTheStateWithMinG(AStarState newState, AStarState existingState) {
            // decide which state to keep, seeing as how they are both equal and in open.
            AStarState dropped = openList.keepOne(existingState, newState, SingleAgentAStar_Solver.equalStatesDiscriminator);
            if (dropped == existingState){
                SingleAgentAStar_Solver.this.numRegeneratedNodes++;
            }
        }

        /**
         * Trace back a plan from this state, return the plan after it was updates with the moves found by going back
         * from this state.
         * @param existingPlan an existing plan which we are continuing.
         * @return the existingPlan after updating it with the plan that this state represents.
         */
        public SingleAgentPlan backTracePlan(SingleAgentPlan existingPlan) {
            List<Move> moves = getOrderedMoves();

            // patch move times in case we had moves that don't progress time, because they were after last constraint time
            for (int i = 1; i < moves.size(); i++) {
                Move prevMove = moves.get(i-1);
                Move currMove = moves.get(i);
                if (currMove.timeNow != prevMove.timeNow + 1){
                    moves.set(i, new Move(currMove.agent, prevMove.timeNow + 1, currMove.prevLocation, currMove.currLocation));
                }
            }

            //if there was an existing plan before solving, then we started from its last move, and don't want to duplicate it.
            if(existingPlan.size() > 0) {moves.remove(0);}
            existingPlan.addMoves(moves);
            return existingPlan;
        }

        @NotNull
        protected List<Move> getOrderedMoves() {
            List<Move> moves = new ArrayList<>();
            AStarState currentState = this;
            while (currentState != null){
                moves.add(currentState.move);
                currentState = currentState.prev;
            }
            Collections.reverse(moves); //reorder moves because they were reversed
            return moves;
        }


        /**
         * equality is determined by location (current), and time.
         * @param o {@inheritDoc}
         * @return {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AStarState that)) return false;
            if (move.timeNow != that.move.timeNow) return false;
            if (visitedTarget != that.visitedTarget) return false;
            if (isALastMove != that.isALastMove) return false;
            return move.currLocation.equals(that.move.currLocation);
        }

        @Override
        public int hashCode() {
            int result = move.currLocation.hashCode();
            result = 31 * result + move.timeNow;
            result = 31 * result + (visitedTarget ? 1 : 0);
            result = 31 * result + (isALastMove ? 1 : 0);
            return result;
        }

        @Override
        public int compareTo(@NotNull AStarState o) {
            return stateComparator.compare(this, o);
        }

        @Override
        public String toString() {
            return "AStarState{" +
                    "id=" + id +
                    ", g=" + g +
                    ", h=" + h +
                    ", conflicts=" + conflicts +
                    ", plan=\n" + this.backTracePlan(new SingleAgentPlan(this.move.agent)).toString() +
                    '}';
        }

    } ////////// end AStarState

    /**
     * @return true if the agent has visited its target location before, either now or in the past.
     */
    protected boolean visitedTarget(@Nullable AStarState prevState, boolean isMoveToTargetLocation) {
        return goalCondition instanceof VisitedTargetAStarGoalCondition &&
                (isMoveToTargetLocation || (prevState != null && prevState.visitedTarget));
    }

    protected int getG(@Nullable AStarState prev, Move move){
        return prev != null ? prev.g + gAndH.cost(move, prev.visitedTarget /*so we visited in the past, not first time now*/)
                : gAndH.cost(move);
    }


    /**
     * For sorting the open list.
     */
    public static class TieBreakingForLessConflictsAndHigherG implements BucketingComparator<AStarState>{
        @Override
        public int compare(AStarState o1, AStarState o2) {
            int fCompared = bucketCompare(o1, o2);
            if(fCompared == 0){ // f is equal
                // if f() value is equal, we consider the state with less conflicts to be better.
                if(o1.conflicts == o2.conflicts){
                    // if equal in conflicts, we break ties for higher g. Therefore, we want to return a negative
                    // integer if o1.g is bigger than o2.g
                    if (o2.g == o1.g){
                        // If still equal, we tie break for smaller ID (older nodes) (arbitrary) to force a total ordering and remain deterministic
                        return o1.id - o2.id;

                    }
                    else {
                        return o2.g - o1.g; //higher g is better
                    }
                }
                else{
                    return o1.conflicts - o2.conflicts; // less conflicts is better
                }
            }
            else {
                return fCompared;
            }
        }

        @Override
        public int getBucket(AStarState aStarState) {
            return aStarState.getF();
        }
    }

    /**
     * For deciding which state to keep between two equal states in open.
     */
    private static class TieBreakingForLowerGAndLessConflicts implements Comparator<AStarState>{

        @Override
        public int compare(AStarState o1, AStarState o2) {
            // if f() is monotone non-decreasing, we should never actually find a new state with lower G than
            // existing equal state in open.
            // we break ties for lower g. Therefore, we want to return a positive integer if o1.g is bigger than o2.g.
            // g should be equal in practice if G=state-time, and state equality is also defined by state-time.
            if (o2.g == o1.g){
                // if G() value is equal, we consider the state with less conflicts to be better.
                if(o1.conflicts == o2.conflicts){
                    // If still equal, we tie break for smaller ID (older nodes) (arbitrary) to remain deterministic
                    return o1.id - o2.id;
                }
                else{
                    return o1.conflicts - o2.conflicts; // less conflicts is better
                }
            }
            else {
                return o1.g - o2.g; //lower g is better
            }
        }
    }

}
