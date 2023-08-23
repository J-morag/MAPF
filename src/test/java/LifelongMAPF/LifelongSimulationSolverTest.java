package LifelongMAPF;

import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DeepPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.WidePartialSolutionsStrategy;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.FreespaceConflictingAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.FailPolicies.AStarFailPolicies.IAvoid1ASFP;
import LifelongMAPF.FailPolicies.FailPolicy;
import LifelongMAPF.FailPolicies.OneActionFailPolicy;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static org.junit.jupiter.api.Assertions.*;
import static LifelongMAPF.LifelongTestUtils.*;
import static LifelongMAPF.LifelongTestConstants.*;

class LifelongSimulationSolverTest {

    I_Solver snapshotOptimal = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new AllAgentsSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, false), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
    I_Solver mandatoryAgentsOptimal = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, false), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
    I_Solver freespaceConflictingAgentsOptimal = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new FreespaceConflictingAgentsSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, false), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
    I_Solver replanSingle = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
    I_Solver allAgentsPrPr = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new AllAgentsSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
    I_Solver mandatoryAgentsPrPr = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
    I_Solver freespaceConflictingAgentsPrPr = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new FreespaceConflictingAgentsSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver allAgentsLNS = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new AllAgentsSelector(),
            new LargeNeighborhoodSearch_Solver(null, null, true, false, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver baselineRHCR_w20_p5 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null, 20, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver mandatoryAgentsPrPrDeepPartial = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);

    I_Solver stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP();
    
    I_Solver lotsAndPrPT_h1 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
                    new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(new IAvoid1ASFP()), null, null,
                            new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                            true, false, true, 10, new FailPolicy(1, new OneActionFailPolicy(true))),
                    null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 1, null);

    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
    }


    /* = Snapshot Optimal = */

    @Test
    void emptyMapValidityTest1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    @Disabled
    void smallMazeDenseValidityTest_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(150L * 1000, null, instanceReport, null), 150L * 1000);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        // cheating: response time = max time left. to get snapshot optimal results.
        LifelongRunParameters parameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), DEFAULT_TIMEOUT);
        Solution solved = solver.solve(testInstance, parameters);
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Replan Single = */

    @Test
    void emptyMapValidityTest1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        
        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        
        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = All Agents PrPr = */

    @Test
    void emptyMapValidityTest1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Mandatory Agents Optimal = */

    @Test
    void emptyMapValidityTest1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Mandatory Agents PrPr = */

    @Test
    void emptyMapValidityTest1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Freespace Conflicting Agents PrPr = */

    @Test
    void emptyMapValidityTest1_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);

    }

    @Test
    void circleMapValidityTest2_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_FreespaceConflictingAgentsPrP() {
        I_Solver solver = freespaceConflictingAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Freespace Conflicting Agents Optimal = */

    @Test
    void emptyMapValidityTest1_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);

    }

    @Test
    void circleMapValidityTest2_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_freespaceConflictingAgentsOptimal() {
        I_Solver solver = freespaceConflictingAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }


    /* = All Agents LNS = */

    @Test
    void emptyMapValidityTest1_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest2_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_AllAgentsLNS() {
        I_Solver solver = allAgentsLNS;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }


    /* = baseline RHCR with window of 10 and replanning period of 5 = */

    @Test
    void emptyMapValidityTest1_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest2_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isValidFullOrPartialSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        LifelongRunParameters lifelongRunParameters = new LifelongRunParameters(new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null), 500L);
        Solution solved = solver.solve(testInstance, lifelongRunParameters);
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        System.out.println(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_baselineRHCR_w10_p5() {
        I_Solver solver = baselineRHCR_w20_p5;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isValidFullOrPartialSolution(solved, testInstance);
    }


    /* = Mandatory Agents PrPr With RHCR window of 1 = */

    @Test
    void emptyMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPCutoff25PercentPartialRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w10();
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    
    /* = Mandatory Agents PrPr Deep Partial = */

    @Test
    void emptyMapValidityTest1_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldBePartialSolution_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2L*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        isPartialSolution(testInstance, solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    @Test
    void unsolvableBecauseConstraintsShouldWorkThanksToFailPolicy2_mandatoryAgentsPrPrDeepPartial() {
        I_Solver solver = mandatoryAgentsPrPrDeepPartial;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNotNull(solved);
        isFullSolution(solved, testInstance);
    }

    /* = Lots = */

    @Test
    void emptyMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10();
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10();
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10();
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10();
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10();
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }
    
    /* = Other Lots = */

    @Test
    void emptyMapValidityTest1_stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP() {
        I_Solver solver = stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }



    /* = Lifelong using PrPT (Transient MAPF) = */

    @Test
    void emptyMapValidityTest1_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_lotsAndPrPT() {
        I_Solver solver = lotsAndPrPT_h1;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

//    @Test
//    void worksWithPrPT_andPlanningPeriod() {
//        I_Solver PrPT = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
//                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(new IAvoid1ASFP()), null, new SSTCostFunction(),
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.none),
//                        false, false, true, null, null),
//                null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 1);
//
//
//        I_Solver PrP = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(1)),
//                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(new IAvoid1ASFP()), null, new SOCCostFunction(),
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0, RestartsStrategy.RestartsKind.none),
//                        true, false, false, null, null),
//                null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 1);
//
//
//        Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
//        Enum_MapLocationType w = Enum_MapLocationType.WALL;
//        Enum_MapLocationType[][] map_2D_empty_with_wall = {
//                {e, e, e, e, e, e},
//                {e, e, e, w, e, e},
//                {e, e, e, w, e, e},
//                {e, e, e, w, e, e},
//                {e, e, e, e, e, e},
//                {e, e, e, e, e, e},
//        };
//        I_ExplicitMap map_empty_with_wall = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty_with_wall);
//        Agent agentXMoving = new LifelongAgent(new Agent(1, coor32, coor10, 1), new I_Coordinate[]{coor32, coor11, coor10});
//        Agent agentYMoving = new LifelongAgent(new Agent(0, coor10, coor42, 1), new I_Coordinate[]{coor10, coor12, coor42});
//        Agent agentXMoving2 = new LifelongAgent(new Agent(2, coor43, coor10, 1), new I_Coordinate[]{coor43, coor02, coor10});
//        MAPF_Instance testInstance = new MAPF_Instance("testInstance", map_empty_with_wall, new Agent[]{agentYMoving, agentXMoving, agentXMoving2});
//
//        Solution solvedNormal = PrP.solve(testInstance, new RunParameters(10 * 1000L, null, instanceReport, null));
//        assertTrue(solvedNormal.solves(testInstance));
//
//        Solution solvedPrPT = PrPT.solve(testInstance, new RunParameters(10 * 1000L, null, instanceReport, null));
//        assertTrue(solvedPrPT.solves(testInstance));
//        System.out.println(solvedPrPT);
//        assertTrue(((LifelongSolution)solvedNormal).throughputAtT(7) < ((LifelongSolution)solvedPrPT).throughputAtT(7));
//    }

}