package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictAvoidanceTable;
import Environment.Metrics.InstanceReport;
import BasicCBS.Solvers.*;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;

import java.util.*;

/**
 * An A* solver that only solves single agent problems. It assumes the first {@link Agent} from {@link MAPF_Instance instances}
 * that it is given is the agent to solve for.
 * By default, it uses {@link I_Coordinate#distance(I_Coordinate)} as a heuristic.
 */
public class SingleAgentAStar_Solver extends A_Solver {

    /**
     * Since A* should solve even very large single agent problems very quickly, set default timeout to 3 seconds.
     * A timeout would therefore very likely mean the problem is unsolvable.
     * Without a timeout, unsolvable problems would not resolve, since new states with higher time ({@link Move#timeNow},
     * would continue to be generated ad infinitum. This would eventually result in a heap overflow.
     */
    protected static final long DEFAULT_TIMEOUT = 3 * 1000;
    protected static final int DEFAULT_PROBLEM_START_TIME = 0;
    private Comparator<AStarState> stateFComparator = new TieBreakingForLessConflictsAndHigherG();
    private static final Comparator<AStarState> equalStatesDiscriminator = new TieBreakingForLowerGAndLessConflicts();

    public boolean agentsStayAtGoal = true;

    private ConstraintSet constraints;
    private AStarHeuristic heuristicFunction;
    private final I_OpenList<AStarState> openList = new OpenList<>(stateFComparator);
    private final Set<AStarState> closed = new HashSet<>();
    private Agent agent;
    private I_Map map;
    private SingleAgentPlan existingPlan;
    private Solution existingSolution;
    private I_ConflictAvoidanceTable conflictAvoidanceTable;
    public I_Coordinate sourceCoor;
    public I_Coordinate targetCoor;
    /**
     * Not real-world time. The problem's start time.
     */
    private int problemStartTime;
    private int expandedNodes;
    private int generatedNodes;

    public SingleAgentAStar_Solver() {
        super.DEFAULT_TIMEOUT = SingleAgentAStar_Solver.DEFAULT_TIMEOUT;
    }

    public SingleAgentAStar_Solver(boolean agentsStayAtGoal) {
        this.agentsStayAtGoal = agentsStayAtGoal;
    }
    /*  = set up =  */

