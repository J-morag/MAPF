package LifelongMAPF;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import Environment.Experiment;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import Environment.RunManagers.A_RunManager;
import LifelongMAPF.AgentSelectors.AllStationaryAgentsSubsetSelector;
import LifelongMAPF.AgentSelectors.FreespaceConflictingAgentsSelector;

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
        A_Solver replanSinglePartialAllowed = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), true, true, true));
        replanSinglePartialAllowed.name = "replanSinglePartialAllowed";
        super.solvers.add(replanSinglePartialAllowed);

        A_Solver replanSingleAllOrNothing = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), true, true, false));
        replanSingleAllOrNothing.name = "replanSingleAllOrNothing";
        super.solvers.add(replanSingleAllOrNothing);

        A_Solver stationaryAgentsLNSPartialAllowed = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new LargeNeighborhoodSearch_Solver(null, null, true, true, null, null, true));
        stationaryAgentsLNSPartialAllowed.name = "stationaryAgentsLNSPartialAllowed";
        super.solvers.add(stationaryAgentsLNSPartialAllowed);

        A_Solver stationaryAgentsLNSAllOrNothing = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new LargeNeighborhoodSearch_Solver(null, null, true, true, null, null, false));
        stationaryAgentsLNSAllOrNothing.name = "stationaryAgentsLNSAllOrNothing";
        super.solvers.add(stationaryAgentsLNSAllOrNothing);

        A_Solver freespaceAgentsLNSPartialAllowed = new LifelongSimulationSolver(null, new FreespaceConflictingAgentsSelector(null, null),
                new LargeNeighborhoodSearch_Solver(null, null, true, true, null, null, true));
        freespaceAgentsLNSPartialAllowed.name = "freespaceAgentsLNSPartialAllowed";
        super.solvers.add(freespaceAgentsLNSPartialAllowed);

        A_Solver freespaceAgentsLNSAllOrNothing = new LifelongSimulationSolver(null, new FreespaceConflictingAgentsSelector(null, null),
                new LargeNeighborhoodSearch_Solver(null, null, true, true, null, null, false));
        freespaceAgentsLNSAllOrNothing.name = "freespaceAgentsLNSAllOrNothing";
        super.solvers.add(freespaceAgentsLNSAllOrNothing);
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
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{maxNumAgents});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{25,50,75,100,125,150});
        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{50,100,150,200});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{200});
//        InstanceProperties properties = new InstanceProperties(null, -1, new int[]{5, 10, 15, 20, 25, 30, 35, 40});

        /*  =   Set Instance Manager   =  */
        InstanceManager instanceManager = new InstanceManager(instancesDir, new InstanceBuilder_Warehouse(),properties);

        /*  =   Add new experiment   =  */
        Experiment warehouseInstances = new Experiment("LifelongWarehouse", instanceManager, null, 2 * 5 * 60 * 1000);
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
                    "reachedTimestepInPlanning",
                    "numPlanningIterations",
                    "avgGroupSize",
                    "waypointTimes",
                    "SOC",
                    "makespan",
                    "timeTo50%Completion",
                    "timeTo80%Completion",
                    "throughputAtT30",
                    "throughputAtT50",
                    "throughputAtT75",
                    "throughputAtT100",
                    "throughputAtT200",
                    "throughputAtT300",
                    "throughputAtT400",
                    "throughputAtT500",
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
