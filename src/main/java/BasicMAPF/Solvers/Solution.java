package BasicMAPF.Solvers;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.VertexConflict;
import Environment.Metrics.InstanceReport;

import java.util.*;
import java.util.function.Consumer;

/**
 * A collection of {@link SingleAgentPlan}s, representing a solution to a Path Finding problem.
 * If the collection contains more than one plan, it is a solution to a Multi Agent Path Finding problem.
 */
public class Solution implements Iterable<SingleAgentPlan>{
    private static final int looseMaxSolutionStringChars = 10000 /*lines*/ * 20 /*chars (roughly)*/;
    /**
     * A {@link Map}, mapping {@link Agent agents} to their {@link SingleAgentPlan plans}.
     */
    private final Map<Agent, SingleAgentPlan> agentPlans;

    public Solution(Map<Agent, SingleAgentPlan> agentPlans) {
        this.agentPlans = new HashMap<>(agentPlans);
    }

    public Solution(Iterable<? extends SingleAgentPlan> plans) {
        if(plans instanceof Solution){
            Solution sol = ((Solution)plans);
            this.agentPlans = new HashMap<>(sol.agentPlans);
        }
        else{
            Map<Agent, SingleAgentPlan> agentPlanMap = new HashMap<>();
            for (SingleAgentPlan plan :
                    plans) {
                agentPlanMap.put(plan.agent, plan);
            }
            this.agentPlans = agentPlanMap;
        }
    }

    public Solution(){
        this(new HashMap<>());
    }

    public SingleAgentPlan getPlanFor(Agent agent){
        return agentPlans.get(agent);
    }

    public SingleAgentPlan putPlan(SingleAgentPlan singleAgentPlan){
        if(singleAgentPlan == null) {throw new IllegalArgumentException();}
        return this.agentPlans.put(singleAgentPlan.agent, singleAgentPlan);
    }

    /**
     * Looks for vertex conflicts ({@link VertexConflict}) or swapping conflicts ({@link SwappingConflict}). Runtime is
     * O( (n-1)*mTotal ) , where n = the number of {@link SingleAgentPlan plans}/{@link Agent agents} in this solution,
     * and mTotal = the total number of moves in all plans together.
     * @return true if the solution is valid (contains no vertex or swapping conflicts).
     */
    public boolean isValidSolution(){
        return this.isValidSolution(false, false);
    }

