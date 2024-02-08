package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.MAPF_Instance;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static BasicMAPF.DataTypesAndStructures.Timeout.getCurrentTimeMS_NSAccuracy;

/**
 * Performs that functionality that is common to all solvers.
 */
public abstract class A_Solver implements I_Solver{
    protected final static long DEFAULT_TIMEOUT = 5*60*1000; //5 minutes
    protected final static String processorInfo = getProcessorInfo();
    protected long maximumRuntime;
    protected long softTimeout;
    protected InstanceReport instanceReport;
    protected boolean commitReport;

    protected long startTime;
    protected long startDate;
    protected long endTime;
    protected boolean abortedForTimeout;
    protected int totalLowLevelNodesGenerated;
    protected int totalLowLevelNodesExpanded;
    protected int totalLowLevelTimeMS;
    protected int totalLowLevelCalls;
    public String name;
    private final Timeout timeout = new Timeout(0);

    /**
     * This implementation provides a skeleton for running a solver. You can override any of the invoked methods, but if
     * you do, it is recommended to also call the implementation defined in this class at some point during your implementation.
     * You can also completely override this method and implement a different workflow, while using the methods defined
     * in this class as services.
     * @param instance {@inheritDoc}
     * @param parameters {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Solution solve(MAPF_Instance instance, RunParameters parameters) {
        init(instance, parameters);
        Solution solution = runAlgorithm(instance, parameters);
        writeMetricsToReport(solution);
        tryCommitReport();
        releaseMemory();
        return solution;
    }

    /*  = initialization =  */

    /**
     * Prepares for a run. Must initialize all fields, making sure that no data from a previous run pollutes this run.
     * @param instance an instance that we are about to solve.
     * @param parameters parameters for this coming run.
     */
    protected void init(MAPF_Instance instance, RunParameters parameters){
        if(instance == null || parameters == null){throw new IllegalArgumentException();}

        this.startTime = getCurrentTimeMS_NSAccuracy();
        this.startDate = System.currentTimeMillis();
        this.maximumRuntime = (parameters.timeout >= 0) ? parameters.timeout : DEFAULT_TIMEOUT;
        this.softTimeout = Math.min(parameters.softTimeout, this.maximumRuntime);
        this.timeout.timeoutTimestampMS = Timeout.getTimeoutTimestampMS(this.startTime, this.maximumRuntime);
        this.endTime = 0;
        this.abortedForTimeout = false;

        this.totalLowLevelNodesGenerated = 0;
        this.totalLowLevelNodesExpanded = 0;
        this.totalLowLevelTimeMS = 0;
        this.totalLowLevelCalls = 0;

        this.instanceReport = parameters.instanceReport == null ? Metrics.newInstanceReport()
                : parameters.instanceReport;
        // If we were given a report, we should leave it be.
        // If we created our report locally, it is unreachable outside the class, and should therefore be committed.
        this.commitReport = parameters.instanceReport == null;
    }

    /*  = algorithm =  */

    protected abstract Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters);

    protected void digestSubproblemReport(InstanceReport subproblemReport) {
        Integer statesGenerated = subproblemReport.getIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel);
        this.totalLowLevelNodesGenerated += statesGenerated==null ? 0 : statesGenerated;
        Integer statesExpanded = subproblemReport.getIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel);
        this.totalLowLevelNodesExpanded += statesExpanded==null ? 0 : statesExpanded;
        Integer totalLowLevelTimeMS = subproblemReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
        this.totalLowLevelTimeMS += totalLowLevelTimeMS==null ? 0 : totalLowLevelTimeMS;
        this.totalLowLevelCalls++;
    }

    /*  = wind down =  */

    /**
     * Writes metrics about the run and the solution to {@link #instanceReport}.
     */
    protected void writeMetricsToReport(Solution solution){
        this.endTime = getCurrentTimeMS_NSAccuracy();

        instanceReport.putIntegerValue(InstanceReport.StandardFields.timeoutThresholdMS, (int) this.maximumRuntime);
        instanceReport.putStringValue(InstanceReport.StandardFields.startDateTime, new Date(startDate).toString());
        instanceReport.putStringValue(InstanceReport.StandardFields.processorInfo, processorInfo);
        instanceReport.putIntegerValue(InstanceReport.StandardFields.elapsedTimeMS, (int)(endTime-startTime));
        if(solution != null){
            instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, 1);
            if (instanceReport.keepSolutionString)
                instanceReport.putStringValue(InstanceReport.StandardFields.solution, solution.toString());
        }
        else{
            instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, 0);
        }
        instanceReport.putIntegerValue(InstanceReport.StandardFields.generatedNodesLowLevel, this.totalLowLevelNodesGenerated);
        instanceReport.putIntegerValue(InstanceReport.StandardFields.expandedNodesLowLevel, this.totalLowLevelNodesExpanded);
        instanceReport.putIntegerValue(InstanceReport.StandardFields.totalLowLevelTimeMS, this.totalLowLevelTimeMS);
        instanceReport.putIntegerValue(InstanceReport.StandardFields.totalLowLevelCalls, this.totalLowLevelCalls);
    }

    public static String getProcessorInfo() {
        try (java.util.stream.Stream<String> lines = Files.lines(Paths.get("/proc/cpuinfo"))) {
                    return lines.filter(line -> line.startsWith("model name"))
                    .map(line -> line.replaceAll(".*: ", ""))
                    .findFirst().orElse("");
        } catch (IOException e) {
            return "N/A";
        }
    }

    /**
     * Commits the report if {@link #commitReport} is true.
     */
    protected void tryCommitReport(){
        if(commitReport){
            try {
                instanceReport.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Releases memory held by the solver.
     *
     * This frees as much memory as possible, so that running multiple solvers in succession would not cause later
     * solvers to slow down or fail.
     * This also helps make sure that successive runs on the same solver object would remain independent, though the
     * responsibility for this lies with {@link #init(MAPF_Instance, RunParameters)}.
     */
    protected void releaseMemory(){
        this.instanceReport = null;
        this.startTime = 0;
        this.endTime = 0;
    }

    /*  = utilities =  */

    protected boolean checkTimeout() {
        if (this.abortedForTimeout) return true;
        if(timeout.isTimeoutExceeded()){
            this.abortedForTimeout = true;
            return true;
        }
        return false;
    }

    protected boolean checkSoftTimeout() {
        return Timeout.isTimeoutExceeded(startTime + softTimeout);
    }

    public Timeout getTimeout(){
        return this.timeout;
    }

    @Override
    public String name() {
        return this.name;
    }
}
