package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.RunParametersLNS;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RunParameters_PP;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.PartialSolutionsStrategy;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.AgentSelectors.I_LifelongAgentSelector;
import LifelongMAPF.AgentSelectors.AllStationaryAgentsSubsetSelector;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import LifelongMAPF.Triggers.I_LifelongPlanningTrigger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Simulates a lifelong environment for a lifelong compatible solver to run in.
 * <p>
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
    private static final boolean DEBUG = true;
    private final Double congestionMultiplier;
    private final PartialSolutionsStrategy partialSolutionsStrategy;

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
    private float avgFailedAgents;
    private float avgBlockedAgents;
    private int numPlanningIterations;
    private CachingDistanceTableHeuristic cachingDistanceTableHeuristic;
    private int numDestinationsAchieved;
    Map<LifelongAgent, I_Coordinate> agentsActiveDestination;
    Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationStartTimes;
    Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes;
    Set<LifelongAgent> finishedAgents;

    public LifelongSimulationSolver(I_LifelongPlanningTrigger planningTrigger, I_LifelongAgentSelector agentSelector,
                                    I_LifelongCompatibleSolver offlineSolver, @Nullable Double congestionMultiplier,
                                    @Nullable PartialSolutionsStrategy partialSolutionsStrategy) {
        if(offlineSolver == null) {
            throw new IllegalArgumentException("offlineSolver is mandatory");
        }
        if (!(offlineSolver.sharedSources() && offlineSolver.sharedGoals())){
            throw new IllegalArgumentException("offline solver should have shared sources and goals");
        }
        this.offlineSolver = offlineSolver;
        this.congestionMultiplier = congestionMultiplier;
        this.partialSolutionsStrategy = Objects.requireNonNullElse(partialSolutionsStrategy, new DisallowedPartialSolutionsStrategy());

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
        this.agentsActiveDestination = new HashMap<>();
        this.agentsActiveDestinationStartTimes = new HashMap<>();
        this.agentsActiveDestinationEndTimes = new HashMap<>();
        for (LifelongAgent a :
                this.lifelongAgents) {
            agentsActiveDestinationStartTimes.put(a, new ArrayList<>());
            agentsActiveDestinationEndTimes.put(a, new ArrayList<>());
        }
        this.finishedAgents = new HashSet<>();
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
        this.partialSolutionsStrategy.resetState();
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
        int sumBlockedSizesAfterPlanning = 0;
        int sumAttemptedAgentsThatFailed = 0;
        int farthestCommittedTime = 0; // at this time locations are committed, and we choose locations for next time

        Solution latestSolution = new Solution();
        for (LifelongAgent a : this.lifelongAgents){
            latestSolution.putPlan(getSingleStayPlan(0, a, lifelongInstance.map.getMapLocation(a.source)));
        }
        List<LifelongAgent> agentsWaitingToStart = new ArrayList<>(this.lifelongAgents);

        Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents = getLifelongAgentsToTimelyOfflineAgentsAndUpdateDestinationStartAndEndTimes(farthestCommittedTime,
                latestSolution, agentDestinationQueues, this.lifelongAgents);

        while (farthestCommittedTime < maxTimeSteps && this.finishedAgents.size() < this.lifelongAgents.size()){

            if (checkTimeout()){
                break;
            }

            // run enforcement at every time step
            Set<Agent> blockedAgentsBeforePlanningIteration = new HashSet<>();
            // done agents get "stay in place once". Same if they were blocked before
            List<SingleAgentPlan> advancedPlansToCurrentTime = getAdvancedPlansForAgents(farthestCommittedTime, latestSolution, lifelongAgentsToTimelyOfflineAgents, this.lifelongAgents);
            latestSolution = enforceSafeExecutionNextTimeStep(advancedPlansToCurrentTime, farthestCommittedTime, blockedAgentsBeforePlanningIteration);

            Set<Agent> selectedTimelyOfflineAgentsSubset = new HashSet<>(lifelongAgentsToTimelyOfflineAgents.values());
            selectedTimelyOfflineAgentsSubset = selectedTimelyOfflineAgentsSubset.stream().filter(agentSelector.getAgentSelectionPredicate(instance, latestSolution
                    , lifelongAgentsToTimelyOfflineAgents, agentsWaitingToStart, agentDestinationQueues, agentsActiveDestination)).collect(Collectors.toSet());

            if ( ! selectedTimelyOfflineAgentsSubset.isEmpty()){ // solve an offline MAPF problem of the current conditions

                agentsWaitingToStart.removeAll(selectedTimelyOfflineAgentsSubset);
                Set<LifelongAgent> notSelectedAgents = getUnchangingAgents(selectedTimelyOfflineAgentsSubset);
                List<SingleAgentPlan> nextPlansForNotSelectedAgents = subsetPlansCollection(latestSolution, notSelectedAgents);

                numPlanningIterations++;
                sumGroupSizes += selectedTimelyOfflineAgentsSubset.size();

                MAPF_Instance timelyOfflineProblem = getTimelyOfflineProblem(farthestCommittedTime, selectedTimelyOfflineAgentsSubset);
                RunParameters timelyOfflineProblemRunParameters = getTimelyOfflineProblemRunParameters(farthestCommittedTime, nextPlansForNotSelectedAgents, selectedTimelyOfflineAgentsSubset);

                Solution subgroupSolution = offlineSolver.solve(timelyOfflineProblem, timelyOfflineProblemRunParameters); // TODO solver strategy ?
                if (DEBUG && subgroupSolution != null){
                    checkSolutionStartTimes(subgroupSolution, farthestCommittedTime);
                }
                digestSubproblemReport(timelyOfflineProblemRunParameters.instanceReport);
                int numAgentsWithPlansInSolutionBeforeBlocking = subgroupSolution != null ? subgroupSolution.size() : 0;
                sumAttemptedAgentsThatFailed += selectedTimelyOfflineAgentsSubset.size() - numAgentsWithPlansInSolutionBeforeBlocking;

                Set<Agent> blockedAgentsAfterPlanning = new HashSet<>();
                latestSolution = addUncoveredAgents(farthestCommittedTime, selectedTimelyOfflineAgentsSubset, nextPlansForNotSelectedAgents, subgroupSolution, blockedAgentsAfterPlanning, this.lifelongInstance);
                latestSolution = enforceSafeExecutionNextTimeStep(latestSolution, farthestCommittedTime, blockedAgentsAfterPlanning);

                sumBlockedSizesAfterPlanning += blockedAgentsAfterPlanning.size();

                if (DEBUG){
                    printProgressAndStats(farthestCommittedTime, selectedTimelyOfflineAgentsSubset.size(), numAgentsWithPlansInSolutionBeforeBlocking, blockedAgentsAfterPlanning.size());
                }
            }
            else if (DEBUG){
                printProgressAndStats(farthestCommittedTime, 0, 0, blockedAgentsBeforePlanningIteration.size());
            }

            solutionsAtTimes.put(farthestCommittedTime, latestSolution);
            farthestCommittedTime++;
            // this is for the next iteration! must happen after advancing farthest committed time!
            lifelongAgentsToTimelyOfflineAgents = getLifelongAgentsToTimelyOfflineAgentsAndUpdateDestinationStartAndEndTimes(farthestCommittedTime,
                    latestSolution, agentDestinationQueues, this.lifelongAgents);
        }
        this.avgGroupSize = (float) sumGroupSizes / (float) numPlanningIterations;
        this.avgFailedAgents = (float) sumAttemptedAgentsThatFailed / (float) numPlanningIterations;
        this.avgBlockedAgents = (float) sumBlockedSizesAfterPlanning / (float) numPlanningIterations;
        this.reachedTimestepInPlanning = farthestCommittedTime;

        if (DEBUG){
            verifyAgentsActiveDestinationEndTimes(solutionsAtTimes, agentsActiveDestinationEndTimes);
        }

        // combine the stored solutions at times into a single lifelong solution
        return new LifelongSolution(solutionsAtTimes, (List<LifelongAgent>)(List)(instance.agents), agentsActiveDestinationEndTimes);
    }

    @NotNull
    private static List<SingleAgentPlan> subsetPlansCollection(Iterable<? extends SingleAgentPlan> plans, Set<LifelongAgent> notSelectedAgents) {
        List<SingleAgentPlan> plansSubset = new ArrayList<>();
        for (SingleAgentPlan plan:
                plans) {
            if (notSelectedAgents.contains(plan.agent)){
                plansSubset.add(plan);
            }
        }
        if(DEBUG && plansSubset.size() < notSelectedAgents.size() ){
            throw new RuntimeException(String.format("Subset doesn't contain all agents: %d of %d", plansSubset.size(), notSelectedAgents.size()));
        }
        return plansSubset;
    }

    private void printProgressAndStats(int farthestCommittedTime, int selectedTimelyOfflineAgentsSubset, int subgroupSolution, int numStunnedAgents) {
        System.out.print("\rLifelongSim: ");
        System.out.printf("iteration %1$3s, @ timestep %2$3s, #agent/solved/(newly)blocked %3$3s",
                numPlanningIterations, farthestCommittedTime, selectedTimelyOfflineAgentsSubset);
        System.out.printf("/%1$3s", subgroupSolution);
        System.out.printf("/%1$3s", numStunnedAgents);
        System.out.printf(", destinations achieved (prev iter.) %d [avg_thr %.2f]",
                this.numDestinationsAchieved, (farthestCommittedTime > 0 ? (float)(numDestinationsAchieved) / farthestCommittedTime : 0));
        System.out.print('\r');
    }

    private static void verifyAgentsActiveDestinationEndTimes(SortedMap<Integer, Solution> solutionsAtTimes, Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes) {
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
    private static Solution addUncoveredAgents(int farthestCommittedTime, Set<Agent> selectedTimelyOfflineAgentsSubset,
                                               List<SingleAgentPlan> nextPlansForNotSelectedAgents, @Nullable Solution subgroupSolution,
                                               Set<Agent> blockedAgents, MAPF_Instance lifelongInstance) {
        // handle fails by agents with no plan staying in place
        List<SingleAgentPlan> invalidSolutionAsList = new ArrayList<>();

        for (Agent a :
                selectedTimelyOfflineAgentsSubset) {
            if (subgroupSolution == null || subgroupSolution.getPlanFor(a) == null){
                I_Location agentLocation = lifelongInstance.map.getMapLocation(a.source);
                invalidSolutionAsList.add(getSingleStayPlan(farthestCommittedTime, a, agentLocation));
                blockedAgents.add(a);
            }
            else {
                invalidSolutionAsList.add(subgroupSolution.getPlanFor(a));
            }
        }

        // add untouched agents
        invalidSolutionAsList.addAll(nextPlansForNotSelectedAgents);

        return new Solution(invalidSolutionAsList);
    }

    /**
     * When a conflict between paths is found for the next time step, one (arbitrary) path is interrupted and replaced
     * with staying in place if doing so resolves the conflict. Otherwise, both are interrupted.
     *
     * @param solutionThatMayContainConflicts a solution with any number of conflicts
     * @param blockedAgents agents that were blocked and got a single stay plan instead of the plan they had.
     * @return a repaired solution with no conflicts in the next time step.
     */
    private static Solution enforceSafeExecutionNextTimeStep(Iterable<? extends SingleAgentPlan> solutionThatMayContainConflicts,
                                                             int farthestCommittedTime, Set<Agent> blockedAgents){
        Solution solutionWithoutConflicts = new Solution(solutionThatMayContainConflicts);
        boolean hadConflictsCurrentIteration = true;
        while (hadConflictsCurrentIteration){
            hadConflictsCurrentIteration = false;
            SingleAgentPlan newPlan1 = null;
            SingleAgentPlan newPlan2 = null;

            for (SingleAgentPlan plan1 :
                    solutionWithoutConflicts) {
                if (newPlan1 != null || newPlan2 != null){
                    break;
                }
                for (SingleAgentPlan plan2 :
                        solutionWithoutConflicts) {
                    if (newPlan1 != null || newPlan2 != null){
                        break;
                    }
                    if (! plan1.agent.equals(plan2.agent)) {
                        Move plan1FirstMove = plan1.getFirstMove();
                        Move plan2FirstMove = plan2.getFirstMove();
                        A_Conflict conflict = A_Conflict.conflictBetween(plan1FirstMove, plan2FirstMove);
                        boolean isStayAtSharedSource = isStayAtSharedSource(plan1FirstMove, plan2FirstMove);
                        boolean isMoveToSharedGoal = isMoveToSharedGoal(plan1FirstMove, plan2FirstMove);
                        if (isStayAtSharedSource || isMoveToSharedGoal){
                            conflict = null; // TODO fix instances so no shared sources, and unique last destinations
                        }
                        if (conflict != null){
                            // try to resolve conflict by interrupting one (preferably) or both plans.
                            SingleAgentPlan agent1StayPlan = getSingleStayPlan(farthestCommittedTime, plan1.agent, plan1FirstMove.prevLocation);
                            SingleAgentPlan agent2StayPlan = getSingleStayPlan(farthestCommittedTime, plan2.agent, plan2FirstMove.prevLocation);
                            if (A_Conflict.conflictBetween(plan2FirstMove, agent1StayPlan.getFirstMove()) == null
                                    // TODO fix instances so no shared sources, and unique last destinations
                                    || isStayAtSharedSource(plan2FirstMove, agent1StayPlan.getFirstMove())
                                    || isMoveToSharedGoal(plan2FirstMove, agent1StayPlan.getFirstMove())
                            ){
                                newPlan1 = agent1StayPlan;
                            } else if (A_Conflict.conflictBetween(plan1FirstMove, agent2StayPlan.getFirstMove()) == null
                                    // TODO fix instances so no shared sources, and unique last destinations
                                    || isStayAtSharedSource(plan1FirstMove, agent2StayPlan.getFirstMove())
                                    || isMoveToSharedGoal(plan1FirstMove, agent2StayPlan.getFirstMove())
                            ) {
                                newPlan2 = agent2StayPlan;
                            }
                            else {
                                newPlan1 = agent1StayPlan;
                                newPlan2 = agent2StayPlan;
                                if (DEBUG && agent1StayPlan.conflictsWith(agent2StayPlan, true, true)){ // TODO fix instances so no shared sources, and unique last destinations
                                    throw new RuntimeException(String.format("Both agents staying in place should not result in a conflict. \nconflict = %1$s \noriginal plan1 = %2$s\noriginal plan2= %3$s\nnew plan1= %4$s\nnew plan 2=%5$s",
                                            agent1StayPlan.firstConflict(agent2StayPlan), plan1, plan2, newPlan1, newPlan2));
                                }
                            }
                        }
                    }
                }
            }

            if (newPlan1 != null || newPlan2 != null){
                hadConflictsCurrentIteration = true;
                if (newPlan1 != null){
                    solutionWithoutConflicts.putPlan(newPlan1);
                    blockedAgents.add(newPlan1.agent);
                }
                if (newPlan2 != null){
                    solutionWithoutConflicts.putPlan(newPlan2);
                    blockedAgents.add(newPlan2.agent);
                }
            }
        }

        if (DEBUG){
            verifyNextStepSafe(solutionThatMayContainConflicts, solutionWithoutConflicts);
        }
//        if (DEBUG && ! solutionWithoutConflicts.isValidSolution(true, true)){
//            throw new RuntimeException(String.format("""
//                    The solution should be safe for the duration of the horizon.
//                    %s
//                    %s""", solutionWithoutConflicts, solutionWithoutConflicts.arbitraryConflict(true, true)));
//        } // TODO what to do with this now that there is a horizon? Should check if safe for k steps? But we also have blocked agents
        return solutionWithoutConflicts;
    }

    private static void verifyNextStepSafe(Iterable<? extends SingleAgentPlan> solutionThatMayContainConflicts, Solution solutionWithoutConflicts) {
        Solution oneStepSolution = getOneStepSolution(solutionWithoutConflicts);
        boolean isSafeNextStep = isSafeOneStepSolution(oneStepSolution);
        if ( ! isSafeNextStep){
            throw new RuntimeException(String.format("""
                    Got conflicts in next step after supposedly enforcing safe next time step execution.\s
                     original solution: %s
                     solution after enforcement: %s
                     next step solution : %s""", solutionThatMayContainConflicts.toString(), solutionWithoutConflicts, oneStepSolution));
        }
    }

    public static boolean isSafeOneStepSolution(Solution oneStepSolution) {
        return oneStepSolution.isValidSolution(true, true); // TODO fix instances so no shared goals and sources
    }

    @NotNull
    public static Solution getOneStepSolution(Solution solutionWithoutConflicts) {
        Solution oneStepSolution = new Solution();
        for (SingleAgentPlan plan :
                solutionWithoutConflicts) {
            oneStepSolution.putPlan(new SingleAgentPlan(plan.agent, List.of(plan.getFirstMove())));
        }
        return oneStepSolution;
    }

    private static boolean isMoveToSharedGoal(Move plan1FirstMove, Move plan2FirstMove) {
        return plan1FirstMove.currLocation.equals(plan2FirstMove.currLocation)
                && plan1FirstMove.currLocation.getCoordinate().equals(plan1FirstMove.agent.target)
                && plan2FirstMove.currLocation.getCoordinate().equals(plan2FirstMove.agent.target);
    }

    private static boolean isStayAtSharedSource(Move plan1FirstMove, Move plan2FirstMove) {
        return plan1FirstMove.prevLocation.equals(plan2FirstMove.prevLocation) && plan1FirstMove.prevLocation.equals(plan1FirstMove.currLocation) && plan2FirstMove.prevLocation.equals(plan2FirstMove.currLocation);
    }

    @NotNull
    private static List<SingleAgentPlan> getAdvancedPlansForAgents(int farthestCommittedTime, @NotNull Solution latestSolution, Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents, List<LifelongAgent> lifelongAgents) {
        List<SingleAgentPlan> advancedPlans = new ArrayList<>();
        for (LifelongAgent lifelongAgent :
                lifelongAgents) {
            SingleAgentPlan existingPlan = latestSolution.getPlanFor(lifelongAgent);
            if (existingPlan.getEndTime() <= farthestCommittedTime){
                // stay plan if plan ended
                advancedPlans.add(getSingleStayPlan(farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents.get(lifelongAgent), existingPlan.getLastMove().currLocation));
            }
            else {
                // continue with current plan
                advancedPlans.add(getAdvancedPlan(farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents, latestSolution.getPlanFor(lifelongAgent)));
            }
        }
        return advancedPlans;
    }

    @NotNull
    private static SingleAgentPlan getSingleStayPlan(int farthestCommittedTime, Agent a, I_Location agentLocation) {
        return new SingleAgentPlan(a, List.of(getStayMove(farthestCommittedTime, a, agentLocation)));
    }

    @NotNull
    private static Move getStayMove(int farthestCommittedTime, Agent a, I_Location agentLocation) {
        return new Move(a, farthestCommittedTime + 1, agentLocation, agentLocation);
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
    private Map<LifelongAgent, Agent> getLifelongAgentsToTimelyOfflineAgentsAndUpdateDestinationStartAndEndTimes(int farthestCommittedTime, @NotNull Solution previousSolution,
                                                                                                                 Map<Agent, Queue<I_Coordinate>> agentDestinationQueues,
                                                                                                                 List<LifelongAgent> agentsSubset) {
        // TODO can I brake this into smaller pieces that only do one thing?
        Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents = new HashMap<>();
        for (LifelongAgent agent : agentsSubset){

            List<TimeCoordinate> destinationStartTimes = agentsActiveDestinationStartTimes.get(agent);
            List<TimeCoordinate> destinationEndTimes = agentsActiveDestinationEndTimes.get(agent);

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
                    updateAgentActiveDestination(farthestCommittedTime, agent, destinationStartTimes, nextDestinationCoordinate);
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
                        this.finishedAgents.add(agent);
                    }
                }
                else { // got a new destination
                    updateDestinationEndTimeAndCount(destinationEndTimes, farthestCommittedTime, previousDestinationCoordinate, true);
                    updateAgentActiveDestination(farthestCommittedTime, agent, destinationStartTimes, nextDestinationCoordinate);
                }
            }
            Agent agentFromCurrentLocationToNextDestination = new Agent(agent.iD, initialCoordinateAtTime, nextDestinationCoordinate);
            lifelongAgentsToOfflineAgents.put(agent, agentFromCurrentLocationToNextDestination);
        }
        return lifelongAgentsToOfflineAgents;
    }

    private void updateAgentActiveDestination(int farthestCommittedTime, LifelongAgent agent, List<TimeCoordinate> destinationStartTimes, I_Coordinate nextDestinationCoordinate) {
        destinationStartTimes.add(new TimeCoordinate(farthestCommittedTime + 1, nextDestinationCoordinate));
        agentsActiveDestination.put(agent, nextDestinationCoordinate);
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

    private RunParameters getTimelyOfflineProblemRunParameters(int farthestCommittedTime, List<SingleAgentPlan> nextPlansForNotSelectedAgents, Set<Agent> selectedTimelyOfflineAgentsSubset) {
        // protect the plans of agents not included in the subset
        ConstraintSet constraints = this.initialConstraints != null ? new ConstraintSet(this.initialConstraints): new ConstraintSet();
        constraints.sharedSources = true;
        constraints.sharedGoals = true;
        nextPlansForNotSelectedAgents.forEach(plan -> constraints.addAll(constraints.allConstraintsForPlan(plan)));

        AStarGAndH costAndHeuristic = this.cachingDistanceTableHeuristic;
        if (congestionMultiplier != null && congestionMultiplier > 0){
            List<Agent> agents = new ArrayList<>(selectedTimelyOfflineAgentsSubset);
            costAndHeuristic = new DistanceTableAStarHeuristic(agents, this.lifelongInstance.map, new CongestionMap(nextPlansForNotSelectedAgents, congestionMultiplier));
        }

        long hardTimeout = Math.min(minResponseTime, Math.max(0, super.maximumRuntime - (getCurrentTimeMS_NSAccuracy() - super.startTime)));

        RunParameters runParameters = new RunParameters(hardTimeout, constraints, new InstanceReport(), null, Math.min(minResponseTime, hardTimeout), farthestCommittedTime, this.random);
        if (offlineSolver instanceof LargeNeighborhoodSearch_Solver){
            RunParametersLNS runParametersLNS = new RunParametersLNS(runParameters, costAndHeuristic);
            runParametersLNS.partialSolutionsStrategy = this.partialSolutionsStrategy;
            runParameters =runParametersLNS;
        }
        else if (offlineSolver instanceof PrioritisedPlanning_Solver){
            RunParameters_PP runParameters_pp = new RunParameters_PP(runParameters, costAndHeuristic);
            runParameters_pp.partialSolutionsStrategy = this.partialSolutionsStrategy;
            runParameters = runParameters_pp;
        }

        return runParameters;
    }

    @NotNull
    private Set<LifelongAgent> getUnchangingAgents(Set<Agent> agentsSubset) {
        Set<LifelongAgent> unchangingAgents = new HashSet<>(lifelongAgents);
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
        Integer lowLevelRuntime = instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
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
        super.instanceReport.putFloatValue("avgFailedAgents", this.avgFailedAgents);
        super.instanceReport.putFloatValue("avgBlockedAgents", this.avgBlockedAgents);

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
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.initialConstraints = null;
        this.lifelongInstance = null;
        this.random = null;
        this.agentsActiveDestination = null;
        this.agentsActiveDestinationStartTimes = null;
        this.agentsActiveDestinationEndTimes = null;
        this.finishedAgents = null;
    }
}
