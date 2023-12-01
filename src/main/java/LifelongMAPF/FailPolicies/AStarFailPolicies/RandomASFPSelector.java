package LifelongMAPF.FailPolicies.AStarFailPolicies;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.DataTypesAndStructures.I_OpenList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class RandomASFPSelector implements I_AStarFailPolicy {

    private final I_AStarFailPolicy[] policies;
    private final Random random;

    public RandomASFPSelector(@NotNull I_AStarFailPolicy[] policies, @Nullable Random random) {
        this.policies = policies;
        this.random = Objects.requireNonNullElseGet(random, Random::new);
    }

    @Override
    public SingleAgentPlan getFailPlan(int farthestCommittedTime, @NotNull Agent a, @NotNull I_Location agentLocation, @NotNull I_OpenList<SingleAgentAStar_Solver.AStarState> openList, @NotNull Set<SingleAgentAStar_Solver.AStarState> ClosedList, @NotNull SingleAgentPlan existingPlan, @Nullable CongestionMap congestionMap, @NotNull RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable) {
        return policies[random.nextInt(policies.length)].getFailPlan(farthestCommittedTime, a, agentLocation, openList, ClosedList, existingPlan, congestionMap, conflictAvoidanceTable);
    }

}
