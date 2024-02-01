package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Metrics.InstanceReport;

import java.util.Random;

public class RunParametersBuilder {
    private long timeout = 1000*60*5 /*5 minutes*/;
    private long softTimeout = timeout;
    private I_ConstraintSet constraints = null;
    private InstanceReport instanceReport = null;
    private Solution existingSolution = null;
    private SingleAgentGAndH singleAgentGAndH = null;
    public int problemStartTime = 0;
    public Random randomNumberGenerator;
    public Agent[] priorityOrder;

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
    public RunParametersBuilder setConstraints(I_ConstraintSet constraints) {
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
     * @see RunParameters#singleAgentGAndH
     */
    public RunParametersBuilder setAStarGAndH(SingleAgentGAndH singleAgentGAndH) {
        this.singleAgentGAndH = singleAgentGAndH;
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

    /**
     * @see RunParameters#priorityOrder
     */
    public RunParametersBuilder setPriorityOrder(Agent[] priorityOrder) {
        this.priorityOrder = priorityOrder;
        return this;
    }

    public RunParameters createRP() {
        return new RunParameters(timeout, constraints, instanceReport, existingSolution, softTimeout, singleAgentGAndH, problemStartTime, randomNumberGenerator, priorityOrder);
    }
}