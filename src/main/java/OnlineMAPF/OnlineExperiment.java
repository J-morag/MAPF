package OnlineMAPF;

import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.I_Solver;
import Environment.Experiment;
import Environment.Metrics.InstanceReport;

public class OnlineExperiment extends Experiment {

    public OnlineExperiment(String experimentName, InstanceManager instanceManager) {
        super(experimentName, instanceManager);
    }

    public OnlineExperiment(String experimentName, InstanceManager instanceManager, int numOfInstances) {
        super(experimentName, instanceManager, numOfInstances);
    }

    @Override
    public InstanceReport setReport(MAPF_Instance instance, I_Solver solver) {
        InstanceReport instanceReport = super.setReport(instance, solver);
        OnlineMAPF_Instance onlineMAPF_instance = (OnlineMAPF_Instance)instance;
        instanceReport.putStringValue(InstanceReport.StandardFields.agentSelection, onlineMAPF_instance.agentSelection);
        instanceReport.putStringValue(InstanceReport.StandardFields.arrivalDistribution, onlineMAPF_instance.arrivalDistribution);
        instanceReport.putStringValue(InstanceReport.StandardFields.arrivalRate, onlineMAPF_instance.arrivalRate);
        return instanceReport;
    }
}
