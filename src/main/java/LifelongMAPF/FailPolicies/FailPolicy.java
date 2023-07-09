package LifelongMAPF.FailPolicies;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import org.apache.commons.lang.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class FailPolicy {

    private final I_SingleAgentFailPolicy SAFailPolicy;
    private final int detectConflictsHorizon;

    public FailPolicy() {
        this(null, null);
    }

    public FailPolicy(Integer detectConflictsHorizon, I_SingleAgentFailPolicy saFailPolicy) {
        this.SAFailPolicy = Objects.requireNonNullElse(saFailPolicy, new StayOnceFailPolicy());
        this.detectConflictsHorizon  = Objects.requireNonNullElse(detectConflictsHorizon, 1);
    }

    public SingleAgentPlan getFailPolicyPlan(int farthestCommittedTime, Agent a, I_Location agentLocation, @Nullable I_ConflictAvoidanceTable softConstraints) {
        return SAFailPolicy.getFailPolicyPlan(farthestCommittedTime, a, agentLocation, softConstraints);
    }

    public Solution getKSafeSolution(Iterable<? extends SingleAgentPlan> solutionThatMayContainConflicts,
                                      int farthestCommittedTime, Set<Agent> failedAgents, @NotNull RemovableConflictAvoidanceTableWithContestedGoals cat,
                                      MutableInt iterations) {
        return getKSafeSolution(detectConflictsHorizon, solutionThatMayContainConflicts, farthestCommittedTime, failedAgents, cat, SAFailPolicy, iterations);
    }

    @NotNull
    public static Solution getKSafeSolution(int detectConflictsHorizon, Iterable<? extends SingleAgentPlan> solutionThatMayContainConflicts,
                                             int farthestCommittedTime, Set<Agent> failedAgents, @NotNull RemovableConflictAvoidanceTableWithContestedGoals cat,
                                             I_SingleAgentFailPolicy SAFailPolicy, MutableInt iterations) {
        if (detectConflictsHorizon < 1){
            throw new RuntimeException("detectConflictsHorizon must be at least 1");
        }
        Set<Agent> mobileAgents = new HashSet<>();
        for (SingleAgentPlan plan :
                solutionThatMayContainConflicts) {
            if (!isStayInPlacePlan(plan)){
                mobileAgents.add(plan.agent);
            }
        }
        Solution solutionWithoutConflicts = new Solution(solutionThatMayContainConflicts);

        if ( ! failedAgents.isEmpty() && SAFailPolicy instanceof AllStayOnceFailPolicy allStayOnceFailPolicy){ // TODO change this so it's not a special case?
            solutionWithoutConflicts = allStayOnceFailPolicy.stopAll(solutionThatMayContainConflicts);
            for (SingleAgentPlan oldPlan : solutionThatMayContainConflicts) {
                failedAgents.add(oldPlan.agent);
                cat.replacePlan(oldPlan, solutionWithoutConflicts.getPlanFor(oldPlan.agent));
            }
        }

        boolean hadConflictsCurrentIteration = true;
        while (hadConflictsCurrentIteration){
            hadConflictsCurrentIteration = false;

            Iterator<Agent> mobileAgentsIterator = mobileAgents.iterator();
            while (mobileAgentsIterator.hasNext()) {
                Agent agent = mobileAgentsIterator.next();
                SingleAgentPlan plan = solutionWithoutConflicts.getPlanFor(agent);
                cat.removePlan(plan);
                for (int t = plan.getFirstMoveTime(); t <= plan.getEndTime() && t <= farthestCommittedTime + detectConflictsHorizon; t++) {
                    int firstConflictTime = cat.firstConflictTime(plan.moveAt(t), t == plan.getEndTime());
                    if (firstConflictTime != -1 && firstConflictTime <= farthestCommittedTime + detectConflictsHorizon) {
                        plan = SAFailPolicy.getFailPolicyPlan(farthestCommittedTime, plan.agent, plan.getFirstMove().prevLocation, cat);

                        solutionWithoutConflicts.putPlan(plan);
                        failedAgents.add(plan.agent);
                        if (isStayInPlacePlan(plan)){
                            mobileAgentsIterator.remove();
                        }

                        iterations.increment();
                        hadConflictsCurrentIteration = true;
                        break;
                    }
                }
                cat.addPlan(plan);
            }
        }

        return solutionWithoutConflicts;
    }

    private static boolean isStayInPlacePlan(SingleAgentPlan plan) {
        return plan.size() == 1 && plan.getFirstMove().prevLocation.equals(plan.getFirstMove().currLocation);
    }
}
