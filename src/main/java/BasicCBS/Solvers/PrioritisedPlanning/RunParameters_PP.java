package BasicCBS.Solvers.PrioritisedPlanning;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.AStar.AStarHeuristic;
import Environment.Metrics.InstanceReport;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;

/**
 * {@link RunParameters} for {@link PrioritisedPlanning_Solver}.
 */
public class RunParameters_PP extends RunParameters {
    /**
     * The {@link PrioritisedPlanning_Solver} will use this as the priority of the {@link Agent}s, with lower index
     * {@link Agent}s being treated as having higher priority. In practise this means they will be planned for first,
     * and then avoided when planning for higher index {@link Agent}s.
     * If the {@link PrioritisedPlanning_Solver} is given an {@link BasicCBS.Instances.MAPF_Instance} which contains agents not in
     * this collection, they will all be treated as having lower priority, and their internal order will be determined
     * arbitrarily. If this collection contains {@link Agent}s that are not in the {@link BasicCBS.Instances.MAPF_Instance},
     * they will be ignored.
     */
    public final Agent[] preferredPriorityOrder;
    /**
     * optional heuristic function to use in the low level solver.
     */
    public final AStarHeuristic heuristic;

    public RunParameters_PP(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, Agent[] preferredPriorityOrder, AStarHeuristic heuristic) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(ConstraintSet constraints, InstanceReport instanceReport, Agent[] preferredPriorityOrder, AStarHeuristic heuristic) {
        super(constraints, instanceReport);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(InstanceReport instanceReport, Agent[] preferredPriorityOrder, AStarHeuristic heuristic) {
        super(instanceReport);
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(Agent[] preferredPriorityOrder, AStarHeuristic heuristic) {
        super();
        this.preferredPriorityOrder = preferredPriorityOrder;
        this.heuristic = heuristic;
    }

    public RunParameters_PP(AStarHeuristic heuristic) {
        super();
        this.heuristic = heuristic;
        this.preferredPriorityOrder = new Agent[0];
    }
}
