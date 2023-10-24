package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.I_AStarGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.SingleTargetCoordinateGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedAGoalAtSomePointInPlanGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * An A* solver that only solves single agent problems. It assumes the first {@link Agent} from {@link MAPF_Instance instances}
 * that it is given is the agent to solve for.
 * By default, it uses {@link I_Coordinate#distance(I_Coordinate)} as a heuristic.
 */
public class SingleAgentAStar_Solver extends A_Solver {

    private final Comparator<AStarState> stateFComparator = new TieBreakingForLessConflictsAndHigherG();
    private static final Comparator<AStarState> equalStatesDiscriminator = new TieBreakingForLowerGAndLessConflicts();

    public boolean agentsStayAtGoal;

    protected ConstraintSet constraints;
    protected AStarGAndH gAndH;
    protected final I_OpenList<AStarState> openList = new OpenListTree<>(stateFComparator);
    protected final Set<AStarState> closed = new HashSet<>();
    protected Agent agent;

    protected I_Map map;
    protected SingleAgentPlan existingPlan;
    protected Solution existingSolution;
    private I_ConflictAvoidanceTable conflictAvoidanceTable;
    public I_Coordinate sourceCoor;
    public I_Coordinate targetCoor;
    public I_AStarGoalCondition goalCondition;
    /**
     * Not real-world time. The problem's start time.
     */
    protected int problemStartTime;
    protected int expandedNodes;
    private int generatedNodes;
    /**
     * Maximum allowed f value ({@link SingleAgentPlan} cost). Will stop and return null if proved that it cannot be found.
     */
    protected float fBudget;

    public SingleAgentAStar_Solver() {this(null);}

    public SingleAgentAStar_Solver(Boolean agentsStayAtGoal) {
        super.name = "AStar";
        this.agentsStayAtGoal = Objects.requireNonNullElse(agentsStayAtGoal, true);
    }
    /*  = set up =  */

    protected void init(MAPF_Instance instance, RunParameters runParameters){
        super.init(instance, runParameters);

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
                && ((RunParameters_SAAStar) runParameters).conflictAvoidanceTable != null){
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
            this.goalCondition = new SingleTargetCoordinateGoalCondition(this.targetCoor);
        }

        this.gAndH = Objects.requireNonNullElseGet(runParameters.aStarGAndH, () -> new UnitCostsAndManhattanDistance(this.targetCoor));
        if (! this.gAndH.isConsistent()){
            throw new IllegalArgumentException("Support for inconsistent heuristics is not implemented.");
        }

        if(runParameters instanceof RunParameters_SAAStar parameters){
            this.fBudget = parameters.fBudget;
        }
        else{
            this.fBudget = Float.POSITIVE_INFINITY;
        }

        this.expandedNodes = 0;
        this.generatedNodes = 0;
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

        while ((currentState = openList.poll()) != null){ //dequeu in the while condition
            if(checkTimeout()) {return null;}
            // early stopping if we already exceed fBudget.
            if (currentState.getF() > fBudget) {return null;}
            closed.add(currentState);

            // nicetohave - change to early goal test
            if (isGoalState(currentState)){
                // check to see if a rejecting constraint on the goal's location exists at some point in the future,
                // which would mean we can't finish the plan there and stay forever
                // TODO move caching responsibility inside the data structure.
                //  And then this should be checked preemptively in the expand function, where other constraints are checked.
                //  Also, this doesn't support multiple possible goal locations. But I guess nothing really does.
                int firstRejectionAtLocationTime = agentsStayAtGoal ?
                        constraints.rejectsEventually(currentState.move,
                                goalCondition instanceof VisitedAGoalAtSomePointInPlanGoalCondition) // kinda messy. For PIBT style (Transient MAPF) paths
                        : -1;

                if(firstRejectionAtLocationTime == -1){ // no rejections
                    // update this.existingPlan which is contained in this.existingSolution
                    currentState.backTracePlan(this.existingPlan);
                    return this.existingSolution; // the goal is good, and we can return the plan.
                } else { // we are rejected from the goal location at some point in the future.
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

            openList.add(new AStarState(existingPlan.moveAt(existingPlan.getEndTime()),null, 0, 0, existingPlan.containsTarget()));
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
                    AStarState rootState = new AStarState(possibleMove, null, this.gAndH.cost(possibleMove), 0, isMoveToTarget(possibleMove));
                    openList.add(rootState);
                }
            }

        }
        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    public void expand(@NotNull AStarState state) {
        expandedNodes++;
        // can move to neighboring locations or stay put
        List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(state.move.currLocation.outgoingEdges());
        // no point to do stay moves or search the time dimension after the time of last constraint.
        // this makes A* complete even when there are goal constraints (infinite constraints)
        boolean afterLastConstraint = state.move.timeNow > constraints.getLastConstraintTime();
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
                AStarState child = new AStarState(possibleMove, state,
                        state.g + gAndH.cost(possibleMove),
                        state.conflicts + numConflicts(possibleMove), isMoveToTarget(possibleMove));

                addToOpenList(child);
            }
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
        }
    }

    protected void releaseMemory() {
        super.releaseMemory();
        this.constraints = null;
        this.gAndH = null;
        this.instanceReport = null;
        this.openList.clear();
        this.closed.clear();
        this.agent = null;
        this.map = null;
        this.existingSolution = null;
        this.existingPlan = null;
        this.conflictAvoidanceTable = null;
        this.sourceCoor = null;
        this.targetCoor = null;
        this.goalCondition = null;
    }

    protected int numConflicts(Move move){
        // TODO to support tie breaking by num conflicts, may have to create duplicate nodes after reaching a goal,
        //  one as a last move and one as an intermediate move, because they would have a different number of conflicts.
        return conflictAvoidanceTable == null ? 0 : conflictAvoidanceTable.numConflicts(move, false);
    }

    /*  = inner classes =  */

    public class AStarState implements Comparable<AStarState>{

        /**
         * Needed to enforce total ordering on nodes, which is needed to make node expansions fully deterministic. That
         * is to say, if all tie breaking methods still result in equality, tie break for using serialID.
         */
        private final int serialID = SingleAgentAStar_Solver.this.generatedNodes++; // take and increment
        public final Move move;
        private final AStarState prev;
        protected final int g;
        protected final float h;
        /**
         * counts the number of conflicts generated by this node and all its ancestors.
         */
        protected final int conflicts;
        public final boolean hasVisitedTargetLocationAncestor;

        public AStarState(Move move, AStarState prevState, int g, int conflicts, boolean isMoveToTargetLocation) {
            this.move = move;
            this.prev = prevState;
            this.g = g;
            this.conflicts = conflicts;
            this.hasVisitedTargetLocationAncestor = isMoveToTargetLocation || (prevState != null && prevState.hasVisitedTargetLocationAncestor);

            // must call this last, since it needs some other fields to be initialized already.
            this.h = calcH();
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

        public float getF(){
            return g + h;
        }

        private float calcH() {
            return SingleAgentAStar_Solver.this.gAndH.getH(this);
        }

        protected void keepTheStateWithMinG(AStarState newState, AStarState existingState) {
            // decide which state to keep, seeing as how they are both equal and in open.
            openList.keepOne(existingState, newState, SingleAgentAStar_Solver.equalStatesDiscriminator);
        }

        /**
         * Trace back a plan from this state, return the plan after it was updates with the moves found by going back
         * from this state.
         * @param existingPlan an existing plan which we are continuing.
         * @return the existingPlan after updating it with the plan that this state represents.
         */
        public SingleAgentPlan backTracePlan(SingleAgentPlan existingPlan) {
            List<Move> moves = new ArrayList<>();
            AStarState currentState = this;
            while (currentState != null){
                moves.add(currentState.move);
                currentState = currentState.prev;
            }
            Collections.reverse(moves); //reorder moves because they were reversed

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
            if (hasVisitedTargetLocationAncestor != that.hasVisitedTargetLocationAncestor) return false;
            return move.currLocation.equals(that.move.currLocation);
        }

        @Override
        public int hashCode() {
            int result = move.currLocation.hashCode();
            result = 31 * result + move.timeNow;
            result = 31 * result + (hasVisitedTargetLocationAncestor ? 1 : 0);
            return result;
        }

        @Override
        public int compareTo(@NotNull AStarState o) {
            return stateFComparator.compare(this, o);
        }

        @Override
        public String toString() {
            return "AStarState{" +
                    "serialID=" + serialID +
                    ", g=" + g +
                    ", h=" + h +
                    ", conflicts=" + conflicts +
                    ", plan=\n" + this.backTracePlan(new SingleAgentPlan(this.move.agent)).toString() +
                    '}';
        }

    } ////////// end AStarState


    /**
     * For sorting the open list.
     */
    private static class TieBreakingForLessConflictsAndHigherG implements Comparator<AStarState>{

        private static final Comparator<AStarState> fComparator = Comparator.comparing(AStarState::getF);

        @Override
        public int compare(AStarState o1, AStarState o2) {
            if(Math.abs(o1.getF() - o2.getF()) < 0.1){ // floats are equal
                // if f() value is equal, we consider the state with less conflicts to be better.
                if(o1.conflicts == o2.conflicts){
                    // if equal in conflicts, we break ties for higher g. Therefore, we want to return a negative
                    // integer if o1.g is bigger than o2.g
                    if (o2.g == o1.g){
                        // If still equal, we tie break for smaller ID (older nodes) (arbitrary) to force a total ordering and remain deterministic
                        return o1.serialID - o2.serialID;

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
                return fComparator.compare(o1, o2);
            }
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
                    return o1.serialID - o2.serialID;
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
