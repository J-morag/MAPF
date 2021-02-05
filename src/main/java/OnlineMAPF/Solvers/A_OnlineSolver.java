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
public abstract class A_OnlineSolver implements I_OnlineSolver {

    public String name = "BasicOnlineSolver";

    protected Solution latestSolution;
    protected MAPF_Instance baseInstance;
    private InstanceReport instanceReport;
    public boolean ignoreCOR = false;
    protected int costOfReroute = 0;
    protected long timeoutThreshold;
//    /**
//     * If set to true, will start every new search with the plans from the previous solution as its root
//     */
//    final protected boolean preserveSolutionsInNewRoots = false;
    protected long totalRuntime;

    @Override
    public void setEnvironment(MAPF_Instance instance, RunParameters parameters) {
        latestSolution = new Solution();
        this.baseInstance = instance;
        totalRuntime = 0;
        timeoutThreshold = parameters.timeout;
        this.instanceReport = parameters.instanceReport != null ? parameters.instanceReport : S_Metrics.newInstanceReport();
        if(parameters instanceof RunParametersOnline && !this.ignoreCOR){
            this.costOfReroute = ((RunParametersOnline)parameters).costOfReroute;
            if (this.costOfReroute < 0) throw new IllegalArgumentException("cost of reroute must be non negative");
        }
        else{
            this.costOfReroute = 0;
        }
    }

    @Override
    public Solution newArrivals(int time, List<? extends OnlineAgent> agents) {
        HashMap<Agent, I_Location> currentAgentLocations = new HashMap<>(agents.size());
        // existing agents will start where the current solution had them at time
        addExistingAgents(time, currentAgentLocations);
        // new agents will start at their private garages.
        addNewAgents(agents, currentAgentLocations);

        return solveForNewArrivals(time, currentAgentLocations);
    }

    protected abstract Solution solveForNewArrivals(int time, HashMap<Agent, I_Location> currentAgentLocations);

    protected void addExistingAgents(int time, HashMap<Agent, I_Location> currentAgentLocations) {
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

    protected void addNewAgents(List<? extends OnlineAgent> agents, HashMap<Agent, I_Location> currentAgentLocations) {
        for (OnlineAgent agent :
                agents) {
            currentAgentLocations.put(agent, agent.getPrivateGarage(baseInstance.map.getMapCell(agent.source)));
        }
    }

    protected void digestSubproblemReport(InstanceReport instanceReport) {
        Integer statesGenerated = instanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel);
        this.instanceReport.integerAddition(InstanceReport.StandardFields.totalLowLevelTimeMS, statesGenerated==null ? 0 : statesGenerated);
        Integer statesExpanded = instanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
        this.instanceReport.integerAddition(InstanceReport.StandardFields.totalLowLevelTimeMS, statesExpanded==null ? 0 : statesExpanded);
        Integer lowLevelRuntime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        this.instanceReport.integerAddition(InstanceReport.StandardFields.totalLowLevelTimeMS, lowLevelRuntime);
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
