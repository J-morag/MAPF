package TransientMAPF;

import BasicMAPF.CostFunctions.SOCCostFunction;
import BasicMAPF.CostFunctions.SSTCostFunction;
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
            int timeoutEach = 1000 * 10;

            GenericRunManager genericRunManager = new GenericRunManager(instancesDir, agentNums, new InstanceBuilder_MovingAI(),
                    "TransientMAPFExampleMain", true, null, DEFAULT_RESULTS_OUTPUT_DIR,
                    "PrP_vs_PrPT", null, timeoutEach);

            // optimizing for SOC

            PrioritisedPlanning_Solver PrPT_SOC = new PrioritisedPlanning_Solver(null, null, new SOCCostFunction(),
                    new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                    null, null, true);
            PrPT_SOC.name = "PrPT_SOC";

            PrioritisedPlanning_Solver PrP_SOC = new PrioritisedPlanning_Solver(null, null, new SOCCostFunction(),
                    new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                    null, null, false);
            PrP_SOC.name = "PrP_SOC";

            // optimizing for SST

            PrioritisedPlanning_Solver PrPT_SST = new PrioritisedPlanning_Solver(null, null, new SSTCostFunction(),
                    new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                    null, null, true);
            PrPT_SST.name = "PrPT_SST";

            PrioritisedPlanning_Solver PrP_SST = new PrioritisedPlanning_Solver(null, null, new SSTCostFunction(),
                    new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                    null, null, false);
            PrP_SST.name = "PrP_SST";

            genericRunManager.overrideSolvers(Arrays.asList(PrP_SOC, PrPT_SOC, PrP_SST, PrPT_SST));
            genericRunManager.runAllExperiments();
        }
    }
}