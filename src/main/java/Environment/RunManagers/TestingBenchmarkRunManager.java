package Environment.RunManagers;

import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.Maps.MapDimensions;
import BasicMAPF.Solvers.CBS.CBS_Solver;

public class TestingBenchmarkRunManager extends A_RunManager {

    /*  =   Set Path   =*/
    private final String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                                                                "TestingBenchmark"});

    @Override
    void setSolvers() {
        this.solvers.add( new CBS_Solver());
    }

    @Override
    void setExperiments() {

         this.addAllInstancesExperiment();

        // optional, rather than run all instances, filter instances in folder to only those with maps sized 32 and 0.2 obstacle percentage.
//        this.addExperimentInstance_32_20();

    }

    private void addAllInstancesExperiment(){
        InstanceManager instanceManager = new InstanceManager( this.path, new InstanceBuilder_BGU());
        this.experiments.add(new Experiment("All Instances in Test Benchmark", instanceManager));
    }


    private void addExperimentInstance_32_20(){

        /*  Set Properties  */
        InstanceProperties properties = new InstanceProperties( new MapDimensions(new int[]{32,32}, MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL),
                                                                0.2,
                                                                new int[]{5,10,15,20});


        InstanceManager instanceManager = new InstanceManager( this.path, new InstanceBuilder_BGU(), properties);
        this.experiments.add(new Experiment("ExperimentInstance_32_20", instanceManager));

    }
}
