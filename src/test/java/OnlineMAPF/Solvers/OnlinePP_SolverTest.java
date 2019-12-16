package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.*;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Solvers.AStar.RunParameters_SAAStar;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineInstanceBuilder_BGU;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OnlinePP_SolverTest {


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

    private OnlineAgent agent33to12 = new OnlineAgent(new Agent(0, coor33, coor12));
    private OnlineAgent agent12to33 = new OnlineAgent(new Agent(1, coor12, coor33), 1);
    private OnlineAgent agent53to05 = new OnlineAgent(new Agent(2, coor53, coor05), 0);
    private OnlineAgent agent43to11 = new OnlineAgent(new Agent(3, coor43, coor11), 3);
    private OnlineAgent agent04to00 = new OnlineAgent(new Agent(4, coor04, coor00), 0);
    private OnlineAgent agent00to10 = new OnlineAgent(new Agent(5, coor00, coor10), 1);
    private OnlineAgent agent10to00 = new OnlineAgent(new Agent(6, coor10, coor00), 1);

    private OnlineAgent agent12to33t0 = new OnlineAgent(new Agent(1, coor12, coor33), 0);
    private OnlineAgent agent12to34t0 = new OnlineAgent(new Agent(2, coor12, coor33), 0);
    private OnlineAgent agent11to33t0 = new OnlineAgent(new Agent(3, coor12, coor33), 0);

    private OnlineAgent agent12to33t1 = new OnlineAgent(new Agent(4, coor12, coor33), 1);
    private OnlineAgent agent12to33t3 = new OnlineAgent(new Agent(5, coor12, coor33), 3);
    private OnlineAgent agent12to33t6 = new OnlineAgent(new Agent(6, coor12, coor33), 6);
    private OnlineAgent agent12to33t7 = new OnlineAgent(new Agent(7, coor12, coor33), 7);
    private OnlineAgent agent53to05t1 = new OnlineAgent(new Agent(8, coor53, coor05), 1);
    private OnlineAgent agent53to05t4 = new OnlineAgent(new Agent(9, coor53, coor05), 4);
    private OnlineAgent agent53to05t5 = new OnlineAgent(new Agent(10,  coor53, coor05), 5);
    private OnlineAgent agent53to05t6 = new OnlineAgent(new Agent(11, coor53, coor05), 6);
    private OnlineAgent agent53to05t7 = new OnlineAgent(new Agent(12, coor53, coor05), 7);
    private OnlineAgent agent12to33t0anotherOne = new OnlineAgent(new Agent(13, coor12, coor33), 0);

    InstanceBuilder_BGU builder = new OnlineInstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances", "Online"}),
            builder, new InstanceProperties());

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});
    private MAPF_Instance instanceMultipleAgentsSameSource = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent12to33t0, agent12to34t0});
    private MAPF_Instance instanceMultipleAgentsSameTarget = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent12to33t0, agent11to33t0});
    private MAPF_Instance instanceMultipleAgentsSameSourcesTargets = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent12to33t0, agent12to34t0, agent11to33t0, agent12to33t1, agent12to33t3, agent12to33t6, agent12to33t7, agent53to05t1,
                    agent53to05t4, agent53to05t5, agent53to05t6, agent53to05t7, agent12to33t0anotherOne});

    I_Solver ppSolver = new OnlinePP_Solver(new OnlineSingleAgentAStar_Solver());


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

        assertTrue(solved.isValidSolution());
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.isValidSolution());
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.isValidSolution());
    }

    @Test
    void wasUnsolvableNowSolvableWithWaitBeforeEntering() {
        MAPF_Instance testInstance = instanceUnsolvable;

        // set start location to the agent's private garage
        RunParameters_SAAStar parameters = new RunParameters_SAAStar(instanceReport);
        OnlineAgent agent = ((OnlineAgent)testInstance.agents.get(0) );
        parameters.agentStartLocation = agent.getPrivateGarage(testInstance.map.getMapCell(agent.source));

        Solution solved = ppSolver.solve(testInstance, parameters);

        assertNotNull(solved);
        System.out.println(solved.readableToString());
        // the latter agent (6) will stay at its garage and wait for the former agent (5) to get to its destination and disappear
        assertEquals(6, solved.sumIndividualCosts());
    }

    @Test
    void wasUnsolvableNowSolvableBecauseDisappearAtGoal() {
        OnlineAgent lateAgent10to00 = new OnlineAgent(agent10to00, 2);
        MAPF_Instance testInstance = new MAPF_Instance("nowSolvable", mapWithPocket, new Agent[]{agent00to10, lateAgent10to00});
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertNotNull(solved);
    }

    @Test
    void handlesMultipleAgentsSameSource() {
        MAPF_Instance testInstance = instanceMultipleAgentsSameSource;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.isValidSolution());
    }

    @Test
    void handlesMultipleAgentsSameTarget() {
        MAPF_Instance testInstance = instanceMultipleAgentsSameTarget;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.isValidSolution());
    }

    @Test
    void handlesMultipleAgentsSameSourcesTargets() {
        MAPF_Instance testInstance = instanceMultipleAgentsSameSourcesTargets;
        Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

        assertTrue(solved.isValidSolution());
    }

    @Test
    void biggerInstancesFromDisk() {
        MAPF_Instance testInstance = null;
        while((testInstance = im.getNextInstance()) != null){
            System.out.println("------------ solving " + testInstance.name);
            Solution solved = ppSolver.solve(testInstance, new RunParameters(instanceReport));

            assertTrue(solved.isValidSolution());
            System.out.println(solved.readableToString());
        }
    }

}