package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import Environment.Metrics.InstanceReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PCSHeuristicSIPP implements I_PCSHeuristic {
    private final I_Solver singleAgentSolver = new SingleAgentAStarSIPP_Solver();
    @Override
    public int @Nullable [] getH(Agent[] priorityOrderedAgents, int numMDDsAlreadyInNode, @NotNull I_ConstraintSet constraints,
                                 MAPF_Instance currentInstance, SingleAgentGAndH singleAgentHeuristic) {
        int[] res = new int[priorityOrderedAgents.length - numMDDsAlreadyInNode];
        for (int i = numMDDsAlreadyInNode; i < priorityOrderedAgents.length; i++) {
            Agent agent = priorityOrderedAgents[i];
//            long timeLeftToTimeout = Math.max(super.maximumRuntime - (Timeout.getCurrentTimeMS_NSAccuracy() - super.startTime), 0);
            // todo timeout?
            Solution solution = singleAgentSolver.solve(currentInstance.getSubproblemFor(agent),
                    new RunParametersBuilder().setAStarGAndH(singleAgentHeuristic).setInstanceReport(new InstanceReport())
                            .setConstraints(constraints).createRP());
            if (solution == null) {
                return null;
            }
            else {
                int shortestPathLength = solution.getPlanFor(agent).getCost();
                res[i - numMDDsAlreadyInNode] = shortestPathLength;
            }
        }
        return res;
    }
}
