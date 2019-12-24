package BasicCBS.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicCBS.Solvers.ConstraintsAndConflicts.VertexConflict;

import java.util.*;
import java.util.function.Consumer;

/**
 * A collection of {@link SingleAgentPlan}s, representing a solution to a Path Finding problem.
 * If the collection contains more than one plan, it is a solution to a Multi Agent Path Finding problem.
 */
public class Solution implements Iterable<SingleAgentPlan>{
    /**
     * A {@link Map}, mapping {@link Agent agents} to their {@link SingleAgentPlan plans}.
     */
    protected final Map<Agent, SingleAgentPlan> agentPlans;
    protected boolean checkGoalInValidation = true;

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
        List<SingleAgentPlan> allPlans = new ArrayList<>(agentPlans.values());
        for (int i = 0; i < allPlans.size(); i++) {
            SingleAgentPlan plan1 = allPlans.get(i);
            for (int j = i+1; j < allPlans.size(); j++) {
                SingleAgentPlan plan2 = allPlans.get(j);
                if(plan1.conflictsWith(plan2, checkGoalInValidation)) {
                    return false;
                }
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

    public int sumIndividualCosts(){
        int SOC = 0;
        for (SingleAgentPlan plan :
                agentPlans.values()) {
            SOC += plan.getCost();
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

    @Override
    public String toString() {
        return agentPlans.values().toString();
    }

    //nicetohave JSON toString

    /**
     * A string output that is easier for humans to read.
     * @return a string output that is easier for humans to read.
     */
    public String readableToString(){
        StringBuilder sb = new StringBuilder();
        List<Agent> agents = new ArrayList<>(this.agentPlans.keySet());
        Collections.sort(agents, Comparator.comparing(agent -> agent.iD));
        for(Agent agent : agents){
            sb.append("Plan for agent ").append(agent.iD);
            for(Move move : this.agentPlans.get(agent)){
                sb.append('\n').append(move.timeNow).append(": ").append(move.prevLocation.getCoordinate()).append(" -> ").append(move.currLocation.getCoordinate());
            }
            sb.append("\n");
        }
        sb.append('\n');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution that = (Solution) o;
        return agentPlans.equals(that.agentPlans);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentPlans);
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
}
