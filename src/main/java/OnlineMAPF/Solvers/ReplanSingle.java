package OnlineMAPF.Solvers;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import OnlineMAPF.OnlineAgent;

import java.util.List;
import java.util.SortedMap;

public class ReplanSingle extends OnlinePP_Solver {

    public ReplanSingle(I_Solver lowLevelSolver) {
        super(lowLevelSolver, null);
    }

    @Override
    protected Solution solveAtTimeStep(MAPF_Instance instance, ConstraintSet constraints, SortedMap<Integer, Solution> solutionsAtTimes,
                                   SortedMap<Integer, List<OnlineAgent>> agentsForTimes, int timestepWithNewAgents) {
        // solve the initial set of agents with CBS
        if(timestepWithNewAgents == 0){
            OnlineCompatibleOfflineCBS cbs = new OnlineCompatibleOfflineCBS(null, 0, null, new OnlineAStar(), true);
            MAPF_Instance subproblem = instance.getSubproblemFor(agentsForTimes.get(timestepWithNewAgents));
            InstanceReport instanceReport = new InstanceReport();
            Solution initialSolution = cbs.solve(subproblem, super.getSubproblemParameters(subproblem, instanceReport, super.constraints));
            solutionsAtTimes.put(0, initialSolution);
            for(SingleAgentPlan plan : initialSolution){
                constraints.addAll(super.allConstraintsForPlan(plan));
            }
            digestSubproblemReport(instanceReport);
            return initialSolution;
        }
        //solve the rest like prioritised planning
        else {
            return super.solveAtTimeStep(instance, constraints, solutionsAtTimes, agentsForTimes, timestepWithNewAgents);
        }
    }

    @Override
    public String name() {
        return "Replan Single";
    }
}
