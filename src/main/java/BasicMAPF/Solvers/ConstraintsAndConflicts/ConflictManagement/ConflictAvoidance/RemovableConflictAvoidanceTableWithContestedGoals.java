package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.AgentAtGoal;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Supports removing plans, and multiple goal occupancies on the same location.
 * A plan for an agent should always be removed before adding a new plan for that agent.
 */
public class RemovableConflictAvoidanceTableWithContestedGoals extends A_ConflictAvoidanceTable {

    /**
     * An instance of {@link TimeLocation} to be reused again and again when querying data structures, instead of
     * creating thousands of single use instances.
     */
    private final TimeLocation reusableTimeLocation1 = new TimeLocation(0, null);
    /**
     * An instance of {@link TimeLocation} to be reused again and again when querying data structures, instead of
     * creating thousands of single use instances.
     */
    private final TimeLocation reusableTimeLocation2 = new TimeLocation(0, null);
    private final AgentAtGoal reusableAgentAtGoal = new AgentAtGoal(null, 0);
    Set<Agent> coveredAgents;
    /**
     * Contains all goal locations and maps them to the times from which they are occupied (indefinitely) and the agents that occupy them.
     */
    private Map<I_Location, List<AgentAtGoal>> goalOccupancies;
    private Map<I_Location, ArrayList<Move>> regularOccupanciesSorted;
    private static final MoveTimeComparator MOVE_TIME_COMPARATOR = new MoveTimeComparator();

    /**
     * {@inheritDoc}
     */
    public RemovableConflictAvoidanceTableWithContestedGoals(@Nullable Iterable<? extends SingleAgentPlan> plans, @Nullable Agent excludedAgent) {
        super(plans, excludedAgent);
    }

    public RemovableConflictAvoidanceTableWithContestedGoals() {
        super();
    }

