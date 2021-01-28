package OnlineMAPF.RunManagers;

import BasicCBS.Instances.InstanceBuilders.Priorities;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import Environment.A_RunManager;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineExperiment;
import OnlineMAPF.OnlineInstanceBuilder_MovingAI;
import OnlineMAPF.Solvers.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RunManagerPriorities extends A_RunManager {

    String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"});

    /*  = Set Solvers =  */
    @Override
    protected void setSolvers() {
        ReplanSingle replanSingle = new ReplanSingle(new OnlineAStar());
        this.solvers.add(replanSingle);
        StratifiedPrioritiesOnlineSolver stratifiedReplanSingle = new StratifiedPrioritiesOnlineSolver(StratifiedPrioritiesOnlineSolver.OfflineSolverStrategy.PRIORITISED_PLANNING);
        this.solvers.add(new OnlineSolverContainer(stratifiedReplanSingle));
        StratifiedPrioritiesOnlineSolver stratifiedSnapshot= new StratifiedPrioritiesOnlineSolver(StratifiedPrioritiesOnlineSolver.OfflineSolverStrategy.CBS);
        this.solvers.add(new OnlineSolverContainer(stratifiedSnapshot));
        OnlineICTSSolver snapshotICTS = new OnlineICTSSolver();
        snapshotICTS.name = "OnlineScratchICTS";
        this.solvers.add(new OnlineSolverContainer(snapshotICTS));
        OnlineCBSSolver snapshotCBS = new OnlineCBSSolver();
        snapshotCBS.name = "OnlineScratchCBS";
        this.solvers.add(new OnlineSolverContainer(snapshotCBS));
        OnlineCompatibleOfflineCBS oracle =  new OnlineCompatibleOfflineCBS();
        oracle.name = "Oracle";
        this.solvers.add(oracle);
    }

    /*  = Set Experiments =  */
    @Override
    protected void setExperiments() {
        Priorities.PrioritiesPolicy policy = Priorities.PrioritiesPolicy.FOUR_TO_ONE_ROBIN;
        addExperimentsPriorities(1, 10, policy);
//        addExperimentsPriorities(1, 3, 5, policy);
//        addExperimentsPrioritiesHalfAndHalf(1, 100, policy);
    }

    @Override
    public void runAllExperiments() {
        try {
            S_Metrics.setHeader(new String[]{
                    InstanceReport.StandardFields.experimentName,
                    InstanceReport.StandardFields.instanceName,
                    InstanceReport.StandardFields.mapName,
                    InstanceReport.StandardFields.agentSelection,
                    InstanceReport.StandardFields.arrivalDistribution,
                    InstanceReport.StandardFields.arrivalRate,
                    InstanceReport.StandardFields.numAgents,
                    InstanceReport.StandardFields.solver,
                    InstanceReport.StandardFields.solved,
                    InstanceReport.StandardFields.valid,
                    InstanceReport.StandardFields.elapsedTimeMS,
                    InstanceReport.StandardFields.solutionCost,
                    InstanceReport.StandardFields.numReroutes,
                    "priority policy",
                    "SOC priority1",
                    "SOC priority3",
                    "SOC priority5",
                    "SOC priority10",
                    "SOC priority1000",
                    "sum delays",
                    InstanceReport.StandardFields.COR,
                    InstanceReport.StandardFields.totalReroutesCost,
                    InstanceReport.StandardFields.solution});
        } catch (IOException e) {
            e.printStackTrace();
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String pathWithStartTime = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + " .csv";
        try {
            S_Metrics.addOutputStream(new FileOutputStream((pathWithStartTime)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            S_Metrics.addOutputStream(System.out, S_Metrics::instanceReportToHumanReadableString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.runAllExperiments();
        String pathWithEndTime = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + " .csv";

        try {
            S_Metrics.exportCSV(new FileOutputStream(pathWithEndTime)
//                    ,new String[]{   InstanceReport.StandardFields.experimentName,
//                            InstanceReport.StandardFields.experimentName,
//                            InstanceReport.StandardFields.agentSelection,
//                            InstanceReport.StandardFields.arrivalDistribution,
//                            InstanceReport.StandardFields.arrivalRate,
//                            InstanceReport.StandardFields.numAgents,
//                            InstanceReport.StandardFields.solver,
//                            InstanceReport.StandardFields.solved,
//                            InstanceReport.StandardFields.valid,
//                            InstanceReport.StandardFields.elapsedTimeMS,
//                            InstanceReport.StandardFields.solutionCost,
//                            InstanceReport.StandardFields.numReroutes,
//                            InstanceReport.StandardFields.COR,
//                            InstanceReport.StandardFields.totalReroutesCost,
//                            InstanceReport.StandardFields.numReroutes,
//                            InstanceReport.StandardFields.solution}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        S_Metrics.clearAll();
    }


    /* = Experiments =  */

    private void addExperimentsPriorities(int light, int heavy, Priorities.PrioritiesPolicy policy) {
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\Online\\\\MovingAI_Instances\\\\IJCAI2020"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{40});

        Priorities priorities = new Priorities(policy, new int[]{light, heavy});

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new OnlineInstanceBuilder_MovingAI(priorities), properties);

        /*  =   Add new experiment   =  */
        OnlineExperiment experiment = new OnlineExperiment("Priorities_" + policy.name() + "_" + light + "_" + heavy, instanceManager, priorities, null);
        experiment.keepSolutionInReport = false;
        this.experiments.add(experiment);
    }


    private void addExperimentsPriorities(int light, int medium, int heavy, Priorities.PrioritiesPolicy policy) {
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\Online\\\\MovingAI_Instances\\\\IJCAI2020"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{40});

        Priorities priorities = new Priorities(policy, new int[]{light, medium, heavy});

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new OnlineInstanceBuilder_MovingAI(priorities), properties);

        /*  =   Add new experiment   =  */
        OnlineExperiment experiment = new OnlineExperiment("Priorities_" + policy.name() + "_" + light +  "_" + medium + "_" + heavy, instanceManager, priorities, null);
        experiment.keepSolutionInReport = false;
        this.experiments.add(experiment);
    }

}
