package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;

import java.util.*;

/**
 * Simulates a lifelong environment for a lifelong compatible solver to run in.
 *
 * The {@link LifelongAgent agents} each have a list of waypoints to achieve. They start at the first waypoint
 * (at time 0), and the {@link #offlineSolver} has to find a solution for each agent to arrive at its next waypoint. Only
 * the next ( not yet achieved) waypoint is revealed at any time.
 */
public class LifelongSimulationSolver extends A_Solver {

    /**
     * An offline solver to use for solving online problems.
     */
    protected final I_Solver offlineSolver;
    private ConstraintSet initialConstraints;

    public LifelongSimulationSolver(I_LifelongCompatibleSolver offlineSolver) {
        if(offlineSolver == null) {
            throw new IllegalArgumentException("offlineSolver is mandatory");
        }
        if (!(offlineSolver.sharedSources() && offlineSolver.sharedGoals())){
            throw new IllegalArgumentException("offline solver should have shared sources and goals");
        }
        this.offlineSolver = offlineSolver;
        this.name = "Lifelong_" + offlineSolver.name();
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.initialConstraints = parameters.constraints;
        if (this.initialConstraints != null){
            this.initialConstraints.sharedSources = true;
            this.initialConstraints.sharedGoals = true;
        }
        verifyAgents(instance.agents);
    }

    private static void verifyAgents(List<Agent> agents) {
        HashSet<Integer> ids = new HashSet<>(agents.size());
        for (Agent agent :
                agents) {
            if (! (agent instanceof LifelongAgent)){
                throw new IllegalArgumentException(LifelongSimulationSolver.class.getSimpleName() + ": Must receive Lifelong Agents");
            }
            if(ids.contains(agent.iD)){
                throw new IllegalArgumentException(LifelongSimulationSolver.class.getSimpleName() +
                        ": Lifelong solvers require all agents to have unique IDs");
            }
            else ids.add(agent.iD);
        }
    }

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        SortedMap<Integer, Solution> solutionsAtTimes = new TreeMap<>();
        Map<Agent, Queue<I_Coordinate>> agentDestinationQueues = getDestinationQueues(instance);

        int latestSolutionTime = -1;
        int nextPlanningTime = 0; // at this time locations are committed, and we choose locations for next time
        Solution latestSolution = null;
        // every time when new planning is needed, solve an offline MAPF problem of the current conditions
        while (latestSolutionTime < nextPlanningTime){
            // TODO agent subset selection strategy
            MAPF_Instance timelyOfflineProblem = getTimelyOfflineProblem(nextPlanningTime, latestSolution,
                    instance, agentDestinationQueues);
            RunParameters timelyOfflineProblemRunParameters = getTimelyOfflineProblemRunParameters(nextPlanningTime);

            // TODO solver strategy
            latestSolution = offlineSolver.solve(timelyOfflineProblem, timelyOfflineProblemRunParameters);
            if(latestSolution == null){ //probably a timeout
                return null;
            }
            digestSubproblemReport(timelyOfflineProblemRunParameters.instanceReport);

            latestSolutionTime = nextPlanningTime;
            // TODO timing strategy
            nextPlanningTime = getNextPlanningTime(latestSolution, agentDestinationQueues); // 0 if no more destinations

            solutionsAtTimes.put(latestSolutionTime, latestSolution);
        }

        // combine the stored solutions at times into a single online solution
        return new LifelongSolution(solutionsAtTimes, (List<LifelongAgent>)(List)(instance.agents));
    }

    private Map<Agent, Queue<I_Coordinate>> getDestinationQueues(MAPF_Instance instance) {
        Map<Agent, Queue<I_Coordinate>> result = new HashMap<>(instance.agents.size());
        for (Agent agent : instance.agents){
            result.put(agent, new ArrayDeque<>(((LifelongAgent)agent).waypoints));
        }
        return result;
    }

    private MAPF_Instance getTimelyOfflineProblem(int lastCommittedTime, Solution previousSolution,
                                                  MAPF_Instance instance, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues) {
        List<Agent> offlineAgents = new ArrayList<>();
        for (Agent agent : instance.agents){
            // for the first instance take the first destination in the queue as the source, for instances after this
            // agent reached final destination (and stays), take final destination
            I_Coordinate initialCoordinateAtTime = previousSolution == null ? agentDestinationQueues.get(agent).poll() :
                    previousSolution.getPlanFor(agent).moveAt(Math.min(lastCommittedTime, previousSolution.getPlanFor(agent).getEndTime())).currLocation.getCoordinate();

            // for the first instance, or if reached previous destination, dequeue next one, else continue towards current destination
            I_Coordinate previousDestinationCoordinate = previousSolution == null ? null :
                    previousSolution.getPlanFor(agent).getLastMove().currLocation.getCoordinate();
            I_Coordinate nextDestinationCoordinate = previousDestinationCoordinate == null || previousDestinationCoordinate.equals(initialCoordinateAtTime) ?
                    agentDestinationQueues.get(agent).poll():
                    previousDestinationCoordinate; // preserve current destination
            if (nextDestinationCoordinate == null) {
                nextDestinationCoordinate = initialCoordinateAtTime;
            }

            Agent agentFromCurrentLocationToNextDestination = new Agent(agent.iD, initialCoordinateAtTime, nextDestinationCoordinate);
            offlineAgents.add(agentFromCurrentLocationToNextDestination);
        }
        return new MAPF_Instance(instance.name + " subproblem at " + lastCommittedTime, instance.map,
                offlineAgents.toArray(Agent[]::new), instance.extendedName + " subproblem at " + lastCommittedTime);
    }

    private RunParameters getTimelyOfflineProblemRunParameters(int problemStartTime) {
        return new RunParameters(super.maximumRuntime - (getCurrentTimeMS_NSAccuracy() - super.startTime),
                this.initialConstraints, new InstanceReport(), null, problemStartTime);
    }

    /**
     * @return the next planning time, or 0 if no next planning time.
     */
    private int getNextPlanningTime(Solution latestSolution, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues) {
        int minGoalArrivalTime = Integer.MAX_VALUE;
        for (SingleAgentPlan plan : latestSolution){
            if ( ! agentDestinationQueues.get(plan.agent).isEmpty()){
                minGoalArrivalTime = Math.min(minGoalArrivalTime, plan.getEndTime());
            }
        }
        return minGoalArrivalTime == Integer.MAX_VALUE ? 0 : minGoalArrivalTime;
    }

    protected void digestSubproblemReport(InstanceReport instanceReport) {
        Integer statesGenerated = instanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel);
        this.totalLowLevelStatesGenerated += statesGenerated == null ? 0 : statesGenerated;
        Integer statesExpanded = instanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
        this.totalLowLevelStatesExpanded += statesExpanded == null ? 0 : statesExpanded;
        Integer lowLevelRuntime = instanceReport.getIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS);
        this.instanceReport.integerAddition(InstanceReport.StandardFields.totalLowLevelTimeMS, lowLevelRuntime == null ? 0 : lowLevelRuntime);
        Integer generatedNodes = instanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        this.instanceReport.integerAddition(InstanceReport.StandardFields.generatedNodes, generatedNodes == null ? 0 : generatedNodes);
        Integer expandedNodes = instanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        this.instanceReport.integerAddition(InstanceReport.StandardFields.expandedNodes, expandedNodes == null ? 0 : expandedNodes);
        S_Metrics.removeReport(instanceReport);
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        if(solution != null){
            // TODO makespan, throughput
            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
        }
    }
}
