package LifelongMAPF;

import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.LaCAM.LaCAMBuilder;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LNSBuilder;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DeepPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.WidePartialSolutionsStrategy;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.FreespaceConflictingAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.FailPolicies.*;
import LifelongMAPF.FailPolicies.AStarFailPolicies.Avoid1ASFP;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import TransientMAPF.TransientMAPFSettings;
import org.junit.jupiter.api.*;

import java.util.Arrays;
import java.util.List;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.ex1_optimal_planning_yields_low_throughput;
import static BasicMAPF.TestConstants.Maps.ex2_planning_for_MAPF_is_incomplete_or_inefficient;
import static LifelongMAPF.LifelongRunManagers.LifelongSolversFactory.terminateFailPolicySolver;
import static org.junit.jupiter.api.Assertions.*;
import static LifelongMAPF.LifelongTestUtils.*;
import static LifelongMAPF.LifelongTestConstants.*;

class LifelongSimulationSolverTest {

    I_Solver snapshotOptimal = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(1)),
            new CBSBuilder().setSharedGoals(true).setSharedSources(false).createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
    I_Solver mandatoryAgentsOptimal = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
            new CBSBuilder().setSharedGoals(true).setSharedSources(false).createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
    I_Solver freespaceConflictingAgentsOptimal = new LifelongSimulationSolver(null, new FreespaceConflictingAgentsSelector(new PeriodicSelector(1)),
            new CBSBuilder().setSharedGoals(true).setSharedSources(false).createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
    I_Solver replanSingle = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.none, 1, RestartsStrategy.reorderingStrategy.none, null), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
    I_Solver allAgentsPrPr = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(1)),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 30, RestartsStrategy.reorderingStrategy.none, null), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
    I_Solver mandatoryAgentsPrPr = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 30, RestartsStrategy.reorderingStrategy.none, null), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
    I_Solver freespaceConflictingAgentsPrPr = new LifelongSimulationSolver(null, new FreespaceConflictingAgentsSelector(new PeriodicSelector(1)),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 30, RestartsStrategy.reorderingStrategy.none, null), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver allAgentsLNS = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(1)),
            new LNSBuilder().setSharedGoals(true).setSharedSources(false).createLNS(), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver baselineRHCR_w20_p5 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 30, RestartsStrategy.reorderingStrategy.none, null), true, false, null, 20, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver mandatoryAgentsPrPrDeepPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 30, RestartsStrategy.reorderingStrategy.none, null), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP();
    
    I_Solver lotsAndPrPT_h1 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
                    new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(new Avoid1ASFP()), null, new SumServiceTimes(),
                            new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 100, RestartsStrategy.reorderingStrategy.randomRestarts, null),
                            true, false, TransientMAPFSettings.defaultTransientMAPF, 10, new FailPolicy(1, new AvoidFailPolicy(true))),
                    null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 1, null);

    I_Solver modern1 = LifelongSolversFactory.LH1_Approach10ASFP_Cap18_Timeout1p5();

    InstanceReport instanceReport;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }


    /* = Snapshot Optimal = */

    @Test
    void emptyMapValidityTest1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    @Disabled
    void smallMazeDenseValidityTest_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(150L * 1000).setInstanceReport(instanceReport).createRP(), 150L * 1000);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Replan Single = */

    @Test
    void emptyMapValidityTest1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        
        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        
        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = All Agents PrPr = */

    @Test
    void emptyMapValidityTest1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Mandatory Agents Optimal = */

    @Test
    void emptyMapValidityTest1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Mandatory Agents PrPr = */

    @Test
    void emptyMapValidityTest1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Freespace Conflicting Agents PrPr = */

    @Test
    void emptyMapValidityTest1_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest2_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Freespace Conflicting Agents Optimal = */

    @Test
    void emptyMapValidityTest1_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);

    }

    @Test
    void circleMapValidityTest2_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }


    /* = All Agents LNS = */

    @Test
    void emptyMapValidityTest1_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest2_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP(), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }


    /* = baseline RHCR with window of 10 and replanning period of 5 = */

    @Test
    void emptyMapValidityTest1_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest2_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP(), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP(), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }


    /* = Mandatory Agents PrPr With RHCR window of 1 = */

    @Test
    void emptyMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    
    /* = Mandatory Agents PrPr Deep Partial = */

    @Test
    void emptyMapValidityTest1_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(2L * 1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setConstraints(constraintSet).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Lots = */

    @Test
    void emptyMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10();
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10();
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10();
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10();
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10();
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }
    
    /* = Other Lots = */

    @Test
    void emptyMapValidityTest1_stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }


    /* = Modern 1 = */

    @Test
    void emptyMapValidityTest1_modern1() {
        I_Solver solver = modern1;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_modern1() {
        I_Solver solver = modern1;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);

    }

    @Test
    void circleMapValidityTest2_modern1() {
        I_Solver solver = modern1;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_modern1() {
        I_Solver solver = modern1;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_modern1() {
        I_Solver solver = modern1;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }



    /* = Lifelong using PrPT (Transient MAPF) = */

    @Test
    void emptyMapValidityTest1_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void testNoneFailPolicyShouldReturnNullSolution1() {
        I_Solver solver = terminateFailPolicySolver();
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(instanceUnsolvable, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNull(solved);
    }

    @Test
    void testNoneFailPolicyShouldReturnNullSolution2() {
        I_Solver solver = terminateFailPolicySolver();
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = solver.solve(instanceSmallMazeDenser, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);
        assertNull(solved);
    }

    @Test
    void TestMultipleSolversWithTransientBehaviorNarrowCorridor() {
        I_Coordinate start1 = coor00;
        I_Coordinate goal1 = coor03;
        I_Coordinate start2 = coor01;
        I_Coordinate goal2 = coor02;

        I_Coordinate[] repeatedPathAgent1 = new I_Coordinate[1000];
        I_Coordinate[] repeatedPathAgent2 = new I_Coordinate[1000];
        for (int i = 0; i < repeatedPathAgent1.length; i++) {
            repeatedPathAgent1[i] = (i % 2 == 0) ? start1 : goal1;
            repeatedPathAgent2[i] = (i % 2 == 0) ? start2 : goal2;
        }

        MAPF_Instance testInstance = new MAPF_Instance("agent need to clear path" , ex2_planning_for_MAPF_is_incomplete_or_inefficient, new Agent[]{
                new LifelongAgent(new Agent(1, start1, goal1), repeatedPathAgent1),
                new LifelongAgent(new Agent(2, start2, goal2), repeatedPathAgent2)
        });

        int replanningPeriod = 1;
        List<String> solverNames = Arrays.asList("PIBT", "CBS", "CBSt", "PrP", "PrPt", "LaCAM", "LaCAMt");
        List<I_Solver> solvers = Arrays.asList(
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PIBT_Solver(null, null, null, TransientMAPFSettings.defaultTransientMAPF), null, null, null, null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new CBSBuilder().createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new CBSBuilder().setTransientMAPFSettings(TransientMAPFSettings.defaultTransientMAPF).setCostFunction(new SumServiceTimes()).createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultRegularMAPF, null, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF, null, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new LaCAMBuilder().createLaCAM(), null, null, new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new LaCAMBuilder().setSolutionCostFunction(new SumServiceTimes()).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLaCAM(),null, null, new TerminateFailPolicy(), null, null)
        );

        List<RunParameters> parameters = Arrays.asList(
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500)
        );
        TestUtils.solveAndPrintSolutionReportForMultipleSolvers(solvers, solverNames, testInstance, parameters,
                Arrays.asList("Expanded Nodes (High Level)", "Expanded Nodes (Low Level)", "Total Low Level Time (ms)", "Elapsed Time (ms)",  "SOC", "SST", "throughputAt500", "totalOfflineSolverRuntimeMS"));

    }

    @Test
    void TestMultipleSolversWithTransientBehavior() {
        I_Coordinate start1 = coor31;
        I_Coordinate goal1 = coor33;
        I_Coordinate start2 = coor34;
        I_Coordinate goal2 = coor32;

        I_Coordinate[] repeatedPathAgent1 = new I_Coordinate[1000];
        I_Coordinate[] repeatedPathAgent2 = new I_Coordinate[1000];
        for (int i = 0; i < repeatedPathAgent1.length; i++) {
            repeatedPathAgent1[i] = (i % 2 == 0) ? start1 : goal1;
            repeatedPathAgent2[i] = (i % 2 == 0) ? start2 : goal2;
        }

        MAPF_Instance testInstance = new MAPF_Instance("agent need to clear path" , ex1_optimal_planning_yields_low_throughput, new Agent[]{
                new LifelongAgent(new Agent(1, start1, goal1), repeatedPathAgent1),
                new LifelongAgent(new Agent(2, start2, goal2), repeatedPathAgent2)
        });

        int replanningPeriod = 1;
        List<String> solverNames = Arrays.asList("PIBT", "CBS", "CBSt", "PrP", "PrPt", "LaCAM", "LaCAMt");
        List<I_Solver> solvers = Arrays.asList(
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), CanonicalSolversFactory.createPIBTtSolver(), null, null, null, null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new CBSBuilder().createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new CBSBuilder().setTransientMAPFSettings(TransientMAPFSettings.defaultTransientMAPF).setCostFunction(new SumServiceTimes()).createCBS_Solver(), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, null, new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultRegularMAPF, null, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(), null, new SumServiceTimes(), new RestartsStrategy(RestartsStrategy.reorderingStrategy.randomRestarts, 10000, RestartsStrategy.reorderingStrategy.randomRestarts, null), null, null, TransientMAPFSettings.defaultTransientMAPF, null, null), null, new DisallowedPartialSolutionsStrategy(), new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new LaCAMBuilder().createLaCAM(), null, null, new TerminateFailPolicy(), null, null),
                new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)), new LaCAMBuilder().setSolutionCostFunction(new SumServiceTimes()).setTransientMAPFBehaviour(TransientMAPFSettings.defaultTransientMAPF).createLaCAM(),null, null, new TerminateFailPolicy(), null, null)
        );

        List<RunParameters> parameters = Arrays.asList(
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500),
                new LifelongRunParameters(new RunParametersBuilder().setInstanceReport(new InstanceReport()).createRP(), null, 500)
        );
        TestUtils.solveAndPrintSolutionReportForMultipleSolvers(solvers, solverNames, testInstance, parameters,
                Arrays.asList("Expanded Nodes (High Level)", "Expanded Nodes (Low Level)", "Total Low Level Time (ms)", "Elapsed Time (ms)",  "SOC", "SST", "throughputAtT500", "totalOfflineSolverRuntimeMS"));
    }

}