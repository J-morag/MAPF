package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;

import java.util.Random;

public class RunParametersBuilder {
    private long timeout = 1000*60*5 /*5 minutes*/;
    private long softTimeout = timeout;
    private ConstraintSet constraints = null;
    private InstanceReport instanceReport = null;
    private Solution existingSolution = null;
    private AStarGAndH aStarGAndH = null;
    public int problemStartTime = 0;
    public Random randomNumberGenerator;

    /**
     * @see RunParameters#timeout
     */
    public RunParametersBuilder setTimeout(long timeout) {
        this.timeout = timeout;
        this.softTimeout = Math.min(timeout, softTimeout);
        return this;
    }

    /**
     * @see RunParameters#softTimeout
     */
    public RunParametersBuilder setSoftTimeout(long softTimeout) {
        this.softTimeout = softTimeout;
        return this;
    }

    /**
     * @see RunParameters#constraints
     */
    public RunParametersBuilder setConstraints(ConstraintSet constraints) {
        this.constraints = constraints;
        return this;
    }

    /**
     * @see RunParameters#instanceReport
     */
    public RunParametersBuilder setInstanceReport(InstanceReport instanceReport) {
        this.instanceReport = instanceReport;
        return this;
    }

    /**
     * @see RunParameters#existingSolution
     */
    public RunParametersBuilder setExistingSolution(Solution existingSolution) {
        this.existingSolution = existingSolution;
        return this;
    }

    /**
     * @see RunParameters#aStarGAndH
     */
    public RunParametersBuilder setAStarGAndH(AStarGAndH aStarGAndH) {
        this.aStarGAndH = aStarGAndH;
        return this;
    }

    /**
     * @see RunParameters#problemStartTime
     */
    public RunParametersBuilder setProblemStartTime(int problemStartTime) {
        this.problemStartTime = problemStartTime;
        return this;
    }

    /**
     * @see RunParameters#randomNumberGenerator
     */
    public RunParametersBuilder setRNG(Random randomNumberGenerator) {
        this.randomNumberGenerator= randomNumberGenerator;
        return this;
    }

    public RunParameters createRP() {
        return new RunParameters(timeout, constraints, instanceReport, existingSolution, softTimeout, aStarGAndH, problemStartTime, randomNumberGenerator);
    }
}