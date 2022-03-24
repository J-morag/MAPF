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
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.RunParameters;
import BasicMAPF.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import LifelongMAPF.AgentSelectors.AllAgentsSubsetSelector;
import LifelongMAPF.AgentSelectors.MandatoryAgentsSubsetSelector;
import LifelongMAPF.Triggers.DestinationAchievedTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LifelongSimulationSolverTest {

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
    private final LifelongAgent agent34to33 = new LifelongAgent(new Agent(10, coor34, coor33), new I_Coordinate[]{coor34, coor04, coor12, coor00, coor54,  coor35, coor15, coor33});
    private final LifelongAgent agent34to43 = new LifelongAgent(new Agent(11, coor34, coor43), new I_Coordinate[]{coor34, coor15, coor35, coor54, coor04, coor00, coor12, coor43});

    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle2", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00, agent04to00, agent33to35, agent34to32});
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
                    agent34to33,
                    agent34to43
    });
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});

    I_Solver snapshotOptimal = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new AllAgentsSubsetSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, true));
    I_Solver mandatoryAgentsOptimal = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new MandatoryAgentsSubsetSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, true));
    I_Solver replanSingle = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new MandatoryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, 0, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
    I_Solver allAgentsPrPr = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new AllAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, 30, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
    I_Solver mandatoryAgentsPrPr = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new MandatoryAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, 30, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));


    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
    }

    void validate(Solution solution, int expectedSOC, int expectedMakespan, MAPF_Instance instance){
        validate(solution, instance);

        assertEquals(expectedSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(expectedMakespan, solution.makespan()); // makespan is optimal
    }

    void validate(Solution solution, MAPF_Instance instance){
        assertTrue(solution.isValidSolution()); //is valid (no conflicts)
        assertTrue(solution.solves(instance));

        assertEquals(instance.agents.size(), solution.size()); // solution includes all agents
    }

    @Test
    void emptyMapValidityTest1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance);
    }

    @Test
    void circleMapValidityTest1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_SnapshotOptimal() {
        I_Solver solver = snapshotOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void emptyMapValidityTest1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void circleMapValidityTest1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        
        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        
        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void startAdjacentGoAroundValidityTest_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_ReplanSingle() {
        I_Solver solver = replanSingle;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void emptyMapValidityTest1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void circleMapValidityTest1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void startAdjacentGoAroundValidityTest_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_AllAgentsPrPr() {
        I_Solver solver = allAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void emptyMapValidityTest1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void circleMapValidityTest1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void startAdjacentGoAroundValidityTest_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_MandatoryAgentsOptimal() {
        I_Solver solver = mandatoryAgentsOptimal;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void emptyMapValidityTest1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void circleMapValidityTest1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void smallMazeDenseValidityTest_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void startAdjacentGoAroundValidityTest_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);
        if (solved != null){
            System.out.println(solved.readableToString());
            validate(solved, testInstance);
        }
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_MandatoryAgentsPrPr() {
        I_Solver solver = mandatoryAgentsPrPr;
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = solver.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

}