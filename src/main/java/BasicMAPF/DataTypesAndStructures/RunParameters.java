package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import Environment.Metrics.InstanceReport;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.Set;

/**
 * A set of parameters for a {@link I_Solver solver} to use when solving an {@link BasicMAPF.Instances.MAPF_Instance instance}.
 * All parameters can be null or invalid, so {@link I_Solver solvers} should validate all fields before using them, and
 * provide default values if possible. When using this class, {@link I_Solver solvers} don't have to use all the fields,
 * as some fields may not be relevant to some solvers.
 */
public class RunParameters {

    /*  =Fields=  */

    /**
     * The maximum time (milliseconds) allotted to the search. If the search exceeds this time, it is aborted.
     * Can also be 0, or negative.
     * TODO change this to a {@link Timeout} object.
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
    public final I_ConstraintSet constraints;

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
    public final Solution existingSolution;

    /**
     * optional heuristic function to use in the single agent solver.
     */
    public final SingleAgentGAndH singleAgentGAndH;

    /**
     * Set of separating vertices for a given map.
     * Separating vertices are vertices in a graph whose removal increases the number of connected componenets in a graph,
     * effectively disconneccting it.
     */
    public final Set<I_Location> separatingVertices;

    /**
     * Start time of the problem. {@link Solution solutions} and {@link SingleAgentPlan plans} start at this time.
     * Not real-time.
     */
    public int problemStartTime;
    /**
     * A random number generator to use in the solver.
     * Can be null.
     */
    public Random randomNumberGenerator;
    /**
     * A total priority ordering of the {@link Agent agents}, with lower index {@link Agent}s being treated as having
     * higher priority.
     * Different {@link I_Solver solver} may treat this information differently, including ignoring it.
     * It is the best practice for this array to cover exactly the same set of agents that exist in the
     * {@link BasicMAPF.Instances.MAPF_Instance instance}, or be null.
     * Otherwise, behavior is undefined.
     */
    public final Agent[] priorityOrder;

    public I_ConflictAvoidanceTable conflictAvoidanceTable;

    /*  =Constructors=  */

    /**
     * Intentionally package-private constructor.
     * Use {@link RunParametersBuilder} to create a {@link RunParameters} object.
     */
    RunParameters(long timeout, I_ConstraintSet constraints, InstanceReport instanceReport, Solution existingSolution, long softTimeout, SingleAgentGAndH singleAgentGAndH, int problemStartTime, @Nullable Random randomNumberGenerator, Agent[] priorityOrder, Set<I_Location> separatingVertices, I_ConflictAvoidanceTable conflictAvoidanceTable) {
        this.timeout = timeout;
        this.softTimeout = softTimeout;
        if (this.softTimeout > this.timeout){
            throw new IllegalArgumentException("softTimeout parameter must be <= timeout parameter");
        }
        this.constraints = constraints;
        this.instanceReport = instanceReport;
        this.existingSolution = existingSolution;
        this.singleAgentGAndH = singleAgentGAndH;
        this.problemStartTime = problemStartTime;
        this.randomNumberGenerator = randomNumberGenerator;
        this.priorityOrder = priorityOrder;
        this.separatingVertices = separatingVertices;
        this.conflictAvoidanceTable = conflictAvoidanceTable;
    }

    public RunParameters(RunParameters runParameters) {
        this(runParameters.timeout, runParameters.constraints, runParameters.instanceReport, runParameters.existingSolution, runParameters.softTimeout, runParameters.singleAgentGAndH, runParameters.problemStartTime, runParameters.randomNumberGenerator, runParameters.priorityOrder, runParameters.separatingVertices, runParameters.conflictAvoidanceTable);
    }

}
