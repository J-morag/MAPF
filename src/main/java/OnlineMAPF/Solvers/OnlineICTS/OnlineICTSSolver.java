package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.HighLevel.ICT_NodeComparator;
import BasicCBS.Solvers.ICTS.MDDs.I_MDDSearcherFactory;
import BasicCBS.Solvers.ICTS.MergedMDDs.I_MergedMDDCreator;
import BasicCBS.Solvers.ICTS.MergedMDDs.I_MergedMDDSolver;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineSolution;
import OnlineMAPF.Solvers.I_OnlineSolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Solves online problems naively, by delegating to a standard offline solver, and solving a brand new offline problem
 * every time new agents arrive.
 */
public class OnlineICTSSolver implements I_OnlineSolver {

    public String name = "OnlineICTSSolver";

    protected Solution latestSolution;
    protected MAPF_Instance baseInstance;
    private InstanceReport instanceReport;
    public boolean ignoreCOR = false;
    protected int costOfReroute = 0;
    protected long timeoutThreshold;

    protected long totalRuntime;

    private ICT_NodeComparator comparator;
    protected I_MDDSearcherFactory searcherFactory;
    private I_MergedMDDSolver mergedMDDSolver;
    private ICTS_Solver.PruningStrategy pruningStrategy;
    private final I_MergedMDDCreator mergedMDDCreator;

    public OnlineICTSSolver(ICT_NodeComparator comparator, I_MDDSearcherFactory searcherFactory, I_MergedMDDSolver mergedMDDSolver,
                            ICTS_Solver.PruningStrategy pruningStrategy, I_MergedMDDCreator mergedMDDCreator) {
        if (mergedMDDSolver != null && ! (mergedMDDSolver instanceof Online_ID_MergedMDDSolver))
            throw new IllegalArgumentException("Must use an online MergedMDDSolver.");
        if (mergedMDDCreator != null && ! (mergedMDDCreator instanceof OnlineBFS_MergedMDDCreator))
            throw new IllegalArgumentException("Must use an online MergedMDDCreator.");
        this.comparator = comparator;
        this.searcherFactory = searcherFactory;
        // set searcher factory to online
        if (this.searcherFactory != null) this.searcherFactory.setDefaultDisappearAtGoal(true);
        this.mergedMDDSolver = mergedMDDSolver;
        this.pruningStrategy = pruningStrategy;
        this.mergedMDDCreator = mergedMDDCreator;
    }

    public OnlineICTSSolver() {
        this(null, null, null, null, null);
    }

    @Override
    public void setEnvironment(MAPF_Instance instance, RunParameters parameters) {
        latestSolution = new Solution();
        this.baseInstance = instance;
        totalRuntime = 0;
        timeoutThreshold = parameters.timeout;
        this.instanceReport = parameters.instanceReport != null ? parameters.instanceReport : S_Metrics.newInstanceReport();
//        if(parameters instanceof RunParametersOnline && !this.ignoreCOR){
//            this.costOfReroute = ((RunParametersOnline)parameters).costOfReroute;
//            if (this.costOfReroute < 0) throw new IllegalArgumentException("cost of reroute must be non negative");
//        }
//        else{
//            this.costOfReroute = 0;
//        }
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

    protected Solution solveForNewArrivals(int time, HashMap<Agent, I_Location> currentAgentLocations) {
        OnlineCompatibleICTS offlineSolver = new OnlineCompatibleICTS(this.comparator, this.searcherFactory,
                this.mergedMDDSolver, this.pruningStrategy, this.mergedMDDCreator, currentAgentLocations, time);
        MAPF_Instance subProblem = baseInstance.getSubproblemFor(currentAgentLocations.keySet());
        RunParameters runParameters = new RunParameters(timeoutThreshold - totalRuntime, null, S_Metrics.newInstanceReport(), null);
        latestSolution = offlineSolver.solve(subProblem, runParameters);
        if (latestSolution != null){
            // the ICTS solver assumes everyone starts at 0
            updateTimes(latestSolution, time);
        }
        digestSubproblemReport(runParameters.instanceReport);
        return latestSolution;
    }

    private void updateTimes(Solution latestSolution, int time) {
        for (SingleAgentPlan plan:
             latestSolution) {
            List<Move> updatedMoves = new ArrayList<>();
            for (Move move :
                    plan) {
                updatedMoves.add(new Move(move.agent, move.timeNow + time, move.prevLocation, move.currLocation));
            }
            latestSolution.putPlan(new SingleAgentPlan(plan.agent, updatedMoves));
        }
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

    protected void addNewAgents(List<? extends OnlineAgent> agents, HashMap<Agent, I_Location> currentAgentLocations) {
        for (OnlineAgent agent :
                agents) {
            currentAgentLocations.put(agent, agent.getPrivateGarage(baseInstance.map.getMapCell(agent.source)));
        }
    }

    protected void digestSubproblemReport(InstanceReport instanceReport) {
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

        this.totalRuntime = 0;
        this.latestSolution = null;
        this.baseInstance = null;
        this.instanceReport = null;
        this.costOfReroute = 0;
    }

    @Override
    public String name() {
        return name;
    }


}