    protected void init(MAPF_Instance instance, RunParameters runParameters){
        super.init(instance, runParameters);

        this.constraints = runParameters.constraints == null ? new ConstraintSet(): runParameters.constraints;
        this.agent = instance.agents.get(0);
        this.map = instance.map;

        if(runParameters.existingSolution != null){
            this.existingSolution = runParameters.existingSolution;
            if(runParameters.existingSolution.getPlanFor(this.agent) != null){
                this.existingPlan = runParameters.existingSolution.getPlanFor(this.agent);
                this.problemStartTime = this.existingPlan.getEndTime();
            }
            else {
                this.existingPlan = new SingleAgentPlan(this.agent);
                this.existingSolution.putPlan(this.existingPlan);
                this.problemStartTime = DEFAULT_PROBLEM_START_TIME;
            }
        }
        else{
            // make a new, empty solution, with a new, empty, plan
            this.existingSolution = new Solution();
            this.existingPlan = new SingleAgentPlan(this.agent);
            this.existingSolution.putPlan(this.existingPlan);
        }

        if(runParameters instanceof  RunParameters_SAAStar
                && ((RunParameters_SAAStar) runParameters).heuristicFunction != null){
            RunParameters_SAAStar parameters = ((RunParameters_SAAStar) runParameters);
            this.heuristicFunction = parameters.heuristicFunction;
        }
        else{
            this.heuristicFunction = new defaultHeuristic();
        }
        if(runParameters instanceof  RunParameters_SAAStar
                && ((RunParameters_SAAStar) runParameters).problemStartTime >= 0){
            RunParameters_SAAStar parameters = ((RunParameters_SAAStar) runParameters);
            this.problemStartTime = parameters.problemStartTime;
        }
        if(runParameters instanceof  RunParameters_SAAStar
                && ((RunParameters_SAAStar) runParameters).conflictAvoidanceTable != null){
            RunParameters_SAAStar parameters = ((RunParameters_SAAStar) runParameters);
            this.conflictAvoidanceTable = parameters.conflictAvoidanceTable;
        }
        // else keep the value that it has already been initialised with (above)
        if(runParameters instanceof  RunParameters_SAAStar){
            RunParameters_SAAStar parameters = ((RunParameters_SAAStar) runParameters);
            this.sourceCoor = parameters.sourceCoor != null ? parameters.sourceCoor : agent.source;
            this.targetCoor = parameters.targetCoor != null ? parameters.targetCoor : agent.target;
        }
        else{
            this.sourceCoor = agent.source;
            this.targetCoor = agent.target;
        }

//        this.openList = new OpenList<>(stateFComparator);
//        this.closed = new HashSet<>();
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
        int firstRejectionAtGoalTime = -1;

        while ((currentState = openList.poll()) != null){ //dequeu in the if
            if(checkTimeout()) {return null;}
            closed.add(currentState);

            // nicetohave -  change to early goal test
            if (isGoalState(currentState)){
                // check to see if a rejecting constraint on the goal exists at some point in the future.

                // smaller means we have passed the current rejection (if one existed) and should check if another exists.
                // shouldn't be equal because such a state would not be generated
                if(agentsStayAtGoal && firstRejectionAtGoalTime < currentState.move.timeNow) {
                    // do the expensive update/check
                    firstRejectionAtGoalTime = constraints.rejectsEventually(currentState.move);
                }

                if(firstRejectionAtGoalTime == -1){ // no rejections
                    // update this.existingPlan which is contained in this.existingSolution
                    currentState.backTracePlan(this.existingPlan);
                    return this.existingSolution; // the goal is good and we can return the plan.
                }
                else{ // we are rejected from the goal at some point in the future.
                    currentState.expand();
                }
            }
            else{ //expand
                currentState.expand(); //doesn't generate closed or duplicate states
            }
        }
        return null; //no goal state found (problem unsolvable)
    }

    /**
     * Initialises {@link #openList OPEN}.
     *
     * OPEN is not initialised with a single root state as is common. This is because states in this solver represent
     * {@link Move moves} (classically - operators) rather than {@link I_Location map cells} (classically - states).
     * Instead, OPEN is initialised with all possible moves from the starting position.
     * @return true if OPEN was successfully initialised, else false.
     */
    protected boolean initOpen() {
        // if the existing plan isn't empty, we start from the last move of the existing plan.
        if(existingPlan.size() > 0){
            Move lastExistingMove = existingPlan.moveAt(existingPlan.getEndTime());
            // We assume that we cannot change the existing plan, so if it is rejected by constraints, we can't initialise OPEN.
            if(constraints.rejects(lastExistingMove)) {return false;}

            openList.add(new AStarState(existingPlan.moveAt(existingPlan.getEndTime()),null,
                    /*g=number of moves*/existingPlan.size(), 0 ));
        }
        else { // the existing plan is empty (no existing plan)

            I_Location sourceCell = map.getMapCell(this.sourceCoor);
            // can move to neighboring cells or stay put
            List<I_Location> neighborCellsIncludingCurrent = new ArrayList<>(sourceCell.getNeighbors());
            neighborCellsIncludingCurrent.add(sourceCell);

            for (I_Location destination: neighborCellsIncludingCurrent) {
                Move possibleMove = new Move(agent, problemStartTime + 1, sourceCell, destination);
                if (constraints.accepts(possibleMove)) { //move not prohibited by existing constraint
                    AStarState rootState = new AStarState(possibleMove, null, 1, 0);
                    openList.add(rootState);
                }
            }

        }

        // if none of the root nodes was valid, OPEN will be empty, and thus uninitialised.
        return !openList.isEmpty();
    }

    private boolean isGoalState(AStarState state) {
        return state.move.currLocation.getCoordinate().equals(this.targetCoor);
    }

    /*  = wind down =  */

