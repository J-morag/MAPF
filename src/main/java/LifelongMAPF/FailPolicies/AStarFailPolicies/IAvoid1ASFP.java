package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.Solvers.SingleAgentPlan;
import LifelongMAPF.FailPolicies.OneActionFailPolicy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class IAvoid1ASFP implements I_AStarFailPolicy {

    OneActionFailPolicy oneActionFailPolicy = new OneActionFailPolicy(true);

    public IAvoid1ASFP() {
    }

    @Override
    public SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                       @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList,
                                       @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList, @NotNull SingleAgentPlan existingPlan,
                                       @Nullable CongestionMap congestionMap,
                                       @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        return oneActionFailPolicy.getFailPolicyPlan(farthestCommittedTime, a, agentLocation, conflictAvoidanceTable);
    }
}
