package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.*;

/**
 * A solution to a lifelong problem.
 *
 * Contains the solutions that were returned by the solver at different times. Also contains (at {@link Solution}) a merged
 * solution, representing just the paths that agents ended up following.
 */
public class LifelongSolution extends Solution{

    public final SortedMap<Integer, Solution> solutionsAtTimes;
    public final SortedMap<LifelongAgent, List<Integer>> agentsWaypointArrivalTimes;

    public LifelongSolution(SortedMap<Integer, Solution> solutionsAtTimes, List<LifelongAgent> agents) {
        // make unified solution for super
        super(mergeSolutions(solutionsAtTimes, agents));
        this.solutionsAtTimes = solutionsAtTimes;
        this.agentsWaypointArrivalTimes = getAgentsWaypointArrivalTimes(this);
    }

    /**
     * Merge the solutions that the solver produced at different times into one solution, which represents the paths that
     * the agents actually ended up taking.
     * @param solutionsAtTimes solutions that the solver produced at different times
     * @param agents {@link LifelongAgent lifelong agents} to use for the merged solution's plans
     * @return a merged solution
     */
    private static Map<Agent, SingleAgentPlan> mergeSolutions(SortedMap<Integer, Solution> solutionsAtTimes,
                                                              List<LifelongAgent> agents) {
        Map<Agent, SingleAgentPlan> mergedAgentPlans = new HashMap<>();
        for (LifelongAgent agent : agents){
            Integer[] waypointSegmentsEndTimes = new Integer[agent.waypoints.size()];
            waypointSegmentsEndTimes[0] = 0; // first waypoint is start location
            int waypointIndex = 1;
            SingleAgentPlan mergedPlanUpToTime = new SingleAgentPlan(agent);
            for (int time : solutionsAtTimes.keySet()) {
                Solution solution = solutionsAtTimes.get(time);
                SingleAgentPlan timelyPlan = solution.getPlanFor(agent);
                I_Coordinate currWaypoint = agent.waypoints.get(waypointIndex);
                List<Move> mergedMovesIncludingTime = mergePlans(mergedPlanUpToTime, timelyPlan, agent);
                int newWaypointTimeHypothesis = mergedMovesIncludingTime.get(mergedMovesIncludingTime.size()-1).timeNow;
                if (! currWaypoint.equals(timelyPlan.agent.target)){
                    // might need to fix existing hypothesis, in the event that there was an arrival at goal also before
                    // final move in prev plan
                    waypointSegmentsEndTimes[waypointIndex] = timelyPlan.getPlanStartTime();

                    // hypothesis about old waypoint is proven; add hypothesis about the new waypoint
                    waypointIndex++;
                }
                // update hypothesis about time of achieving waypoint (can be same value)
                waypointSegmentsEndTimes[waypointIndex] = newWaypointTimeHypothesis;
                // for next iteration
                mergedPlanUpToTime = new SingleAgentPlan(agent, mergedMovesIncludingTime); // now includes current iteration time
            }
            LifelongSingleAgentPlan mergedPlanIncludingTime = new LifelongSingleAgentPlan(mergedPlanUpToTime, waypointSegmentsEndTimes);
            mergedAgentPlans.put(agent, mergedPlanIncludingTime);
        }
        return mergedAgentPlans;
    }

    /**
     * Merges two {@link SingleAgentPlan plans}.
     * @param mergedPlanUpToTime keeps the moves from this plan that don't overlap with the other plan.
     * @param newPlan keeps the moves from this plan that are newer (larger time value) than all moves in the other plan.
     * @param agent a {@link LifelongAgent} to use for the merged plan.
     * @return a merged plan.
     */
    private static List<Move> mergePlans(SingleAgentPlan mergedPlanUpToTime, SingleAgentPlan newPlan, LifelongAgent agent){
        if (mergedPlanUpToTime.size() == 0){
            List<Move> res = new ArrayList<>();
            newPlan.forEach(res::add);
            return res;
        }

        List<Move> mergedMoves = new ArrayList<>();

        // add every move before the time when the new plan starts
        for (int time = mergedPlanUpToTime.getFirstMoveTime(); time < Math.min(newPlan.getFirstMoveTime(), mergedPlanUpToTime.getEndTime() + 1); time++) {
            mergedMoves.add(mergedPlanUpToTime.moveAt(time));
        }

        // add stays at end of existing plan if it ends before next plan starts
        for (int t = mergedPlanUpToTime.getEndTime() + 1; t < newPlan.getFirstMoveTime(); t++) {
            mergedMoves.add(new Move(agent, t, mergedPlanUpToTime.getLastMove().currLocation, mergedPlanUpToTime.getLastMove().currLocation));
        }
        // add the new plan
        for (Move move :
                newPlan) {
            mergedMoves.add(move);
        }
        // remove excess stays at goal if they exist. May exist after reaching last destination
        Move lastMove = mergedMoves.get(mergedMoves.size() - 1);
        while (mergedMoves.size() > 1 && lastMove.prevLocation.equals(lastMove.currLocation)){
            mergedMoves.remove(mergedMoves.size() - 1);
            lastMove = mergedMoves.get(mergedMoves.size() - 1);
        }

//        return new LifelongSingleAgentPlan(mergedPlanUpToTime.agent, mergedMoves, waypointTimes);
        return mergedMoves;
    }

