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

    }

    private void addExperimentStandardNormalDistribution() {

    }


    private void addExperiment_16_7(){
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\BGU_Instances"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(new MapDimensions(16,16), 0, new int[]{7});
        int numOfInstances = 1;

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_BGU(),properties);

        /*  =   Add new experiment   =  */
        Experiment gridExperiment = new Experiment("Experiment_16_7", instanceManager,numOfInstances);
        this.experiments.add(gridExperiment);
    }

    private void addExperimentMovingAI_8room(){
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                "Instances\\\\MovingAI_Instances"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(new MapDimensions(512,512), -1, new int[]{7,10,15});


        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(), properties);

        /*  =   Add new experiment   =  */
        Experiment gridExperiment = new Experiment("Experiment_8_Room", instanceManager);
        this.experiments.add(gridExperiment);
    }
}