package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.*;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;

import java.util.*;

public class RemovableConflictManager implements I_ConflictManager{



    /*  = Data structures =   */
    public final RemovableConflictAvoidance removableConflictAvoidance;
    public final TimeLocationTables timeLocationTables;
    private final Map<Agent, SingleAgentPlan> agent_plan; // maps from Agent to Agent's plan

    private final ConflictSelectionStrategy conflictSelectionStrategy; // Strategy for selecting conflicts



    /**
     * Constructor.
     * @param conflictSelectionStrategy how to choose conflicts.
     */
    public RemovableConflictManager(ConflictSelectionStrategy conflictSelectionStrategy) {
        /* Might want to changed allConflicts from a HashSet to a TreeSet to make MinTimeConflictSelectionStrategy more efficient.
         If we want to make this more generic, we should scrap ConflictSelectionStrategy and instead make this field
         an instance of some new class, thus combining storage and selection of conflicts. @Jonathan Morag 28/10/2019
         */
        this.removableConflictAvoidance = new RemovableConflictAvoidance();
        this.timeLocationTables = new TimeLocationTables();
        this.agent_plan = new HashMap<>();

        this.conflictSelectionStrategy = conflictSelectionStrategy;
    }

    /* Default constructor */
    public RemovableConflictManager() {
        this(new MinTimeConflictSelectionStrategy());
    }

    /**
     * Copy constructor.
     * @param other another {@link ConflictManager} to copy.
     */
    public RemovableConflictManager(RemovableConflictManager other){

        this.removableConflictAvoidance = other.removableConflictAvoidance.copy();
        this.timeLocationTables = other.timeLocationTables.copy();
        this.agent_plan = new HashMap<>();
        for ( Map.Entry<Agent,SingleAgentPlan> agentPlanFromOther: other.agent_plan.entrySet()){
            this.agent_plan.put(agentPlanFromOther.getKey(),agentPlanFromOther.getValue());
        }
        this.conflictSelectionStrategy = other.conflictSelectionStrategy;
    }


    public RemovableConflictManager(ConflictManager conflictManager){

        this.removableConflictAvoidance = new RemovableConflictAvoidance(conflictManager.getAllConflicts());


        this.timeLocationTables = conflictManager.timeLocationTables.copy();
        this.agent_plan = new HashMap<>();
        for ( Map.Entry<Agent,SingleAgentPlan> agentPlanFromOther: conflictManager.agentPlans.entrySet()){
            this.agent_plan.put(agentPlanFromOther.getKey(),agentPlanFromOther.getValue());
        }
        this.conflictSelectionStrategy = conflictManager.conflictSelectionStrategy;
    }

    @Override
    public I_ConflictManager copy() {
        return new RemovableConflictManager(this);
    }

    /***
     * This method adds a new plan for SingleAgentPlan.
     * Note that if agent's plan already exists, it removes before adding.
     * = Removes =
     * 1. All of previous plan {@link TimeLocation} from 'this.timeLocation_agent'
     * 2. The {@link AgentAtGoal} of the plan.
     * 3. All {@link A_Conflict} for every other {@link Agent} that it conflicts with from 'this.agent_conflicts'
     * 4. The {@link Agent} of the new plan from 'this.agent_conflicts' , 'this.location_timeList'
     * = Adds =
     * 1. The {@link SingleAgentPlan} to 'this.agent_plan'
     * 2. All of the new plan {@link TimeLocation} to 'this.timeLocation_agent' , 'this.location_timeList'
     * 3. All {@link A_Conflict} for every other {@link Agent} that it conflicts with to 'this.agent_conflicts'
     * 4. All Conflicts regarding the goal of {@link SingleAgentPlan}
     *
     * @param singleAgentPlan a new {@link SingleAgentPlan}.
     *                        The {@link SingleAgentPlan#agent} may already have a plan
     */
    @Override
    public void addPlan(SingleAgentPlan singleAgentPlan) {

        /*  = Remove methods =  */
        SingleAgentPlan previousPlan = this.agent_plan.get(singleAgentPlan.agent);
        this.removeAgentPreviousPlan(previousPlan);
        this.removableConflictAvoidance.removeAgentConflicts(singleAgentPlan.agent);

        /*  = Add methods =  */
        this.addAgentNewPlan(singleAgentPlan);
        this.agent_plan.put(singleAgentPlan.agent, singleAgentPlan); // Updates if already exists
    }





