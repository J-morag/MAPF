package BasicCBS.Solvers.PrioritisedPlanning;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.AStar.CachingDistanceTableHeuristic;
import BasicCBS.Solvers.AStar.RunParameters_SAAStar;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicCBS.Solvers.*;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;

import java.util.*;

/**
 * An implementation of the Prioritised Planning algorithm for Multi Agent Path Finding.
 * It solves {@link MAPF_Instance MAPF problems} very quickly, but does not guarantee optimality, and will very likely
 * return a sub-optimal {@link Solution}.
 * Agents disappear at goal!
 */
public class PrioritisedPlanning_Solver extends A_Solver {

    /*  = Fields =  */
    /*  =  = Fields related to the MAPF instance =  */
    /**
     * An array of {@link Agent}s to plan for, ordered by priority (descending).
     */
    private List<Agent> agents;

    /*  =  = Fields related to the run =  */

    private ConstraintSet constraints;

    private Random random;

    /*  =  = Fields related to the class instance =  */

    /**
     * A {@link I_Solver solver}, to be used for solving sub-problems where only one agent is to be planned for, and the
     * existing {@link BasicCBS.Solvers.SingleAgentPlan plans} for other {@link Agent}s are to be avoided.
     */
    private final I_Solver lowLevelSolver;
    /**
     * How to sort the agents. This sort determines their priority. The first agent will be treated as having
     * the highest priority, the one after will have the second highest priority, and so forth.
     */
    private final Comparator<Agent> agentComparator;

    /**
     * How many random restarts to perform. Will reorder the agents and re-plan this many times. will return the best solution found.
     * Total number of runs will be this + 1, or less if a timeout occurs (may still return a valid solution when that happens).
     */
    private final int restarts;

    /**
     * How to perform restarts.
     * DeterministicRescheduling is from: Andreychuk, Anton, and Konstantin Yakovlev. "Two techniques that enhance the performance of multi-robot prioritized path planning." arXiv preprint arXiv:1805.01270 (2018).
     */
    public enum RestartStrategy{
        randomRestarts, deterministicRescheduling
    }

    private final RestartStrategy restartStrategy;

    /**
     * The cost function to evaluate solutions with.
     */
    private SolutionCostFunction solutionCostFunction;

    public interface SolutionCostFunction{
        float solutionCost(Solution solution);
    }

      /**
     * if agents share goals, they will not conflict at their goal.
     */
    private final boolean sharedGoals;


    /*  = Constructors =  */

    /**
     * Constructor.
     * @param lowLevelSolver A {@link I_Solver solver}, to be used for solving sub-problems where only one agent is to
     *                      be planned for, and the existing {@link BasicCBS.Solvers.SingleAgentPlan plans} for other
     *                      {@link Agent}s are to be avoided.
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver) {
        this(lowLevelSolver, null, null, null, null, null);
    }

    /**
     * Constructor.
     * @param agentComparator How to sort the agents. This sort determines their priority. High priority first.
     */
    public PrioritisedPlanning_Solver(Comparator<Agent> agentComparator) {
        this(null, agentComparator, null, null, null, null);
    }

    /**
     * Constructor.
     * @param lowLevelSolver A {@link I_Solver solver}, to be used for solving sub-problems for only one agent.
     * @param agentComparator How to sort the agents. This sort determines their priority. High priority first.
     * @param restarts How many random restarts to perform. Will reorder the agents and re-plan this many times. will return the best solution found.
     * @param solutionCostFunction A cost function to evaluate solutions with. Only used when using random restarts.
     * @param restartStrategy how to do restarts.
     * @param sharedGoals if agents share goals, they will not conflict at their goal.
     */
    public PrioritisedPlanning_Solver(I_Solver lowLevelSolver, Comparator<Agent> agentComparator, Integer restarts,
                                      SolutionCostFunction solutionCostFunction, RestartStrategy restartStrategy,
                                      Boolean sharedGoals) {
        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.agentComparator = agentComparator;
        this.restarts = Objects.requireNonNullElse(restarts, 0);
        this.solutionCostFunction = Objects.requireNonNullElse(solutionCostFunction, Solution::sumIndividualCosts);
        this.restartStrategy = Objects.requireNonNullElse(restartStrategy, RestartStrategy.randomRestarts);
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
    }

