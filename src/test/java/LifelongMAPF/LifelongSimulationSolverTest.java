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

    private I_Coordinate coor12 = new Coordinate_2D(1,2);
    private I_Coordinate coor13 = new Coordinate_2D(1,3);
    private I_Coordinate coor14 = new Coordinate_2D(1,4);
    private I_Coordinate coor22 = new Coordinate_2D(2,2);
    private I_Coordinate coor24 = new Coordinate_2D(2,4);
    private I_Coordinate coor32 = new Coordinate_2D(3,2);
    private I_Coordinate coor33 = new Coordinate_2D(3,3);
    private I_Coordinate coor34 = new Coordinate_2D(3,4);
    private I_Coordinate coor35 = new Coordinate_2D(3,5);

    private I_Coordinate coor11 = new Coordinate_2D(1,1);
    private I_Coordinate coor43 = new Coordinate_2D(4,3);
    private I_Coordinate coor53 = new Coordinate_2D(5,3);
    private I_Coordinate coor54 = new Coordinate_2D(5,4);
    private I_Coordinate coor55 = new Coordinate_2D(5,5);
    private I_Coordinate coor05 = new Coordinate_2D(0,5);

    private I_Coordinate coor04 = new Coordinate_2D(0,4);
    private I_Coordinate coor00 = new Coordinate_2D(0,0);
    private I_Coordinate coor01 = new Coordinate_2D(0,1);
    private I_Coordinate coor10 = new Coordinate_2D(1,0);
    private I_Coordinate coor15 = new Coordinate_2D(1,5);

    private LifelongAgent agent33to12 = new LifelongAgent(new Agent(0, coor33, coor12), new I_Coordinate[]{coor33, coor14, coor12});
    private LifelongAgent agent12to33 = new LifelongAgent(new Agent(1, coor12, coor33), new I_Coordinate[]{coor12, coor22, coor33});
    private LifelongAgent agent53to05 = new LifelongAgent(new Agent(2, coor53, coor05), new I_Coordinate[]{coor53, coor04, coor33, coor10, coor00, coor04, coor32, coor05});
    private LifelongAgent agent43to11 = new LifelongAgent(new Agent(3, coor43, coor11), new I_Coordinate[]{coor43, coor04, coor54, coor33, coor54, coor35, coor32, coor11});
    private LifelongAgent agent04to54 = new LifelongAgent(new Agent(4, coor04, coor54), new I_Coordinate[]{coor04, coor00, coor04, coor10, coor34, coor35, coor10, coor54});
    private LifelongAgent agent00to10 = new LifelongAgent(new Agent(5, coor00, coor10), new I_Coordinate[]{coor00, coor04, coor33, coor00, coor43, coor04, coor54, coor10});
    private LifelongAgent agent10to00 = new LifelongAgent(new Agent(6, coor10, coor00), new I_Coordinate[]{coor10, coor53, coor35, coor00, coor05, coor54, coor10, coor00});
    private LifelongAgent agent04to00 = new LifelongAgent(new Agent(7, coor04, coor00), new I_Coordinate[]{coor04, coor00, coor53, coor32, coor14, coor00, coor10, coor00});
    private LifelongAgent agent33to35 = new LifelongAgent(new Agent(8, coor33, coor35), new I_Coordinate[]{coor33, coor35, coor53, coor15, coor43, coor04, coor00, coor35});
    private LifelongAgent agent34to32 = new LifelongAgent(new Agent(9, coor34, coor32), new I_Coordinate[]{coor34, coor04, coor35, coor00, coor54, coor12, coor15, coor32});

    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00, agent04to00, agent33to35, agent34to32});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{
            new LifelongAgent(new Agent(5, coor00, coor10), new I_Coordinate[]{coor00, coor10, coor00, coor10}),
            new LifelongAgent(new Agent(6, coor10, coor00), new I_Coordinate[]{coor10, coor00, coor10, coor00})
    });
    private MAPF_Instance instanceSmallMaze = new MAPF_Instance("instanceSmallMaze", mapSmallMaze, new Agent[]{agent04to00, agent00to10});
    private MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});

    I_Solver lifelongSolverCBS = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new AllAgentsSubsetSelector(),
            new CBS_Solver(null, null, null, null, null, null, true, true));
    I_Solver lifelongSolverPrP = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new AllAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, 0, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));
    I_Solver lifelongSolverPrPr = new LifelongSimulationSolver(new DestinationAchievedTrigger(), new AllAgentsSubsetSelector(),
            new PrioritisedPlanning_Solver(null, null, 2, null, PrioritisedPlanning_Solver.RestartStrategy.randomRestarts, true, true));


    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
    }

    void validate(Solution solution, int optimalSOC, int optimalMakespan, MAPF_Instance instance){
        assertTrue(solution.isValidSolution()); //is valid (no conflicts)
        assertTrue(solution.solves(instance));

        assertEquals(instance.agents.size(), solution.size()); // solution includes all agents
        assertEquals(optimalSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(optimalMakespan, solution.makespan()); // makespan is optimal
    }

    void validate(Solution solution, MAPF_Instance instance){
        assertTrue(solution.isValidSolution()); //is valid (no conflicts)
        assertTrue(solution.solves(instance));

        assertEquals(instance.agents.size(), solution.size()); // solution includes all agents
    }

    @Test
    void emptyMapValidityTest1_CBS() {
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance); //need to find actual optimal costs
    }

    @Test
    void emptyMapValidityTest1_PrP() {
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance); //need to find actual optimal costs
    }

    @Test
    void emptyMapValidityTest1_PrPr() {
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance); //need to find actual optimal costs
    }

    @Test
    void circleMapValidityTest1_CBS() {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest1_PrP() {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest1_PrPr() {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2_CBS() {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_PrP() {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void circleMapValidityTest2_PrPr() {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, 8, 5, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_CBS() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_PrP() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest_PrPr() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        validate(solved, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_CBS() {
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_PrP() {
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeoutOrFail_PrPr() {
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(2*1000,null, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_CBS() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_PrP() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull1_PrPr() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_CBS() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = lifelongSolverCBS.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_PrP() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = lifelongSolverPrP.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void unsolvableBecauseConstraintsShouldReturnNull2_PrPr() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        Solution solved = lifelongSolverPrPr.solve(testInstance, new RunParameters(constraintSet, instanceReport, null));
        S_Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

}