    public RemovableConflictAvoidanceTableWithContestedGoals(RemovableConflictAvoidanceTableWithContestedGoals other){
        if (other.goalOccupancies != null){
            this.goalOccupancies = new HashMap<>();
            for (Map.Entry<I_Location, List<AgentAtGoal>> entry : other.goalOccupancies.entrySet()){
                this.goalOccupancies.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        if (other.regularOccupanciesSorted != null){
            this.regularOccupanciesSorted = new HashMap<>();
            for (Map.Entry<I_Location, ArrayList<Move>> entry : other.regularOccupanciesSorted.entrySet()){
                this.regularOccupanciesSorted.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }
        if (other.coveredAgents != null){
            this.coveredAgents = new HashSet<>(other.coveredAgents);
        }
    }

    /**
     * Adds a plan to the table. If the agent already has a plan in the table, will throw an exception.
     * @param plan the plan to add
     */
    @Override
    public void addPlan(SingleAgentPlan plan) {
        super.addPlan(plan);
        if (coveredAgents == null){
            coveredAgents = new HashSet<>();
        }
        if (coveredAgents.contains(plan.agent)){
            throw new IllegalStateException("Agent " + plan.agent + " already has a plan in the table");
        }
        coveredAgents.add(plan.agent);
    }

    @Override
    protected void addOccupancy(TimeLocation timeLocation, Move move) {
        super.addOccupancy(timeLocation, move);

        ArrayList<Move> moves = this.regularOccupanciesSorted.computeIfAbsent(timeLocation.location, k -> new ArrayList<>());
        int insertionIndex = Collections.binarySearch(moves, move, MOVE_TIME_COMPARATOR);
        if (insertionIndex < 0){
            insertionIndex = -insertionIndex - 1;
        }
        moves.add(insertionIndex, move);
    }

    @Override
    protected void initDataStructures() {
        if (this.goalOccupancies == null){
            this.goalOccupancies = new HashMap<>();
        }
        if (this.regularOccupanciesSorted == null){
            this.regularOccupanciesSorted = new HashMap<>();
        }
    }

    @Override
    protected void addGoalOccupancy(I_Location location, Move finalMove) {
        List<AgentAtGoal> agentsAtGoal = goalOccupancies.computeIfAbsent(location, k -> new ArrayList<>());
        // add 1 to time so as not to overlap with the vertex conflict
        agentsAtGoal.add(new AgentAtGoal(finalMove.agent, getGoalOccupancyStartTimeFromMove(finalMove)));
    }

    private static int getGoalOccupancyStartTimeFromMove(Move finalMove) {
        return finalMove.timeNow + 1;
    }

    @Override
    int getNumGoalConflicts(Move move, TimeLocation to, boolean isALastMove) {
        int numConflicts = 0;

        if ( ! (sharedGoals && isALastMove)){
            // look for conflicts where the other agent is a goal move
            // TODO throw an exception if sharedGoals is false?
            List<AgentAtGoal> agentsAtGoal = goalOccupancies.get(move.currLocation);
            if (agentsAtGoal != null) {
                for (AgentAtGoal agentAtGoal : agentsAtGoal) { // TODO more efficient with sorted list?
                    if (agentAtGoal.time <= to.time
                            // Only relevant if agents may finish their plans at locations other than their targets (any two last moves to the same location conflict)
                            || isALastMove
                    ) {
                        numConflicts++;
                    }
                }
            }
        }

        // look for conflicts where this a goal move and the other agent is not a goal move
        if (isALastMove){
            ArrayList<Move> sortedMovesAtLocation = regularOccupanciesSorted.get(move.currLocation);
            if (sortedMovesAtLocation != null && !sortedMovesAtLocation.isEmpty()){
                // use binary search to count how many occupancies exist after the move
                int timeIndex = Collections.binarySearch(sortedMovesAtLocation, move, MOVE_TIME_COMPARATOR);
                if (timeIndex >= 0){ // time is in the list
                    numConflicts += sortedMovesAtLocation.size() - timeIndex - 1;
                }
                if (timeIndex < 0){
                    int numSmallerOrEqualElements = -timeIndex - 1;
                    numConflicts += sortedMovesAtLocation.size() - numSmallerOrEqualElements;
                }
            }
        }
        return numConflicts;
    }

    /**
     * Removes a plan from the table. Should be called before adding a new plan for the same agent.
     * @param plan the plan to remove
     */
    public void removePlan(SingleAgentPlan plan){
        for (Move move : plan){
            TimeLocation from = reusableTimeLocation1.setTo(move.timeNow - 1, move.prevLocation);
            TimeLocation to = reusableTimeLocation2.setTo(move.timeNow, move.currLocation);
            removeOccupancy(from, move);
            removeOccupancy(to, move);
            if(move.timeNow == plan.getEndTime()){
                removeGoalOccupancy(move);
            }
        }

        if (coveredAgents != null){
            coveredAgents.remove(plan.agent); // TODO log a warning if the agent was not in the table?
        }
    }

    private void removeOccupancy(TimeLocation timeLocation, Move move) {
        List<Move> occupanciesAtTimeLocation = regularOccupancies.get(timeLocation);
        if(occupanciesAtTimeLocation != null){
            occupanciesAtTimeLocation.remove(move);
            if(removeOccupancyListsWhenEmptied && occupanciesAtTimeLocation.isEmpty()){
                regularOccupancies.remove(timeLocation);
            }
        }
        List<Move> sortedOccupanciesAtTimeLocation = regularOccupanciesSorted.get(timeLocation.location);
        if(sortedOccupanciesAtTimeLocation != null){
            int index = Collections.binarySearch(sortedOccupanciesAtTimeLocation, move, MOVE_TIME_COMPARATOR);
            if (index >= 0){
                sortedOccupanciesAtTimeLocation.remove(index);
            }
            if(removeOccupancyListsWhenEmptied && sortedOccupanciesAtTimeLocation.isEmpty()){
                regularOccupanciesSorted.remove(timeLocation.location);
            }
        }
    }

    private void removeGoalOccupancy(Move move) {
        List<AgentAtGoal> agentsAtGoal = goalOccupancies.get(move.currLocation);
        if(agentsAtGoal != null){
            agentsAtGoal.remove(reusableAgentAtGoal.setTo(move.agent, getGoalOccupancyStartTimeFromMove(move)));
            if(removeOccupancyListsWhenEmptied && agentsAtGoal.isEmpty()){
                goalOccupancies.remove(move.currLocation);
            }
        }
    }

    public void replacePlan(SingleAgentPlan plan, SingleAgentPlan newPlan) {
        removePlan(plan);
        addPlan(newPlan);
    }

    public int firstConflictTime(Move move, boolean isALastMove) {
        TimeLocation from = reusableTimeLocation1.setTo(move.timeNow - 1, move.prevLocation);

        TimeLocation to = reusableTimeLocation2.setTo(move.timeNow, move.currLocation);

        Move conflictingMove = getAMoveWithVertexConflictExcludingGoalConflicts(move, to);
        if (conflictingMove != null){
            return conflictingMove.timeNow;
        }

        int firstGoalConflictTime = -1;

        if(checkGoals){ // TODO move this to the end to optimize?
            firstGoalConflictTime = getFirstGoalConflict(move, to, isALastMove);
        }

        // time locations of a move that would create a swapping conflict
        TimeLocation reverseFrom = to;
        reverseFrom.time -= 1;
        TimeLocation reverseTo = from;
        reverseTo.time += 1;

        conflictingMove = getAMoveWithSwappingConflict(reverseFrom, reverseTo);
        if (conflictingMove != null){
            return conflictingMove.timeNow;
        }

        return firstGoalConflictTime; // only the goal conflicts can have a time greater than the move's time
    }

    private Move getAMoveWithSwappingConflict(TimeLocation reverseFrom, TimeLocation reverseTo) {
        if(regularOccupancies.containsKey(reverseFrom) && regularOccupancies.containsKey(reverseTo)){
            // so there are occupancies at the times + locations of interest, now check if they are from a move from
            // reverseFrom to reverseTo
            for(Move fromMove : regularOccupancies.get(reverseFrom)){
                if (fromMove.currLocation.equals(reverseTo.location)){
                    return fromMove;
                }
            }
        }
        return null;
    }

    private Move getAMoveWithVertexConflictExcludingGoalConflicts(Move move, TimeLocation to) {
        if(regularOccupancies.containsKey(to)){
            if (sharedSources && move.isStayAtSource){
                // conflicts excluding stay at source
                for (Move otherMove : regularOccupancies.get(to)){
                    if (!otherMove.isStayAtSource){
                        return otherMove;
                    }
                }
            }
            else {
                for (Move otherMove : regularOccupancies.get(to)) {
                    return otherMove;
                }
            }
        }
        return null;
    }

    private int getFirstGoalConflict(Move move, TimeLocation to, boolean isALastMove) {
        int earliestGoalConflict = -1;

        if ( ! (sharedGoals && isALastMove)){
            // look for conflicts where the other agent is a goal move
            List<AgentAtGoal> agentsAtGoal = goalOccupancies.get(move.currLocation);
            if (agentsAtGoal != null) {
                for (AgentAtGoal agentAtGoal : agentsAtGoal) { // TODO more efficient with sorted list?
                    if (agentAtGoal.time <= to.time) {
                        if (earliestGoalConflict == -1 || to.time < earliestGoalConflict) {
                            earliestGoalConflict = to.time;
                        }
                    } else if (isALastMove) {
                        // Only relevant if agents may finish their plans at locations other than their targets (any two last moves to the same location conflict)
                        if (earliestGoalConflict == -1 || agentAtGoal.time < earliestGoalConflict) {
                            earliestGoalConflict = agentAtGoal.time;
                        }
                    }
                }
            }
        }

        // look for conflicts where this a goal move and the other agent is not a goal move
        if (isALastMove){
            ArrayList<Move> sortedMovesAtLocation = regularOccupanciesSorted.get(move.currLocation);
            if (sortedMovesAtLocation != null && !sortedMovesAtLocation.isEmpty()){
                // use binary search to find occupancies exist after the move
                int timeIndex = Collections.binarySearch(sortedMovesAtLocation, move, MOVE_TIME_COMPARATOR);
                if (timeIndex < 0){
                    timeIndex = -timeIndex - 1;
                }
                for (int i = timeIndex; i < sortedMovesAtLocation.size(); i++){
                    Move otherMove = sortedMovesAtLocation.get(i);
                    if (earliestGoalConflict > -1 && otherMove.timeNow >= earliestGoalConflict){
                        break;
                    }
                    if (otherMove.timeNow == move.timeNow){
                        continue;
                    }
                    else if (earliestGoalConflict == -1 || otherMove.timeNow < earliestGoalConflict){
                        earliestGoalConflict = otherMove.timeNow;
                    }
                }
            }
        }
        return earliestGoalConflict;
    }

    private static class MoveTimeComparator implements Comparator<Move> {
        @Override
        public int compare(Move o1, Move o2) {
            return Integer.compare(o1.timeNow, o2.timeNow);
        }
    }
}
