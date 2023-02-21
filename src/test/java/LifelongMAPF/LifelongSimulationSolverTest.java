package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Instances.Maps.MapFactory;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.LargeNeighborhoodSearch.LargeNeighborhoodSearch_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.DisallowedPartialSolutionsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.WidePartialSolutionsStrategy;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.FreespaceConflictingAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import LifelongMAPF.Triggers.ActiveButPlanEndedTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifelongSimulationSolverTest {

    private static final long DEFAULT_TIMEOUT = 90L * 1000;
    private final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final Enum_MapLocationType w = Enum_MapLocationType.WALL;
    private final Enum_MapLocationType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    private final I_Map mapCircle = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);

    private final Enum_MapLocationType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    private final I_Map mapEmpty = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);

    Enum_MapLocationType[][] map_2D_withPocket = {
            {e, w, e, w, e, w},
            {e, w, e, e, e, e},
            {w, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, e, w, e, e, e},
    };
    private final I_Map mapWithPocket = MapFactory.newSimple4Connected2D_GraphMap(map_2D_withPocket);

    private final Enum_MapLocationType[][] map_2D_smallMaze = {
            {e, e, e, w, e, w},
            {e, w, e, e, e, e},
            {e, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, w, w, e, e, e},
    };
    private final I_Map mapSmallMaze = MapFactory.newSimple4Connected2D_GraphMap(map_2D_smallMaze);

    private final I_Coordinate coor12 = new Coordinate_2D(1,2); // maze
    private final I_Coordinate coor13 = new Coordinate_2D(1,3); // maze
    private final I_Coordinate coor14 = new Coordinate_2D(1,4); // maze
    private final I_Coordinate coor22 = new Coordinate_2D(2,2); // maze
    private final I_Coordinate coor24 = new Coordinate_2D(2,4);
    private final I_Coordinate coor32 = new Coordinate_2D(3,2); // maze
    private final I_Coordinate coor33 = new Coordinate_2D(3,3); // maze
    private final I_Coordinate coor34 = new Coordinate_2D(3,4); // maze
    private final I_Coordinate coor35 = new Coordinate_2D(3,5); // maze

    private final I_Coordinate coor11 = new Coordinate_2D(1,1);
    private final I_Coordinate coor43 = new Coordinate_2D(4,3); // maze
    private final I_Coordinate coor53 = new Coordinate_2D(5,3); // maze
    private final I_Coordinate coor54 = new Coordinate_2D(5,4); // maze
    private final I_Coordinate coor55 = new Coordinate_2D(5,5); // maze
    private final I_Coordinate coor05 = new Coordinate_2D(0,5);

    private final I_Coordinate coor04 = new Coordinate_2D(0,4); // maze
    private final I_Coordinate coor00 = new Coordinate_2D(0,0); // maze
    private final I_Coordinate coor01 = new Coordinate_2D(0,1); // maze
    private final I_Coordinate coor10 = new Coordinate_2D(1,0); // maze
    private final I_Coordinate coor15 = new Coordinate_2D(1,5); // maze

    private final LifelongAgent agent33to12 = new LifelongAgent(new Agent(0, coor33, coor12), new I_Coordinate[]{coor33, coor14, coor12});
    private final LifelongAgent agent12to33 = new LifelongAgent(new Agent(1, coor12, coor33), new I_Coordinate[]{coor12, coor22, coor33});
    private final LifelongAgent agent53to05 = new LifelongAgent(new Agent(2, coor53, coor05), new I_Coordinate[]{coor53, coor04, coor33, coor10, coor00, coor04, coor32, coor05});
    private final LifelongAgent agent43to11 = new LifelongAgent(new Agent(3, coor43, coor11), new I_Coordinate[]{coor43, coor04, coor54, coor33, coor54, coor35, coor32, coor11});
    private final LifelongAgent agent04to54 = new LifelongAgent(new Agent(4, coor04, coor54), new I_Coordinate[]{coor04, coor00, coor04, coor10, coor34, coor35, coor10, coor54});
    private final LifelongAgent agent00to10 = new LifelongAgent(new Agent(5, coor00, coor10), new I_Coordinate[]{coor00, coor04, coor33, coor00, coor43, coor04, coor54, coor10});
    private final LifelongAgent agent10to00 = new LifelongAgent(new Agent(6, coor10, coor00), new I_Coordinate[]{coor10, coor53, coor35, coor00, coor05, coor54, coor10, coor00});
    private final LifelongAgent agent04to00 = new LifelongAgent(new Agent(7, coor04, coor00), new I_Coordinate[]{coor04, coor00, coor53, coor32, coor14, coor00, coor10, coor00});
    private final LifelongAgent agent33to35 = new LifelongAgent(new Agent(8, coor33, coor35), new I_Coordinate[]{coor33, coor35, coor53, coor15, coor43, coor04, coor00, coor35});
    private final LifelongAgent agent34to32 = new LifelongAgent(new Agent(9, coor34, coor32), new I_Coordinate[]{coor34, coor04, coor35, coor00, coor54, coor12, coor15, coor32});
    private final LifelongAgent agent43to14 = new LifelongAgent(new Agent(10, coor43, coor14), new I_Coordinate[]{coor43, coor04, coor12, coor00, coor54,  coor35, coor15, coor14});
    private final LifelongAgent agent34to43 = new LifelongAgent(new Agent(11, coor34, coor43), new I_Coordinate[]{coor34, coor15, coor35, coor54, coor04, coor00, coor12, coor43});

    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle2", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00, agent34to32});
    private final MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{
            new LifelongAgent(new Agent(5, coor00, coor10), new I_Coordinate[]{coor00, coor10, coor00, coor10}),
            new LifelongAgent(new Agent(6, coor10, coor00), new I_Coordinate[]{coor10, coor00, coor10, coor00})
    });
    private final MAPF_Instance instanceSmallMaze = new MAPF_Instance("instanceSmallMaze", mapSmallMaze, new Agent[]{agent04to00, agent00to10});
    private final MAPF_Instance instanceSmallMazeDense = new MAPF_Instance("instanceSmallMazeDense", mapSmallMaze,
            new Agent[]{
                    agent33to12,
                    agent12to33,
                    agent04to54,
                    agent34to32,
                    agent43to14
    });
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});

    I_Solver snapshotOptimal = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new AllAgentsSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, false), null, new DisallowedPartialSolutionsStrategy(), null, null);
    I_Solver mandatoryAgentsOptimal = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, false), null, new DisallowedPartialSolutionsStrategy(), null, null);
    I_Solver freespaceConflictingAgentsOptimal = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new FreespaceConflictingAgentsSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, false), null, new DisallowedPartialSolutionsStrategy(), null, null);
    I_Solver replanSingle = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), true, false, null), null, new WidePartialSolutionsStrategy(), null, null);
    I_Solver allAgentsPrPr = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new AllAgentsSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null), null, new WidePartialSolutionsStrategy(), null, null);
    I_Solver mandatoryAgentsPrPr = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null), null, new WidePartialSolutionsStrategy(), null, null);
    I_Solver freespaceConflictingAgentsPrPr = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new FreespaceConflictingAgentsSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null), null, new WidePartialSolutionsStrategy(), null, null);

    I_Solver allAgentsLNS = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new AllAgentsSelector(),
            new LargeNeighborhoodSearch_Solver(null, null, true, false, null, null), null, new WidePartialSolutionsStrategy(), null, null);

    I_Solver baselineRHCR_w20_p5 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, 20), null, new WidePartialSolutionsStrategy(), null, null);

    I_Solver mandatoryAgentsPrPrDeepPartial = new LifelongSimulationSolver(new ActiveButPlanEndedTrigger(), new StationaryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, false, null), null, new WidePartialSolutionsStrategy(), null, null);

    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
    }

    void isFullSolution(Solution solution, int expectedSOC, int expectedMakespan, MAPF_Instance instance){
        isFullSolution(solution, instance);

        assertEquals(expectedSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(expectedMakespan, solution.makespan()); // makespan is optimal
    }

    void isValidFullOrPartialSolution(Solution solution, MAPF_Instance instance){
        assertTrue(solution.isValidSolution(false, false)); //is valid (no conflicts)
        assertTrue(solution.solves(instance, false, false)); // solves (could be partial)
    }

    void isFullSolution(Solution solution, MAPF_Instance instance){
        assertTrue(solution.isValidSolution(false, false)); //is valid (no conflicts)
        assertTrue(solution.solves(instance, false, false)); // solves (could be partial)
        // TODO can never guarantee a full solution now - an agent at its last destination will prevent others from ever reaching it (if they have it as one of their destinations)... handle this somehow...
//        assertTrue(new Solution(solution).solves(instance, true, false)); // solves (is full solution)
    }

    private static void isPartialSolution(MAPF_Instance instance, Solution solution) {
        assertTrue(solution.isValidSolution(false, false)); //is valid (no conflicts)
        assertTrue(solution.solves(instance, false, false)); // solves (could be partial)
        assertFalse(new Solution(solution).solves(instance, false, false)); // solves (is full solution)
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
    void emptyMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialRHCR_w01() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w01();
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialRHCR_w01() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w01();
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPCutoff25PercentPartialRHCR_w01() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w01();
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPCutoff25PercentPartialRHCR_w01() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w01();
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPCutoff25PercentPartialRHCR_w01() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialRHCR_w01();
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
    void emptyMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05();
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05();
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05();
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05();
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05() {
        I_Solver solver = LifelongSolversFactory.stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05();
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(DEFAULT_TIMEOUT, null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

}