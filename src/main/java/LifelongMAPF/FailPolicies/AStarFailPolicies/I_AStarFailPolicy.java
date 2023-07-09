package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.I_OpenList;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface I_AStarFailPolicy {

    SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation,
                                @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList,
                                @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList,
                                @NotNull SingleAgentPlan existingPlan, @Nullable CongestionMap congestionMap,
                                @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable);

}
