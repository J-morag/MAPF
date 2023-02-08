package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.GoalConditions.I_AStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictAvoidanceTable;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;

public class RunParameters_SAAStar extends RunParameters {
    /**
     * A heuristic function to use for this run.
     */
    public AStarGAndH heuristicFunction;
    public I_ConflictAvoidanceTable conflictAvoidanceTable;
    public I_Coordinate sourceCoor;
    public I_Coordinate targetCoor;
    public float fBudget = Float.POSITIVE_INFINITY;
    public I_AStarGoalCondition goalCondition;

    public RunParameters_SAAStar(RunParameters parameters){
        super(parameters);
    }

    public RunParameters_SAAStar(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarGAndH heuristicFunction) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarGAndH heuristicFunction) {
        super(constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, AStarGAndH heuristicFunction) {
        super(constraints, instanceReport);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, Solution existingSolution, AStarGAndH heuristicFunction) {
        super(instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, AStarGAndH heuristicFunction) {
        super(instanceReport);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, AStarGAndH heuristicFunction) {
        super(constraints);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(Solution existingSolution, AStarGAndH heuristicFunction) {
        super(existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(long timeout, AStarGAndH heuristicFunction) {
        super(timeout);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(AStarGAndH heuristicFunction) {
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(RunParameters runParameters, AStarGAndH heuristicFunction) {
        super(runParameters);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarGAndH heuristicFunction, float fBudget) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarGAndH heuristicFunction, float fBudget) {
        super(constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, AStarGAndH heuristicFunction, float fBudget) {
        super(constraints, instanceReport);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, Solution existingSolution, AStarGAndH heuristicFunction, float fBudget) {
        super(instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, AStarGAndH heuristicFunction, float fBudget) {
        super(instanceReport);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, AStarGAndH heuristicFunction, float fBudget) {
        super(constraints);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(Solution existingSolution, AStarGAndH heuristicFunction, float fBudget) {
        super(existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(long timeout, AStarGAndH heuristicFunction, float fBudget) {
        super(timeout);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(AStarGAndH heuristicFunction, float fBudget) {
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

    public RunParameters_SAAStar(RunParameters runParameters, AStarGAndH heuristicFunction, float fBudget) {
        super(runParameters);
        this.heuristicFunction = heuristicFunction;
        this.fBudget = fBudget;
    }

}