    /*  = Add methods =  */

    /**
     * = Adds =
     * 2. All {@link A_Conflict} for every other {@link Agent} that it conflicts with to {@link #removableConflictAvoidance}
     * 3. All of plan's {@link TimeLocation} to {@link #timeLocationTables}
     * 4. All Conflicts regarding the goal of {@link SingleAgentPlan}
     * @param singleAgentPlan - {@inheritDoc}
     */
    private void addAgentNewPlan(SingleAgentPlan singleAgentPlan) {

        if ( singleAgentPlan == null ){
            return;
        }

        int agentFirstMoveTime = singleAgentPlan.getFirstMoveTime();
        int goalTime = singleAgentPlan.getEndTime();

        /*  Check for conflicts and Add timeLocations */
        for (int time = agentFirstMoveTime; time <= goalTime; time++) {
            // Move's from location is 'prevLocation' , therefor timeLocation is time - 1
            I_Location location = singleAgentPlan.moveAt(time).prevLocation;
            TimeLocation timeLocation = new TimeLocation(time - 1, location);

            this.checkAddConflictsByTimeLocation(timeLocation, singleAgentPlan); // Checks for conflicts
            this.timeLocationTables.addTimeLocation(timeLocation, singleAgentPlan);
        }

        // Check final move to goalLocation
        I_Location location = singleAgentPlan.moveAt(goalTime).currLocation;
        TimeLocation timeLocation = new TimeLocation(goalTime, location);
        this.checkAddConflictsByTimeLocation(timeLocation, singleAgentPlan); // Checks for conflicts
        this.timeLocationTables.addTimeLocation(timeLocation, singleAgentPlan);


        // Checks for conflicts and add if exists. Adds the goal's timeLocation
        this.manageGoalLocationFromPlan(goalTime, singleAgentPlan);


    }


    /**
     * Checks for {@link VertexConflict} with goal
     * Add the goal timeLocation to {@link #timeLocationTables}
     * @param goalTime - The time of the last move in plan
     * @param singleAgentPlan - Agent's new plan
     */
    private void manageGoalLocationFromPlan(int goalTime, SingleAgentPlan singleAgentPlan) {

        I_Location goalLocation = singleAgentPlan.moveAt(goalTime).currLocation;

        TimeLocation goalTimeLocation = new TimeLocation(goalTime, goalLocation);

        /*  = Check if this agentAtGoal conflicts with other agents =   */
        this.checkAddSwappingConflicts(goalTime, singleAgentPlan);
        this.checkAddVertexConflictsWithGoal(goalTimeLocation, singleAgentPlan);


        /*  = Add goal timeLocation =  */
        this.timeLocationTables.addGoalTimeLocation(goalTimeLocation, singleAgentPlan);
    }


    /**
     * Checks if agent's {@link TimeLocation} at goal conflicts with other agents plans
     * Adds the conflicts {@link #addVertexConflicts(TimeLocation, Agent, Set)}
     * @param timeLocation - {@inheritDoc}
     * @param singleAgentPlan - {@inheritDoc}
     */
    private void checkAddVertexConflictsWithGoal(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan){

        I_Location location = timeLocation.location;
        // A Set of time that at least one agent is occupying
        Set<Integer> timeList = this.timeLocationTables.getTimeListAtLocation(location);

        if(timeList == null){
            return; // There are no agents at timeLocation
        }

        // Check if other plans are using this location after the agent arrived at goal
        for (int time : timeList) {
            if( time > timeLocation.time){
                Set<Agent> agentsAtTimeLocation = this.timeLocationTables.timeLocation_Agents.get(new TimeLocation(time,location));

                // Adds if agent != agentAtTimeLocation
                this.addVertexConflicts(new TimeLocation(time, location), singleAgentPlan.agent, agentsAtTimeLocation);
            }
        }
    }



