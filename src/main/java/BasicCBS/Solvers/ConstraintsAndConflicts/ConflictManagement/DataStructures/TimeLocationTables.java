package BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.SingleAgentPlan;

import java.util.*;


public class TimeLocationTables {

    // BasicCBS.Maps from a time&location to all relevant agents
    public final Map<TimeLocation, Set<Agent>> timeLocation_Agents;

    // BasicCBS.Maps from a location to all time units where at least one agent is occupying the location
    public final Map<I_Location,Set<Integer>> location_timeList;

    // BasicCBS.Maps from GoalLocation to Agent&time
    public final Map<I_Location, SingleAgentPlan> goal_plan;



    public TimeLocationTables(){
        this.timeLocation_Agents = new HashMap<>();
        this.location_timeList = new HashMap<>();
        this.goal_plan = new HashMap<>();
    }


    public TimeLocationTables(TimeLocationTables other){
        this.timeLocation_Agents = new HashMap<>();
        for ( Map.Entry<TimeLocation,Set<Agent>> timeLocationAgentFromOther: other.timeLocation_Agents.entrySet()){
            this.timeLocation_Agents.put(timeLocationAgentFromOther.getKey(), new HashSet<>(timeLocationAgentFromOther.getValue()));
        }

        this.location_timeList = new HashMap<>();
        for ( Map.Entry<I_Location,Set<Integer>> location_timeListFromOther: other.location_timeList.entrySet()){
            this.location_timeList.put(location_timeListFromOther.getKey(), new HashSet<>(location_timeListFromOther.getValue()));
        }

        this.goal_plan = new HashMap<>();
        for ( Map.Entry<I_Location,SingleAgentPlan> goalPlanFromOther : other.goal_plan.entrySet()){
            this.goal_plan.put(goalPlanFromOther.getKey(),goalPlanFromOther.getValue());
        }
    }


    public TimeLocationTables copy(){
        return new TimeLocationTables(this);
    }



    /**
     * Updates {@link #timeLocation_Agents}, {@link #location_timeList}
     * @param timeLocation - {@inheritDoc}
     * @param singleAgentPlan - {@inheritDoc}
     */
    public void addTimeLocation(TimeLocation timeLocation , SingleAgentPlan singleAgentPlan){

        this.timeLocation_Agents.computeIfAbsent(timeLocation, k -> new HashSet<>());
        this.timeLocation_Agents.get(timeLocation).add(singleAgentPlan.agent);
        this.location_timeList.computeIfAbsent(timeLocation.location, k -> new HashSet<>());
        this.location_timeList.get(timeLocation.location).add(timeLocation.time);
    }


    public void addGoalTimeLocation(TimeLocation goalTimeLocation, SingleAgentPlan singleAgentPlan){

        this.addTimeLocation(new TimeLocation(goalTimeLocation.time, goalTimeLocation.location), singleAgentPlan);

        // Add to goal_agentTime, 'put' method will update its value if already exists
        // Keep the earlier one if one exists (should only happen with shared goals?)
        SingleAgentPlan currentGoalPlanForLocation = this.goal_plan.get(goalTimeLocation.location);
        if (currentGoalPlanForLocation == null || currentGoalPlanForLocation.getEndTime() > goalTimeLocation.time){
            this.goal_plan.put(goalTimeLocation.location, singleAgentPlan);
        }

        this.location_timeList.computeIfAbsent(goalTimeLocation.location, k -> new HashSet<>());
        // A Set of time that at least one agent is occupying
        Set<Integer> timeList = this.location_timeList.get(goalTimeLocation.location);
        timeList.add(goalTimeLocation.time); // add the plan's timeLocation at goal
    }



    public Set<Agent> getAgentsAtTimeLocation(TimeLocation timeLocation){
        return this.timeLocation_Agents.get(timeLocation);
    }

    public AgentAtGoal getAgentAtGoalTime(I_Location goalLocation){
        SingleAgentPlan plan = this.goal_plan.get(goalLocation);
        if( plan == null ){
            return null;
        }
        AgentAtGoal agentAtGoal = new AgentAtGoal(plan.agent, plan.getEndTime());
        return agentAtGoal;
    }

    public Set<Integer> getTimeListAtLocation(I_Location location){
        return this.location_timeList.get(location);
    }

    public void removeTimeLocationFromAgentAtTimeLocation(TimeLocation timeLocation){
        this.timeLocation_Agents.remove(timeLocation);
    }

    public void removeGoalLocation(I_Location goalLocation){
        this.goal_plan.remove(goalLocation);
    }

    public void removeLocationFromTimeList(I_Location location){
        this.location_timeList.remove(location);
    }




    public static boolean equalsTimeLocations(Map<TimeLocation,Set<Agent>> expectedTimeLocation_agents, Map<TimeLocation,Set<Agent>> actualTimeLocation_agents){

        if( actualTimeLocation_agents.size() != expectedTimeLocation_agents.size() ){
            return false;
        }
        for (Map.Entry<TimeLocation,Set<Agent>> timeLocation_agents: expectedTimeLocation_agents.entrySet()){

            TimeLocation timeLocation = timeLocation_agents.getKey();
            Set<Agent> expectedAgents = expectedTimeLocation_agents.get(timeLocation);
            Set<Agent> actualAgents = actualTimeLocation_agents.get(timeLocation);
            if (! equalsAllAgents(expectedAgents,actualAgents)){
                return false;
            }
        }
        return true;
    }


    private static boolean equalsAllAgents(Set<Agent> expectedAgents, Set<Agent> actualAgents){

        if( expectedAgents.size() != actualAgents.size() ){
            return false;
        }

        for (Agent agent: expectedAgents){
            if (! actualAgents.contains(agent)){
                return false;
            }
        }
        return true;
    }

}
