package BasicMAPF.DataTypesAndStructures;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import Environment.Metrics.InstanceReport;

import java.util.Random;
import java.util.Set;

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
    public Set<I_Location> separatingVertices;
    public I_ConflictAvoidanceTable conflictAvoidanceTable;

    public RunParametersBuilder copy(RunParameters rp) {
        this.timeout = rp.timeout;
        this.softTimeout = rp.softTimeout;
        this.constraints = rp.constraints;
        this.instanceReport = rp.instanceReport;
        this.existingSolution = rp.existingSolution;
        this.singleAgentGAndH = rp.singleAgentGAndH;
        this.problemStartTime = rp.problemStartTime;
        this.randomNumberGenerator = rp.randomNumberGenerator;
        this.priorityOrder = rp.priorityOrder;
        this.separatingVertices = rp.separatingVertices;
        this.conflictAvoidanceTable = rp.conflictAvoidanceTable;
        return this;
    }

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

    /**
     * @see RunParameters#separatingVertices
     */
    public RunParametersBuilder setSeparatingVertices(Set<I_Location> separatingVertices) {
        this.separatingVertices = separatingVertices;
        return this;
    }

    /**
     * @see RunParameters#conflictAvoidanceTable;
     */
    public RunParametersBuilder setConflictAvoidanceTable(I_ConflictAvoidanceTable conflictAvoidanceTable) {
        this.conflictAvoidanceTable = conflictAvoidanceTable;
        return this;
    }

    public RunParameters createRP() {
        return new RunParameters(timeout, constraints, instanceReport, existingSolution, softTimeout, singleAgentGAndH, problemStartTime, randomNumberGenerator, priorityOrder, separatingVertices, conflictAvoidanceTable);
    }
}