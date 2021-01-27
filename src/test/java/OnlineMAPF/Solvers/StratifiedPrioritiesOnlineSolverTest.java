package OnlineMAPF.Solvers;

import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceBuilders.Priorities;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StratifiedPrioritiesOnlineSolverTest {

    @Test
    void cbsWithPrioritiesUsingBuilder() {
        boolean useAsserts = true;

        List<I_Solver> solvers = new ArrayList<>();
        solvers.add(new OnlineSolverContainer(new StratifiedPrioritiesOnlineSolver(StratifiedPrioritiesOnlineSolver.OfflineSolverStrategy.CBS)));
        solvers.add(new OnlineSolverContainer(new StratifiedPrioritiesOnlineSolver(StratifiedPrioritiesOnlineSolver.OfflineSolverStrategy.PRIORITISED_PLANNING)));

        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "TestingBenchmark"});
        for (I_Solver solver : solvers) {
            for (Priorities.PrioritiesPolicy policy : Priorities.PrioritiesPolicy.values()){
                InstanceManager instanceManager = new InstanceManager(path,
                        new InstanceBuilder_BGU(new Priorities(policy, new int[]{1, 3, 5})));

                MAPF_Instance instance = null;
                long timeout = 30 /*seconds*/
                        *1000L;
                int i= 0;
                int max = 15;
                // run all benchmark instances. this code is mostly copied from Environment.Experiment.
                while ((instance = instanceManager.getNextInstance()) != null && max > i++) {
                    InstanceReport report = new InstanceReport();
//                    InstanceReport report = S_Metrics.newInstanceReport();
//                    report.putStringValue(InstanceReport.StandardFields.experimentName, "TestingBenchmark");
//                    report.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
//                    report.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
//                    report.putStringValue(InstanceReport.StandardFields.solver, solver.name());

                    RunParameters runParameters = new RunParameters(timeout, null, report, null);

                    //solve
                    System.out.println("---------- solving "  + instance.name + " ----------");
                    Solution solution = solver.solve(instance, runParameters);

                    // validate
                    boolean solved = solution != null;
                    System.out.println("Solved?: " + (solved ? "yes" : "no"));

                    if(solution != null){
                        boolean valid = solution.solves(instance);
                        System.out.println("Valid?: " + (valid ? "yes" : "no"));
                        if (useAsserts) assertTrue(valid);
                    }
                }
            }
        }
//
//        //save results
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
//        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "CBS_Tests"});
//        File directory = new File(resultsOutputDir);
//        if (! directory.exists()){
//            directory.mkdir();
//        }
//        String updatedPath = resultsOutputDir + "\\results " + dateFormat.format(System.currentTimeMillis()) + ".csv";
//        try {
//            S_Metrics.exportCSV(new FileOutputStream(updatedPath));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }

}