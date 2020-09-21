package BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.*;

/**
 * A conflict avoidance table that is meant to be used in a single CT node. Meant to be filled once with plans, queried
 * many times about conflicts with those plans, and then be discarded.
 */
public class SingleUseConflictAvoidanceTable implements I_ConflictAvoidanceTable {

    /**
     * An instance of {@link TimeLocation} to be reused again and again when querying data structures, instead of
     * creating thousands of single use instances.
     */
    private TimeLocation reusableTimeLocation1 = new TimeLocation(0, null);
    /**
     * An instance of {@link TimeLocation} to be reused again and again when querying data structures, instead of
     * creating thousands of single use instances.
     */
    private TimeLocation reusableTimeLocation2 = new TimeLocation(0, null);

    /**
     * Maps time locations to agents that occupy them.
     */
    private Map<TimeLocation, Set<Agent>> allOccupancies = new HashMap<>();

    /**
     * If set to true, will check for conflicts at goal locations (after time of reaching them)
     */
    public boolean checkGoals = true;
    /**
     * Contains all goal locations and maps them to the time from which they are occupied (indefinitely).
     * Can't have more than one agent occupying a goal, since that would make the problem unsolvable (in classic MAPF).
     */
    private Map<I_Location, Integer> goalOccupancies = new HashMap<>();

    /**
     * Constructor
     * @param plans plans to put into maps so we can find conflicts with them. Will not be modified.
     * @param targetAgent an agent who's plan is to be ignored (optional).
     */
    public SingleUseConflictAvoidanceTable(Iterable<? extends SingleAgentPlan> plans, Agent targetAgent) {
        for (SingleAgentPlan plan: plans){
            if(targetAgent == null || ! targetAgent.equals(plan.agent)){
                addPlan(plan);
            }
        }
    }

    /**
     * Constructor
     * @param plans plans to put into maps so we can find conflicts with them. Will not be modified.
     * @param targetAgent an agent who's plan is to be ignored (optional).
     * @param checkGoals whether or not to check goals
     */
    public SingleUseConflictAvoidanceTable(Iterable<? extends SingleAgentPlan> plans, Agent targetAgent, boolean checkGoals) {
        for (SingleAgentPlan plan: plans){
            if(targetAgent == null || ! targetAgent.equals(plan.agent)){
                addPlan(plan);
            }
        }
        this.checkGoals = checkGoals;
    }

    private void addPlan(SingleAgentPlan plan){
        for (Move move : plan){
            TimeLocation from = new TimeLocation(move.timeNow - 1, move.prevLocation);
            TimeLocation to = new TimeLocation(move.timeNow, move.currLocation);
            addOccupancy(from, plan.agent);
            addOccupancy(to, plan.agent);
            if(move.timeNow == plan.getEndTime()){
                addGoalOccupancy(move.currLocation, move.timeNow);
            }
        }

    }

    private void addOccupancy(TimeLocation timeLocation, Agent agent){
        if( ! allOccupancies.containsKey(timeLocation)){
            allOccupancies.put(timeLocation, new HashSet<>());
        }
        allOccupancies.get(timeLocation).add(agent);
    }

    private void addGoalOccupancy(I_Location location, int entryTime){
        // add 1 to entry time, so as not to count twice with the entry and in allOccupancies, and also not miss the
        // possible swapping conflict on the last move in the plan (if we were to instead remove the last from allOccupancies)
        if(checkGoals){
            this.goalOccupancies.put(location, entryTime + 1);
        }
    }

    @Override
    public int numConflicts(Move move) {
        int numVertexConflicts = 0;
        int numSwappingConflicts = 0;

        TimeLocation from = reusableTimeLocation1;
        from.location = move.prevLocation;
        from.time = move.timeNow - 1;

        TimeLocation to = reusableTimeLocation2;
        to.location = move.currLocation;
        to.time = move.timeNow;

        if(allOccupancies.containsKey(to)){
            numVertexConflicts += allOccupancies.get(to).size();
        }
        if(checkGoals && goalOccupancies.containsKey(to.location)){
            if(goalOccupancies.get(to.location) <= to.time){
                numVertexConflicts++;
            }
        }

        // time locations of a move that would create a swapping conflict
        TimeLocation reverseFrom = to;
        reverseFrom.time -= 1;
        TimeLocation reverseTo = from;
        reverseTo.time += 1;

        if(allOccupancies.containsKey(reverseFrom) && allOccupancies.containsKey((reverseTo))){
            // check if they were made by the same agent
            Set<Agent> fromAgents = allOccupancies.get(reverseFrom);
            Set<Agent> toAgents = allOccupancies.get(reverseTo);
            for(Agent agent : fromAgents){
                if(toAgents.contains(agent)){
                    numSwappingConflicts++;
                }
            }
        }

        return numVertexConflicts + numSwappingConflicts;
    }
}
