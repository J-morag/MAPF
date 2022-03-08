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
 *
 * Contains the solutions that were returned by the solver at different times. Also contains (at {@link Solution}) a merged
 * solution, representing just the paths that agents ended up following.
 */
public class LifelongSolution extends Solution{

    public final SortedMap<Integer, Solution> solutionsAtTimes;

    public LifelongSolution(SortedMap<Integer, Solution> solutionsAtTimes, List<LifelongAgent> agents) {
        //make unified solution for super
        super(mergeSolutions(solutionsAtTimes, agents));
        this.solutionsAtTimes = solutionsAtTimes;
    }

    private static SortedMap<Integer, Solution> putOfflineSolutionInMap(Solution offlineSolution){
        SortedMap<Integer, Solution> solutionsAtTimes = new TreeMap<>();
        solutionsAtTimes.put(0, offlineSolution);
        return solutionsAtTimes;
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
            Integer[] waypointTimes = new Integer[agent.waypoints.size()];
            waypointTimes[0] = 0; // first waypoint is start location
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
                    waypointTimes[waypointIndex] = timelyPlan.getPlanStartTime();

                    // hypothesis about old waypoint is proven; add hypothesis about the new waypoint
                    waypointIndex++;
                }
                // update hypothesis about time of achieving waypoint (can be same value)
                waypointTimes[waypointIndex] = newWaypointTimeHypothesis;
                // for next iteration
                mergedPlanUpToTime = new SingleAgentPlan(agent, mergedMovesIncludingTime); // now includes current iteration time
            }
            LifelongSingleAgentPlan mergedPlanIncludingTime = new LifelongSingleAgentPlan(mergedPlanUpToTime, waypointTimes);
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

    @Override
    public boolean solves(MAPF_Instance instance, boolean sharedGoals, boolean sharedSources) {
        boolean offlineSolves = super.solves(instance, sharedGoals, sharedSources);
        if (!offlineSolves){
            super.solves(instance, sharedGoals, sharedSources);
            return false;
        }
        for (Agent agent : instance.agents){
            if (!(agent instanceof  LifelongAgent)){
                throw new IllegalArgumentException("a LifelongSolution is only for lifelong instances.");
            }
            else {
                // verify passes through all waypoints in order
                LifelongAgent lifelongAgent = ((LifelongAgent) agent);
                SingleAgentPlan plan = this.getPlanFor(agent);
                if (!plan.getFirstMove().prevLocation.getCoordinate().equals(((LifelongAgent) agent).waypoints.get(0))){
                    // verify first waypoint which is also the source
                    return false;
                }
                int prevWaypointTime = 0;
                for (int i = 1 ; i < lifelongAgent.waypoints.size() ; i++){
                    I_Coordinate waypoint = lifelongAgent.waypoints.get(i);
                    boolean waypointAchieved = false;
                    for (int t = prevWaypointTime + 1; t <= plan.getEndTime(); t++) {
                        if (plan.moveAt(t).currLocation.getCoordinate().equals(waypoint)){
                            prevWaypointTime = t;
                            waypointAchieved = true;
                            break;
                        }
                    }
                    if (!waypointAchieved){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
