package LifelongMAPF;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import Environment.RunManagers.A_RunManager;
import LifelongMAPF.AgentSelectors.AllAgentsSubsetSelector;
import LifelongMAPF.AgentSelectors.FreespaceConflictingAgentsSelector;
import LifelongMAPF.AgentSelectors.MandatoryAgentsSubsetSelector;
import LifelongMAPF.LifelongSimulationSolver;
import LifelongMAPF.Triggers.DestinationAchievedTrigger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class LifelongRunManagerWarehouse extends A_RunManager {

    private final String warehouseMaps;
    private final Integer maxNumAgents;
    String resultsOutputDir = IO_Manager.buildPath(new String[]{System.getProperty("user.home"), "CBS_Results"});

    public LifelongRunManagerWarehouse(String warehouseMaps, Integer maxNumAgents) {
        this.warehouseMaps = warehouseMaps;
        this.maxNumAgents = maxNumAgents;
    }

    @Override
    public void setSolvers() {
        A_Solver replanSingle = new LifelongSimulationSolver(null, new MandatoryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, 0, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
        replanSingle.name = "ReplanSingle";
        super.solvers.add(replanSingle);
        A_Solver mandatoryAgentsPrPr3 = new LifelongSimulationSolver(null, new MandatoryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, 4, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
        mandatoryAgentsPrPr3.name = "mandatoryAgentsPrPr4";
        super.solvers.add(mandatoryAgentsPrPr3);
        A_Solver freespaceConflictingAgentsPrPr3 = new LifelongSimulationSolver(null, new FreespaceConflictingAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, 4, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
        freespaceConflictingAgentsPrPr3.name = "freespaceConflictingAgentsPrPr4";
        super.solvers.add(freespaceConflictingAgentsPrPr3);
//        A_Solver freespaceConflictingAgentsOptimal = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new FreespaceConflictingAgentsSelector(),
//                new CBS_Solver(null, null, null, null, null, null, true, true));
//        freespaceConflictingAgentsOptimal.name = "freespaceConflictingAgentsOptimal";
//        super.solvers.add(freespaceConflictingAgentsOptimal);

//        A_Solver allAgentsPrPr3 = new LifelongSimulationSolver(null, new AllAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, 3, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
//        allAgentsPrPr3.name = "allAgentsPrPr3";
//        super.solvers.add(allAgentsPrPr3);
//        A_Solver snapshotOptimal = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new AllAgentsSubsetSelector(),
//                new CBS_Solver(null, null, null, null, null, null, true, true));
//        snapshotOptimal.name = "SnapshotOptimal";
//        super.solvers.add(snapshotOptimal);
    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.warehouseMaps);
    }


    /* = Experiments =  */

    private void addAllMapsAndInstances(Integer maxNumAgents, String instancesDir){
        maxNumAgents = maxNumAgents != null ? maxNumAgents : -1;

        /*  =   Set Properties   =  */
//        InstanceProperties properties = new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{maxNumAgents});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{25,50,75,100,125,150});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60});

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, new InstanceBuilder_Warehouse(),properties);

        /*  =   Add new experiment   =  */
        Experiment warehouseInstances = new Experiment("LifelongWarehouse", instanceManager);
        warehouseInstances.keepSolutionInReport = false;
        warehouseInstances.sharedGoals = true;
        warehouseInstances.sharedSources = true;
        this.experiments.add(warehouseInstances);
    }

    @Override
    public void runAllExperiments() {
        try {
            S_Metrics.setHeader(new String[]{
                    InstanceReport.StandardFields.experimentName,
                    InstanceReport.StandardFields.mapName,
                    InstanceReport.StandardFields.instanceName,
                    InstanceReport.StandardFields.numAgents,
                    InstanceReport.StandardFields.solver,
                    InstanceReport.StandardFields.solved,
                    InstanceReport.StandardFields.skipped,
                    InstanceReport.StandardFields.valid,
                    InstanceReport.StandardFields.elapsedTimeMS,
                    InstanceReport.StandardFields.generatedNodes,
                    InstanceReport.StandardFields.expandedNodes,
                    InstanceReport.StandardFields.solutionCost,
                    InstanceReport.StandardFields.solution,
                    "attempt #0 cost",
                    "attempt #1 cost",
                    "attempt #2 cost",
                    "attempt #3 cost",
                    "attempt #4 cost",
                    "attempt #5 cost",
                    "attempt #6 cost",
                    "attempt #7 cost",
                    "attempt #8 cost",
                    "attempt #9 cost",
                    "attempt #0 time",
                    "attempt #1 time",
                    "attempt #2 time",
                    "attempt #3 time",
                    "attempt #4 time",
                    "attempt #5 time",
                    "attempt #6 time",
                    "attempt #7 time",
                    "attempt #8 time",
                    "attempt #9 time",
                    "waypointTimes",
                    "SOC",
                    "makespan",
                    "timeTo50%Completion",
                    "timeTo80%Completion",
                    "throughputAtT250",
                    "averageThroughput",
                    "averageIndividualThroughput"
            });
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
//                            InstanceReport.StandardFields.numAgents,
//                            InstanceReport.StandardFields.solver,
//                            InstanceReport.StandardFields.solved,
//                            InstanceReport.StandardFields.valid,
//                            InstanceReport.StandardFields.elapsedTimeMS,
//                            InstanceReport.StandardFields.solutionCost,
//                            InstanceReport.StandardFields.solution}
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        S_Metrics.clearAll();
    }

}
