package Environment.RunManagers;

import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.Maps.MapDimensions;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;


public class RunManagerSimpleExample extends A_RunManager {

    /*  = Set BasicCBS.Solvers =  */
    @Override
    public void setSolvers() {
        this.solvers.add(new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver()));
        this.solvers.add(new CBS_Solver());
    }

    /*  = Set Experiments =  */
    @Override
    public void setExperiments() {
        addExperiment_16_7();
        addExperimentMovingAI_8room();
    }


    /* = Experiments =  */

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
