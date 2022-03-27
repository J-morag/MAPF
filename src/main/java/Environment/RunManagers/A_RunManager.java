package Environment.RunManagers;

import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import Environment.Experiment;

import java.util.ArrayList;
import java.util.List;

/**
 * This in an abstract class that overcomes the need to comment out lines in the 'Main' method
 * Moreover, it focuses the user on what it needs to run an experiment.
 * Any RunManager holds a list of {@link I_Solver} and a list of {@link Experiment}
 */
public abstract class A_RunManager {

    protected List<I_Solver> solvers = new ArrayList<>();
    protected List<Experiment> experiments = new ArrayList<>();

    protected abstract void setSolvers();
    protected abstract void setExperiments();

    public void runAllExperiments(){

        setSolvers();
        setExperiments();

        for ( Experiment experiment : experiments ) {

            experiment.runExperiment(solvers);

            System.out.println(experiment.experimentName + " - Done!");
        }

        System.out.println("RunAllExperiments - Done!");
    }


    public static MAPF_Instance getInstanceFromPath(InstanceManager manager, InstanceManager.InstancePath absolutePath){
        return manager.getSpecificInstance(absolutePath);
    }


}
