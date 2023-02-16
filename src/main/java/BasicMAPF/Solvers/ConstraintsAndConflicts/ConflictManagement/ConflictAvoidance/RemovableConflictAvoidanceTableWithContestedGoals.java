package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.AgentAtGoal;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
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
     * Contains all goal locations and maps them to the times from which they are occupied (indefinitely) and the agents that occupy them..
     */
    private Map<I_Location, List<AgentAtGoal>> goalOccupancies = new HashMap<>();

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
    protected void checkInitGoalOccupancies() {
        if (this.goalOccupancies == null){
            this.goalOccupancies = new HashMap<>();
        }
    }

    @Override
    protected void addGoalOccupancy(I_Location location, Move finalMove) {
        checkInitGoalOccupancies();
        List<AgentAtGoal> agentsAtGoal = goalOccupancies.computeIfAbsent(location, k -> new ArrayList<>());
        // add 1 to time so as not to overlap with the vertex conflict
        agentsAtGoal.add(new AgentAtGoal(finalMove.agent, finalMove.timeNow + 1));
    }

    @Override
    int getNumGoalConflicts(Move move, TimeLocation to) {
        checkInitGoalOccupancies();
        List<AgentAtGoal> agentsAtGoal = goalOccupancies.get(move.currLocation);
        if (agentsAtGoal == null) {
            return 0;
        }
        int numConflicts = 0;
        for (AgentAtGoal agentAtGoal : agentsAtGoal) { // TODO more efficient with sorted list?
            if (agentAtGoal.time <= to.time) {
                numConflicts++;
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
                removeGoalOccupancy(move.currLocation, move);
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
    }

    private void removeGoalOccupancy(I_Location currLocation, Move move) {
        List<AgentAtGoal> agentsAtGoal = goalOccupancies.get(currLocation);
        if(agentsAtGoal != null){
            agentsAtGoal.remove(reusableAgentAtGoal.setTo(move.agent, move.timeNow));
            if(removeOccupancyListsWhenEmptied && agentsAtGoal.isEmpty()){
                goalOccupancies.remove(currLocation);
            }
        }
    }

    public void replacePlan(SingleAgentPlan plan, SingleAgentPlan newPlan) {
        removePlan(plan);
        addPlan(newPlan);
    }
}
