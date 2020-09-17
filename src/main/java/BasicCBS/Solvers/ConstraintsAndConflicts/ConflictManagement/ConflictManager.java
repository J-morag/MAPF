package BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.*;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.AgentAtGoal;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocationTables;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.*;

public class ConflictManager implements I_ConflictManager {

    /*  = Data structures =   */
    private final Set<A_Conflict> allConflicts; // Keeps all conflicts
    /**
     * TimeLocationTables mapping:
     *      1. Time&location to relevant agents
     *      2. Location to all time units
     *      3. GoalLocation to Agent&time
     */
    public final TimeLocationTables timeLocationTables;
    public final Map<Agent, SingleAgentPlan> agent_plan; // BasicCBS.Maps from Agent to Agent's plan

    /*  = instance variables =   */
    /**
     * Strategy for selecting conflicts
     */
    public final ConflictSelectionStrategy conflictSelectionStrategy;


    /**
     * Constructor.
     * @param conflictSelectionStrategy how to choose conflicts.
     */
    public ConflictManager(ConflictSelectionStrategy conflictSelectionStrategy) {
        /* Might want to change allConflicts from a HashSet to a TreeSet to make MinTimeConflictSelectionStrategy more efficient.
         If we want to make this more generic, we should scrap ConflictSelectionStrategy and instead make this field
         an instance of some new class, thus combining storage and selection of conflicts. @Jonathan Morag 28/10/2019
         */
        this.allConflicts = new HashSet<>();
        this.timeLocationTables = new TimeLocationTables();
        this.agent_plan = new HashMap<>();

        this.conflictSelectionStrategy = conflictSelectionStrategy;
    }

    /* Default constructor */
    public ConflictManager() {
        this(new MinTimeConflictSelectionStrategy());
    }

    /**
     * Copy constructor.
     * @param other another {@link ConflictManager} to copy.
     */
    public ConflictManager(ConflictManager other){
        this.allConflicts = new HashSet<>();
        this.addConflicts(other.allConflicts);
        this.timeLocationTables = other.timeLocationTables.copy();
        this.agent_plan = new HashMap<>();
        for ( Map.Entry<Agent,SingleAgentPlan> agentPlanFromOther: other.agent_plan.entrySet()){
            this.agent_plan.put(agentPlanFromOther.getKey(),agentPlanFromOther.getValue());
        }
        this.conflictSelectionStrategy = other.conflictSelectionStrategy;
    }

    @Override
    public I_ConflictManager copy() {
        return new ConflictManager(this);
    }

    /*  = Add methods =  */

    /***
     * This method adds a new plan for SingleAgentPlan.
     * Note that if agent's plan already exists, it removes before adding.
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
        this.agent_plan.put(singleAgentPlan.agent, singleAgentPlan); // Updates if already exists
        this.addAgentNewPlan(singleAgentPlan);
    }

    /**
     * = Adds =
     * 2. All {@link A_Conflict} for every other {@link Agent} that it conflicts with to {@link #allConflicts}
     * 3. All of plan's {@link TimeLocation} to {@link #timeLocationTables}
     * 4. All Conflicts regarding the goal of {@link SingleAgentPlan}
     * @param singleAgentPlan - {@inheritDoc}
     */
    private void addAgentNewPlan(SingleAgentPlan singleAgentPlan) {

        if ( singleAgentPlan == null ){ return; }

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
    protected void checkAddVertexConflictsWithGoal(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan){

        I_Location location = timeLocation.location;
        // A Set of time that at least one agent is occupying
        Set<Integer> timeList = this.timeLocationTables.getTimeListAtLocation(location);

        if(timeList == null){ return; /* There are no agents at timeLocation */ }

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
    protected void checkAddConflictsByTimeLocation(TimeLocation timeLocation, SingleAgentPlan singleAgentPlan) {

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
     *      2. Add conflicts to both agents in {@link #allConflicts}
     * @param time - The move's time.
     * @param singleAgentPlan - {@inheritDoc}
     */
    protected void checkAddSwappingConflicts(int time, SingleAgentPlan singleAgentPlan) {
        if( time < 1 ){ return;}
        I_Location previousLocation = singleAgentPlan.moveAt(time).prevLocation;
        I_Location nextLocation = singleAgentPlan.moveAt(time).currLocation;
        Set<Agent> agentsMovingToPrevLocations = this.timeLocationTables.timeLocation_Agents.get(new TimeLocation(time,previousLocation));
        if ( agentsMovingToPrevLocations == null ){ return; }

        /* Add conflict with all the agents that:
            1. Coming from agent's moveAt(time).currLocation
            2. Going to agent's moveAt(time).prevLocation
        */
        for (Agent agentMovingToPrevPosition : agentsMovingToPrevLocations) {
            if( agentMovingToPrevPosition.equals(singleAgentPlan.agent) ){ continue; /* Self Conflict */ }
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
                this.addConflict(swappingConflict_addedAgentFirst);
                this.addConflict(swappingConflict_addedAgentSecond);
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
    protected void addVertexConflicts(TimeLocation timeLocation, Agent agent, Set<Agent> agentsAtTimeLocation) {

        if( agentsAtTimeLocation == null ){ return; }

        for (Agent agentConflictsWith : agentsAtTimeLocation) {
            if( agentConflictsWith.equals(agent) ){ continue; /* Self Conflict */ }
            VertexConflict vertexConflict = new VertexConflict(agent,agentConflictsWith,timeLocation);

            // Add conflict to both of the agents
            addConflict(vertexConflict);
        }
    }

    protected boolean addConflict(A_Conflict conflict) {
        return this.allConflicts.add(conflict);
    }

    protected boolean addConflicts(Collection<A_Conflict> conflicts) {
        boolean changed = false;
        for (A_Conflict conflict : conflicts) {
            changed |= this.addConflict(conflict);
        }
        return changed;
    }

    public Set<A_Conflict> getAllConflicts(){
        return this.allConflicts;
    }

    @Override
    public A_Conflict selectConflict() {
        return conflictSelectionStrategy.selectConflict(this.allConflicts);
    }

}
