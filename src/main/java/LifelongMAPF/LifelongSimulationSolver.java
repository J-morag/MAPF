package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.AgentSelectors.I_LifelongAgentSelector;
import LifelongMAPF.AgentSelectors.MandatoryAgentsSubsetSelector;
import LifelongMAPF.Triggers.DestinationAchievedTrigger;
import LifelongMAPF.Triggers.I_LifelongPlanningTrigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Simulates a lifelong environment for a lifelong compatible solver to run in.
 *
 * The {@link LifelongAgent agents} each have a list of waypoints to achieve. They start at the first waypoint
 * (at time 0), and the {@link #offlineSolver} has to find a solution for each agent to arrive at its next waypoint. Only
 * the next ( not yet achieved) waypoint is revealed at any time.
 */
public class LifelongSimulationSolver extends A_Solver {

    /* fields related to instance */
    /**
     * An offline solver to use for solving online problems.
     */
    protected final I_Solver offlineSolver;
    private final I_LifelongPlanningTrigger planningTrigger;
    private final I_LifelongAgentSelector agentSelector;

    /*  = fields related to run =  */

    private ConstraintSet initialConstraints;
    private MAPF_Instance lifelongInstance;
    private Random random;

    public LifelongSimulationSolver(I_LifelongPlanningTrigger planningTrigger,
                                    I_LifelongAgentSelector agentSelector, I_LifelongCompatibleSolver offlineSolver) {
        if(offlineSolver == null) {
            throw new IllegalArgumentException("offlineSolver is mandatory");
        }
        if (!(offlineSolver.sharedSources() && offlineSolver.sharedGoals())){
            throw new IllegalArgumentException("offline solver should have shared sources and goals");
        }
        this.offlineSolver = offlineSolver;

        this.planningTrigger = Objects.requireNonNullElse(planningTrigger, new DestinationAchievedTrigger());
        this.agentSelector = Objects.requireNonNullElse(agentSelector, new MandatoryAgentsSubsetSelector());
        this.name = "Lifelong_" + offlineSolver.name();
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        verifyAgents(instance.agents);
        this.initialConstraints = parameters.constraints;
        this.lifelongInstance = instance;
        this.random = new Random(42);
        if (this.initialConstraints != null){
            this.initialConstraints.sharedSources = true;
            this.initialConstraints.sharedGoals = true;
        }
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

        int farthestCommittedTime = 0; // at this time locations are committed, and we choose locations for next time
        Solution latestSolution = null;
        // every time when new planning is needed, solve an offline MAPF problem of the current conditions
        while (farthestCommittedTime > -1){
            Map<Agent, Agent> lifelongAgentsToTimelyOfflineAgents = getLifelongAgentsToTimelyOfflineAgents(farthestCommittedTime, latestSolution, agentDestinationQueues, instance.agents);

            Set<Agent> selectedTimelyOfflineAgentsSubset = new HashSet<>(lifelongAgentsToTimelyOfflineAgents.values());
            selectedTimelyOfflineAgentsSubset.retainAll(
                    // could be lifelong or offline agents (and new or old), depending on implementation.
                    agentSelector.selectAgentsSubset(instance, latestSolution, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents));

            MAPF_Instance timelyOfflineProblem = getTimelyOfflineProblem(farthestCommittedTime, selectedTimelyOfflineAgentsSubset);
            RunParameters timelyOfflineProblemRunParameters = getTimelyOfflineProblemRunParameters(farthestCommittedTime, selectedTimelyOfflineAgentsSubset, latestSolution);

            // TODO solver strategy ?
            Solution partialSolution = offlineSolver.solve(timelyOfflineProblem, timelyOfflineProblemRunParameters);
            if(partialSolution == null){ //probably a timeout
                return null;
            }
            digestSubproblemReport(timelyOfflineProblemRunParameters.instanceReport);
            // handle partial solution (add the other agents)
            latestSolution = addUntouchedAgentsToPartialSolution(partialSolution, latestSolution,
                    selectedTimelyOfflineAgentsSubset, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents);
            solutionsAtTimes.put(farthestCommittedTime, latestSolution);

            farthestCommittedTime = planningTrigger.getNextFarthestCommittedTime(latestSolution, agentDestinationQueues); // -1 if done
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

    /**
     * Map each lifelong agent to a suitable offline representation at time.
     */
    @NotNull
    private static Map<Agent, Agent> getLifelongAgentsToTimelyOfflineAgents(int lastCommittedTime, Solution previousSolution,
                                                                     Map<Agent, Queue<I_Coordinate>> agentDestinationQueues,
                                                                     List<Agent> agentsSubset) {
        Map<Agent, Agent> lifelongAgentsToOfflineAgents = new HashMap<>();
        for (Agent agent : agentsSubset){
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
            lifelongAgentsToOfflineAgents.put(agent, agentFromCurrentLocationToNextDestination);
        }
        return lifelongAgentsToOfflineAgents;
    }

    private MAPF_Instance getTimelyOfflineProblem(int farthestCommittedTime, Set<Agent> timelyOfflineAgentsSubset) {
        List<Agent> shuffledAgentsSubset = new ArrayList<>(timelyOfflineAgentsSubset);
        Collections.shuffle(shuffledAgentsSubset, this.random);
        return new MAPF_Instance(this.lifelongInstance.name + " subproblem at " + farthestCommittedTime,
                this.lifelongInstance.map, timelyOfflineAgentsSubset.toArray(Agent[]::new),
                this.lifelongInstance.extendedName + " subproblem at " + farthestCommittedTime);
    }

    private RunParameters getTimelyOfflineProblemRunParameters(int farthestCommittedTime, Set<Agent> agentsSubset, Solution latestSolution) {
        // protect the plans of agents not included in the subset
        ConstraintSet constraints = this.initialConstraints != null ? new ConstraintSet(this.initialConstraints): new ConstraintSet();
        constraints.sharedSources = true;
        constraints.sharedGoals = true;
        List<Agent> unchangingAgents = new ArrayList<>(lifelongInstance.agents);
        unchangingAgents.removeAll(agentsSubset);
        unchangingAgents.forEach(agent -> constraints.addAll(constraints.allConstraintsForPlan(latestSolution.getPlanFor(agent))));

        return new RunParameters(Math.max(0, super.maximumRuntime - (getCurrentTimeMS_NSAccuracy() - super.startTime)),
                constraints, new InstanceReport(), null, farthestCommittedTime);
    }

    private Solution addUntouchedAgentsToPartialSolution(Solution partialSolution, @Nullable Solution latestSolution,
                                                         Set<Agent> agentsSubset, int partialSolutionStartTime,
                                                         Map<Agent, Agent> lifelongAgentsToTimelyOfflineAgents) {
        if (latestSolution == null){
            return partialSolution;
        }
        for (SingleAgentPlan plan : latestSolution){
            if (! agentsSubset.contains(plan.agent)){
                // trim plan and add to the partial solution
                SingleAgentPlan trimmedPlan = new SingleAgentPlan(lifelongAgentsToTimelyOfflineAgents.get(plan.agent));
                plan.forEach(move -> {if (move.timeNow > partialSolutionStartTime) trimmedPlan.addMove(move);});
                partialSolution.putPlan(trimmedPlan);
            }
        }
        return partialSolution;
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
            LifelongSolution lifelongSolution = ((LifelongSolution)solution);
            super.instanceReport.putStringValue("waypointTimes", lifelongSolution.agentsWaypointArrivalTimes());

            super.instanceReport.putIntegerValue("SOC", lifelongSolution.sumIndividualCosts());
            super.instanceReport.putIntegerValue("makespan", lifelongSolution.makespan());
            super.instanceReport.putIntegerValue("timeTo50%Completion", lifelongSolution.timeToXProportionCompletion(0.5));
            super.instanceReport.putIntegerValue("timeTo80%Completion", lifelongSolution.timeToXProportionCompletion(0.8));
            super.instanceReport.putIntegerValue("throughputAtT250", lifelongSolution.throughputAtT(250));

            super.instanceReport.putFloatValue("averageThroughput", lifelongSolution.averageThroughput());
            super.instanceReport.putFloatValue("averageIndividualThroughput", lifelongSolution.averageIndividualThroughput());

//            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
//            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
        }
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.initialConstraints = null;
        this.lifelongInstance = null;
        this.random = null;
    }
}
