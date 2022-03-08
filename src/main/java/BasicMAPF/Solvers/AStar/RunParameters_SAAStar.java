package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictAvoidanceTable;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;

public class RunParameters_SAAStar extends RunParameters {
    /**
     * A heuristic function to use for this run.
     */
    public AStarHeuristic heuristicFunction;
    public I_ConflictAvoidanceTable conflictAvoidanceTable;
    public I_Coordinate sourceCoor;
    public I_Coordinate targetCoor;
    public float fBudget = Float.POSITIVE_INFINITY;

    public RunParameters_SAAStar(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction) {
        super(constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, AStarHeuristic heuristicFunction) {
        super(constraints, instanceReport);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction) {
        super(instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, AStarHeuristic heuristicFunction) {
        super(instanceReport);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, AStarHeuristic heuristicFunction) {
        super(constraints);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(Solution existingSolution, AStarHeuristic heuristicFunction) {
        super(existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(long timeout, AStarHeuristic heuristicFunction) {
        super(timeout);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(AStarHeuristic heuristicFunction) {
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(RunParameters runParameters, AStarHeuristic heuristicFunction) {
        super(runParameters);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction, float fBudget) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction, float fBudget) {
        super(constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, AStarHeuristic heuristicFunction, float fBudget) {
        super(constraints, instanceReport);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction, float fBudget) {
        super(instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, AStarHeuristic heuristicFunction, float fBudget) {
        super(instanceReport);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, AStarHeuristic heuristicFunction, float fBudget) {
        super(constraints);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(Solution existingSolution, AStarHeuristic heuristicFunction, float fBudget) {
        super(existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(long timeout, AStarHeuristic heuristicFunction, float fBudget) {
        super(timeout);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(AStarHeuristic heuristicFunction, float fBudget) {
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(RunParameters runParameters, AStarHeuristic heuristicFunction, float fBudget) {
        super(runParameters);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

}