    /**
     * Adds {@link VertexConflict},{@link SwappingConflict} with agents at a given {@link TimeLocation}
     * Check for {@link VertexConflict} with agents at their goal
     * @param timeLocation - {@inheritDoc}
     * @param singleAgentPlan - {@inheritDoc}
     */
    private void checkAddConflictsByTimeLocation(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan) {

        Set<Agent> agentsAtTimeLocation = this.timeLocationTables.getAgentsAtTimeLocation(timeLocation);
        this.addVertexConflicts(timeLocation, singleAgentPlan.agent, agentsAtTimeLocation);

        /*  = Check conflicts with agents at their goal =    */
        AgentAtGoal agentAtGoal = this.timeLocationTables.getAgentAtGoalTime(timeLocation.location);
        if( agentAtGoal != null ){
            if ( timeLocation.time >= agentAtGoal.time ){
                // Adds a Vertex conflict if time at location is greater than another agent time at goal
                this.addVertexConflicts(timeLocation, singleAgentPlan.agent, new HashSet<>(){{add(agentAtGoal.agent);}});
            }
        }


        /*      = Check for swapping conflicts =     */
        this.checkAddSwappingConflicts(timeLocation.time, singleAgentPlan);
    }


    /***
     * Looks for {@link SwappingConflict}
     * If {@link SwappingConflict} is found:
     *      1. Create two {@link SwappingConflict} for both direction.
     *      2. Add conflicts to both agents in {@link #removableConflictAvoidance}
     * @param time - The move's time.
     * @param singleAgentPlan - {@inheritDoc}
     */
    private void checkAddSwappingConflicts(int time, SingleAgentPlan singleAgentPlan) {
        if( time < 1 ){ return;}
        I_Location previousLocation = singleAgentPlan.moveAt(time).prevLocation;
        I_Location nextLocation = singleAgentPlan.moveAt(time).currLocation;
        Set<Agent> agentsMovingToPrevLocations = this.timeLocationTables.timeLocation_Agents.get(new TimeLocation(time,previousLocation));
        if ( agentsMovingToPrevLocations == null ){
            return;
        }

        /* Add conflict with all the agents that:
            1. Coming from agent's moveAt(time).currLocation
            2. Going to agent's moveAt(time).prevLocation
        */
        for (Agent agentMovingToPrevPosition : agentsMovingToPrevLocations) {
            if( agentMovingToPrevPosition.equals(singleAgentPlan.agent) ){
                continue; // Self Conflict
            }
            if ( this.agent_plan.get(agentMovingToPrevPosition).moveAt(time).prevLocation.equals(nextLocation)){


                // Create two conflicts
                SwappingConflict swappingConflict_addedAgentFirst = new SwappingConflict(   singleAgentPlan.agent,
                        agentMovingToPrevPosition,
                        time,
                        nextLocation,
                        previousLocation);

                SwappingConflict swappingConflict_addedAgentSecond = new SwappingConflict(  agentMovingToPrevPosition,
                        singleAgentPlan.agent,
                        time,
                        previousLocation,
                        nextLocation);



                // Add conflicts to both of the agents
                this.removableConflictAvoidance.addConflictToAgent(singleAgentPlan.agent, swappingConflict_addedAgentFirst);
                this.removableConflictAvoidance.addConflictToAgent(agentMovingToPrevPosition, swappingConflict_addedAgentFirst);

                this.removableConflictAvoidance.addConflictToAgent(singleAgentPlan.agent, swappingConflict_addedAgentSecond);
                this.removableConflictAvoidance.addConflictToAgent(agentMovingToPrevPosition, swappingConflict_addedAgentSecond);
            }
        }
    }


