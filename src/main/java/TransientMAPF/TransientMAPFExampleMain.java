package TransientMAPF;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.IO_Package.IO_Manager;
import Environment.RunManagers.A_RunManager;
import Environment.RunManagers.GenericRunManager;

import java.util.Arrays;

import static Environment.RunManagers.A_RunManager.DEFAULT_RESULTS_OUTPUT_DIR;
import static Environment.RunManagers.A_RunManager.verifyOutputPath;

/**
 * Runs examples of how to use the framework.
 * Good for sanity checks and getting started.
 * Running a real experiment should be done through an implementation of {@link A_RunManager}.
 * Solving a single Instance is also possible by giving a path.
 * For more information, view the examples below.
 */
public class TransientMAPFExampleMain {
    public static void main(String[] args) {
        if (verifyOutputPath(DEFAULT_RESULTS_OUTPUT_DIR)){
            System.out.println("Will print results to the default directory: " + DEFAULT_RESULTS_OUTPUT_DIR);
            System.out.print("Starting in 3 seconds...");
            try {
                Thread.sleep(1000);
                System.out.print(" 2...");
                Thread.sleep(1000);
                System.out.print(" 1...");
                Thread.sleep(1000);
                System.out.println("Running examples...");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            String instancesDir = IO_Manager.buildPath( new String[]{IO_Manager.resources_Directory,"Instances", "MovingAI_Instances"});
            int[] agentNums = new int[]{10};
            int timeoutEach = 1000 * 30;

            GenericRunManager genericRunManager = new GenericRunManager(instancesDir, agentNums, new InstanceBuilder_MovingAI(),
                    "TransientMAPFExampleMain", true, null, DEFAULT_RESULTS_OUTPUT_DIR, "PrP_vs_PrPT", null, timeoutEach);

            PrioritisedPlanning_Solver PrPT = new PrioritisedPlanning_Solver(null, null, null,
                    new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.randomRestarts),
                    null, null, true);
            PrPT.name = "PrPT";

            PrioritisedPlanning_Solver PrP = new PrioritisedPlanning_Solver(null, null, null,
                    new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.randomRestarts),
                    null, null, false);
            PrP.name = "PrP";

            genericRunManager.overrideSolvers(Arrays.asList(PrP, PrPT));
            genericRunManager.runAllExperiments();
        }
    }
}
