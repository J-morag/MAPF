package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ReplanSingleGrouped extends OnlineCBSSolver {

    @Override
    public Solution newArrivals(int time, List<? extends OnlineAgent> agents) {
        HashMap<Agent, I_Location> currentAgentLocations = new HashMap<>(agents.size());
        // new agents will start at their private garages.
        addNewAgents(agents, currentAgentLocations);

        return solveForNewArrivals(time, currentAgentLocations);
    }

    @Override
    protected Solution solveForNewArrivals(int time, HashMap<Agent, I_Location> currentAgentLocations) {
        // contains only the new agents
        OnlineAStar onlineAStar = new OnlineAStar(costOfReroute);
        // reduce the problem to just the new agents
        MAPF_Instance subProblem = baseInstance.getSubproblemFor(currentAgentLocations.keySet());
        // protect existing agents with constraints
        ConstraintSet constraints = new ConstraintSet(allConstraintsForSolution(latestSolution));
        OnlineCompatibleOfflineCBS offlineSolver = new OnlineCompatibleOfflineCBS(currentAgentLocations, time, null, onlineAStar, true);
        RunParameters runParameters = new RunParameters(timeoutThreshold - totalRuntime, constraints,
                S_Metrics.newInstanceReport(), null);
        Solution solutionForNewAgents = offlineSolver.solve(subProblem, runParameters);
        // add the remaining parts of the plans of existing agents
        for(SingleAgentPlan existingAgentPlan : latestSolution){
            if(existingAgentPlan.getEndTime() >= time){
                SingleAgentPlan trimmedPlan = new SingleAgentPlan(existingAgentPlan.agent);
                for (int t = time; t <= existingAgentPlan.getEndTime(); t++) {
                    trimmedPlan.addMove(existingAgentPlan.moveAt(t));
                }
                solutionForNewAgents.putPlan(trimmedPlan);
            }
        }
        latestSolution = solutionForNewAgents;
        digestSubproblemReport(runParameters.instanceReport);
        return latestSolution;
    }

    protected List<Constraint> allConstraintsForSolution(Solution solution) {
        List<Constraint> constraints = new LinkedList<>();
        for( SingleAgentPlan plan : solution){
            // protect the agent's plan
            for (Move move :
                    plan) {
                constraints.add(move.vertexConstraintsForMove(null));
                constraints.add(move.swappingConstraintsForMove(null));
            }
        }
        return constraints;
    }

    @Override
    public String name() {
        return "Replan Single Grouped";
    }
}
