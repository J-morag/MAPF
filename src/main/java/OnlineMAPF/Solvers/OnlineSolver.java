package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineSolution;
import OnlineMAPF.RunParametersOnline;

import java.util.HashMap;
import java.util.List;

/**
 * Solves online problems naively, by delegating to a standard offline solver, and solving a brand new offline problem
 * every time new agents arrive.
 */
public class OnlineSolver implements I_OnlineSolver {

    public String name = "OnlineSolver";

    private Solution latestSolution;
    private MAPF_Instance baseInstance;
    private InstanceReport instanceReport;
    private int costOfReroute = 0;
    private long timeoutThreshold;
    /**
     * If set to true, will start every new CBS with the plans from the previous solution as its root
     */
    private boolean preserveSolutionsInNewRoots = true;

    private long totalRuntime;

    public OnlineSolver() {
    }

    public OnlineSolver(boolean preserveSolutionsInNewRoots) {
        this.preserveSolutionsInNewRoots = preserveSolutionsInNewRoots;
    }

    @Override
    public void setEnvironment(MAPF_Instance instance, RunParameters parameters) {
        latestSolution = new Solution();
        this.baseInstance = instance;
        totalRuntime = 0;
        timeoutThreshold = parameters.timeout;
        this.instanceReport = parameters.instanceReport != null ? parameters.instanceReport : S_Metrics.newInstanceReport();
        if(parameters instanceof RunParametersOnline){
            this.costOfReroute = ((RunParametersOnline)parameters).costOfReroute;
            if (this.costOfReroute < 0) throw new IllegalArgumentException("cost of reroute must be non negative");
        }
    }

    @Override
    public Solution newArrivals(int time, List<? extends OnlineAgent> agents) {
        HashMap<Agent, I_Location> currentAgentLocations = new HashMap<>(agents.size());
        // existing agents will start where the current solution had them at time
        addExistingAgents(time, currentAgentLocations);
        // new agents will start at their private garages.
        addNewAgents(agents, currentAgentLocations);

        OnlineAStar onlineAStar = preserveSolutionsInNewRoots ? new OnlineAStar(this.costOfReroute, latestSolution) : new OnlineAStar(costOfReroute);
        OnlineCompatibleOfflineCBS offlineSolver = new OnlineCompatibleOfflineCBS(currentAgentLocations, time,
                new COR_CBS_CostFunction(this.costOfReroute, latestSolution), onlineAStar);
        MAPF_Instance subProblem = baseInstance.getSubproblemFor(currentAgentLocations.keySet());
        Solution previousSolution = preserveSolutionsInNewRoots ? new Solution(latestSolution) : null;
        RunParameters runParameters = new RunParameters(timeoutThreshold - totalRuntime, null, S_Metrics.newInstanceReport(), previousSolution);
        latestSolution = offlineSolver.solve(subProblem, runParameters);
        digestSubproblemReport(runParameters.instanceReport);

        return latestSolution;
    }

    private void addExistingAgents(int time, HashMap<Agent, I_Location> currentAgentLocations) {
        for (SingleAgentPlan plan :
                latestSolution) {
            Agent agent = plan.agent;
            // if the agent's plan doesn't already finish before the new agents arrive
            if(plan.getEndTime() > time){
                // existing agents will start where the current solution had them at time
                currentAgentLocations.put(agent, plan.moveAt(time).currLocation);
            }
        }
    }

    private void addNewAgents(List<? extends OnlineAgent> agents, HashMap<Agent, I_Location> currentAgentLocations) {
        for (OnlineAgent agent :
                agents) {
            currentAgentLocations.put(agent, agent.getPrivateGarage(baseInstance.map.getMapCell(agent.source)));
        }
    }

    private void digestSubproblemReport(InstanceReport instanceReport) {
        // todo implement
        this.totalRuntime += instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);

        S_Metrics.removeReport(instanceReport);
    }

    @Override
    public void writeReportAndClearData(OnlineSolution solution) {
        instanceReport.putIntegerValue(InstanceReport.StandardFields.elapsedTimeMS, (int)totalRuntime);
        instanceReport.putStringValue(InstanceReport.StandardFields.solver, this.name());
        instanceReport.putIntegerValue(InstanceReport.StandardFields.COR, costOfReroute);

        if(solution != null){
            instanceReport.putIntegerValue(InstanceReport.StandardFields.numReroutes, solution.numReroutes());
            instanceReport.putIntegerValue(InstanceReport.StandardFields.totalReroutesCost, solution.costOfReroutes(costOfReroute));
        }

//        instanceReport.putIntegerValue(InstanceReport.StandardFields.timeoutThresholdMS, (int) this.maximumRuntime);
//        instanceReport.putStringValue(InstanceReport.StandardFields.startDateTime, new Date(startTime).toString());
//        instanceReport.putIntegerValue(InstanceReport.StandardFields.elapsedTimeMS, (int)(endTime-startTime));

//        instanceReport.putIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel, this.totalLowLevelStatesGenerated);
//        instanceReport.putIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel, this.totalLowLevelStatesExpanded);


        this.totalRuntime = 0;
        this.latestSolution = null;
        this.baseInstance = null;
        this.costOfReroute = 0;
    }

    @Override
    public String name() {
        return name;
    }
}
