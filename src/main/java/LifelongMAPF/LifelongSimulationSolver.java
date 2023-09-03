package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CongestionMap;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.RunParametersLNS;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RunParameters_PP;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.PartialSolutionsStrategy;
import Environment.Metrics.InstanceReport;
import LifelongMAPF.AgentSelectors.I_LifelongAgentSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.FailPolicies.FailPolicy;
import LifelongMAPF.FailPolicies.I_SingleAgentFailPolicy;
import LifelongMAPF.FailPolicies.IStayFailPolicy;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import LifelongMAPF.Triggers.I_LifelongPlanningTrigger;
import org.apache.commons.lang.mutable.MutableInt;
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

    /* static fields */
    private static final IStayFailPolicy STAY_ONCE_FAIL_POLICY = new IStayFailPolicy();
    private static final int DEBUG = 2;

    /* fields related to instance */
    /**
     * An offline solver to use for solving online problems.
     */
    protected final I_Solver offlineSolver;
    private final I_LifelongPlanningTrigger planningTrigger;
    private final I_LifelongAgentSelector agentSelector;
    private final Double congestionMultiplier;
    private final PartialSolutionsStrategy partialSolutionsStrategy;
    private final I_SingleAgentFailPolicy SAFailPolicy;
    private final int failPolicyKSafety;
    public final boolean enforceKSafetyBetweenPlanningIterations = false;
    /**
     * How many agents may attempt to plan to get to a target at any point in time.
     */
    public final int targetsReservationsCapacity;

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
    private float avgGroupSizeMetric;
    private float avgFailedAgentsAfterPlanningMetric;
    private float avgFailedAgentsAfterPolicyMetric;
    private float avgReachedIndexInPlanningFractionMetric;
    private List<int[]> numAgentsAndNumIterationsMetric;
    private int numPlanningIterations;
    private CachingDistanceTableHeuristic cachingDistanceTableHeuristic;
    private int numDestinationsAchieved;
    /**
     * Destination that each agent is currently trying to get to.
     */
    Map<LifelongAgent, I_Coordinate> agentsActiveDestination;
    /**
     * Agents that are currently trying to get to each destination.
     */
    Map<I_Coordinate, List<LifelongAgent>> destinationsActiveAgents;
    Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationStartTimes;
    Map<LifelongAgent, List<TimeCoordinate>> agentsActiveDestinationEndTimes;
    private int sumFailPolicyIterations;
    private int countFailPolicyLoops;
    private int maxFailPolicyIterations;
    Set<LifelongAgent> finishedAgents;
    private final Integer selectionLookaheadLength;
    private int totalAStarNodesGenerated;
    private int totalAStarNodesExpanded;
    private int totalAStarRuntimeMS;
    private int totalAStarCalls;

    public LifelongSimulationSolver(I_LifelongPlanningTrigger planningTrigger, I_LifelongAgentSelector agentSelector,
                                    I_LifelongCompatibleSolver offlineSolver, @Nullable Double congestionMultiplier,
                                    @Nullable PartialSolutionsStrategy partialSolutionsStrategy,
                                    @Nullable I_SingleAgentFailPolicy singleAgentFailPolicy, @Nullable Integer selectionLookaheadLength, Integer targetsReservationsCapacity) {
        if(offlineSolver == null) {
            throw new IllegalArgumentException("offlineSolver is mandatory");
        }
        if (!offlineSolver.sharedGoals()){
            throw new IllegalArgumentException("offline solver should have shared goals");
        }
        this.offlineSolver = offlineSolver;
        this.congestionMultiplier = congestionMultiplier;
        this.partialSolutionsStrategy = Objects.requireNonNullElse(partialSolutionsStrategy, new DisallowedPartialSolutionsStrategy());

        this.planningTrigger = Objects.requireNonNullElse(planningTrigger, new ActiveButPlanEndedTrigger());
        this.agentSelector = Objects.requireNonNullElse(agentSelector, new StationaryAgentsSubsetSelector());
        this.failPolicyKSafety = agentSelector.getPlanningFrequency();
        this.name = "Lifelong_" + offlineSolver.name();
        this.SAFailPolicy = Objects.requireNonNullElse(singleAgentFailPolicy, STAY_ONCE_FAIL_POLICY);
        if (selectionLookaheadLength != null && selectionLookaheadLength < 1) {
            throw new IllegalArgumentException("Safety enforcement lookahead must be at least 1 (or null for default value of 1)." +
                    " Given value: " + selectionLookaheadLength);
        }
        this.selectionLookaheadLength = Objects.requireNonNullElse(selectionLookaheadLength, 1);
        this.targetsReservationsCapacity = Objects.requireNonNullElse(targetsReservationsCapacity, Integer.MAX_VALUE);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.lifelongAgents = verifyAndCastAgents(instance.agents);
        this.initialConstraints = parameters.constraints;
        this.lifelongInstance = instance;
        this.random = new Random(42);
        this.reachedTimestepInPlanning = 0;
        this.avgGroupSizeMetric = 0;
        this.avgFailedAgentsAfterPlanningMetric = 0;
        this.avgFailedAgentsAfterPolicyMetric = 0;
        this.numAgentsAndNumIterationsMetric = new ArrayList<>();
        this.numPlanningIterations = 0;
        this.numDestinationsAchieved = 0;

        this.agentsActiveDestinationStartTimes = new HashMap<>();
        this.agentsActiveDestinationEndTimes = new HashMap<>();
        for (LifelongAgent a :
                this.lifelongAgents) {
            agentsActiveDestinationStartTimes.put(a, new ArrayList<>());
            agentsActiveDestinationEndTimes.put(a, new ArrayList<>());
        }
        this.agentsActiveDestination = new HashMap<>();
        this.destinationsActiveAgents = new HashMap<>();

        this.finishedAgents = new HashSet<>();
        if (this.initialConstraints != null){
            this.initialConstraints.sharedSources = true;
            this.initialConstraints.sharedGoals = true;
        }
        this.cachingDistanceTableHeuristic = new CachingDistanceTableHeuristic(1, instance.agents.size());
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
        this.partialSolutionsStrategy.resetState(this.random);
        totalAStarNodesGenerated = 0;
        totalAStarNodesExpanded = 0;
        totalAStarRuntimeMS = 0;
        totalAStarCalls = 0;
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
        int sumFailedAgentsAfterPolicy = 0;
        int sumAttemptedAgentsThatFailed = 0;
        float sumReachedIndexInPlanningFraction = 0;
        int farthestCommittedTime = 0; // at this time locations are committed, and we choose locations for next time

        Solution latestSolution = new Solution();
        for (LifelongAgent a : this.lifelongAgents){
            latestSolution.putPlan(STAY_ONCE_FAIL_POLICY.getFailPolicyPlan(0, a, lifelongInstance.map.getMapLocation(a.source), null));
        }
        List<LifelongAgent> agentsWaitingToStart = new ArrayList<>(this.lifelongAgents);

        Map<LifelongAgent, Agent> lifelongAgentsToTimelyOfflineAgents = updateAgentsDestinationsAndBookkeeping(farthestCommittedTime,
                latestSolution, agentDestinationQueues, this.lifelongAgents);

        Set<Agent> failedAgents = new HashSet<>();

        while (farthestCommittedTime < maxTimeSteps && this.finishedAgents.size() < this.lifelongAgents.size()){

            if (checkTimeout()){
                break;
            }

            Set<Agent> blockedAgentsBeforePlanningIteration = new HashSet<>();
            // done agents get "stay in place once". Same if they were blocked before
            List<SingleAgentPlan> advancedPlansToCurrentTime = getAdvancedPlansForAgents(farthestCommittedTime, latestSolution, lifelongAgentsToTimelyOfflineAgents, this.lifelongAgents);
            // don't check conflicts between planning iterations (they shouldn't happen when k-safe >= planning frequency)
            if (agentSelector.timeToPlan(farthestCommittedTime)){
                // TODO maybe just mark them? It would mean we won't block recursively.
                latestSolution = enforceSafeExecution(selectionLookaheadLength, advancedPlansToCurrentTime,
                        farthestCommittedTime, blockedAgentsBeforePlanningIteration,
                        new RemovableConflictAvoidanceTableWithContestedGoals(advancedPlansToCurrentTime, null), STAY_ONCE_FAIL_POLICY);
            }
            else {
                latestSolution = new Solution(advancedPlansToCurrentTime);
            }

            Set<Agent> selectedTimelyOfflineAgentsSubset = new HashSet<>(lifelongAgentsToTimelyOfflineAgents.values());
            selectedTimelyOfflineAgentsSubset = selectedTimelyOfflineAgentsSubset.stream().filter(agentSelector.getAgentSelectionPredicate(instance, latestSolution
                    , lifelongAgentsToTimelyOfflineAgents, agentsWaitingToStart, agentDestinationQueues, agentsActiveDestination, failedAgents)).collect(Collectors.toSet());

            failedAgents = new HashSet<>();
            Set<LifelongAgent> notSelectedAgents = getUnchangingAgents(selectedTimelyOfflineAgentsSubset);
            List<SingleAgentPlan> nextPlansForNotSelectedAgents = subsetPlansCollection(latestSolution, notSelectedAgents);
            RemovableConflictAvoidanceTableWithContestedGoals cat = new RemovableConflictAvoidanceTableWithContestedGoals(nextPlansForNotSelectedAgents, null);
            int numFailedAgentsAfterPlanner = 0;
            Integer reachedIndexInPlanner = null;

            if ( ! selectedTimelyOfflineAgentsSubset.isEmpty()){ // solve an offline MAPF problem of the current conditions

                agentsWaitingToStart.removeAll(selectedTimelyOfflineAgentsSubset);
                numPlanningIterations++;
                sumGroupSizes += selectedTimelyOfflineAgentsSubset.size();

                MAPF_Instance timelyOfflineProblem = getTimelyOfflineProblem(farthestCommittedTime, selectedTimelyOfflineAgentsSubset);
                RunParameters timelyOfflineProblemRunParameters = getTimelyOfflineProblemRunParameters(farthestCommittedTime,
                        nextPlansForNotSelectedAgents, selectedTimelyOfflineAgentsSubset, failedAgents, cat);

                Solution subgroupSolution = offlineSolver.solve(timelyOfflineProblem, timelyOfflineProblemRunParameters); // TODO solver strategy ?
                if (DEBUG >= 1 && subgroupSolution != null){
                    checkSolutionStartTimes(subgroupSolution, farthestCommittedTime);
                }
                digestSubproblemReport(timelyOfflineProblemRunParameters.instanceReport, timelyOfflineProblem);

                if (offlineSolver instanceof PrioritisedPlanning_Solver){
                    reachedIndexInPlanner = timelyOfflineProblemRunParameters.instanceReport.getIntegerValue(PrioritisedPlanning_Solver.maxReachedIndexBeforeTimeoutString);
                    sumReachedIndexInPlanningFraction += (float) reachedIndexInPlanner / (float) selectedTimelyOfflineAgentsSubset.size();
                }
                latestSolution = addMissingAgents(farthestCommittedTime, selectedTimelyOfflineAgentsSubset, nextPlansForNotSelectedAgents, subgroupSolution, failedAgents, this.lifelongInstance, cat);
                numFailedAgentsAfterPlanner = failedAgents.size();
                sumAttemptedAgentsThatFailed += numFailedAgentsAfterPlanner;

                if (DEBUG >= 3){
                    System.out.printf("timestep %d, solution after planner: %s%n",farthestCommittedTime, latestSolution);
                }
                if (!enforceKSafetyBetweenPlanningIterations){
                    latestSolution = enforceSafeExecution(failPolicyKSafety, latestSolution, farthestCommittedTime,
                            failedAgents, cat, SAFailPolicy);
                }
            }
            if (enforceKSafetyBetweenPlanningIterations){
                latestSolution = enforceSafeExecution(failPolicyKSafety, latestSolution, farthestCommittedTime,
                        failedAgents, cat, SAFailPolicy);
            }
            if (DEBUG >= 1){
                // just the next step because this also happens between planning iterations
                verifyNextKStepsSafe(null, latestSolution, 1);
            }

            sumFailedAgentsAfterPolicy += failedAgents.size();

            if (DEBUG >= 2){
                printProgressAndStats(farthestCommittedTime, selectedTimelyOfflineAgentsSubset.size(), numFailedAgentsAfterPlanner, failedAgents.size(), reachedIndexInPlanner);
                if (DEBUG >= 3){
                    System.out.println(latestSolution);
                }
            }

            solutionsAtTimes.put(farthestCommittedTime, latestSolution);
            farthestCommittedTime++;
            // this is for the next iteration! must happen after advancing farthest committed time!
            lifelongAgentsToTimelyOfflineAgents = updateAgentsDestinationsAndBookkeeping(farthestCommittedTime,
                    latestSolution, agentDestinationQueues, this.lifelongAgents);
        }
        this.avgGroupSizeMetric = (float) sumGroupSizes / (float) numPlanningIterations;
        this.avgFailedAgentsAfterPlanningMetric = (float) sumAttemptedAgentsThatFailed / (float) numPlanningIterations;
        this.avgFailedAgentsAfterPolicyMetric = (float) sumFailedAgentsAfterPolicy / (float) numPlanningIterations;
        this.avgReachedIndexInPlanningFractionMetric = sumReachedIndexInPlanningFraction / (float) numPlanningIterations;
        this.reachedTimestepInPlanning = farthestCommittedTime;

        if (DEBUG >= 1){
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
        if(DEBUG >= 1 && plansSubset.size() < notSelectedAgents.size() ){
            throw new RuntimeException(String.format("Subset doesn't contain all agents: %d of %d", plansSubset.size(), notSelectedAgents.size()));
        }
        return plansSubset;
    }

    private void printProgressAndStats(int farthestCommittedTime, int selectedTimelyOfflineAgentsSubset, int numAgentsWithPlansInSolutionBeforeEnforcingSafety, int numFailedAgents, Integer reachedIndexInPlanner) {
        System.out.print("\rLifelongSim: ");
        System.out.printf("iteration %1$3s, @ timestep %2$3s, #chosen/#reachedIndex/#failed(planner)/#failed(FP) %3$3s",
                numPlanningIterations, farthestCommittedTime, selectedTimelyOfflineAgentsSubset);
        System.out.printf("/%1$3s", reachedIndexInPlanner != null ? reachedIndexInPlanner : "N/A");
        System.out.printf("/%1$3s", numAgentsWithPlansInSolutionBeforeEnforcingSafety);
        System.out.printf("/%1$3s", numFailedAgents);
        System.out.printf(", destinations achieved (prev iter.) %d [avg_thr %.2f]",
                this.numDestinationsAchieved, (farthestCommittedTime > 0 ? (float)(numDestinationsAchieved) / farthestCommittedTime : 0));
        if (DEBUG >= 1 && DEBUG < 3){
            System.out.print('\r');
        }
        if (reachedIndexInPlanner != null && reachedIndexInPlanner + 1 > selectedTimelyOfflineAgentsSubset){
            throw new RuntimeException("ERROR: reached index in planner is larger than the number of agents in the subgroup");
        }
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
    private Solution addMissingAgents(int farthestCommittedTime, Set<Agent> selectedTimelyOfflineAgentsSubset,
                                      List<SingleAgentPlan> nextPlansForNotSelectedAgents, @Nullable Solution subgroupSolution,
                                      Set<Agent> failedAgents, MAPF_Instance lifelongInstance, RemovableConflictAvoidanceTableWithContestedGoals catWithNotSelectedAgents) {
        // handle fails
        List<SingleAgentPlan> invalidSolutionAsList = new ArrayList<>();
        if (subgroupSolution != null){
            catWithNotSelectedAgents.addAll(subgroupSolution);
        }

        for (Agent a :
                selectedTimelyOfflineAgentsSubset) {
            if (subgroupSolution == null || subgroupSolution.getPlanFor(a) == null){
                I_Location agentLocation = lifelongInstance.map.getMapLocation(a.source);
                // TODO replace with stay at this stage?
                SingleAgentPlan failPlan = this.SAFailPolicy.getFailPolicyPlan(farthestCommittedTime, a, agentLocation, catWithNotSelectedAgents);
                invalidSolutionAsList.add(failPlan);
                catWithNotSelectedAgents.addPlan(failPlan);
                failedAgents.add(a);
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
     * Gets a solution with conflicts and returns a solution that is k-safe.
     *
     * @param detectConflictsHorizon          How far to look ahead for conflicts.
     * @param solutionThatMayContainConflicts a solution with any number of conflicts
     * @param failedAgents                   agents that are already failed.
     * @param cat                            a conflict avoidance table that contains all current plans for all agents, including failed agents.
     * @return a repaired solution with no conflicts at the next time step.
     */
    private Solution enforceSafeExecution(int detectConflictsHorizon, Iterable<? extends SingleAgentPlan> solutionThatMayContainConflicts,
                                          int farthestCommittedTime, Set<Agent> failedAgents,
                                          @NotNull RemovableConflictAvoidanceTableWithContestedGoals cat,
                                          I_SingleAgentFailPolicy SAFailPolicy){
        MutableInt iterations = new MutableInt(0);
        Solution solutionWithoutConflicts = FailPolicy.getKSafeSolution(detectConflictsHorizon, solutionThatMayContainConflicts, farthestCommittedTime, failedAgents, cat, SAFailPolicy, iterations);

        this.sumFailPolicyIterations += iterations.intValue();
        this.countFailPolicyLoops++;
        this.maxFailPolicyIterations = Math.max(this.maxFailPolicyIterations, iterations.intValue());
        if (DEBUG >= 1){
            verifyNextKStepsSafe(solutionThatMayContainConflicts, solutionWithoutConflicts, detectConflictsHorizon);
        }
        return solutionWithoutConflicts;
    }

    private static void verifyNextKStepsSafe(@Nullable Iterable<? extends SingleAgentPlan> solutionThatMayContainConflicts, Solution solutionSupposedlyWithoutConflicts, int kSafety) {
        Solution kStepSolution = getKStepSolution(solutionSupposedlyWithoutConflicts, kSafety);
        A_Conflict conflict = kStepSolution.arbitraryConflict(false, false);
        if ( conflict != null){
            throw new RuntimeException(String.format("""
                    Got conflicts in next step after supposedly enforcing safe next time step execution.\s
                     original solution: %s
                     solution after enforcement: %s
                     next step solution : %s
                     conflict : %s"""
                    , solutionThatMayContainConflicts != null ? solutionThatMayContainConflicts.toString() : "",
                    solutionSupposedlyWithoutConflicts, kStepSolution, conflict));
        }
    }

    @NotNull
    public static Solution getKStepSolution(Solution solutionWithoutConflicts, int k) {
        Solution kStepSolution = new Solution();
        for (SingleAgentPlan plan :
                solutionWithoutConflicts) {
            SingleAgentPlan kStepPlan = new SingleAgentPlan(plan.agent);
            for (Move move : plan) {
                kStepPlan.addMove(move);
                if (kStepPlan.size() == k){
                    break;
                }
            }
            kStepSolution.putPlan(kStepPlan);
        }
        return kStepSolution;
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
                advancedPlans.add(STAY_ONCE_FAIL_POLICY.getFailPolicyPlan(farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents.get(lifelongAgent), existingPlan.getLastMove().currLocation, null));
            }
            else {
                // continue with current plan
                advancedPlans.add(getAdvancedPlan(farthestCommittedTime, lifelongAgentsToTimelyOfflineAgents, latestSolution.getPlanFor(lifelongAgent)));
            }
        }
        return advancedPlans;
    }

    private Map<Agent, Queue<I_Coordinate>> getDestinationQueues(MAPF_Instance instance) {
        Map<Agent, Queue<I_Coordinate>> result = new HashMap<>(instance.agents.size());
        for (Agent agent : instance.agents){
            result.put(agent, new ArrayDeque<>(((LifelongAgent)agent).waypoints));
        }
        return result;
    }

    /**
     * Big method that does too much.
     * Update the destination start and end times for each agent.
     * Assign each agent a destination (or temporary destination) as needed.
     * Map each lifelong agent to a suitable offline representation at time.
     */
    @NotNull
    private Map<LifelongAgent, Agent> updateAgentsDestinationsAndBookkeeping(int farthestCommittedTime, @NotNull Solution previousSolution,
                                                                             Map<Agent, Queue<I_Coordinate>> agentDestinationQueues,
                                                                             List<LifelongAgent> agentsSubset) {
        // TODO can we brake this into smaller pieces that only do one thing?
        Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents = new HashMap<>();
        Map<LifelongAgent, I_Coordinate> initialCoordinatesAtTime = new HashMap<>();
        for (LifelongAgent agent : agentsSubset){

            List<TimeCoordinate> destinationEndTimes = agentsActiveDestinationEndTimes.get(agent);
            SingleAgentPlan agentPlan = previousSolution.getPlanFor(agent);
            int lastExecutedPlannedMoveTime = Math.min(farthestCommittedTime, agentPlan.getEndTime());

            // for the first iteration take the first destination in the queue as the source
            I_Coordinate initialCoordinateAtTime;
            if (farthestCommittedTime == 0){
                initialCoordinateAtTime = agentDestinationQueues.get(agent).poll();
                if (initialCoordinateAtTime == null){
                    throw new IllegalArgumentException("agent with no destinations");
                }
                bookkeepingGotNewDestination(0, agent, initialCoordinateAtTime);
                bookkeepingFinishedDestination(destinationEndTimes, 0, initialCoordinateAtTime, true, agent, false);
            }
            else {
                initialCoordinateAtTime = agentPlan.moveAt(lastExecutedPlannedMoveTime).currLocation.getCoordinate();
            }
            initialCoordinatesAtTime.put(agent, initialCoordinateAtTime);

            // for the first iteration there is no previous destination, otherwise it's whichever destination was active
            I_Coordinate previousDestinationCoordinate;
            if (farthestCommittedTime == 0){
                previousDestinationCoordinate = null;
            }
            else { // get currently active destination
                previousDestinationCoordinate = agentsActiveDestination.get(agent);
            }

            int reachedDestinationTime = reachedDestinationTime(agentPlan, lastExecutedPlannedMoveTime, previousDestinationCoordinate);
            // for the first iteration, or if finished previous destination, dequeue next one destination, else continue towards current destination
            I_Coordinate nextDestinationCoordinate;
            if (previousDestinationCoordinate == null){ // first instance
                nextDestinationCoordinate = agentDestinationQueues.get(agent).poll();
                if (nextDestinationCoordinate == null) { // no more destinations in the queue
                    throw new IllegalArgumentException("Agent only has a source, not even one destination beyond.");
                }
                else {
                    bookkeepingGotNewDestination(farthestCommittedTime + 1, agent, nextDestinationCoordinate);
                }
            }
            else if (reachedDestinationTime < 0) // still on the way to current destination
            {
                nextDestinationCoordinate = previousDestinationCoordinate; // preserve current destination
            }
            else { // achieved a destination
                nextDestinationCoordinate = agentDestinationQueues.get(agent).poll();
                if (nextDestinationCoordinate == null){ // achieved the last destination
                    nextDestinationCoordinate = previousDestinationCoordinate; // keep last destination as placeholder destination
                    if (! destinationEndTimes.get(destinationEndTimes.size()-1).coordinate.equals(previousDestinationCoordinate)){
                        // achieved the last destination for the first time (between previous (exclusive) and current (inclusive) iteration)
                        bookkeepingFinishedDestination(destinationEndTimes, farthestCommittedTime, previousDestinationCoordinate, false, agent, false);
                        finishedAgents.add(agent);
                    }
                }
                else { // got a new destination
                    bookkeepingFinishedDestination(destinationEndTimes, reachedDestinationTime, previousDestinationCoordinate, false, agent, false);
                    bookkeepingGotNewDestination(farthestCommittedTime + 1, agent, nextDestinationCoordinate);
                }
            }
            setOfflineAgentFromCurrentLocationToATarget(initialCoordinatesAtTime, lifelongAgentsToOfflineAgents, agent, nextDestinationCoordinate);
        }

//        skipOvercapacityDestinations(agentsSubset, initialCoordinatesAtTime, lifelongAgentsToOfflineAgents, agentDestinationQueues, farthestCommittedTime);
        assignTemporaryDestinationsAsNeeded(agentsSubset, initialCoordinatesAtTime, lifelongAgentsToOfflineAgents);

        return lifelongAgentsToOfflineAgents;
    }

//    private void skipOvercapacityDestinations(List<LifelongAgent> agentsSubset, Map<LifelongAgent, I_Coordinate> initialCoordinatesAtTime,
//                                              Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents, Map<Agent, Queue<I_Coordinate>> agentDestinationQueues,
//                                              int farthestCommittedTime) {
//        for (LifelongAgent agent : agentsSubset){
//            // Iterate over agents. If an agent is trying to get to a destination that exceeds capacity, skip destinations
//            // until finding a destination of the same subtype that isn't over-capacity.
//            I_Coordinate nextDestinationCoordinate = agentsActiveDestination.get(agent);
//            List<LifelongAgent> agentsTryingToGetToDestination = destinationsActiveAgents.get(nextDestinationCoordinate);
//            if (agentsTryingToGetToDestination.size() > targetsReservationsCapacity){ // destination exceeds capacity
//                int agentIndexInList = agentsTryingToGetToDestination.indexOf(agent); // TODO something faster?
//                if (agentIndexInList >= targetsReservationsCapacity){ // agent is one of the ones that exceeds capacity
//                    // skip destinations until finding a destination with the same subtype that isn't over-capacity
//                    String originalNextDestinationSubtype = getDestinationSubtype(nextDestinationCoordinate);
//                    String newNextDestinationSubtype = "";
//                    List<I_Coordinate> skippedDestinations = new ArrayList<>();
//                    do {
//                        skippedDestinations.add(nextDestinationCoordinate);
//                        bookkeepingFinishedDestination(agentsActiveDestinationEndTimes.get(agent), farthestCommittedTime,
//                                nextDestinationCoordinate, false, agent, true);
//
//                        nextDestinationCoordinate = agentDestinationQueues.get(agent).poll();
//                        bookkeepingGotNewDestination(farthestCommittedTime + 1, agent, nextDestinationCoordinate);
//
//                        if (nextDestinationCoordinate == null) continue;
//                        newNextDestinationSubtype = getDestinationSubtype(nextDestinationCoordinate);
//                    }
//                    while (!newNextDestinationSubtype.equals(originalNextDestinationSubtype)
//                            || destinationsActiveAgents.get(nextDestinationCoordinate).size() > targetsReservationsCapacity);
//
//                    if (nextDestinationCoordinate == null){
//                        // no more destinations
//                        finishedAgents.add(agent);
//                        // and allow the temporary destinations logic to assign a temporary destination later
//                    }
//                    else {
//                        setOfflineAgentFromCurrentLocationToATarget(initialCoordinatesAtTime, lifelongAgentsToOfflineAgents, agent, nextDestinationCoordinate);
//                    }
//                }
//            }
//        }
//    }

    private static void setOfflineAgentFromCurrentLocationToATarget(Map<LifelongAgent, I_Coordinate> initialCoordinatesAtTime,
                                                                     Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents,
                                                                    LifelongAgent agent, I_Coordinate targetCoordinate) {
        lifelongAgentsToOfflineAgents.put(agent, new Agent(agent.iD, initialCoordinatesAtTime.get(agent), targetCoordinate));
    }

    @NotNull
    private String getDestinationSubtype(I_Coordinate nextDestinationCoordinate) {
        List<String> destinationCoordinateSubtypes = lifelongInstance.map.getMapLocation(nextDestinationCoordinate).getSubtypes();
        return destinationCoordinateSubtypes != null && !destinationCoordinateSubtypes.isEmpty() ?
                destinationCoordinateSubtypes.get(0) : "";
    }

    private void assignTemporaryDestinationsAsNeeded(List<LifelongAgent> agentsSubset, Map<LifelongAgent, I_Coordinate> initialCoordinatesAtTime, Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents) {
        for (LifelongAgent agent : agentsSubset){
            // Iterate over agents. If agent is finished or trying to get to a destination that exceeds capacity, give it a temporary destination.
            if (finishedAgents.contains(agent)){
                assignAgentWithTemporaryDestination(agent, initialCoordinatesAtTime, lifelongAgentsToOfflineAgents);
            }
            else {
                I_Coordinate nextDestinationCoordinate = agentsActiveDestination.get(agent);
                List<LifelongAgent> agentsTryingToGetToDestination = destinationsActiveAgents.get(nextDestinationCoordinate);
                if (agentsTryingToGetToDestination.size() > targetsReservationsCapacity){ // destination exceeds capacity
                    int agentIndexInList = agentsTryingToGetToDestination.indexOf(agent); // TODO something faster?
                    if (agentIndexInList >= targetsReservationsCapacity){ // agent is one of the ones that exceeds capacity
                        assignAgentWithTemporaryDestination(agent, initialCoordinatesAtTime, lifelongAgentsToOfflineAgents);
                    }
                }
            }
        }
    }

    private static void assignAgentWithTemporaryDestination(LifelongAgent agent, Map<LifelongAgent, I_Coordinate> initialCoordinatesAtTime, Map<LifelongAgent, Agent> lifelongAgentsToOfflineAgents) {
        // this agent is trying to exceed capacity, give it a temporary destination to stay in place
        // TODO use instead a destination that nobody else wants? or a fail policy? or a near location with low centrality? and low h? low distance from current location?
        setOfflineAgentFromCurrentLocationToATarget(initialCoordinatesAtTime, lifelongAgentsToOfflineAgents, agent, initialCoordinatesAtTime.get(agent));
    }

    private static int reachedDestinationTime(SingleAgentPlan plan, int lastExecutedPlannedMoveTime, I_Coordinate destination) {
        for (int t = plan.getFirstMoveTime(); t <= lastExecutedPlannedMoveTime; t++) {
            if (plan.moveAt(t).currLocation.getCoordinate().equals(destination)){
                return t;
            }
        }
        return -1;
    }

    private void bookkeepingGotNewDestination(int willStartWorkingOnItAtTime, LifelongAgent agent, I_Coordinate nextDestinationCoordinate) {
        if (nextDestinationCoordinate == null){
            agentsActiveDestination.remove(agent);
        }
        else {
            List<TimeCoordinate> destinationStartTimes = agentsActiveDestinationStartTimes.get(agent);
            destinationStartTimes.add(new TimeCoordinate(willStartWorkingOnItAtTime, nextDestinationCoordinate));
            agentsActiveDestination.put(agent, nextDestinationCoordinate);
            destinationsActiveAgents.computeIfAbsent(nextDestinationCoordinate, (a) -> new LinkedList<>()).add(agent);
        }
    }

    /**
     * @param initializationIteration If the agent is at its initial start location (at the start of the instance)
     * @param skipped If we're finished with the destination because we're skipping it
     */
    private void bookkeepingFinishedDestination(List<TimeCoordinate> destinationEndTimes, int time, I_Coordinate achievedDestinationCoordinate,
                                                boolean initializationIteration, LifelongAgent agent, boolean skipped) {
        destinationEndTimes.add(new TimeCoordinate(time, achievedDestinationCoordinate));
        if (!(initializationIteration || skipped))
            this.numDestinationsAchieved++;
        if (DEBUG >= 1){
            if (!achievedDestinationCoordinate.equals(agentsActiveDestination.get(agent))){
                throw new IllegalArgumentException("Agent " + agent + " achieved destination " + achievedDestinationCoordinate + " but was heading to " + agentsActiveDestination.get(agent) + ".");
            }
            List<TimeCoordinate> destinationStartTimes = agentsActiveDestinationStartTimes.get(agent);
            if (!( achievedDestinationCoordinate.equals(destinationStartTimes.get(destinationStartTimes.size() - 1).coordinate))){
                throw new IllegalArgumentException("Agent " + agent + " achieved destination " + achievedDestinationCoordinate + " but was heading to " + destinationStartTimes.get(destinationStartTimes.size() - 1).coordinate + ".");
            }
        }
        destinationsActiveAgents.get(achievedDestinationCoordinate).remove(agent);
    }

    private MAPF_Instance getTimelyOfflineProblem(int farthestCommittedTime, Set<Agent> timelyOfflineAgentsSubset) {
        List<Agent> shuffledAgentsSubset = new ArrayList<>(timelyOfflineAgentsSubset);
        Collections.shuffle(shuffledAgentsSubset, this.random);
        return new MAPF_Instance(this.lifelongInstance.name + " subproblem at " + farthestCommittedTime,
                this.lifelongInstance.map, shuffledAgentsSubset.toArray(Agent[]::new),
                this.lifelongInstance.extendedName + " subproblem at " + farthestCommittedTime);
    }

    private RunParameters getTimelyOfflineProblemRunParameters(int farthestCommittedTime, List<SingleAgentPlan> nextPlansForNotSelectedAgents,
                                                               Set<Agent> selectedTimelyOfflineAgentsSubset, Set<Agent> failedAgents,
                                                               RemovableConflictAvoidanceTableWithContestedGoals cat) {
        // protect the plans of agents not included in the subset
        ConstraintSet constraints = this.initialConstraints != null ? new ConstraintSet(this.initialConstraints): new ConstraintSet();
        constraints.sharedSources = true;
        constraints.sharedGoals = true;
        nextPlansForNotSelectedAgents.forEach(plan -> constraints.addAll(constraints.allConstraintsForPlan(plan)));

        AStarGAndH costAndHeuristic = this.cachingDistanceTableHeuristic;
        if (congestionMultiplier != null && congestionMultiplier > 0){
            List<Agent> agents = new ArrayList<>(selectedTimelyOfflineAgentsSubset);
            costAndHeuristic = new DistanceTableAStarHeuristic(agents, this.lifelongInstance.map, null, new CongestionMap(nextPlansForNotSelectedAgents, congestionMultiplier));
        }

        long hardTimeout = Math.min(minResponseTime, Math.max(0, super.maximumRuntime - (getCurrentTimeMS_NSAccuracy() - super.startTime)));

        RunParameters runParameters = new RunParameters(hardTimeout, constraints, new InstanceReport(), null, Math.min(minResponseTime, hardTimeout), farthestCommittedTime, this.random);
        if (offlineSolver instanceof LargeNeighborhoodSearch_Solver){
            RunParametersLNS runParametersLNS = new RunParametersLNS(runParameters, costAndHeuristic);
            runParametersLNS.partialSolutionsStrategy = this.partialSolutionsStrategy;
            // TODO failed agents, CAT ?
            runParameters =runParametersLNS;
        }
        else if (offlineSolver instanceof PrioritisedPlanning_Solver){
            RunParameters_PP runParameters_pp = new RunParameters_PP(runParameters, costAndHeuristic);
            runParameters_pp.partialSolutionsStrategy = this.partialSolutionsStrategy;
            runParameters_pp.failedAgents = failedAgents;
            runParameters_pp.conflictAvoidanceTable = cat;
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

    @Override
    protected void digestSubproblemReport(InstanceReport subproblemReport) {
        throw new RuntimeException("Should not be called. Use digestSubproblemReport(InstanceReport subproblemInstanceReport, MAPF_Instance timelyOfflineProblem) instead.");
    }

    protected void digestSubproblemReport(InstanceReport subproblemInstanceReport, MAPF_Instance timelyOfflineProblem) {
        if (offlineSolver instanceof PrioritisedPlanning_Solver || offlineSolver instanceof CBS_Solver || offlineSolver instanceof LargeNeighborhoodSearch_Solver){
            Integer AStarNodesGenerated = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel);
            this.totalAStarNodesGenerated += AStarNodesGenerated == null ? 0 : AStarNodesGenerated;
            Integer AStarNodesExpanded = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
            this.totalAStarNodesExpanded += AStarNodesExpanded == null ? 0 : AStarNodesExpanded;
            Integer AStarRuntime = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS);
            this.totalAStarRuntimeMS += AStarRuntime == null ? 0 : AStarRuntime;
            Integer AStarCalls = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.totalLowLevelCalls);
            this.totalAStarCalls += AStarCalls == null ? 0 : AStarCalls;
        }

        Integer offlineSolverRuntime = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        super.totalLowLevelTimeMS += offlineSolverRuntime == null ? 0 : offlineSolverRuntime;
        Integer offlineSolverGeneratedNodes = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        super.totalLowLevelNodesGenerated += offlineSolverGeneratedNodes == null ? 0 : offlineSolverGeneratedNodes;
        Integer offlineSolverExpandedNodes = subproblemInstanceReport.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        super.totalLowLevelNodesExpanded += offlineSolverExpandedNodes == null ? 0 : offlineSolverExpandedNodes;
        super.totalLowLevelCalls++;

        if (this.offlineSolver instanceof  PrioritisedPlanning_Solver){
            int numAgents = timelyOfflineProblem.agents.size();
            int numAttempts = subproblemInstanceReport.getIntegerValue(PrioritisedPlanning_Solver.countInitialAttemptsMetricString) + subproblemInstanceReport.getIntegerValue(PrioritisedPlanning_Solver.countContingencyAttemptsMetricString);
            this.numAgentsAndNumIterationsMetric.add(new int[]{numAgents, numAttempts});
            this.sumFailPolicyIterations += subproblemInstanceReport.getIntegerValue("fail policy iterations") != null ?
                    subproblemInstanceReport.getIntegerValue("fail policy iterations") : 0;
        }
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);

        super.instanceReport.putIntegerValue("reachedTimestepInPlanning", this.reachedTimestepInPlanning);
        super.instanceReport.putIntegerValue("numPlanningIterations", this.numPlanningIterations);
        super.instanceReport.putFloatValue("avgGroupSize", this.avgGroupSizeMetric);
        super.instanceReport.putFloatValue("avgFailedAgentsAfterPlanning", this.avgFailedAgentsAfterPlanningMetric);
        super.instanceReport.putFloatValue("avgFailedAgentsAfterPolicy", this.avgFailedAgentsAfterPolicyMetric);

        if (!numAgentsAndNumIterationsMetric.isEmpty()){ // only when using PrP as the offline solver
            this.numAgentsAndNumIterationsMetric.sort(Comparator.comparingInt(agentsAndIterations -> agentsAndIterations[1]));
            super.instanceReport.putFloatValue("numAttempts10thPercentile", this.numAgentsAndNumIterationsMetric.get((int)(numAgentsAndNumIterationsMetric.size() * 0.1))[1]);
            super.instanceReport.putFloatValue("numAttempts50thPercentile", this.numAgentsAndNumIterationsMetric.get((int)(numAgentsAndNumIterationsMetric.size() * 0.5))[1]);
            super.instanceReport.putFloatValue("numAttempts90thPercentile", this.numAgentsAndNumIterationsMetric.get((int)(numAgentsAndNumIterationsMetric.size() * 0.9))[1]);

            this.numAgentsAndNumIterationsMetric.sort(Comparator.comparingInt(agentsAndIterations -> agentsAndIterations[0]));
            double sumIterations = 0;
            double sumIterationsOver100Agents = 0;
            int numSamplesOver100Agents = 0;
            double sumIterationsOver200Agents = 0;
            int numSamplesOver200Agents = 0;
            for (int[] ints : numAgentsAndNumIterationsMetric) {
                int agents = ints[0];
                int iterations = ints[1];
                sumIterations += iterations;
                if (agents >= 200) {
                    sumIterationsOver200Agents += iterations;
                    numSamplesOver200Agents += 1;
                }
                if (agents >= 100) {
                    sumIterationsOver100Agents += iterations;
                    numSamplesOver100Agents += 1;
                }
            }
            super.instanceReport.putFloatValue("averageNumAttempts", (float) (sumIterations / numAgentsAndNumIterationsMetric.size()));
            super.instanceReport.putFloatValue("averageNumAttemptsOver100Agents", (float) (sumIterationsOver100Agents / numSamplesOver100Agents));
            super.instanceReport.putFloatValue("averageNumAttemptsOver200Agents", (float) (sumIterationsOver200Agents / numSamplesOver200Agents));
        }
        if (offlineSolver instanceof PrioritisedPlanning_Solver || offlineSolver instanceof CBS_Solver || offlineSolver instanceof LargeNeighborhoodSearch_Solver){
            super.instanceReport.putIntegerValue("totalAStarNodesGenerated", this.totalAStarNodesGenerated);
            super.instanceReport.putIntegerValue("totalAStarNodesExpanded", this.totalAStarNodesExpanded);
            super.instanceReport.putIntegerValue("totalAStarRuntimeMS", this.totalAStarRuntimeMS);
            super.instanceReport.putIntegerValue("totalAStarCalls", this.totalAStarCalls);
        }

        LifelongSolution lifelongSolution = ((LifelongSolution)solution);
        super.instanceReport.putStringValue("waypointTimes", lifelongSolution.agentsWaypointArrivalTimes());

        super.instanceReport.putIntegerValue("SOC", lifelongSolution.sumIndividualCosts());
        super.instanceReport.putIntegerValue("makespan", lifelongSolution.makespan());
        super.instanceReport.putIntegerValue("timeTo50%Completion", lifelongSolution.timeToXProportionCompletion(0.5));
        super.instanceReport.putIntegerValue("timeTo80%Completion", lifelongSolution.timeToXProportionCompletion(0.8));
        super.instanceReport.putIntegerValue("throughputAtT25", lifelongSolution.throughputAtT(25));
        super.instanceReport.putIntegerValue("throughputAtT50", lifelongSolution.throughputAtT(50));
        super.instanceReport.putIntegerValue("throughputAtT75", lifelongSolution.throughputAtT(75));
        super.instanceReport.putIntegerValue("throughputAtT100", lifelongSolution.throughputAtT(100));
        super.instanceReport.putIntegerValue("throughputAtT150", lifelongSolution.throughputAtT(150));
        super.instanceReport.putIntegerValue("throughputAtT200", lifelongSolution.throughputAtT(200));
        super.instanceReport.putIntegerValue("throughputAtT250", lifelongSolution.throughputAtT(250));
        super.instanceReport.putIntegerValue("throughputAtT300", lifelongSolution.throughputAtT(300));
        super.instanceReport.putIntegerValue("throughputAtT400", lifelongSolution.throughputAtT(400));
        super.instanceReport.putIntegerValue("throughputAtT500", lifelongSolution.throughputAtT(500));
        super.instanceReport.putIntegerValue("maxFailPolicyIterations", this.maxFailPolicyIterations);

        super.instanceReport.putFloatValue("averageThroughput", lifelongSolution.averageThroughput());
        super.instanceReport.putFloatValue("averageIndividualThroughput", lifelongSolution.averageIndividualThroughput());
        super.instanceReport.putFloatValue("avgFailPolicyIterations", this.sumFailPolicyIterations / (float)this.countFailPolicyLoops);
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.initialConstraints = null;
        this.lifelongInstance = null;
        this.random = null;
        this.agentsActiveDestination = null;
        this.destinationsActiveAgents = null;
        this.agentsActiveDestinationStartTimes = null;
        this.agentsActiveDestinationEndTimes = null;
        this.finishedAgents = null;
        this.numAgentsAndNumIterationsMetric = null;
        this.cachingDistanceTableHeuristic = null;
        this.lifelongAgents = null;
    }
}
