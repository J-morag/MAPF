package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.Move;
import BasicMAPF.Solvers.SingleAgentPlan;
import BasicMAPF.Solvers.Solution;

import java.util.*;

/**
 * A solution to a lifelong problem.
 * <p>
 * Contains the solutions that were returned by the solver at different times. Also contains (at {@link Solution}) a merged
 * solution, representing just the paths that agents ended up following.
 */
public class LifelongSolution extends Solution{

    public final SortedMap<Integer, Solution> solutionsAtTimes;
    public final SortedMap<LifelongAgent, List<Integer>> agentsWaypointArrivalTimes;
    private final List<LifelongAgent> agents;

    public LifelongSolution(SortedMap<Integer, Solution> solutionsAtTimes, List<LifelongAgent> agents, Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes) {
        // make unified solution for super
        super(mergeSolutions(solutionsAtTimes, agents, getAgentsWaypointArrivalTimes(agentsActiveDestinationEndTimes)));
        this.agents = agents;
        this.agentsWaypointArrivalTimes = getAgentsWaypointArrivalTimes(agentsActiveDestinationEndTimes);
        this.solutionsAtTimes = solutionsAtTimes;
    }

    /**
     * Merge the solutions that the solver produced at different times into one solution, which represents the paths that
     * the agents actually ended up taking.
     *
     * @param solutionsAtTimes                solutions that the solver produced at different times
     * @param agents                          {@link LifelongAgent lifelong agents} to use for the merged solution's plans
     * @param agentsWaypointArrivalTimes
     * @return a merged solution
     */
    private static Map<Agent, SingleAgentPlan> mergeSolutions(SortedMap<Integer, Solution> solutionsAtTimes,
                                                              List<LifelongAgent> agents, SortedMap<LifelongAgent, List<Integer>> agentsWaypointArrivalTimes) {
        Map<Agent, SingleAgentPlan> mergedAgentPlans = new HashMap<>();
        for (LifelongAgent agent : agents){
            SingleAgentPlan mergedPlanUpToTime = new SingleAgentPlan(agent);
            int lastPlanFirstMoveTime = solutionsAtTimes.lastKey() + 1;
            for (Solution solution : solutionsAtTimes.values()) { // sorted by time
                SingleAgentPlan timelyPlan = solution.getPlanFor(agent);
                List<Move> mergedMovesIncludingTime = mergePlans(mergedPlanUpToTime, timelyPlan, agent, lastPlanFirstMoveTime);
                mergedPlanUpToTime = new SingleAgentPlan(agent, mergedMovesIncludingTime); // now includes current iteration time
            }

            Integer[] waypointSegmentsEndTimes = agentsWaypointArrivalTimes.get(agent).toArray(Integer[]::new);

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
     * @param upTo max time step to include in the result. Must be >= first move time of the new plan
     * @return a merged plan.
     */
    private static List<Move> mergePlans(SingleAgentPlan mergedPlanUpToTime, SingleAgentPlan newPlan, LifelongAgent agent, int upTo){
        if (upTo < newPlan.getFirstMoveTime()){
            throw new IllegalArgumentException(String.format("upTo %d is smaller than the first move time %d of the new plan %s", upTo, newPlan.getFirstMoveTime(), newPlan));
        }

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
            if (move.timeNow > upTo){
                break;
            }
            mergedMoves.add(move);
        }
        // remove excess stays at goal if they exist. May exist after reaching last destination
        Move lastMove = mergedMoves.get(mergedMoves.size() - 1);
        while (mergedMoves.size() > 1 && lastMove.prevLocation.equals(lastMove.currLocation)){
            mergedMoves.remove(mergedMoves.size() - 1);
            lastMove = mergedMoves.get(mergedMoves.size() - 1);
        }

        return mergedMoves;
    }

    private static SortedMap<LifelongAgent, List<Integer>> getAgentsWaypointArrivalTimes(Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes){
        SortedMap<LifelongAgent, List<Integer>> agentsWaypointArrivalTimes = new TreeMap<>();
        for (LifelongAgent lifelongAgent :
                agentsActiveDestinationEndTimes.keySet()) {
            List<Integer> asTimesList = new ArrayList<>();
            List<TimeCoordinate> destinationEndTimes = agentsActiveDestinationEndTimes.get(lifelongAgent);
            for (int i = 0; i < destinationEndTimes.size(); i++) { // TODO skip the first one (source)?
                TimeCoordinate tc = destinationEndTimes.get(i);
                if (! tc.coordinate.equals(lifelongAgent.waypoints.get(i))){
                    throw new IllegalArgumentException("Destination end times list at index " + i + " has coordinate "
                            + tc.coordinate + " instead of " + lifelongAgent.waypoints.get(i));
                }
                asTimesList.add(tc.time);
            }
            agentsWaypointArrivalTimes.put(lifelongAgent, asTimesList);
        }
        return Collections.unmodifiableSortedMap(agentsWaypointArrivalTimes);
    }

    public String agentsWaypointArrivalTimes(){
        StringBuilder sb = new StringBuilder();
        for (LifelongAgent agent: this.agentsWaypointArrivalTimes.keySet()){
            sb.append("a");
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

    private int totalNumTaskInInstance() {
        int res = 0;
        for (LifelongAgent agent: this.agents){
            List<I_Coordinate> tasksIncludingSource = agent.waypoints;
            res += tasksIncludingSource.size();
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
     * @param x - float between 0 and 1 of the percent of completed tasks for the metric.
     * @return the time when the required percent of all tasks or higher was completed.
     */
    public int timeToXProportionCompletion(double x){
        return timeToXCompletion((int)(((double) totalNumTaskInInstance()) * x));
    }

    /**
     * @param x - number of completed tasks
     * @return the time when the required number of tasks or higher was completed, or -1 if completed fewer tasks than x.
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
        return -1; // not enough tasks completed
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

    @Override
    protected boolean checkStartAndEnd(MAPF_Instance instance, SingleAgentPlan plan) {
        // lifelong solution doesn't have to be complete (finish all destinations) to be valid - only check start, not end
        return !plan.moveAt(plan.getFirstMoveTime()).prevLocation.equals(instance.map.getMapLocation(plan.agent.source));
    }

}
