package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class A_ConflictAvoidanceTable implements I_ConflictAvoidanceTable {
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

    /**
     * Maps time locations to agents that occupy them.
     */
    protected final Map<TimeLocation, List<Move>> regularOccupancies = new HashMap<>();

    /**
     * If set to true, will check for conflicts at goal locations (after time of reaching them)
     */
    public boolean checkGoals = true;
    /**
     * If set to true, will not count agents being together at their (shared) goal as a conflict.
     */
    public boolean sharedGoals = false;
    /**
     * If true, agents staying at their source (since the start) will not conflict
     */
    public boolean sharedSources = false;
    public boolean removeOccupancyListsWhenEmptied;

    public int lastOccupancyStartTime;

    /**
     * Constructor
     *
     * @param plans                           plans to put into maps, so we can find conflicts with them. Will not be modified.
     * @param excludedAgent                     an agent whose plan is to be ignored (optional).
     */
    public A_ConflictAvoidanceTable(@Nullable Iterable<? extends SingleAgentPlan> plans, @Nullable Agent excludedAgent) {
        initDataStructures();
        if(plans != null){
            for (SingleAgentPlan plan: plans){
                if(excludedAgent == null || ! excludedAgent.equals(plan.agent)){
                    addPlan(plan);
                }
            }
        }
    }

    public A_ConflictAvoidanceTable() {
        this(null, null);
    }

    public void addAll(@NotNull Iterable<SingleAgentPlan> plans){
        for (SingleAgentPlan plan : plans){
            addPlan(plan);
        }
    }

    public void addPlan(SingleAgentPlan plan){
        if (plan.size() == 0){
            return;
        }

        // necessary for detecting edge conflicts on first move
        addOccupancy(new TimeLocation(0, plan.getFirstMove().prevLocation), plan.getFirstMove());

        for (Move move : plan){
            TimeLocation to = new TimeLocation(move.timeNow, move.currLocation);
            addOccupancy(to, move);
            if(move.timeNow == plan.getEndTime()){
                addGoalOccupancy(move.currLocation, move);
                this.lastOccupancyStartTime = Math.max(this.lastOccupancyStartTime, move.timeNow);
            }
        }

    }

    protected void addOccupancy(TimeLocation timeLocation, Move move){
        List<Move> occupanciesAtTimeLocation = regularOccupancies.computeIfAbsent(timeLocation, tl -> new ArrayList<>());
        occupanciesAtTimeLocation.add(move);
    }
    protected abstract void initDataStructures();

    protected abstract void addGoalOccupancy(I_Location location, Move finalMove);


    /**
     * {@inheritDoc}
     */
    public int numConflicts(Move move, boolean isALastMove) {
        TimeLocation from = reusableTimeLocation1.setTo(move.timeNow - 1, move.prevLocation);

        TimeLocation to = reusableTimeLocation2.setTo(move.timeNow, move.currLocation);

        int numVertexConflicts = getNumVertexConflictsExcludingGoalConflicts(move, to);

        if(checkGoals){
            numVertexConflicts += getNumGoalConflicts(move, to, isALastMove);
        }

        // time locations of a move that would create a swapping conflict
        TimeLocation reverseFrom = to;
        reverseFrom.time -= 1;
        TimeLocation reverseTo = from;
        reverseTo.time += 1;

        int numSwappingConflicts = getNumSwappingConflicts(reverseFrom, reverseTo);

        return numVertexConflicts + numSwappingConflicts;
    }

    private int getNumSwappingConflicts(TimeLocation reverseFrom, TimeLocation reverseTo) {
        int numSwappingConflicts = 0;
        for(Move fromMove : regularOccupancies.getOrDefault(reverseTo, Collections.emptyList())) {
            // so there are moves at the time interest from reverseFrom. check if they are to reverseTo
            if (fromMove.prevLocation.equals(reverseFrom.location)){
                numSwappingConflicts++;
            }
        }
        return numSwappingConflicts;
    }

    protected Move getAMoveWithSwappingConflict(TimeLocation reverseFrom, TimeLocation reverseTo) {
        for(Move fromMove : regularOccupancies.getOrDefault(reverseTo, Collections.emptyList())) {
            // so there are moves at the time interest from reverseFrom. check if they are to reverseTo
            if (fromMove.prevLocation.equals(reverseFrom.location)){
                return fromMove;
            }
        }
        return null;
    }

    private int getNumVertexConflictsExcludingGoalConflicts(Move move, TimeLocation to) {
        int numVertexConflicts = 0;
        List<Move> occupanciesAtTimeLocation = regularOccupancies.getOrDefault(to, Collections.emptyList());
        if(!occupanciesAtTimeLocation.isEmpty()){
            if (sharedSources && move.isStayAtSource){
                // count conflicts excluding stay at source
                for (Move otherMove : occupanciesAtTimeLocation){
                    numVertexConflicts += otherMove.isStayAtSource ? 0 : 1;
                }
            }
            else {
                numVertexConflicts += occupanciesAtTimeLocation.size();
            }
        }
        return numVertexConflicts;
    }

    protected Move getAMoveWithVertexConflictExcludingGoalConflicts(Move move, TimeLocation to) {
        List<Move> occupanciesAtTimeLocation = regularOccupancies.getOrDefault(to, Collections.emptyList());
        if(!occupanciesAtTimeLocation.isEmpty()){
            if (sharedSources && move.isStayAtSource){
                // count conflicts excluding stay at source
                for (Move otherMove : occupanciesAtTimeLocation){
                    if (!otherMove.isStayAtSource){
                        return otherMove;
                    }
                }
            }
            else {
                return occupanciesAtTimeLocation.get(0);
            }
        }
        return null;
    }

    abstract int getNumGoalConflicts(Move move, TimeLocation to, boolean isALastMove);

    public int getLastOccupancyTime() {
        return this.lastOccupancyStartTime;
    }

    public int getNumberOfEdgeConflicts(Move move) {
        TimeLocation from = reusableTimeLocation1.setTo(move.timeNow - 1, move.prevLocation);

        TimeLocation to = reusableTimeLocation2.setTo(move.timeNow, move.currLocation);

        // time locations of a move that would create a swapping conflict
        TimeLocation reverseFrom = to;
        reverseFrom.time -= 1;
        TimeLocation reverseTo = from;
        reverseTo.time += 1;

        return getNumSwappingConflicts(reverseFrom, reverseTo);
    }
}
