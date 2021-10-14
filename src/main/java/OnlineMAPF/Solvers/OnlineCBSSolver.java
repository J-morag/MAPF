package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.S_Metrics;

import java.util.HashMap;

/**
 * Solves online problems naively, by delegating to a standard offline solver, and solving a brand new offline problem
 * every time new agents arrive.
 */
public class OnlineCBSSolver extends A_OnlineSolver {

    protected boolean useCorridorReasoning = false;

    public OnlineCBSSolver() {
        super();
        super.name = "Restart-CBS";
    }

    public OnlineCBSSolver(boolean useCorridorReasoning) {
        super();
        this.useCorridorReasoning = useCorridorReasoning;
        super.name = "Restart-CBS";
    }

    protected Solution solveForNewArrivals(int time, HashMap<Agent, I_Location> currentAgentLocations) {
//        OnlineAStar onlineAStar = preserveSolutionsInNewRoots ? new OnlineAStar(this.costOfReroute, latestSolution) : new OnlineAStar(costOfReroute);
        OnlineAStar onlineAStar = new OnlineAStar(costOfReroute);
        OnlineCompatibleOfflineCBS offlineSolver = new OnlineCompatibleOfflineCBS(currentAgentLocations, time,
                new COR_CBS_CostFunction(this.costOfReroute, latestSolution), onlineAStar, useCorridorReasoning);
        MAPF_Instance subProblem = baseInstance.getSubproblemFor(currentAgentLocations.keySet());
//        Solution previousSolution = preserveSolutionsInNewRoots ? new Solution(latestSolution) : null;
        Solution previousSolution = null;
        RunParameters runParameters = new RunParameters(timeoutThreshold - totalRuntime, null, S_Metrics.newInstanceReport(), previousSolution);
        latestSolution = offlineSolver.solve(subProblem, runParameters);
        digestSubproblemReport(runParameters.instanceReport);
        return latestSolution;
    }

}
