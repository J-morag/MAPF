package LargeAgents_CBS.Environment_LargeAgents;

import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.Maps.MapDimensions;
import Environment.A_RunManager;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import LargeAgents_CBS.Instances.InstanceBuilder_Shapes;
import LargeAgents_CBS.Solvers.HighLevel.CBS_LargeAgents;

public class RunManager_LargeAgents extends A_RunManager {

    /*  = Set BasicCBS.Solvers =  */
    @Override
    protected void setSolvers() {
        super.solvers.add(new CBS_LargeAgents());
    }

    /*  = Set Experiments =  */
    @Override
    protected void setExperiments() {
        this.addExperiment_16_7();
    }


    /* = Experiments =  */

    private void addExperiment_16_7(){
        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.resources_Directory,
                                                            "Instances\\\\LargeAgents_Instances"});

        /*  =   Set Properties   =  */
        InstanceProperties properties = new InstanceProperties(new MapDimensions(16,16), 0, new int[]{7});
        int numOfInstances = 1;

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_Shapes(),properties);

        /*  =   Add new experiment   =  */
        Experiment gridExperiment = new Experiment("Experiment_16_7", instanceManager,numOfInstances);
        this.experiments.add(gridExperiment);
    }


    public static void main(String[] args) {
        A_RunManager largeAgentRunManager = new RunManager_LargeAgents();
        largeAgentRunManager.runAllExperiments();
    }

}
