package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;

import java.util.HashMap;
import java.util.Map;

/**
 * A solution to an online problem.
 *
 * Contains the solutions that were returned by the solver at different times. Also contains (at {@link Solution}) a merged
 * solution, representing just the paths that agents ended up following.
 */
public class OnlineSolution extends Solution{

    public final Map<Integer, Solution> solutionsAtTimes;

    public OnlineSolution(Map<Integer, Solution> solutionsAtTimes) {
        //make unified solution for super
        super(mergeSolutions(solutionsAtTimes));
        this.solutionsAtTimes = solutionsAtTimes;
        super.checkGoalInValidation = false;
    }

    public OnlineSolution(Solution offlineSolution) {
        this(putOfflineSolutionInMap(offlineSolution));
    }

    private static Map<Integer, Solution> putOfflineSolutionInMap(Solution offlineSolution){
        Map<Integer, Solution> solutionsAtTimes = new HashMap<>();
        solutionsAtTimes.put(0, offlineSolution);
        return solutionsAtTimes;
    }

    /**
     * Merge the solutions that the solver produced at different times into one solution, which represents the paths that
     * the agents actually ended up taking.
     * @param solutionsAtTimes solutions that the solver produced at different times
     * @return a merged solution
     */
    private static Map<Agent, SingleAgentPlan> mergeSolutions(Map<Integer, Solution> solutionsAtTimes) {
        Map<Agent, SingleAgentPlan> agentPlans = new HashMap<>();
        // for every time where new agents arrived (and so the existing plans were changed)
        for (int time :
                solutionsAtTimes.keySet()) {
            Solution solution = solutionsAtTimes.get(time);
            // every agent included in this solution. meaning it arrived at/before time (the solution's start time), and
            // hasn't reached its goal yet.
            for (SingleAgentPlan plan :
                    solution) {
                Agent agent = plan.agent;
                if(! agentPlans.containsKey(agent)){ // agent arrived at time
                    agentPlans.put(agent, plan);
                }
                else{ // agent was already around, merge the plans to represent what it actually ended up doing.
                    SingleAgentPlan previousPlan = agentPlans.get(agent);
                    SingleAgentPlan newPlan = solution.getPlanFor(agent);
                    SingleAgentPlan mergedPlan = mergePlans(previousPlan, newPlan);
                    agentPlans.put(agent, mergedPlan);
                }
            }
        }
        return agentPlans;
    }

    /**
     * Merges two {@link SingleAgentPlan plans}.
     * @param oldPlan keeps the moves from this plan that don't overlap with the other plan.
     * @param newPlan keeps the moves from this plan that are newer (larger time value) than all moves in the other plan.
     * @return a merged plan.
     */
    private static SingleAgentPlan mergePlans(SingleAgentPlan oldPlan, SingleAgentPlan newPlan){
        SingleAgentPlan merged = new SingleAgentPlan(oldPlan.agent);
        // if the new plan is empty (agent had just arrived at its goal at the time of the new plan's start)
        if(newPlan.size() == 0){
            return oldPlan;
        }
        else {
            // add every move before the time where the new plan starts
            for (int time = oldPlan.getFirstMoveTime(); time < newPlan.getFirstMoveTime(); time++) {
                merged.addMove(oldPlan.moveAt(time));
            }
            // add all of the moves in the new plan
            for (Move move :
                    newPlan) {
                merged.addMove(move);
            }
            return merged;
        }
    }

    @Override
    protected boolean planStartsAtSourceEndsAtTarget(SingleAgentPlan plan, MAPF_Instance instance) {
        return plan.size() == 0 || (
                plan.moveAt(plan.getFirstMoveTime()).prevLocation.equals(     /*start at source*/
                ((OnlineAgent)(plan.agent)).getPrivateGarage(instance.map.getMapCell(plan.agent.source))) /*convert to online (private garage)*/
                && plan.moveAt(plan.getEndTime()).currLocation.equals(instance.map.getMapCell(plan.agent.target)) )/*end at target*/;
    }
}
