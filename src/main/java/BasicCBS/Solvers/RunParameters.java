package BasicCBS.Solvers;

import Environment.Metrics.InstanceReport;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;

/**
 * A set of parameters for a {@link I_Solver solver} to use when solving an {@link BasicCBS.Instances.MAPF_Instance instance}.
 * All parameters can be null or invalid, so {@link I_Solver solvers} should validate all fields before using them, and
 * provide default values if possible. When using this class, {@link I_Solver solvers} don't have to use all the fields,
 * as some fields may not be relevant to some solvers.
 */
public class RunParameters {
    /*  =Constants=  */
    private static final long defaultTimeout = 1000*60*15 /*15 minutes*/;
//    private static final long defaultTimeout = 1000*60*5 /*5 minutes*/;

    /*  =Fields=  */
    /**
     * The maximum time (milliseconds) allotted to the search. If the search exceeds this time, it is aborted.
     * Can also be 0, or negative.
     */
    public final long timeout;

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

    /*  =Constructors=  */

    public RunParameters(long timeout, ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution) {
        this.timeout = timeout;
        this.constraints = constraints;
        this.instanceReport = instanceReport;
        this.existingSolution = existingSolution;
    }

    public RunParameters(ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution) {
        this(defaultTimeout, constraints, instanceReport, existingSolution);
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
        this(timeout, null, null, null);
    }

    public RunParameters() {
        this(null, null, null);
    }

}
