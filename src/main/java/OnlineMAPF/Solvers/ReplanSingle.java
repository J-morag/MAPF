package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineSolution;

import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;

public class ReplanSingle extends OnlinePP_Solver {

    public ReplanSingle(I_Solver lowLevelSolver) {
        super(lowLevelSolver);
    }

    @Override
    protected void solveAtTimeStep(MAPF_Instance instance, ConstraintSet constraints, SortedMap<Integer, Solution> solutionsAtTimes,
                                   SortedMap<Integer, List<OnlineAgent>> agentsForTimes, int timestepWithNewAgents) {
        // solve the initial set of agents with CBS
        if(timestepWithNewAgents == 0){
            OnlineCompatibleOfflineCBS cbs = new OnlineCompatibleOfflineCBS(null, 0, null, new OnlineAStar());
            MAPF_Instance subproblem = instance.getSubproblemFor(agentsForTimes.get(timestepWithNewAgents));
            InstanceReport instanceReport = new InstanceReport();
            Solution initialSolution = cbs.solve(subproblem, super.getSubproblemParameters(subproblem, instanceReport, super.constraints));
            solutionsAtTimes.put(0, initialSolution);
            for(SingleAgentPlan plan : initialSolution){
                constraints.addAll(super.allConstraintsForPlan(plan));
            }
            digestSubproblemReport(instanceReport);
        }
        //solve the rest like prioritised planning
        else {
            super.solveAtTimeStep(instance, constraints, solutionsAtTimes, agentsForTimes, timestepWithNewAgents);
        }
    }

    @Override
    public String name() {
        return "Replan Single";
    }
}