    /**
     * Adds {@link VertexConflict} with other agents at a given {@link TimeLocation}
     * Note: Adds if agent != agentAtTimeLocation
     * @param timeLocation - {@inheritDoc}
     * @param agent - {@inheritDoc}
     * @param agentsAtTimeLocation - {@inheritDoc}
     */
    private void addVertexConflicts(TimeLocation timeLocation, Agent agent, Set<Agent> agentsAtTimeLocation) {

        if( agentsAtTimeLocation == null ){
            return;
        }

        for (Agent agentConflictsWith : agentsAtTimeLocation) {
            if( agentConflictsWith.equals(agent) ){
                continue; // Self Conflict
            }
            VertexConflict vertexConflict = new VertexConflict(agent,agentConflictsWith,timeLocation);

            // Add conflict to both of the agents
            this.removableConflictAvoidance.addConflictToAgent(agent, vertexConflict);
            this.removableConflictAvoidance.addConflictToAgent(agentConflictsWith, vertexConflict);
        }
    }





    /*  = Remove methods =  */

    /**
     * Agent has a new plan, therefor the old plan needs to be removed.
     * Removes the plan's goal location from:
     *    1. {@link TimeLocationTables#timeLocation_Agents}
     *    2. {@link TimeLocationTables#location_timeList}
     *    3. {@link TimeLocationTables#goal_plan}
     * @param previousPlan - Agent's previous plan in {@link #agent_plan}
     */
    private void removeAgentPreviousPlan(SingleAgentPlan previousPlan) {
        if ( previousPlan == null ){
            return; // Agent has no previous plan
        }

        for (int time = previousPlan.getFirstMoveTime(); time <= previousPlan.getEndTime(); time++) {
            Move prevMove = previousPlan.moveAt(time);
            if ( prevMove != null ){
                TimeLocation timeLocation = new TimeLocation(time - 1, prevMove.prevLocation);
                // 1. remove from {@link TimeLocationTables#timeLocation_Agents}
                // 2. remove from {@link TimeLocationTables#location_timeList}
                this.removeTimeLocation(timeLocation, previousPlan);
            }
        }

        /*  = Plan's goal =  */
        int goalTime = previousPlan.size();
        I_Location goalLocation = previousPlan.moveAt(goalTime).currLocation;
        TimeLocation goalTimeLocation = new TimeLocation(goalTime, goalLocation);
        // 1. remove from this.timeLocation_Agents
        // 2. remove from this.location_timeList
        this.removeTimeLocation(goalTimeLocation, previousPlan);

        // 3. remove from this.goal_agentTime
        this.timeLocationTables.removeGoalLocation(goalLocation);
    }


    /**
     * Removes {@link TimeLocation} from {@link #timeLocationTables}
     * @param timeLocation - agent's {@link TimeLocation} to remove
     * @param plan - agent's {@link SingleAgentPlan} to remove
     */
    private void removeTimeLocation(TimeLocation timeLocation, SingleAgentPlan plan){

        Set<Agent> agentsAtTimeLocation = this.timeLocationTables.getAgentsAtTimeLocation(timeLocation);
        agentsAtTimeLocation.remove(plan.agent);
        if (agentsAtTimeLocation.isEmpty()){
            this.timeLocationTables.removeTimeLocationFromAgentAtTimeLocation(timeLocation);
            Set<Integer> timeList = this.timeLocationTables.getTimeListAtLocation(timeLocation.location);
            timeList.remove(timeLocation.time);
            if ( timeList.isEmpty() ){
                this.timeLocationTables.removeLocationFromTimeList(timeLocation.location); // No agents at this timeLocation
            }
        }
    }



    @Override
    public A_Conflict selectConflict() {
        return conflictSelectionStrategy.selectConflict(this.removableConflictAvoidance.getAllConflicts());
    }


}
