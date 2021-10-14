package OnlineMAPF.RunManagers;

import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import Environment.A_RunManager;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineExperiment;
import OnlineMAPF.OnlineInstanceBuilder_MovingAI;
import OnlineMAPF.Solvers.OnlineCBSSolver;
import OnlineMAPF.Solvers.OnlineICTS.OnlineICTSSolver;
import OnlineMAPF.Solvers.OnlineICTS.OnlineLifelongICTS;
import OnlineMAPF.Solvers.OnlineSolverContainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RunManagerOnlineICTS extends A_RunManager {

    String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"});

    /*  = Set Solvers =  */
    @Override
    protected void setSolvers() {
//        ReplanSingle replanSingle = new ReplanSingle(new OnlineAStar());
//        this.solvers.add(replanSingle);

//        OnlineCompatibleOfflineCBS oracle =  new OnlineCompatibleOfflineCBS();
//        oracle.name = "Oracle";
//        this.solvers.add(oracle);

        OnlineCBSSolver snapshot = new OnlineCBSSolver(false);
        this.solvers.add(new OnlineSolverContainer(snapshot));

        OnlineICTSSolver onlineICTSS3P =  new OnlineICTSSolver(null, null, null, ICTS_Solver.PruningStrategy.S3P, null);
        this.solvers.add(new OnlineSolverContainer(onlineICTSS3P));

        OnlineLifelongICTS LICTS_withUpdateMDDs = new OnlineLifelongICTS();
        LICTS_withUpdateMDDs.name = LICTS_withUpdateMDDs.name + "_withUpdateMDDs";
        LICTS_withUpdateMDDs.updateMDDsWhenTimeProgresses = true;
        LICTS_withUpdateMDDs.keepOnlyRelevantUpdatedMDDs = false;
        this.solvers.add(LICTS_withUpdateMDDs);

        OnlineLifelongICTS LICTS_noUpdateMDDs = new OnlineLifelongICTS();
        LICTS_noUpdateMDDs.name = LICTS_noUpdateMDDs.name + "_noUpdateMDDs";
        LICTS_noUpdateMDDs.updateMDDsWhenTimeProgresses = false;
        this.solvers.add(LICTS_noUpdateMDDs);


//        OnlineLifelongICTS LICTS_withUpdateMDDs_NOP = new OnlineLifelongICTS(ICTS_Solver.PruningStrategy.NO_PRUNING);
//        LICTS_withUpdateMDDs_NOP.name = LICTS_withUpdateMDDs_NOP.name + "LICTS_withUpdateMDDs_NOP";
//        LICTS_withUpdateMDDs_NOP.updateMDDsWhenTimeProgresses = true;
//        LICTS_withUpdateMDDs_NOP.keepOnlyRelevantUpdatedMDDs = false;
//        this.solvers.add(LICTS_withUpdateMDDs_NOP);
//
//        OnlineLifelongICTS LICTS_withUpdateMDDs_2SP = new OnlineLifelongICTS(ICTS_Solver.PruningStrategy.S2P);
//        LICTS_withUpdateMDDs_2SP.name = LICTS_withUpdateMDDs_2SP.name + "LICTS_withUpdateMDDs_2SP";
//        LICTS_withUpdateMDDs_2SP.updateMDDsWhenTimeProgresses = true;
//        LICTS_withUpdateMDDs_2SP.keepOnlyRelevantUpdatedMDDs = false;
//        this.solvers.add(LICTS_withUpdateMDDs_2SP);
//
//        OnlineLifelongICTS LICTS_withUpdateMDDs_3SP = new OnlineLifelongICTS(ICTS_Solver.PruningStrategy.S3P);
//        LICTS_withUpdateMDDs_3SP.name = LICTS_withUpdateMDDs_3SP.name + "LICTS_withUpdateMDDs_3SP";
//        LICTS_withUpdateMDDs_3SP.updateMDDsWhenTimeProgresses = true;
//        LICTS_withUpdateMDDs_3SP.keepOnlyRelevantUpdatedMDDs = false;
//        this.solvers.add(LICTS_withUpdateMDDs_3SP);
//
//        OnlineLifelongICTS LICTS_withUpdateMDDs_2EP = new OnlineLifelongICTS(ICTS_Solver.PruningStrategy.E2P);
//        LICTS_withUpdateMDDs_2EP.name = LICTS_withUpdateMDDs_2EP.name + "LICTS_withUpdateMDDs_2EP";
//        LICTS_withUpdateMDDs_2EP.updateMDDsWhenTimeProgresses = true;
//        LICTS_withUpdateMDDs_2EP.keepOnlyRelevantUpdatedMDDs = false;
//        this.solvers.add(LICTS_withUpdateMDDs_2EP);
//
//        OnlineLifelongICTS LICTS_withUpdateMDDs_3EP = new OnlineLifelongICTS(ICTS_Solver.PruningStrategy.E3P);
//        LICTS_withUpdateMDDs_3EP.name = LICTS_withUpdateMDDs_3EP.name + "LICTS_withUpdateMDDs_3EP";
//        LICTS_withUpdateMDDs_3EP.updateMDDsWhenTimeProgresses = true;
//        LICTS_withUpdateMDDs_3EP.keepOnlyRelevantUpdatedMDDs = false;
//        this.solvers.add(LICTS_withUpdateMDDs_3EP);

    }

    /*  = Set Experiments =  */
    @Override
    protected void setExperiments() {
        addExperimentsLifelongICTS();
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
                    "Average Delta Cost",
                    InstanceReport.StandardFields.numReroutes,
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

    private void addExperimentsLifelongICTS() {
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\Online\\\\MovingAI_Instances\\\\extensive - AR1"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{40});

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new OnlineInstanceBuilder_MovingAI(), properties);

        /*  =   Add new experiment   =  */
        OnlineExperiment experiment = new OnlineExperiment("L-ICTS", instanceManager, null);
        experiment.keepSolutionInReport = false;
        this.experiments.add(experiment);

    }

}