    protected void writeMetricsToReport(Solution solution) {
        // skips super's writeMetricsToReport(Solution solution).
        super.endTime = System.currentTimeMillis();
        if(instanceReport != null){
            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel, this.expandedNodes);
            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel, this.generatedNodes);
            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.elapsedTimeMS, (int)(super.endTime-super.startTime));
        }
    }

    protected void releaseMemory() {
        super.releaseMemory();
        this.constraints = null;
        this.instanceReport = null;
        // todo clear instead
        this.openList.clear();
        this.closed.clear();
        this.agent = null;
        this.map = null;
        this.existingSolution = null;
        this.existingPlan = null;
    }

    @Override
    public String name() {
        return "AStar";
    }

    private int numConflicts(Move move){
        return conflictAvoidanceTable == null ? 0 : conflictAvoidanceTable.numConflicts(move);
    }

    /*  = inner classes =  */

    public class AStarState implements Comparable<AStarState>{

        /**
         * Needed to enforce total ordering on nodes, which is needed to make node expansions fully deterministic. That
         * is to say, if all tie breaking methods still result in equality, tie break for using serialID.
         */
        private final int serialID = SingleAgentAStar_Solver.this.generatedNodes++; // take and increment
        private final Move move;
        private final AStarState prev;
        private final int g;
        private final float h;
        /**
         * counts the number of conflicts generated by this node and all its ancestors.
         */
        private final int conflicts;

        public AStarState(Move move, AStarState prevState, int g, int conflicts) {
            this.move = move;
            this.prev = prevState;
            this.g = g;

            this.conflicts = conflicts;

            // must call this last, since it needs the other fields to be initialized already.
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

        /*  = other methods =  */

        public float getF(){
            return g + h;
        }

        private float calcH() {
            return SingleAgentAStar_Solver.this.heuristicFunction.getH(this);
        }

        public void expand() {
            expandedNodes++;
            // can move to neighboring cells or stay put
            List<I_Location> neighborCellsIncludingCurrent = new ArrayList<>(this.move.currLocation.getNeighbors());
            neighborCellsIncludingCurrent.add(this.move.currLocation);

            for (I_Location destination: neighborCellsIncludingCurrent){
                Move possibleMove = new Move(this.move.agent, this.move.timeNow+1, this.move.currLocation, destination);
                if(constraints.accepts(possibleMove)){ //move not prohibited by existing constraint
                    AStarState child = new AStarState(possibleMove, this, this.g + 1, conflicts + numConflicts(possibleMove));

                    AStarState existingState;
                    if(closed.contains(child)){ // state visited already
                        // for non consistent heuristics - if the new one has a lower f, remove the old one from closed
                        // and add the new one to open
                    }
                    else if(null != (existingState = openList.get(child)) ){ //an equal state is waiting in open
                        //keep the one with min G
                        keepTheStateWithMinG(child, existingState); //O(LOGn)
                    }
                    else{ // it's a new state
                        openList.add(child);
                    }
                }
            }
        }

        private void keepTheStateWithMinG(AStarState newState, AStarState existingState) {
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
            List<Move> moves = new LinkedList<>();
            AStarState currentState = this;
            while (currentState != null){
                moves.add(currentState.move);
                currentState = currentState.prev;
            }
            Collections.reverse(moves); //reorder moves because they were reversed

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
            if (!(o instanceof AStarState)) return false;

            AStarState that = (AStarState) o;

            if (move.timeNow != that.move.timeNow) return false;
            return move.currLocation.equals(that.move.currLocation);
        }

        @Override
        public int hashCode() {
            int result = move.currLocation.hashCode();
            result = 31 * result + move.timeNow;
            return result;
        }

        @Override
        public int compareTo(AStarState o) {
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

    private class defaultHeuristic implements AStarHeuristic{

        @Override
        public float getH(AStarState state) {
            return state.move.currLocation.getCoordinate().distance(SingleAgentAStar_Solver.this.targetCoor);
        }
    }


    /**
     * For sorting the open list.
     */
    private static class TieBreakingForLessConflictsAndHigherG implements Comparator<AStarState>{

        private static Comparator<AStarState> fComparator = Comparator.comparing(AStarState::getF);

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
            // if the heuristic is monotone increasing, we should never actually find a new state with lower G than
            // existing equal state in open.
            // we break ties for lower g. Therefore, we want to return a positive integer if o1.g is bigger than o2.g.
            // g should be equal in practice since G=state-time. and state equality is also defined by state-time.
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
