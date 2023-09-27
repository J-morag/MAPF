package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.OpenListTree;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.I_OpenList;
import LifelongMAPF.FailPolicies.I_SingleAgentFailPolicy;
import LifelongMAPF.FailPolicies.StayFailPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GoASFP implements I_AStarFailPolicy, I_SingleAgentFailPolicy {

    private static final double EPSILON = 0.0001;
    protected final int d;
    private int runningGoStatesID;
    private static final Comparator<GoState> statesCostComparator = Comparator.comparingInt(GoState::getCost);

    public GoASFP(int d) {
        this.d = d;
    }

    @Override
    public SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                       @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> aStarOpenList,
                                       @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList, @NotNull SingleAgentPlan existingPlan,
                                       @Nullable CongestionMap congestionMap, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        return getGoFailPlan(farthestCommittedTime, a, agentLocation, conflictAvoidanceTable);
    }

    @Override
    public @NotNull SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, @Nullable I_ConflictAvoidanceTable softConstraints) {
        return getGoFailPlan(farthestCommittedTime, a, agentLocation, softConstraints);
    }

    @NotNull
    private SingleAgentPlan getGoFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation, @NotNull I_ConflictAvoidanceTable conflictAvoidanceTable) {
        // BFS: edge cost 1 if increases Manhattan distance, d+1 if stays the same, (d+1)^2 if decreases
        // goal: any leaf (depth d)

        runningGoStatesID = 0;
        I_OpenList<GoState> GoStateOpen = new OpenListTree<>(GoState::compareTo);
        GoStateOpen.add(new GoState(0, 0, agentLocation, farthestCommittedTime, null, null, getH(0)));
        Set<GoState> closed = new HashSet<>();
        while (!GoStateOpen.isEmpty()){
            GoState curr = GoStateOpen.poll();
            closed.add(curr);
            if (curr.depth == d){
                return planFromGoal(curr, a);
            }
            else {
                // expand
                List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(curr.location.outgoingEdges());
                neighborLocationsIncludingCurrent.add(curr.location);
                for (I_Location neighbor : neighborLocationsIncludingCurrent) {
                    int newTime = curr.time + 1;
                    Move move = new Move(a, newTime, curr.location, neighbor);
                    if (conflictAvoidanceTable.numConflicts(move, false) == 0){
                        // generate
                        double currDistanceFromSource = curr.location.getCoordinate().distance(agentLocation.getCoordinate());
                        double neighborDistanceFromSource = neighbor.getCoordinate().distance(agentLocation.getCoordinate());
                        double edgeDistanceFromSourceDelta = Math.abs(neighborDistanceFromSource - currDistanceFromSource) > EPSILON ?
                                neighborDistanceFromSource - currDistanceFromSource : 0;
                        int cost = getCost(curr, edgeDistanceFromSourceDelta);
                        int newDepth = curr.depth + 1;
                        int h = getH(newDepth);
                        GoState childState = new GoState(newDepth, curr.cost + cost, neighbor, newTime, curr, move, h);

                        if ( ! closed.contains(childState)){
                            GoState existingState;
                            if(null != (existingState = GoStateOpen.get(childState)) ){
                                //keep the one with min G
                                GoStateOpen.keepOne(childState, existingState, statesCostComparator);
                            }
                            else{ // it's a new state
                                GoStateOpen.add(childState);
                            }
                        }
                    }
                }
            }
        }
        return StayFailPolicy.getStayOncePlan(farthestCommittedTime, a, agentLocation, conflictAvoidanceTable);
    }

    protected int getCost(GoState curr, double edgeDistanceFromSourceDelta) {
        return curr.cost + (edgeDistanceFromSourceDelta < 0 ? (d + 1) * (d + 1) : (edgeDistanceFromSourceDelta == 0 ? d + 1 : 1));
    }

    private int getH(int depth) {
        return d - depth;
    }

    private SingleAgentPlan planFromGoal(GoState lastState, Agent a) {
        List<Move> moves = new ArrayList<>();
        for (GoState state = lastState; state.parent != null; state = state.parent) {
            moves.add(state.move);
        }
        Collections.reverse(moves);
        return new SingleAgentPlan(a, moves);
    }

    protected class GoState implements Comparable<GoState>{

        private final int id = runningGoStatesID++;
        public final int depth;
        public final int cost;
        public final I_Location location;
        public final int time;
        public final GoState parent;
        /**
         * the move that got us to this state from the parent state
         */
        public final Move move;
        public final int h;

        public GoState(int depth, int cost, I_Location location, int time, GoState parent, Move move, int h) {
            this.depth = depth;
            this.cost = cost;
            this.location = location;
            this.time = time;
            this.parent = parent;
            this.move = move;
            this.h = h;
        }

        public int getCost() {
            return cost;
        }

        @Override
        public int compareTo(@NotNull GoASFP.GoState o) {
            int res = Integer.compare(cost + h, o.cost + o.h);
            if (res == 0)
                // reversed to prefer higher cost under same f
                res = Integer.compare(o.cost, cost);
            if (res == 0)
                res = Integer.compare(id, o.id);
            return res;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GoState state)) return false;

            if (depth != state.depth) return false;
            if (time != state.time) return false;
            return location.equals(state.location);
        }

        @Override
        public int hashCode() {
            int result = depth;
            result = 31 * result + location.hashCode();
            result = 31 * result + time;
            return result;
        }
    }
}
