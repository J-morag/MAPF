package BasicMAPF.Solvers.PrioritisedPlanning;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.PartialSolutionsStrategy;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;

/**
 * {@link RunParameters} for {@link PrioritisedPlanning_Solver}.
 */
public class RunParameters_PP extends RunParameters {
    /**
     * The {@link PrioritisedPlanning_Solver} will use this as the priority of the {@link Agent}s, with lower index
     * {@link Agent}s being treated as having higher priority. In practise this means they will be planned for first,
     * and then avoided when planning for higher index {@link Agent}s.
     * If the {@link PrioritisedPlanning_Solver} is given an {@link BasicMAPF.Instances.MAPF_Instance} which contains agents not in
     * this collection, they will all be treated as having lower priority, and their internal order will be determined
     * arbitrarily. If this collection contains {@link Agent}s that are not in the {@link BasicMAPF.Instances.MAPF_Instance},
     * they will be ignored.
     */
    public final Agent[] preferredPriorityOrder;
    /**
     * optional heuristic function to use in the low level solver.
     */
    public final AStarGAndH heuristic;
    public PartialSolutionsStrategy partialSolutionsStrategy;

    public RunParameters_PP(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, Agent[] preferredPriorityOrder, AStarGAndH heuristic, Long softTimeout) {
        super(timeout, constraints, instanceReport, existingSolution, softTimeout);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }
    public RunParameters_PP(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, Agent[] preferredPriorityOrder, AStarGAndH heuristic) {
        super(timeout, constraints, instanceReport, existingSolution, null);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(ConstraintSet constraints, InstanceReport instanceReport, Agent[] preferredPriorityOrder, AStarGAndH heuristic) {
        super(constraints, instanceReport);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(InstanceReport instanceReport, Agent[] preferredPriorityOrder, AStarGAndH heuristic) {
        super(instanceReport);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(Agent[] preferredPriorityOrder, AStarGAndH heuristic) {
        super();
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(AStarGAndH heuristic) {
        this(new Agent[0], heuristic);
    }

    public RunParameters_PP(RunParameters runParameters, AStarGAndH costAndHeuristic) {
        super(runParameters);
        this.heuristic = costAndHeuristic;
        this.preferredPriorityOrder = null;
    }
}
