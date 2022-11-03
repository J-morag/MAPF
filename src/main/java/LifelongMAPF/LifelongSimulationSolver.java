package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.AStar.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.RunParametersLNS;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.AgentSelectors.I_LifelongAgentSelector;
import LifelongMAPF.AgentSelectors.AllStationaryAgentsSubsetSelector;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import LifelongMAPF.Triggers.I_LifelongPlanningTrigger;
import org.apache.commons.lang.mutable.MutableInt;
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
    public boolean DEBUG = true;

    /*  = fields related to run =  */

    private ConstraintSet initialConstraints;
    private MAPF_Instance lifelongInstance;
    private List<LifelongAgent> lifelongAgents;
    private Random random;
    /**
     * At any point in time, must not take longer than this to respond and advance the simulation time.
     */
    private long minResponseTime;
    /**
     * Can reach, at most, this time step. IF reached and not all agents finished all destinations, return a partial solution.
     */
    private int maxTimeSteps;
    private int reachedTimestepInPlanning;
    private float avgGroupSize;
    private int numPlanningIterations;
    private CachingDistanceTableHeuristic cachingDistanceTableHeuristic;
    private int numDestinationsAchieved;

    public LifelongSimulationSolver(I_LifelongPlanningTrigger planningTrigger,
                                    I_LifelongAgentSelector agentSelector, I_LifelongCompatibleSolver offlineSolver) {
        if(offlineSolver == null) {
            throw new IllegalArgumentException("offlineSolver is mandatory");
        }
        if (!(offlineSolver.sharedSources() && offlineSolver.sharedGoals())){
            throw new IllegalArgumentException("offline solver should have shared sources and goals");
        }
        this.offlineSolver = offlineSolver;

        this.planningTrigger = Objects.requireNonNullElse(planningTrigger, new ActiveButPlanEndedTrigger());
        this.agentSelector = Objects.requireNonNullElse(agentSelector, new AllStationaryAgentsSubsetSelector());
        this.name = "Lifelong_" + offlineSolver.name();
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.lifelongAgents = verifyAndCastAgents(instance.agents);
        this.initialConstraints = parameters.constraints;
        this.lifelongInstance = instance;
        this.random = new Random(42);
        this.reachedTimestepInPlanning = 0;
        this.avgGroupSize = 0;
        this.numPlanningIterations = 0;
        this.numDestinationsAchieved = 0;
        if (this.initialConstraints != null){
            this.initialConstraints.sharedSources = true;
            this.initialConstraints.sharedGoals = true;
        }
        this.cachingDistanceTableHeuristic = new CachingDistanceTableHeuristic(1);
        this.cachingDistanceTableHeuristic.setCurrentMap(instance.map);
        if (parameters instanceof LifelongRunParameters lrp){
            this.minResponseTime = lrp.minResponseTime;
            this.maxTimeSteps = lrp.maxTimeSteps;
        }
        else {
            LifelongRunParameters tmpForDefaults = new LifelongRunParameters(parameters);
            this.minResponseTime = tmpForDefaults.minResponseTime;
            this.maxTimeSteps = tmpForDefaults.maxTimeSteps;
        }
    }

    private static List<LifelongAgent> verifyAndCastAgents(List<Agent> agents) {
        HashSet<Integer> ids = new HashSet<>(agents.size());
        List<LifelongAgent> lifelongAgents = new ArrayList<>();
        for (Agent agent :
                agents) {
            if (! (agent instanceof LifelongAgent)){
                throw new IllegalArgumentException(LifelongSimulationSolver.class.getSimpleName() + ": Must receive Lifelong Agents");
            }
            else lifelongAgents.add(((LifelongAgent) agent));
            if(ids.contains(agent.iD)){
                throw new IllegalArgumentException(LifelongSimulationSolver.class.getSimpleName() +
                        ": Lifelong solvers require all agents to have unique IDs");
            }
            else ids.add(agent.iD);
        }
        return lifelongAgents;
    }

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        SortedMap<Integer, Solution> solutionsAtTimes = new TreeMap<>();
        Map<Agent, Queue<I_Coordinate>> agentDestinationQueues = getDestinationQueues(instance);

        int sumGroupSizes = 0;
        int farthestCommittedTime = 0; // at this time locations are committed, and we choose locations for next time
        int nextPlanningTime = 0;

        Solution latestSolution = new Solution();
        for (LifelongAgent a : this.lifelongAgents){
            latestSolution.putPlan(getSingleStayPlan(0, a, lifelongInstance.map.getMapLocation(a.source)));
        }
        List<LifelongAgent> agentsWaitingToStart = new ArrayList<>(this.lifelongAgents);

        HashMap<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationStartTimes = new HashMap<>();
        HashMap<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes = new HashMap<>();
        for (LifelongAgent a :
                this.lifelongAgents) {
            agentsActiveDestinationStartTimes.put(a, new ArrayList<>());
            agentsActiveDestinationEndTimes.put(a, new ArrayList<>());
        }

        // every time when new planning is needed, solve an offline MAPF problem of the current conditions
        while (nextPlanningTime > -1){

            if (checkTimeout() || farthestCommittedTime >= maxTimeSteps){
                break;
            }

            Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents = getLifelongAgentsToTimelyOfflineAgents(farthestCommittedTime,
                    latestSolution, agentDestinationQueues, this.lifelongAgents, agentsActiveDestinationStartTimes, agentsActiveDestinationEndTimes);

            nextPlanningTime = farthestCommittedTime == 0 ? 0 :
                    planningTrigger.getNextPlanningTime(latestSolution, agentDestinationQueues, lifelongAgentsToTimelyOfflineAgents); // -1 if done
            if (farthestCommittedTime == nextPlanningTime){

                Set<Agent> selectedTimelyOfflineAgentsSubset = new HashSet<>(lifelongAgentsToTimelyOfflineAgents.values());
                selectedTimelyOfflineAgentsSubset.retainAll(
                        // could be lifelong or offline agents (and new or old), depending on implementation.
                        agentSelector.selectAgentsSubset(instance, latestSolution, farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents, agentsWaitingToStart, agentDestinationQueues));
                agentsWaitingToStart.removeAll(selectedTimelyOfflineAgentsSubset);
                List<LifelongAgent> notSelectedAgents = getUnchangingAgents(selectedTimelyOfflineAgentsSubset);
                List<SingleAgentPlan> nextPlansForNotSelectedAgents = getNextPlansForNotSelectedAgents(farthestCommittedTime, latestSolution, lifelongAgentsToTimelyOfflineAgents, notSelectedAgents);
                Solution subgroupSolution = null;
                if (! selectedTimelyOfflineAgentsSubset.isEmpty()){
                    numPlanningIterations++;
                    sumGroupSizes += selectedTimelyOfflineAgentsSubset.size();

                    MAPF_Instance timelyOfflineProblem = getTimelyOfflineProblem(farthestCommittedTime, selectedTimelyOfflineAgentsSubset);
                    RunParameters timelyOfflineProblemRunParameters = getTimelyOfflineProblemRunParameters(farthestCommittedTime, nextPlansForNotSelectedAgents);

                    subgroupSolution = offlineSolver.solve(timelyOfflineProblem, timelyOfflineProblemRunParameters); // TODO solver strategy ?
                    if (DEBUG && subgroupSolution != null){
                        checkSolutionStartTimes(subgroupSolution, farthestCommittedTime);
                    }
                    digestSubproblemReport(timelyOfflineProblemRunParameters.instanceReport);
                }

                MutableInt numStunnedAgents = new MutableInt(0);
                if (subgroupSolution == null || subgroupSolution.size() < this.lifelongAgents.size()){
                    latestSolution = handlePartialSolution(farthestCommittedTime, selectedTimelyOfflineAgentsSubset, nextPlansForNotSelectedAgents, subgroupSolution, numStunnedAgents);
                }
                else {
                    latestSolution = subgroupSolution;
                }

                if (DEBUG){
                    System.out.print("\rLifelongSim: ");
                    System.out.print("iteration " + numPlanningIterations + ", @ timestep " + farthestCommittedTime +
                            ", #agent/solved/stunned " + selectedTimelyOfflineAgentsSubset.size());
                    System.out.print("/" + (subgroupSolution != null? subgroupSolution.size(): 0));
                    System.out.print("/" + numStunnedAgents);
                    System.out.print(", destinations achieved (prev iter.) " + this.numDestinationsAchieved +
                            " [avg_thr " + (farthestCommittedTime > 0 ? (float)(numDestinationsAchieved) / farthestCommittedTime : 0) + "]");
                }

                solutionsAtTimes.put(farthestCommittedTime, latestSolution);
            }

            farthestCommittedTime++;
        }
        this.avgGroupSize = (float) sumGroupSizes / (float) numPlanningIterations;
        this.reachedTimestepInPlanning = farthestCommittedTime; // doing this before continuing to iterate over the solution tail
        // keep registering end times of agents
        while (farthestCommittedTime <= latestSolution.getEndTime()){
            getLifelongAgentsToTimelyOfflineAgents(farthestCommittedTime,
                    latestSolution, agentDestinationQueues, this.lifelongAgents, agentsActiveDestinationStartTimes, agentsActiveDestinationEndTimes);
            farthestCommittedTime++;
        }

        if (DEBUG){
            verifyAgentsActiveDestinationEndTimes(solutionsAtTimes, agentsActiveDestinationEndTimes);
        }

        // combine the stored solutions at times into a single lifelong solution
        return new LifelongSolution(solutionsAtTimes, (List<LifelongAgent>)(List)(instance.agents), agentsActiveDestinationEndTimes);
    }

    private static void verifyAgentsActiveDestinationEndTimes(SortedMap<Integer, Solution> solutionsAtTimes, HashMap<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes) {
        for (LifelongAgent agent:
             agentsActiveDestinationEndTimes.keySet()) {
            for (TimeCoordinate destinationArrival :
                    agentsActiveDestinationEndTimes.get(agent)) {
                int arrivalTime = destinationArrival.time;
                I_Coordinate arrivalCoordinate = destinationArrival.coordinate;

                List<Integer> reversedTimesList = new ArrayList<>(solutionsAtTimes.keySet());
                Collections.reverse(reversedTimesList);
                for (int t :
                        reversedTimesList) {
                    if (t < arrivalTime){
                        SingleAgentPlan timelyPlan = solutionsAtTimes.get(t).getPlanFor(agent);
                        if (arrivalTime < 0 || arrivalTime > timelyPlan.getEndTime()){
                            throw new RuntimeException("destination end time " + arrivalTime + " out of range of plan: " + timelyPlan);
                        }
                        Move arrivalMove = timelyPlan.moveAt(arrivalTime);
                        if (! arrivalMove.currLocation.getCoordinate().equals(arrivalCoordinate)){
                            throw new RuntimeException("destination end time " + arrivalTime + " points to wrong move: " + arrivalMove);
                        }
                        break;
                    }
                }
            }
        }
    }

    @NotNull
    private Solution handlePartialSolution(int farthestCommittedTime, Set<Agent> selectedTimelyOfflineAgentsSubset,
                                           List<SingleAgentPlan> nextPlansForNotSelectedAgents, @Nullable Solution subgroupSolution,
                                           MutableInt numStunnedAgents) {
        // handle fails by agents with no plan staying in place
        // give uncovered agents plans where they stay in place once
        List<SingleAgentPlan> partialButConflictFreeSolutionAsList = new ArrayList<>();
        Queue<SingleAgentPlan> stayPlans = new ArrayDeque<>();

        for (Agent a :
                selectedTimelyOfflineAgentsSubset) {
            if (subgroupSolution == null || subgroupSolution.getPlanFor(a) == null){
                I_Location agentLocation = lifelongInstance.map.getMapLocation(a.source);
                SingleAgentPlan singleStayPlan = getSingleStayPlan(farthestCommittedTime, a, agentLocation);
                stayPlans.add(singleStayPlan);
            }
            else {
                partialButConflictFreeSolutionAsList.add(subgroupSolution.getPlanFor(a));
            }
        }

        // add untouched agents
        partialButConflictFreeSolutionAsList.addAll(nextPlansForNotSelectedAgents);
        if (DEBUG && ! new Solution(partialButConflictFreeSolutionAsList).isValidSolution(true, true)){
            throw new RuntimeException("agents we found plans for together with untouched agents should form a conflict-free partial plan");
        }

        numStunnedAgents.setValue(0);
        // put stunned agents in a queue, check for conflicts with other plans and if they conflict trim and add the other plans to queue.
        Solution fullAndConflictFreeSolution = new Solution();
        while (! stayPlans.isEmpty()){
            SingleAgentPlan stayPlan = stayPlans.poll();
            fullAndConflictFreeSolution.putPlan(stayPlan);
            ListIterator<SingleAgentPlan> iter = partialButConflictFreeSolutionAsList.listIterator();
            while (iter.hasNext()){
                SingleAgentPlan plan = iter.next();
                if (plan.conflictsWith(stayPlan, true, true)){
                    numStunnedAgents.increment();
                    SingleAgentPlan shortenedPlan = getSingleStayPlan(farthestCommittedTime, plan.agent, plan.getFirstMove().prevLocation);
                    stayPlans.add(shortenedPlan);
                    iter.remove();
                }
            }
        }
        for (SingleAgentPlan plan :
                partialButConflictFreeSolutionAsList) {
            fullAndConflictFreeSolution.putPlan(plan);
        }
        return fullAndConflictFreeSolution;
    }

    @NotNull
    private static List<SingleAgentPlan> getNextPlansForNotSelectedAgents(int farthestCommittedTime, @NotNull Solution latestSolution, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> notSelectedLifelongAgents) {
        List<SingleAgentPlan> nextPlansForNotSelectedAgents = new ArrayList<>();
        for (LifelongAgent a :
                notSelectedLifelongAgents) {
            SingleAgentPlan existingPlan = latestSolution.getPlanFor(a);
            if (existingPlan.getEndTime() <= farthestCommittedTime){
                // stay plan if plan ended
                nextPlansForNotSelectedAgents.add(getSingleStayPlan(farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents.get(a), existingPlan.getLastMove().currLocation));
            }
            else {
                // continue with current plan
                nextPlansForNotSelectedAgents.add(getAdvancedPlan(farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents, latestSolution.getPlanFor(a)));
            }
        }
        return nextPlansForNotSelectedAgents;
    }

    @NotNull
    private static SingleAgentPlan getSingleStayPlan(int farthestCommittedTime, Agent a, I_Location agentLocation) {
        return new SingleAgentPlan(a, List.of(new Move(a, farthestCommittedTime + 1, agentLocation, agentLocation)));
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
    private Map<LifelongAgent, Agent> getLifelongAgentsToTimelyOfflineAgents(int farthestCommittedTime, @NotNull Solution previousSolution,
                                                                            Map<Agent, Queue<I_Coordinate>> agentDestinationQueues,
                                                                            List<LifelongAgent> agentsSubset,
                                                                            HashMap<LifelongAgent, List<TimeCoordinate>> activeDestinationStartTimes,
                                                                            HashMap<LifelongAgent, List<TimeCoordinate>> activeDestinationEndTimes) {
        Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents = new HashMap<>();
        for (LifelongAgent agent : agentsSubset){

            List<TimeCoordinate> destinationStartTimes = activeDestinationStartTimes.get(agent);
            List<TimeCoordinate> destinationEndTimes = activeDestinationEndTimes.get(agent);

            // for the first instance take the first destination in the queue as the source, for instances after this
            // agent reached final destination (and stays), take final destination
            I_Coordinate initialCoordinateAtTime;
            if (farthestCommittedTime == 0){
                initialCoordinateAtTime = agentDestinationQueues.get(agent).poll();
                if (initialCoordinateAtTime == null){
                    throw new IllegalArgumentException("agent with no destinations");
                }
                destinationStartTimes.add(new TimeCoordinate(0, initialCoordinateAtTime));
                updateDestinationEndTimeAndCount(destinationEndTimes, 0, initialCoordinateAtTime, false);
            }
            else {
                initialCoordinateAtTime = previousSolution.getPlanFor(agent).moveAt(Math.min(farthestCommittedTime, previousSolution.getPlanFor(agent).getEndTime())).currLocation.getCoordinate();
            }

            // for the first instance there is no previous destination, otherwise it's whichever destination was active
            I_Coordinate previousDestinationCoordinate;
            if (farthestCommittedTime == 0){
                previousDestinationCoordinate = null;
            }
            else { // get currently active destination
                previousDestinationCoordinate = destinationStartTimes.get(destinationStartTimes.size() - 1).coordinate;
            }

            // for the first instance, or if finished previous destination, dequeue next one destination, else continue towards current destination
            I_Coordinate nextDestinationCoordinate;
            if (previousDestinationCoordinate == null){ // first instance
                nextDestinationCoordinate = agentDestinationQueues.get(agent).poll();
                if (nextDestinationCoordinate == null) { // no more destinations in the queue
                    throw new IllegalArgumentException("Agent only has a source, not even one destination beyond.");
                }
                else {
                    destinationStartTimes.add(new TimeCoordinate(farthestCommittedTime + 1, nextDestinationCoordinate));
                }
            }
            else if (! previousDestinationCoordinate.equals(initialCoordinateAtTime)) // still on the way to current destination
            {
                nextDestinationCoordinate = previousDestinationCoordinate; // preserve current destination
            }
            else { // achieved a destination
                nextDestinationCoordinate = agentDestinationQueues.get(agent).poll();
                if (nextDestinationCoordinate == null){ // achieved the last destination
                    nextDestinationCoordinate = previousDestinationCoordinate; // keep last destination as placeholder destination
                    if (! destinationEndTimes.get(destinationEndTimes.size()-1).coordinate.equals(previousDestinationCoordinate)){
                        // just now achieved last destination
                        updateDestinationEndTimeAndCount(destinationEndTimes,farthestCommittedTime, previousDestinationCoordinate, true);
                    }
                }
                else { // got a new destination
                    updateDestinationEndTimeAndCount(destinationEndTimes, farthestCommittedTime, previousDestinationCoordinate, true);
                    destinationStartTimes.add(new TimeCoordinate(farthestCommittedTime + 1, nextDestinationCoordinate));
                }
            }
            Agent agentFromCurrentLocationToNextDestination = new Agent(agent.iD, initialCoordinateAtTime, nextDestinationCoordinate);
            lifelongAgentsToOfflineAgents.put(agent, agentFromCurrentLocationToNextDestination);
        }
        return lifelongAgentsToOfflineAgents;
    }

    private void updateDestinationEndTimeAndCount(List<TimeCoordinate> destinationEndTimes, int time, I_Coordinate initialCoordinateAtTime, boolean count) {
        destinationEndTimes.add(new TimeCoordinate(time, initialCoordinateAtTime));
        this.numDestinationsAchieved += count ? 1 : 0;
    }

    private MAPF_Instance getTimelyOfflineProblem(int farthestCommittedTime, Set<Agent> timelyOfflineAgentsSubset) {
        List<Agent> shuffledAgentsSubset = new ArrayList<>(timelyOfflineAgentsSubset);
        Collections.shuffle(shuffledAgentsSubset, this.random);
        return new MAPF_Instance(this.lifelongInstance.name + " subproblem at " + farthestCommittedTime,
                this.lifelongInstance.map, timelyOfflineAgentsSubset.toArray(Agent[]::new),
                this.lifelongInstance.extendedName + " subproblem at " + farthestCommittedTime);
    }

    private RunParameters getTimelyOfflineProblemRunParameters(int farthestCommittedTime, List<SingleAgentPlan> nextPlansForNotSelectedAgents) {
        // protect the plans of agents not included in the subset
        ConstraintSet constraints = this.initialConstraints != null ? new ConstraintSet(this.initialConstraints): new ConstraintSet();
        constraints.sharedSources = true;
        constraints.sharedGoals = true;
        nextPlansForNotSelectedAgents.forEach(plan -> constraints.addAll(constraints.allConstraintsForPlan(plan)));
        long hardTimeout = Math.min(minResponseTime, Math.max(0, super.maximumRuntime - (getCurrentTimeMS_NSAccuracy() - super.startTime)));
        RunParametersLNS runParametersLNS = new RunParametersLNS(new RunParameters(hardTimeout, constraints, new InstanceReport(), null, Math.min(minResponseTime, hardTimeout), farthestCommittedTime),
                this.cachingDistanceTableHeuristic);
        runParametersLNS.randomNumberGenerator = this.random;
        return runParametersLNS;
    }

    @NotNull
    private List<LifelongAgent> getUnchangingAgents(Set<Agent> agentsSubset) {
        List<LifelongAgent> unchangingAgents = new ArrayList<>(lifelongAgents);
        unchangingAgents.removeAll(agentsSubset);
        return unchangingAgents;
    }

    private void checkSolutionStartTimes(Solution subgroupSolution, int expectedPlansStartTime) {
        for (SingleAgentPlan plan :
                subgroupSolution) {
            if (plan.getPlanStartTime() != expectedPlansStartTime){
                throw new RuntimeException("start time " + plan.getPlanStartTime() + " != " + expectedPlansStartTime);
            }
        }
    }

    @NotNull
    private static SingleAgentPlan getAdvancedPlan(int farthestCommittedTime, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, SingleAgentPlan latestPlan) {
        SingleAgentPlan trimmedPlan = new SingleAgentPlan(lifelongAgentsToTimelyOfflineAgents.get(latestPlan.agent));
        latestPlan.forEach(move -> {if (move.timeNow > farthestCommittedTime) trimmedPlan.addMove(move);});
        return trimmedPlan;
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

        super.instanceReport.putIntegerValue("reachedTimestepInPlanning", this.reachedTimestepInPlanning);
        super.instanceReport.putIntegerValue("numPlanningIterations", this.numPlanningIterations);
        super.instanceReport.putFloatValue("avgGroupSize", this.avgGroupSize);

        LifelongSolution lifelongSolution = ((LifelongSolution)solution);
        super.instanceReport.putStringValue("waypointTimes", lifelongSolution.agentsWaypointArrivalTimes());

        super.instanceReport.putIntegerValue("SOC", lifelongSolution.sumIndividualCosts());
        super.instanceReport.putIntegerValue("makespan", lifelongSolution.makespan());
        super.instanceReport.putIntegerValue("timeTo50%Completion", lifelongSolution.timeToXProportionCompletion(0.5));
        super.instanceReport.putIntegerValue("timeTo80%Completion", lifelongSolution.timeToXProportionCompletion(0.8));
        super.instanceReport.putIntegerValue("throughputAtT30", lifelongSolution.throughputAtT(30));
        super.instanceReport.putIntegerValue("throughputAtT50", lifelongSolution.throughputAtT(50));
        super.instanceReport.putIntegerValue("throughputAtT75", lifelongSolution.throughputAtT(75));
        super.instanceReport.putIntegerValue("throughputAtT100", lifelongSolution.throughputAtT(100));
        super.instanceReport.putIntegerValue("throughputAtT200", lifelongSolution.throughputAtT(200));
        super.instanceReport.putIntegerValue("throughputAtT300", lifelongSolution.throughputAtT(300));
        super.instanceReport.putIntegerValue("throughputAtT400", lifelongSolution.throughputAtT(400));
        super.instanceReport.putIntegerValue("throughputAtT500", lifelongSolution.throughputAtT(500));

        super.instanceReport.putFloatValue("averageThroughput", lifelongSolution.averageThroughput());
        super.instanceReport.putFloatValue("averageIndividualThroughput", lifelongSolution.averageIndividualThroughput());

//            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
//            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.initialConstraints = null;
        this.lifelongInstance = null;
        this.random = null;
    }
}
