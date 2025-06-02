package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.RunParameters_SAAStarSIPP;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.DataTypesAndStructures.TimeInterval;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Metrics.InstanceReport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class PaPSHeuristicSIPP implements I_PaPSHeuristic {
    private final SingleAgentAStarSIPP_Solver sippSolver = new SingleAgentAStarSIPP_Solver();
    @Override
    public int @Nullable [] getH(Agent[] priorityOrderedAgents, int numMDDsAlreadyInNode, @NotNull I_ConstraintSet constraints,
                                 MAPF_Instance currentInstance, SingleAgentGAndH singleAgentHeuristic,
                                 @Nullable Map<I_Location, List<TimeInterval>> safeIntervalsByLocation) {
        int[] res = new int[priorityOrderedAgents.length - numMDDsAlreadyInNode];
        for (int i = numMDDsAlreadyInNode; i < priorityOrderedAgents.length; i++) {
            Agent agent = priorityOrderedAgents[i];
//            long timeLeftToTimeout = Math.max(super.maximumRuntime - (Timeout.getCurrentTimeMS_NSAccuracy() - super.startTime), 0);
            // todo timeout?
            RunParameters_SAAStarSIPP parameters = new RunParameters_SAAStarSIPP(new RunParametersBuilder().setAStarGAndH(singleAgentHeuristic).setInstanceReport(new InstanceReport())
                    .setConstraints(constraints).createRP());
            parameters.safeIntervalsByLocation = safeIntervalsByLocation;
            Solution solution = sippSolver.solve(currentInstance.getSubproblemFor(agent), parameters);
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
