package OnlineMAPF;

import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.Maps.MapDimensions;
import Environment.A_RunManager;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineInstanceBuilder_MovingAI;
import OnlineMAPF.Solvers.NaiveOnlineSolver;
import OnlineMAPF.Solvers.OnlineCompatibleOfflineCBS;
import OnlineMAPF.Solvers.OnlineSolverContainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class RunManagerOnline extends A_RunManager {

    String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"});

    /*  = Set Solvers =  */
    @Override
    protected void setSolvers() {
        this.solvers.add(new OnlineCompatibleOfflineCBS());
        this.solvers.add(new OnlineSolverContainer(new NaiveOnlineSolver()));
    }

    /*  = Set Experiments =  */
    @Override
    protected void setExperiments() {
        addExperimentsUniqueAgentsPoissonDistribution();
        addExperimentUniformRepeating();
        addExperimentStandardNormalDistribution();
    }

    @Override
    public void runAllExperiments() {
        try {
            S_Metrics.setHeader(new String[]{   InstanceReport.StandardFields.experimentName,
                    InstanceReport.StandardFields.mapName,
                    InstanceReport.StandardFields.agentSelection,
                    InstanceReport.StandardFields.arrivalDistribution,
                    InstanceReport.StandardFields.arrivalRate,
                    InstanceReport.StandardFields.numAgents,
                    InstanceReport.StandardFields.solver,
                    InstanceReport.StandardFields.solved,
                    "valid",
                    InstanceReport.StandardFields.elapsedTimeMS,
                    InstanceReport.StandardFields.solutionCost,
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
//                            InstanceReport.StandardFields.mapName,
//                            InstanceReport.StandardFields.agentSelection,
//                            InstanceReport.StandardFields.arrivalDistribution,
//                            InstanceReport.StandardFields.arrivalRate,
//                            InstanceReport.StandardFields.numAgents,
//                            InstanceReport.StandardFields.solver,
//                            InstanceReport.StandardFields.solved,
//                            InstanceReport.StandardFields.elapsedTimeMS,
//                            InstanceReport.StandardFields.solutionCost,
//                            InstanceReport.StandardFields.solution}
                            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* = Experiments =  */

    private void addExperimentsUniqueAgentsPoissonDistribution() {
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\Online\\\\MovingAI_Instances\\\\unique_agents_poisson"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{5, 10, 20, 40, 60, 80, 100});


        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new OnlineInstanceBuilder_MovingAI(), properties);

        /*  =   Add new experiment   =  */
        Experiment gridExperiment = new OnlineExperiment("unique_agents_poisson", instanceManager);
        this.experiments.add(gridExperiment);
    }

    private void addExperimentUniformRepeating() {
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\Online\\\\MovingAI_Instances\\\\repeatingUniform_agents_poisson"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{5, 10, 20, 40, 60, 80, 100});


        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new OnlineInstanceBuilder_MovingAI(), properties);

        /*  =   Add new experiment   =  */
        Experiment gridExperiment = new OnlineExperiment("repeatingUniform_agents_poisson", instanceManager);
        this.experiments.add(gridExperiment);
    }

    private void addExperimentStandardNormalDistribution() {
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\Online\\\\MovingAI_Instances\\\\repeatingNormal_agents_poisson"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{5, 10, 20, 40, 60, 80, 100});


        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new OnlineInstanceBuilder_MovingAI(), properties);

        /*  =   Add new experiment   =  */
        Experiment gridExperiment = new OnlineExperiment("repeatingNormal_agents_poisson", instanceManager);
        this.experiments.add(gridExperiment);
    }

}