    /**
     * Default constructor.
     */
    public PrioritisedPlanning_Solver(){
        this(null, null, null, null, null, null);
    }

    /*  = initialization =  */

    /**
     * Initialises the object in preparation to solving an {@link MAPF_Instance}.
     * @param instance - the instance that we will have to solve.
     * @param parameters - parameters that affect the solution process.
     */
    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);

        this.agents = new ArrayList<>(instance.agents);
        this.constraints = parameters.constraints == null ? new ConstraintSet(): parameters.constraints;
        this.constraints.sharedGoals = true;
        this.random = new Random(42);
        // if we were given a comparator for agents, sort the agents according to this priority order.
        if (this.agentComparator != null){
            this.agents.sort(this.agentComparator);
        }
        // if we were given a specific priority order to use for this instance, overwrite the order given by the comparator.
        if(parameters instanceof RunParameters_PP){
            RunParameters_PP parametersPP = (RunParameters_PP)parameters;

            //reorder according to requested priority
            if(parametersPP.preferredPriorityOrder != null) {reorderAgentsByPriority(parametersPP.preferredPriorityOrder);}
        }
        // TODO add caching heuristic?
    }

    private void reorderAgentsByPriority(Agent[] requestedOrder) {
        HashSet<Agent> tmpAgents = new HashSet<>(this.agents);
        this.agents.clear();

        for (Agent orderedAgent: //add by order
                requestedOrder) {
            if(tmpAgents.contains(orderedAgent)){
                this.agents.add(orderedAgent);
                tmpAgents.remove(orderedAgent);
            }
        }
        this.agents.addAll(tmpAgents); //add remaining agents not found in the requested order collection.
    }

    /*  = algorithm =  */

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        return solvePrioritisedPlanning(this.agents, instance, this.constraints);
    }

    /**
     * The main loop that solves the MAPF problem.
     * The basic idea of the algorithm is to solve a single agent path finding problem for each agent while avoiding the
     * plans of previous agents.
     * It returns a valid solution, but does not guarantee optimality.
     * @return a valid, yet non-optimal {@link Solution} to an {@link MAPF_Instance}.
     * @param agents
     * @param instance
     * @param initialConstraints
     */
    protected Solution solvePrioritisedPlanning(List<? extends Agent> agents, MAPF_Instance instance, ConstraintSet initialConstraints) {
        Solution bestSolution = null;
        // if using random restarts, try more than once and randomize between them
        for (int attemptNumber = 0; attemptNumber < this.restarts + 1; attemptNumber++) {
            Solution solution = new Solution();
            ConstraintSet currentConstraints = new ConstraintSet(initialConstraints);
            Agent agentWeFailedOn = null;
            //solve for each agent while avoiding the plans of previous agents
            for (Agent agent : agents) {
                if (checkTimeout()) break;

                //solve the subproblem for one agent
                SingleAgentPlan planForAgent = solveSubproblem(agent, instance, currentConstraints);

                // if an agent is unsolvable, then we can't return a valid solution for the instance (at least for this order of planning). return null.
                if (planForAgent == null) {
                    solution = null;
                    agentWeFailedOn = agent;
                    break;
                }
                //save the plan for this agent
                solution.putPlan(planForAgent);

                //add constraints to prevent the next agents from conflicting with the new plan
                currentConstraints.addAll(currentConstraints.allConstraintsForPlan(planForAgent));
            }

            // random/deterministic restarts
            if (checkTimeout()) break;
            if (bestSolution == null){
                bestSolution = solution;
            }
            else if (solution != null && solutionCostFunction.solutionCost(solution) < solutionCostFunction.solutionCost(bestSolution)){
                bestSolution = solution;
            }
            if (this.restarts > 0){
                // shuffle agents
                if (restartStrategy == RestartStrategy.randomRestarts){
                    Collections.shuffle(this.agents, this.random);
                }
                // give the highest priority to the agent we failed on
                else if (restartStrategy == RestartStrategy.deterministicRescheduling){
                    if (agentWeFailedOn != null){
                        this.agents.remove(agentWeFailedOn);
                    }
                    else { // deterministic restarts only restarts if no solution was found
                        break;
                    }
                }
            }
        }
//        instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, bestSolution == null ? 0: 1);
        return bestSolution;
    }

    protected SingleAgentPlan solveSubproblem(Agent currentAgent, MAPF_Instance fullInstance, ConstraintSet constraints) {
        //create a sub-problem
        MAPF_Instance subproblem = fullInstance.getSubproblemFor(currentAgent);
        InstanceReport subproblemReport = initSubproblemReport(fullInstance);
        RunParameters subproblemParameters = getSubproblemParameters(subproblem, subproblemReport, constraints);

        //solve sub-problem
        Solution singleAgentSolution = this.lowLevelSolver.solve(subproblem, subproblemParameters);
        digestSubproblemReport(subproblemReport);
        if (singleAgentSolution != null){
            return singleAgentSolution.getPlanFor(currentAgent);
        }
        else{ //agent is unsolvable
            return null;
        }
    }

    private static InstanceReport initSubproblemReport(MAPF_Instance instance) {
        InstanceReport subproblemReport = S_Metrics.newInstanceReport();
        subproblemReport.putStringValue("Parent Instance", instance.name);
        subproblemReport.putStringValue("Parent Solver", PrioritisedPlanning_Solver.class.getSimpleName());
        return subproblemReport;
    }

    private void digestSubproblemReport(InstanceReport subproblemReport) {
        Integer statesGenerated = subproblemReport.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel);
        this.totalLowLevelStatesGenerated += statesGenerated==null ? 0 : statesGenerated;
        Integer statesExpanded = subproblemReport.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
        this.totalLowLevelStatesExpanded += statesExpanded==null ? 0 : statesExpanded;
        //we consolidate the subproblem report into the main report, and remove the subproblem report.
        S_Metrics.removeReport(subproblemReport);
    }

    protected RunParameters getSubproblemParameters(MAPF_Instance subproblem, InstanceReport subproblemReport, ConstraintSet constraints) {
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        return new RunParameters(timeLeftToTimeout, new ConstraintSet(constraints), subproblemReport, null);
//        // assume the map we are using is indeed an ExplicitMap if we are going to use a CachingDistanceTableHeuristic (which requires explicit maps)
//        PrioritisedPlanning_Solver.cachingDistanceTableHeuristic.setCurrentMap((I_ExplicitMap) subproblem.map);
//        return new RunParameters_SAAStar(-1, new ConstraintSet(constraints), subproblemReport, null, PrioritisedPlanning_Solver.cachingDistanceTableHeuristic);
    }

    private List<Constraint> vertexConstraintsForPlan(SingleAgentPlan planForAgent) {
        List<Constraint> constraints = new LinkedList<>();
        for (Move move :
                planForAgent) {
            constraints.add(vertexConstraintsForMove(move));
        }
        return constraints;
    }



    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        if(solution != null){
            instanceReport.putIntegerValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
            instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
        }
    }

    /**
     * Clears local fields, to allow the garbage collector to clear the memory that is no longer in use.
     * All fields should be cleared by this method. Any data that might be relevant later should be passed as part
     * of the {@link Solution} that is output by {@link #solve(MAPF_Instance, RunParameters)}, or written to an {@link Environment.Metrics.InstanceReport}.
     */
    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.constraints = null;
        this.agents = null;
        this.instanceReport = null;
    }

    @Override
    public String name() {
        String restartStrategyString = restarts <= 0 ? "":
                (" + (" + restartStrategy.toString() + " x " + restarts + ")");
        return "Prioritised Planning" + restartStrategyString;
    }
}
