package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Maps.I_Location;
import Environment.Metrics.InstanceReport;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;

public class RunParameters_SAAStar extends RunParameters {
    /**
     * A heuristic function to use for this run.
     */
    public AStarHeuristic heuristicFunction;
    public int problemStartTime = 0;
    public I_Location agentStartLocation = null;

    public RunParameters_SAAStar(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction) {
        super(timeout, constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(constraints, instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, InstanceReport instanceReport, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(constraints, instanceReport);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, Solution existingSolution, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(instanceReport, existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(InstanceReport instanceReport, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(instanceReport);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(ConstraintSet constraints, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(constraints);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(Solution existingSolution, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(existingSolution);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(long timeout, AStarHeuristic heuristicFunction, int problemStartTime) {
        super(timeout);
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(RunParameters runParameters, AStarHeuristic heuristicFunction, int problemStartTime) {
        this(runParameters.timeout, runParameters.constraints, runParameters.instanceReport, runParameters.existingSolution, heuristicFunction, problemStartTime);
    }

    public RunParameters_SAAStar(RunParameters runParameters, AStarHeuristic heuristicFunction) {
        this(runParameters.timeout, runParameters.constraints, runParameters.instanceReport, runParameters.existingSolution, heuristicFunction);
    }

    public RunParameters_SAAStar(RunParameters runParameters, int problemStartTime) {
        this(runParameters.timeout, runParameters.constraints, runParameters.instanceReport, runParameters.existingSolution, null, problemStartTime);
    }

    public RunParameters_SAAStar(AStarHeuristic heuristicFunction, int problemStartTime) {
        this.heuristicFunction = heuristicFunction;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(int problemStartTime) {
        this.problemStartTime = problemStartTime;
    }

    public RunParameters_SAAStar(AStarHeuristic heuristicFunction) {
        this.heuristicFunction = heuristicFunction;
    }

}