    /**
     * Looks for vertex conflicts ({@link VertexConflict}) or swapping conflicts ({@link SwappingConflict}). Runtime is
     * O( (n-1)*mTotal ) , where n = the number of {@link SingleAgentPlan plans}/{@link Agent agents} in this solution,
     * and mTotal = the total number of moves in all plans together.
     * @param sharedGoals if agents can share goals
     * @param sharedSources if agents share the same source and so don't conflict if one of them has been staying there since the start
     * @return true if the solution is valid (contains no vertex or swapping conflicts).
     */
    public boolean isValidSolution(boolean sharedGoals, boolean sharedSources){
        List<SingleAgentPlan> allPlans = new ArrayList<>(agentPlans.values());
        for (int i = 0; i < allPlans.size(); i++) {
            SingleAgentPlan plan1 = allPlans.get(i);
            for (int j = i+1; j < allPlans.size(); j++) {
                SingleAgentPlan plan2 = allPlans.get(j);
                if(plan1.conflictsWith(plan2, sharedGoals, sharedSources)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validates that this solution is a valid solution for the given {@link MAPF_Instance}. Where
     * {@link #isValidSolution()} only validates that this solution is free of conflicts, {@link #solves(MAPF_Instance)}
     * also validates that:
     * 1. The solution covers every {@link Agent} with a {@link SingleAgentPlan}.
     * 2. The solution doesn't cover agents not present in the instance.
     * 3. The plan for each agent starts at its source and ends at its target.
     * 4. The times of moves in plans are consistent - always increase by 1.
     * 5. The locations of moves in the plans are consistent - agents always move from a vertex to its neighbor.
     * These extra checks make this validation more expensive, but it is useful for debugging purposes.
     * @param instance an {@link MAPF_Instance} that this solution supposedly solves.
     * @return boolean if this solution solves the instance.
     */
    public boolean solves(MAPF_Instance instance){
        return this.solves(instance, false, false);
    }

    /**
     * Validates that this solution is a valid solution for the given {@link MAPF_Instance}. Where
     * {@link #isValidSolution()} only validates that this solution is free of conflicts, {@link #solves(MAPF_Instance)}
     * also validates that:
     * 1. The solution covers every {@link Agent} with a {@link SingleAgentPlan}.
     * 2. The solution doesn't cover agents not present in the instance.
     * 3. The plan for each agent starts at its source and ends at its target.
     * 4. The times of moves in plans are consistent - always increase by 1.
     * 5. The locations of moves in the plans are consistent - agents always move from a vertex to its neighbor.
     * These extra checks make this validation more expensive, but it is useful for debugging purposes.
     * @param instance an {@link MAPF_Instance} that this solution supposedly solves.
     * @return boolean if this solution solves the instance.
     */
    public boolean solves(MAPF_Instance instance, boolean sharedGoals, boolean sharedSources){
        // check that the solution is conflict free
        if (!isValidSolution(sharedGoals, sharedSources))
            return false;
        // check that the solution covers all agents and no other agents
        if (!this.agentPlans.keySet().containsAll(instance.agents) || !instance.agents.containsAll(this.agentPlans.keySet()))
            return false;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            // if start at goal is represented as empty plan, that plan is always internally consistent
            if (plan.size() == 0){
                continue;
            }
            // check start and end at source and target
            if (!plan.moveAt(plan.getFirstMoveTime()).prevLocation.equals(instance.map.getMapLocation(plan.agent.source)) /*start at source*/
                || !plan.moveAt(plan.getEndTime()).currLocation.equals(instance.map.getMapLocation(plan.agent.target))) /*end at target*/
            {
                return false;
            }
            // agents always move from a vertex to its neighbor
            Move prevMove = plan.moveAt(plan.getFirstMoveTime());
            // check that the move is internally consistent
            if (!(prevMove.prevLocation.isNeighbor(prevMove.currLocation) || prevMove.prevLocation.equals(prevMove.currLocation)))
                return false;
            for (int time = plan.getFirstMoveTime() + 1; time <= plan.getEndTime(); time++) {
                Move currMove = plan.moveAt(time);
                // check that the move is internally consistent
                if (!(currMove.prevLocation.isNeighbor(currMove.currLocation) || currMove.prevLocation.equals(currMove.currLocation)))
                    return false;
                // check that the move is consistent with the next move
                if (!prevMove.currLocation.equals(currMove.prevLocation))
                    return false;
                // check that the time of the move is consistent with the next move
                if (currMove.timeNow-prevMove.timeNow != 1)
                    return false;
                prevMove = currMove;
            }
        }
        return true;
    }

    /**
     * The number of plans (agents) in the solution.
     * @return the number of plans (agents) in the solution.
     */
    public int size(){
        return agentPlans.size();
    }

    /**
     * Calculates the sum of individual costs (SOC), typically defined as the sum of path lengths.
     * @return the sum of individual costs (SOC).
     */
    public int sumIndividualCosts(){
        int SOC = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            SOC += plan.getCost();
        }
        return SOC;
    }

    /**
     * Calculates the sum of individual costs with the priorities ({{@link Agent#priority}} modifier.
     * If the priority of all agents is set to 1, this method behaves the same as {{@link #sumIndividualCosts()}}.
     * @return sum of individual costs with the priorities ({{@link Agent#priority}} modifier
     */
    public int sumIndividualCostsWithPriorities(){
        int SOC = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            SOC += plan.getCost() * plan.agent.priority;
        }
        return SOC;
    }

    /**
     * Expensive!
     * The priorities are on the delta between the optimal free-space plan for each agent, and its plan in this solution.
     * So essentially, what matters is how much the agent was delayed, and that is affected by its priority.
     * @return sum of delays with priorities.
     */
    public int sumDelaysWithPriorities(MAPF_Instance instance){
        A_Solver aStar = new SingleAgentAStar_Solver();
        int sum = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            int freeSpaceCost = aStar.solve(instance.getSubproblemFor(plan.agent), new RunParameters(new InstanceReport()))
                    .getPlanFor(plan.agent).getCost();
            sum += ( plan.getCost() - freeSpaceCost ) * plan.agent.priority;
        }
        return sum;
    }

    /**
     * gets the SOC for all the agents of a certain priority (regular SOC).
     * @return the SOC for all the agents of a certain priority (regular SOC).
     */
    public int SOCOfPriorityLevel(int priorityLevel){
        int SOC = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            if (plan.agent.priority == priorityLevel){
                SOC += plan.getCost();
            }
        }
        return SOC;
    }

    public int makespan(){
        int maxCost = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            maxCost = Math.max(maxCost, plan.getCost());
        }
        return maxCost;
    }

    public int endTime(){
        int maxTime = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            maxTime = Math.max(maxTime, plan.getEndTime());
        }
        return maxTime;
    }

    @Override
    public String toString() {
        return this.readableToString().toString();
    }

    //nicetohave JSON toString

    /**
     * A string output that is easier for humans to read.
     * @return a string output that is easier for humans to read.
     */
    public StringBuilder readableToString(){
        StringBuilder sb = new StringBuilder();
        List<Agent> agents = new ArrayList<>(this.agentPlans.keySet());
        Collections.sort(agents, Comparator.comparing(agent -> agent.iD));
        for(Agent agent : agents){
            sb.append(this.agentPlans.get(agent));
            if (sb.length() > looseMaxSolutionStringChars){
                sb.append("... (truncated)");
                break;
            }
        }
        sb.append('\n');
        return sb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Solution)) return false;

        Solution solution = (Solution) o;

        return agentPlans.equals(solution.agentPlans);
    }

    @Override
    public int hashCode() {
        return agentPlans.hashCode();
    }

    /*  = Iterator Interface =  */

    @Override
    public Iterator<SingleAgentPlan> iterator() {
        return agentPlans.values().iterator();
    }

    @Override
    public void forEach(Consumer<? super SingleAgentPlan> action) {
        agentPlans.values().forEach(action);
    }

    @Override
    public Spliterator<SingleAgentPlan> spliterator() {
        return agentPlans.values().spliterator();
    }

    public I_Location getAgentLocation(Agent agent, int time) {
        if (agentPlans.get(agent).getEndTime() < time)
            return agentPlans.get(agent).moveAt(agentPlans.get(agent).getEndTime()).currLocation;
        else if (agentPlans.get(agent).getFirstMoveTime() > time
                || agentPlans.get(agent).getPlanStartTime() == time)
            return agentPlans.get(agent).moveAt(agentPlans.get(agent).getFirstMoveTime()).prevLocation;
        else
            return agentPlans.get(agent).moveAt(time).currLocation;

    }
}
