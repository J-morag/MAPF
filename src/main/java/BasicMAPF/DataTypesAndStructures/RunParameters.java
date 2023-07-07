package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Solvers.I_Solver;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

/**
 * A set of parameters for a {@link I_Solver solver} to use when solving an {@link BasicMAPF.Instances.MAPF_Instance instance}.
 * All parameters can be null or invalid, so {@link I_Solver solvers} should validate all fields before using them, and
 * provide default values if possible. When using this class, {@link I_Solver solvers} don't have to use all the fields,
 * as some fields may not be relevant to some solvers.
 */
public class RunParameters {
    /*  =Constants=  */
    private static final long DEFAULT_TIMEOUT = 1000*60*5 /*5 minutes*/;

    /*  =Fields=  */
    /**
     * The maximum time (milliseconds) allotted to the search. If the search exceeds this time, it is aborted.
     * Can also be 0, or negative.
     */
    public final long timeout;

    /**
     * For Anytime algorithms.
     * After this soft timeout (milliseconds) is exceeded, the solver should try and return a solution before hitting
     * the hard {@link #timeout}.
     * Can also be 0, or negative. Must be <= {@link #timeout}.
     */
    public final long softTimeout;

    /**
     * An unmodifiable list of {@link Constraint location constraints} for the {@link I_Solver sovler} to use.
     * A {@link I_Solver solver} that uses this field should start its solution process with these constraints, but may
     * later add or remove constraints, depending on the algorithm being used. @Nullable
     */
    public final ConstraintSet constraints;

    /**
     * An {@link InstanceReport} where to {@link I_Solver} will write metrics generated from the run.
     * Can be null.
     * It is best to not {@link InstanceReport#commit() commit} this report. If instead it was null, and thus a
     * replacement report was generated in the solver, that report should be committed.
     */
    public final InstanceReport instanceReport;

    /**
     * A {@link Solution} that already exists, and which the solver should use as a base.
     * The solver should add to, or modify, this solution rather than create a new one.
     */
    public Solution existingSolution;
    /**
     * Start time of the problem. {@link Solution solutions} and {@link SingleAgentPlan plans} start at this time.
     * Not real-time.
     */
    public int problemStartTime = 0;
    public Random randomNumberGenerator;

    /*  =Constructors=  */

    public RunParameters(Long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, Long softTimeout, Integer problemStartTime, @Nullable Random randomNumberGenerator) {
        this.timeout = Objects.requireNonNullElse(timeout, DEFAULT_TIMEOUT);
        this.softTimeout = Objects.requireNonNullElse(softTimeout, this.timeout);
        if (this.softTimeout > this.timeout){
            throw new IllegalArgumentException("softTimeout parameter must be <= timeout parameter");
        }
        this.constraints = constraints;
        this.instanceReport = instanceReport;
        this.existingSolution = existingSolution;
        this.problemStartTime = Objects.requireNonNullElse(problemStartTime, 0);
        this.randomNumberGenerator = randomNumberGenerator;
    }

    public RunParameters(Long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, Long softTimeout) {
        this(timeout, constraints, instanceReport, existingSolution, softTimeout, null, null);
    }

    public RunParameters(Long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution) {
        this(timeout, constraints, instanceReport, existingSolution, null);
    }

    public RunParameters(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution) {
        this(null, constraints, instanceReport, existingSolution, null);
    }

    public RunParameters(ConstraintSet constraints, InstanceReport instanceReport) {
        this(constraints, instanceReport, null);
    }

    public RunParameters(InstanceReport instanceReport, Solution existingSolution) {
        this(null, instanceReport, existingSolution);
    }

    public RunParameters(InstanceReport instanceReport) {
        this(null, instanceReport, null);
    }

    public RunParameters(ConstraintSet constraints) {
        this(constraints, null, null);
    }

    public RunParameters(Solution existingSolution) {
        this(null, null, existingSolution);
    }

    public RunParameters(long timeout) {
        this(timeout, null, null, null, null);
    }

    public RunParameters() {
        this(null, null, null);
    }

    public RunParameters(RunParameters runParameters) {
        this(runParameters.timeout, runParameters.constraints, runParameters.instanceReport, runParameters.existingSolution, runParameters.softTimeout, runParameters.problemStartTime, runParameters.randomNumberGenerator);
    }

    public RunParameters(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, int problemStartTime) {
        this.timeout = timeout;
        this.softTimeout = timeout;
        this.constraints = constraints;
        this.instanceReport = instanceReport;
        this.existingSolution = existingSolution;
        this.problemStartTime = problemStartTime;
    }

    public RunParameters(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, int problemStartTime) {
        this(DEFAULT_TIMEOUT, constraints, instanceReport, existingSolution, problemStartTime);
    }

    public RunParameters(ConstraintSet constraints, InstanceReport instanceReport, int problemStartTime) {
        this(constraints, instanceReport, null, problemStartTime);
    }


    public RunParameters(InstanceReport instanceReport, Solution existingSolution, int problemStartTime) {
        this(null, instanceReport, existingSolution, problemStartTime);
    }


    public RunParameters(InstanceReport instanceReport, int problemStartTime) {
        this(null, instanceReport, null, problemStartTime);
    }

    public RunParameters(ConstraintSet constraints, int problemStartTime) {
        this(constraints, null, null, problemStartTime);
    }

    public RunParameters(Solution existingSolution, int problemStartTime) {
        this(null, null, existingSolution, problemStartTime);
    }

    public RunParameters(long timeout, int problemStartTime) {
        this(timeout, null, null, null, problemStartTime);
    }

    public RunParameters(int problemStartTime) {
        this(null, null, null, problemStartTime);
    }



}
