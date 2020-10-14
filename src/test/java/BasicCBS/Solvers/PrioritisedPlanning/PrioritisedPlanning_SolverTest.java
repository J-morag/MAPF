package BasicCBS.Solvers.PrioritisedPlanning;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import Environment.IO_Package.IO_Manager;
import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.*;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class PrioritisedPlanning_SolverTest {

    private final Enum_MapCellType e = Enum_MapCellType.EMPTY;
    private final Enum_MapCellType w = Enum_MapCellType.WALL;
    private Enum_MapCellType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    private I_Map mapCircle = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);

    Enum_MapCellType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    private I_Map mapEmpty = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);

    Enum_MapCellType[][] map_2D_withPocket = {
            {e, w, e, w, e, w},
            {e, w, e, e, e, e},
            {w, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, e, w, e, e, e},
    };
    private I_Map mapWithPocket = MapFactory.newSimple4Connected2D_GraphMap(map_2D_withPocket);

    private I_Coordinate coor12 = new Coordinate_2D(1,2);
    private I_Coordinate coor13 = new Coordinate_2D(1,3);
    private I_Coordinate coor14 = new Coordinate_2D(1,4);
    private I_Coordinate coor22 = new Coordinate_2D(2,2);
    private I_Coordinate coor24 = new Coordinate_2D(2,4);
    private I_Coordinate coor32 = new Coordinate_2D(3,2);
    private I_Coordinate coor33 = new Coordinate_2D(3,3);
    private I_Coordinate coor34 = new Coordinate_2D(3,4);

    private I_Coordinate coor11 = new Coordinate_2D(1,1);
    private I_Coordinate coor43 = new Coordinate_2D(4,3);
    private I_Coordinate coor53 = new Coordinate_2D(5,3);
    private I_Coordinate coor05 = new Coordinate_2D(0,5);

    private I_Coordinate coor04 = new Coordinate_2D(0,4);
    private I_Coordinate coor00 = new Coordinate_2D(0,0);
    private I_Coordinate coor01 = new Coordinate_2D(0,1);
    private I_Coordinate coor10 = new Coordinate_2D(1,0);

    private I_Location cell12 = mapCircle.getMapCell(coor12);
    private I_Location cell13 = mapCircle.getMapCell(coor13);
    private I_Location cell14 = mapCircle.getMapCell(coor14);
    private I_Location cell22 = mapCircle.getMapCell(coor22);
    private I_Location cell24 = mapCircle.getMapCell(coor24);
    private I_Location cell32 = mapCircle.getMapCell(coor32);
    private I_Location cell33 = mapCircle.getMapCell(coor33);
    private I_Location cell34 = mapCircle.getMapCell(coor34);

    private I_Location cell11 = mapCircle.getMapCell(coor11);
    private I_Location cell43 = mapCircle.getMapCell(coor43);
    private I_Location cell53 = mapCircle.getMapCell(coor53);
    private I_Location cell05 = mapCircle.getMapCell(coor05);

    private I_Location cell04 = mapCircle.getMapCell(coor04);
    private I_Location cell00 = mapCircle.getMapCell(coor00);
    private I_Location cell01 = mapCircle.getMapCell(coor01);
    private I_Location cell10 = mapCircle.getMapCell(coor10);

    private Agent agent33to12 = new Agent(0, coor33, coor12);
    private Agent agent12to33 = new Agent(1, coor12, coor33);
    private Agent agent53to05 = new Agent(2, coor53, coor05);
    private Agent agent43to11 = new Agent(3, coor43, coor11);
    private Agent agent04to00 = new Agent(4, coor04, coor00);
    private Agent agent00to10 = new Agent(5, coor00, coor10);
    private Agent agent10to00 = new Agent(6, coor10, coor00);

    InstanceBuilder_BGU builder = new InstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            builder, new InstanceProperties(new MapDimensions(257, 256), -1, new int[]{10}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});

    I_Solver ppSolver = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver());


    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = S_Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        S_Metrics.removeReport(instanceReport);
    }

    @Test
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void unsolvableShouldBeInvalid() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertNull(solved);
    }

    @Test
    void sortAgents() {
        MAPF_Instance testInstance = instanceCircle1;
        I_Solver solver = new PrioritisedPlanning_Solver((Agent a1, Agent a2) -> a2.priority - a1.priority);

        Agent agent0 = new Agent(0, coor33, coor12, 10);
        Agent agent1 = new Agent(1, coor12, coor33, 1);

        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParameters(instanceReport));

        agent0 = new Agent(0, coor33, coor12, 1);
        agent1 = new Agent(1, coor12, coor33, 10);

        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParameters(instanceReport));

        assertTrue(agent0prioritisedSolution.solves(testInstance));
        assertTrue(agent1prioritisedSolution.solves(testInstance));

        assertEquals(agent0prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent0prioritisedSolution.getPlanFor(agent0).size(), 3);

        assertEquals(agent1prioritisedSolution.sumIndividualCostsWithPriorities(), 35);
        assertEquals(agent1prioritisedSolution.getPlanFor(agent1).size(), 3);
    }

    @Test
    void biggerInstanceFromDisk() {
        MAPF_Instance testInstance = null;
        while((testInstance = im.getNextInstance()) != null){
            System.out.println("------------ solving " + testInstance.name);
            Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

            assertTrue(solved.isValidSolution());
            System.out.println(solved.readableToString());
        }
    }
}