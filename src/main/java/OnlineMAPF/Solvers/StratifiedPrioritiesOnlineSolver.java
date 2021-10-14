package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.*;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineConstraintSet;

import java.util.*;

public class StratifiedPrioritiesOnlineSolver extends A_OnlineSolver {

    private OfflineSolverStrategy offlineSolverStrategy = OfflineSolverStrategy.CBS;

    public StratifiedPrioritiesOnlineSolver() {
        super.name = this.getClass().getSimpleName() + "_" + offlineSolverStrategy.name();
    }

    public StratifiedPrioritiesOnlineSolver(OfflineSolverStrategy offlineSolverStrategy) {
        this.offlineSolverStrategy = offlineSolverStrategy;
        super.name = this.getClass().getSimpleName() + "_" + offlineSolverStrategy.name();
    }

    protected Solution solveForNewArrivals(int time, HashMap<Agent, I_Location> currentAgentLocations) {
        OnlineAStar onlineAStar = new OnlineAStar(costOfReroute);
        I_Solver offlineSolver = this.offlineSolverStrategy == OfflineSolverStrategy.CBS ?
                new OnlineCompatibleOfflineCBS(currentAgentLocations, time, new COR_CBS_CostFunction(this.costOfReroute, latestSolution), onlineAStar, null)
                :
                new OnlinePP_Solver(new OnlineAStar(this.costOfReroute), currentAgentLocations, time);

        // split group agents by priority, solve for higher priorities first, protect solutions of higher priorities
        List<Set<Agent>> groupedByPrioritySortedDescending = toSortedListByPriority(groupByPriority(currentAgentLocations));
        solveStratifiedByPriority(offlineSolver, groupedByPrioritySortedDescending);

        return latestSolution;
    }

    /**
     * Solve for each priority separately and in order. If we find a group to be unsolvable given the previous
     * group, we will merge them and re-solve.
     * @param offlineSolver an offline solver
     * @param groupedByPriority groups of agents by priorities. sorted by descending priority.
     */
    private void solveStratifiedByPriority(I_Solver offlineSolver, List<Set<Agent>> groupedByPriority) {
        OnlineConstraintSet constraints = new OnlineConstraintSet();
        latestSolution = new Solution();

        for (int i = 0; i < groupedByPriority.size(); i++) {
            // solve for this priority
            MAPF_Instance subProblem = baseInstance.getSubproblemFor(groupedByPriority.get(i));
            // add the constraints from previous solutions for higher priorities
            RunParameters runParameters = new RunParameters(timeoutThreshold - totalRuntime, constraints, S_Metrics.newInstanceReport(), null);
            Solution solutionForPriorityGroup = offlineSolver.solve(subProblem, runParameters);
            // like prioritised planning, this is incomplete. If we find this group to be unsolvable given the previous
            // group, we will merge them and re-solve.
            digestSubproblemReport(runParameters.instanceReport); // updates total runtime
            if (solutionForPriorityGroup == null){
                // if null due to timeout, return null.
                if (timeoutThreshold < totalRuntime || this.offlineSolverStrategy == OfflineSolverStrategy.PRIORITISED_PLANNING){
                    latestSolution = null;
                    return;
                }
                else{ // null because unsolvable
                    // merge with higher priority group
                    groupedByPriority.get(i-1).addAll(groupedByPriority.get(i));
                    // remove old
                    groupedByPriority.remove(i);
                    // recursion
                    solveStratifiedByPriority(offlineSolver, groupedByPriority);
                    return;
                }
            }

            // add the constraints from the solution for this priority group
            for (SingleAgentPlan plan : solutionForPriorityGroup) {
                constraints.addAll(allConstraintsForPlan(plan));
                // add the plans to the latest solution
                latestSolution.putPlan(plan);
            }

        }
    }

    private Constraint vertexConstraintsForMove(Move move){
        return new Constraint(null, move.timeNow, move.currLocation);
    }

    private Constraint swappingConstraintsForMove(Move move){
        return new Constraint(null, move.timeNow,
                /*the constraint is in opposite direction of the move*/ move.currLocation, move.prevLocation);
    }

    /**
     * Creates constraints to protect a {@link SingleAgentPlan plan}.
     * To also protect an agent at its goal, extra vertex constraints are added. This is not efficient and doesn't
     * guarantee validity.
     * @param planForAgent
     * @return
     */
    protected List<Constraint> allConstraintsForPlan(SingleAgentPlan planForAgent) {
        List<Constraint> constraints = new LinkedList<>();
        // protect the agent's plan
        for (Move move :
                planForAgent) {
            constraints.add(vertexConstraintsForMove(move));
            constraints.add(swappingConstraintsForMove(move));
        }

        return constraints;
    }


    /**
     * groups agents by priority, returning a list of agent groups.
     */
    private Map<Integer, Set<Agent>> groupByPriority(HashMap<Agent, I_Location> currentAgentLocations){
        Map<Integer, Set<Agent>> res = new HashMap<>();
        for (Agent a : currentAgentLocations.keySet()) {
            Set<Agent> priorityGroup = res.computeIfAbsent(a.priority, k -> new HashSet<>());
            priorityGroup.add(a);
        }
        return res;
    }

    private List<Set<Agent>> toSortedListByPriority(Map<Integer, Set<Agent>> groupedByPriority){
        List<Integer> prioritiesSortedDescending = new ArrayList<>(groupedByPriority.keySet());
        prioritiesSortedDescending.sort(Comparator.comparingInt(i -> -i));
        List<Set<Agent>> res = new ArrayList<>();
        for (int priority : prioritiesSortedDescending) {
            res.add(groupedByPriority.get(priority));
        }
        return res;
    }

    public enum OfflineSolverStrategy {
        CBS,
        ICTS,
        PRIORITISED_PLANNING
    }
}
