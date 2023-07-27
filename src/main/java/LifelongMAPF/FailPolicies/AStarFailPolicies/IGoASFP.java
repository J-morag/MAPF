package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.OpenListTree;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.I_OpenList;
import LifelongMAPF.FailPolicies.StayOnceFailPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IGoASFP implements I_AStarFailPolicy{

    private static final double EPSILON = 0.0001;
    private final int d;
    private int runningIGoStatesID;
    private static final Comparator<IGoState> statesCostComparator = Comparator.comparingInt(IGoState::getCost);

    public IGoASFP(int d) {
        this.d = d;
    }

    @Override
    public SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                       @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> aStarOpenList,
                                       @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList, @NotNull SingleAgentPlan existingPlan,
                                       @Nullable CongestionMap congestionMap, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        // BFS: edge cost 1 if increases Manhattan distance, d+1 if stays the same, (d+1)^2 if decreases
        // goal: any leaf (depth d)

        runningIGoStatesID = 0;
        I_OpenList<IGoState> iGoStateOpen = new OpenListTree<>(IGoState::compareTo);
        iGoStateOpen.add(new IGoState(0, 0, agentLocation, farthestCommittedTime, null, null, getH(0)));
        Set<IGoState> closed = new HashSet<>();
        while (!iGoStateOpen.isEmpty()){
            IGoState curr = iGoStateOpen.poll();
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
                    if (conflictAvoidanceTable.firstConflictTime(move, false) == -1){
                        // generate
                        double currDistanceFromSource = curr.location.getCoordinate().distance(agentLocation.getCoordinate());
                        double neighborDistanceFromSource = neighbor.getCoordinate().distance(agentLocation.getCoordinate());
                        double edgeDistanceFromSourceDelta = Math.abs(neighborDistanceFromSource - currDistanceFromSource) > EPSILON ?
                                neighborDistanceFromSource - currDistanceFromSource : 0;
                        int cost = curr.cost + (edgeDistanceFromSourceDelta < 0 ? (d+1)*(d+1) : (edgeDistanceFromSourceDelta == 0 ? d+1 : 1));
                        int newDepth = curr.depth + 1;
                        int h = getH(newDepth);
                        IGoState childState = new IGoState(newDepth, curr.cost + cost, neighbor, newTime, curr, move, h);

                        if ( ! closed.contains(childState)){
                            IGoState existingState;
                            if(null != (existingState = iGoStateOpen.get(childState)) ){
                                //keep the one with min G
                                iGoStateOpen.keepOne(childState, existingState, statesCostComparator);
                            }
                            else{ // it's a new state
                                iGoStateOpen.add(childState);
                            }
                        }
                    }
                }
            }
        }
        return StayOnceFailPolicy.getStayOncePlan(farthestCommittedTime, a, agentLocation, conflictAvoidanceTable);
    }

    private int getH(int depth) {
        return d - depth;
    }

    private SingleAgentPlan planFromGoal(IGoState lastState, Agent a) {
        List<Move> moves = new ArrayList<>();
        for (IGoState state = lastState; state.parent != null; state = state.parent) {
            moves.add(state.move);
        }
        Collections.reverse(moves);
        return new SingleAgentPlan(a, moves);
    }

    private class IGoState implements Comparable<IGoState>{

        private final int id = runningIGoStatesID++;
        public final int depth;
        public final int cost;
        public final I_Location location;
        public final int time;
        public final IGoState parent;
        /**
         * the move that got us to this state from the parent state
         */
        public final Move move;
        public final int h;

        public IGoState(int depth, int cost, I_Location location, int time, IGoState parent, Move move, int h) {
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
        public int compareTo(@NotNull IGoASFP.IGoState o) {
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
            if (!(o instanceof IGoState state)) return false;

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
