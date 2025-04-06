package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.DataTypesAndStructures.TimeInterval;
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
    private Map<I_Location, ArrayList<Move>> regularOccupanciesSorted; // todo merge with regular occupancies?
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
                if (timeIndex >= 0){ // the time is in the list
                    // find the largest index of moves with the same time
                    while (timeIndex < sortedMovesAtLocation.size() - 1 && sortedMovesAtLocation.get(timeIndex + 1).timeNow == move.timeNow){
                        timeIndex++;
                    }
                    numConflicts += sortedMovesAtLocation.size() - timeIndex - 1;

                }
                else { // timeIndex < 0
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

        // look for conflicts where this is a goal move, and the other agent is not a goal move
        if (isALastMove){
            ArrayList<Move> sortedMovesAtLocation = regularOccupanciesSorted.get(move.currLocation);
            if (sortedMovesAtLocation != null && !sortedMovesAtLocation.isEmpty()){
                // use binary search to find occupancies that exist after the move
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
                        // there may be multiple moves with the same time, so keep scanning until we increase time by at least 1
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

    public Map<I_Location, List<AgentAtGoal>> getGoalOccupancies() {
        return Collections.unmodifiableMap(goalOccupancies);
    }

    public Map<I_Location, ArrayList<Move>> getRegularOccupanciesSorted() {
        return Collections.unmodifiableMap(regularOccupanciesSorted);
    }

    /**
     * Converts a conflict avoidance table into a map of safe time intervals for each location,
     * considering soft constraints such as vertex and target obstacles.
     *
     * @return A map where each location is associated with a list of safe time intervals.
     * @throws IllegalArgumentException If the provided conflict avoidance table is not of the expected type.
     */
    public Map<I_Location, List<TimeInterval>> conflictAvoidanceTableToSafeTimeIntervals() {
        // Output map for refined intervals considering soft constraints
        Map<I_Location, List<TimeInterval>> refinedIntervalMap = new HashMap<>();

        // Retrieve soft constraint data from the conflict avoidance table
        Map<I_Location, List<AgentAtGoal>> goalOccupancies;
        Map<I_Location, ArrayList<Move>> regularOccupanciesSorted;

        goalOccupancies = this.getGoalOccupancies();
        regularOccupanciesSorted = this.getRegularOccupanciesSorted();

        Set<I_Location> allLocations = new HashSet<>(goalOccupancies.keySet());
        allLocations.addAll(regularOccupanciesSorted.keySet());

        // Process each location for soft constraints
        for (I_Location location : allLocations) {
            int goalTime = -1;
            // Initialize list of safe intervals for the location

            // Retrieve the soft constraints (vertex and target obstacles) for this location
            List<Move> softVertexMoves = regularOccupanciesSorted.get(location);  // soft vertex obstacles
            List<AgentAtGoal> softTargetMoves = goalOccupancies.get(location);  // soft target obstacles

            Set<Integer> softConstraintTimeSteps = new HashSet<>();
            if (softVertexMoves != null) {
                for (Move move : softVertexMoves) {
                    softConstraintTimeSteps.add(move.timeNow);
                }
            }
            if (softTargetMoves != null) {
                for (AgentAtGoal agentAtGoal : softTargetMoves) {
                    softConstraintTimeSteps.add(agentAtGoal.time-1);
                    goalTime = agentAtGoal.time-1;
                }
            }

            // Convert the collected time steps into safe intervals
            List<TimeInterval> safeIntervals = timeStepsToSafeIntervals(softConstraintTimeSteps, goalTime);

            // Add the refined intervals to the output map
            refinedIntervalMap.put(location, safeIntervals);
        }
        return refinedIntervalMap;
    }

    /**
     * Converts a list of conflict timeSteps into a list of safe time intervals,
     * ensuring that movements avoid conflicts and properly handle goal times.
     *
     * @param timeSteps A list of time steps where conflicts occur.
     * @param goalTime The specific goal time that marks the end of valid movement periods.
     * @return A list of {@code TimeInterval} objects representing safe movement intervals.
     */
    private List<TimeInterval> timeStepsToSafeIntervals(Set<Integer> timeSteps, int goalTime) {
        if (timeSteps.isEmpty()) {
            return List.of(new TimeInterval(0, Integer.MAX_VALUE)); // No conflicts, entire timeline is safe
        }

        List<Integer> sortedTimestamps = new ArrayList<>(timeSteps);
        Collections.sort(sortedTimestamps);

        List<TimeInterval> safeIntervals = new ArrayList<>();
        int lastSafeStart = 0;

        for (int i = 0; i < sortedTimestamps.size(); ) {
            int startConflict = sortedTimestamps.get(i);
            int endConflict = startConflict;

            // Merge consecutive conflict times
            while (i + 1 < sortedTimestamps.size() && sortedTimestamps.get(i + 1) == endConflict + 1) {
                endConflict = sortedTimestamps.get(++i);
            }

            // Add safe interval before the conflict block
            if (lastSafeStart < startConflict) {
                safeIntervals.add(new TimeInterval(lastSafeStart, startConflict - 1));
            }

            // If conflict block includes the goal time, stop early
            if (startConflict <= goalTime && goalTime <= endConflict) {
                safeIntervals.add(new TimeInterval(startConflict, Integer.MAX_VALUE));
                return safeIntervals;
            }

            // Add the merged conflict interval
            safeIntervals.add(new TimeInterval(startConflict, endConflict));

            lastSafeStart = endConflict + 1;
            i++;
        }

        // Add final safe interval
        safeIntervals.add(new TimeInterval(lastSafeStart, Integer.MAX_VALUE));
        return safeIntervals;
    }

    private static class MoveTimeComparator implements Comparator<Move> {
        @Override
        public int compare(Move o1, Move o2) {
            return Integer.compare(o1.timeNow, o2.timeNow);
        }
    }
}