    private static SortedMap<LifelongAgent, List<Integer>> getAgentsWaypointArrivalTimes(LifelongSolution solution){
        SortedMap<LifelongAgent, List<Integer>> agentsWaypointArrivalTimes = new TreeMap<>();
        for (SingleAgentPlan plan : solution){
            if (!(plan.agent instanceof  LifelongAgent)){
                throw new IllegalArgumentException("a LifelongSolution is only for lifelong agents.");
            }
            // verify passes through all waypoints in order
            LifelongAgent lifelongAgent = ((LifelongAgent) plan.agent);
            List<Integer> waypointArrivalTimes = new ArrayList<>(lifelongAgent.waypoints.size());
            if (!plan.getFirstMove().prevLocation.getCoordinate().equals(lifelongAgent.waypoints.get(0))){
                // verify first waypoint which is also the source
                throw new IllegalArgumentException("missing first waypoint");
            }
            waypointArrivalTimes.add(0);
            int prevWaypointTime = 0;
            for (int i = 1 ; i < lifelongAgent.waypoints.size() ; i++){
                I_Coordinate waypoint = lifelongAgent.waypoints.get(i);
                boolean found = false;
                for (int t = prevWaypointTime + 1; t <= plan.getEndTime(); t++) {
                    if (plan.moveAt(t).currLocation.getCoordinate().equals(waypoint)){
                        waypointArrivalTimes.add(t);
                        prevWaypointTime = t;
                        found = true;
                        break;
                    }
                }
                if (!found){
                    throw new IllegalArgumentException("missing waypoint " + i + ": " + waypoint);
                }
            }
            agentsWaypointArrivalTimes.put(lifelongAgent, Collections.unmodifiableList(waypointArrivalTimes));
        }
        return Collections.unmodifiableSortedMap(agentsWaypointArrivalTimes);
    }

    public String agentsWaypointArrivalTimes(){
        StringBuilder sb = new StringBuilder();
        for (LifelongAgent agent: this.agentsWaypointArrivalTimes.keySet()){
            sb.append("agent ");
            sb.append(agent.iD);
            sb.append(" :");
            List<Integer> arrivalTimes = this.agentsWaypointArrivalTimes.get(agent);
            for (int arrivalTime : arrivalTimes){
                sb.append(" ");
                sb.append(arrivalTime);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private int totalNumTasksCompleted() {
        int res = 0;
        for (List<Integer> waypointTimes: this.agentsWaypointArrivalTimes.values()){
            res += waypointTimes.size();
        }
        return res;
    }

    private SortedMap<Integer, Integer> timeToNumTasksCompleted(){
        SortedMap<Integer, Integer> timesToNumCompletions = new TreeMap<>();
        for (List<Integer> arrivalTimes: this.agentsWaypointArrivalTimes.values()){
            for (int time : arrivalTimes){
                if (time == 0){
                    continue;
                }
                int currentCompletionsAtTime = timesToNumCompletions.getOrDefault(time, 0);
                timesToNumCompletions.put(time, currentCompletionsAtTime + 1);
            }
        }
        return timesToNumCompletions;
    }

    /**
     * @param x - float between 0 and 1 of the percent of completed tasks for the metric
     * @return the time when the required percent of all tasks or higher was completed.
     */
    public int timeToXProportionCompletion(double x){
        return timeToXCompletion((int)(((double) totalNumTasksCompleted()) * x));
    }

    /**
     * @param x - number of completed tasks
     * @return the time when the required number of tasks or higher was completed.
     */
    public int timeToXCompletion(int x){
        int counterTasksCompleted = 0;
        SortedMap<Integer, Integer> timeToNumTasksCompleted = timeToNumTasksCompleted();
        for (int time: timeToNumTasksCompleted.keySet()){
            counterTasksCompleted += timeToNumTasksCompleted.get(time);
            if (counterTasksCompleted >= x){
                return time;
            }
        }
        return -1; // not enough tasks completed. shouldn't happen.
    }

    public float averageThroughput(){
        return ((float) totalNumTasksCompleted()) / ((float) makespan());
    }

    public float averageIndividualThroughput(){
        return ((float) totalNumTasksCompleted()) / ((float) sumIndividualCosts());
    }

    public int throughputAtT(int t){
        SortedMap<Integer, Integer> timeToNumTasksCompleted = timeToNumTasksCompleted();
        int throughput = 0;
        for (int time : timeToNumTasksCompleted().keySet()){
            if (time > t){
                break;
            }
            else {
                throughput += timeToNumTasksCompleted.get(time);
            }
        }
        return throughput;
    }

}
