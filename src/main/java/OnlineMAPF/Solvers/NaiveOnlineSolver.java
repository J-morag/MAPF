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

import java.util.HashMap;
import java.util.List;

/**
 * Solves online problems naively, by delegating to a standard offline solver, and solving a brand new offline problem
 * every time new agents arrive.
 */
public class NaiveOnlineSolver implements I_OnlineSolver {

//    /**
//     * An offline solver used to solve the new problem created every time new agents join. This offline solver must be
//     * compatible with online problems - i.e. agents disappearing at goal and starting at private garages.
//     */
//    private OnlineCompatibleOfflineCBS offlineSolver;

    private Solution latestSolution;
    private MAPF_Instance baseInstance;

    private long totalRuntime;

    public NaiveOnlineSolver() {
    }

    @Override
    public void setEnvironment(MAPF_Instance instance, RunParameters parameters) {
        latestSolution = new Solution();
        this.baseInstance = instance;
        totalRuntime = 0;
    }

    @Override
    public Solution newArrivals(int time, List<? extends OnlineAgent> agents) {
        HashMap<Agent, I_Location> currentAgentLocations = new HashMap<>(agents.size());
        // existing agents will start where the current solution had them at time
        addExistingAgents(time, currentAgentLocations);
        // new agents will start at their private garages.
        addNewAgents(agents, currentAgentLocations);

        OnlineCompatibleOfflineCBS offlineSolver = new OnlineCompatibleOfflineCBS(currentAgentLocations, time);
        MAPF_Instance subProblem = baseInstance.getSubproblemFor(currentAgentLocations.keySet());
        RunParameters runParameters = new RunParameters(S_Metrics.newInstanceReport());
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
    public void writeReportAndClearData() {
        //todo implement
        this.latestSolution = null;
        this.baseInstance = null;
    }
}
