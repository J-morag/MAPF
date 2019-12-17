package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.*;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Solvers.*;
import BasicCBS.Solvers.AStar.DistanceTableAStarHeuristic;
import BasicCBS.Solvers.AStar.RunParameters_SAAStar;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineConstraintSet;
import OnlineMAPF.OnlineDistanceTableAStarHeuristic;
import OnlineMAPF.OnlineInstanceBuilder_BGU;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OnlineAStar_Test {

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

    private I_Location cell12Circle = mapCircle.getMapCell(coor12);
    private I_Location cell13Circle = mapCircle.getMapCell(coor13);
    private I_Location cell14Circle = mapCircle.getMapCell(coor14);
    private I_Location cell22Circle = mapCircle.getMapCell(coor22);
    private I_Location cell24Circle = mapCircle.getMapCell(coor24);
    private I_Location cell32Circle = mapCircle.getMapCell(coor32);
    private I_Location cell33Circle = mapCircle.getMapCell(coor33);
    private I_Location cell34Circle = mapCircle.getMapCell(coor34);

    private I_Location cell11 = mapCircle.getMapCell(coor11);
    private I_Location cell43 = mapCircle.getMapCell(coor43);
    private I_Location cell53 = mapCircle.getMapCell(coor53);
    private I_Location cell05 = mapCircle.getMapCell(coor05);

    private I_Location cell04 = mapCircle.getMapCell(coor04);
    private I_Location cell00 = mapCircle.getMapCell(coor00);

    private Agent agent33to12 = new OnlineAgent(0, coor33, coor12, 0);
    private Agent agent12to33 = new OnlineAgent(1, coor12, coor33, 0);
    private Agent agent53to05 = new OnlineAgent(2, coor53, coor05, 0);
    private Agent agent43to11 = new OnlineAgent(3, coor43, coor11, 0);
    private Agent agent04to00 = new OnlineAgent(4, coor04, coor00, 0);

    String instancesPath = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances", "Online"});
    InstanceBuilder_BGU builder = new OnlineInstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(instancesPath, builder, new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent53to05});
    private MAPF_Instance instanceEmpty2 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent43to11});
    private MAPF_Instance instance1stepSolution = im.getNextInstance();
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent04to00});

    I_Solver aStar = new OnlineAStar();

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
    void oneMoveSolution() {
        MAPF_Instance testInstance = instance1stepSolution;
        Solution s = aStar.solve(testInstance, new RunParameters());

        Map<Agent, SingleAgentPlan> plans = new HashMap<>();
        SingleAgentPlan plan = new SingleAgentPlan(testInstance.agents.get(0));
        I_Location cell = testInstance.map.getMapCell(new Coordinate_2D(4,5));
        plans.put(testInstance.agents.get(0), plan);
        Solution expected = new Solution(plans);

        assertEquals(s, expected);
        assertTrue(s.isValidSolution());
    }

    @Test
    void circleOptimality1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        Solution solved = aStar.solve(testInstance, new RunParameters());

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell33Circle, cell32Circle));
        plan.addMove(new Move(agent, 2, cell32Circle, cell22Circle));
        plan.addMove(new Move(agent, 3, cell22Circle, cell12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(3, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);

    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint vertexConstraint = new Constraint(null, 1, null, cell32Circle);
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(vertexConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell33Circle, cell33Circle));
        plan.addMove(new Move(agent, 2, cell33Circle, cell32Circle));
        plan.addMove(new Move(agent, 3, cell32Circle, cell22Circle));
        plan.addMove(new Move(agent, 4, cell22Circle, cell12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(4, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);

    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint2(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint vertexConstraint = new Constraint(agent, 1, null, cell32Circle);
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(vertexConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell33Circle, cell33Circle));
        plan.addMove(new Move(agent, 2, cell33Circle, cell32Circle));
        plan.addMove(new Move(agent, 3, cell32Circle, cell22Circle));
        plan.addMove(new Move(agent, 4, cell22Circle, cell12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(4, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint3(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint swappingConstraint = new Constraint(agent, 1, cell33Circle, cell32Circle);
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(swappingConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell33Circle, cell33Circle));
        plan.addMove(new Move(agent, 2, cell33Circle, cell32Circle));
        plan.addMove(new Move(agent, 3, cell32Circle, cell22Circle));
        plan.addMove(new Move(agent, 4, cell22Circle, cell12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(4, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalityOtherDirectionBecauseOfConstraints(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint swappingConstraint1 = new Constraint(null, 1, cell33Circle, cell32Circle);
        Constraint swappingConstraint2 = new Constraint(null, 2, cell33Circle, cell32Circle);
        Constraint swappingConstraint3 = new Constraint(null, 3, cell33Circle, cell32Circle);
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(swappingConstraint1);
        constraints.add(swappingConstraint2);
        constraints.add(swappingConstraint3);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell33Circle, cell34Circle));
        plan.addMove(new Move(agent, 2, cell34Circle, cell24Circle));
        plan.addMove(new Move(agent, 3, cell24Circle, cell14Circle));
        plan.addMove(new Move(agent, 4, cell14Circle, cell13Circle));
        plan.addMove(new Move(agent, 5, cell13Circle, cell12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(5, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);

    }

    @Test
    void circleOptimalityNorthwestToSoutheast(){
        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Solution solved = aStar.solve(testInstance, new RunParameters());

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell12Circle, cell22Circle));
        plan.addMove(new Move(agent, 2, cell22Circle, cell32Circle));
        plan.addMove(new Move(agent, 3, cell32Circle, cell33Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(3, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void emptyOptimality(){
        MAPF_Instance testInstance1 = instanceEmpty1;
        Agent agent1 = testInstance1.agents.get(0);

        MAPF_Instance testInstance2 = instanceEmpty2;
        Agent agent2 = testInstance2.agents.get(0);

        Solution solved1 = aStar.solve(testInstance1, new RunParameters());
        Solution solved2 = aStar.solve(testInstance2, new RunParameters());

        assertEquals(7, solved1.getPlanFor(agent1).size());
        assertEquals(5, solved2.getPlanFor(agent2).size());
    }

    @Test
    void unsolvableShouldTimeout(){
        MAPF_Instance testInstance = instanceUnsolvable;

        // three second timeout
        RunParameters runParameters = new RunParameters(1000*3);
        Solution solved = aStar.solve(testInstance, runParameters);

        assertNull(solved);
    }

    @Test
    void ignoresConstraintAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal = new Constraint(agent,9, null, instanceEmpty1.map.getMapCell(coor05));
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertNotEquals(10, solved1.getPlanFor(agent).size());
    }

    @Test
    void ignoresMultipleConstraintsAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapCell(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapCell(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapCell(coor05));
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertNotEquals(15, solved1.getPlanFor(agent).size());
    }

    @Test
    void ignoresMultipleConstraintsAfterReachingGoal2() {
        // now with an expected plan

        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,5, null, cell33Circle);
        ConstraintSet constraints = new OnlineConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, runParameters);

        SingleAgentPlan plan1 = new SingleAgentPlan(agent);
        plan1.addMove(new Move(agent, 1, cell12Circle, cell22Circle));
        plan1.addMove(new Move(agent, 2, cell22Circle, cell32Circle));
        plan1.addMove(new Move(agent, 3, cell32Circle, cell33Circle));
        plan1.addMove(new Move(agent, 4, cell33Circle, cell33Circle));
        plan1.addMove(new Move(agent, 5, cell33Circle, cell32Circle));
        plan1.addMove(new Move(agent, 6, cell32Circle, cell33Circle));
        Solution expected1 = new Solution();
        expected1.putPlan(plan1);

        SingleAgentPlan plan2 = new SingleAgentPlan(agent);
        plan2.addMove(new Move(agent, 1, cell12Circle, cell22Circle));
        plan2.addMove(new Move(agent, 2, cell22Circle, cell32Circle));
        plan2.addMove(new Move(agent, 3, cell32Circle, cell33Circle));
        plan2.addMove(new Move(agent, 4, cell33Circle, cell33Circle));
        plan2.addMove(new Move(agent, 5, cell33Circle, cell34Circle));
        plan2.addMove(new Move(agent, 6, cell34Circle, cell33Circle));
        Solution expected2 = new Solution();
        expected2.putPlan(plan2);

        assertNotEquals(6, solved.getPlanFor(agent).size());
        assertFalse(expected1.equals(solved) || expected2.equals(solved));
    }

    @Test
    void continuingFromExistingPlan() {
        // modified from circleOptimality1()

        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        SingleAgentPlan existingPlan = new SingleAgentPlan(agent);
        existingPlan.addMove(new Move(agent, 1, cell33Circle, cell34Circle));
        existingPlan.addMove(new Move(agent, 2, cell34Circle, cell24Circle));
        Solution existingSolution = new Solution();
        existingSolution.putPlan(existingPlan);

        // give the solver a plan to continue from
        Solution solved = aStar.solve(testInstance, new RunParameters(existingSolution));

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, cell33Circle, cell34Circle));
        plan.addMove(new Move(agent, 2, cell34Circle, cell24Circle));
        plan.addMove(new Move(agent, 3, cell24Circle, cell14Circle));
        plan.addMove(new Move(agent, 4, cell14Circle, cell13Circle));
        plan.addMove(new Move(agent, 5, cell13Circle, cell12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(5, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void reasonableRuntimeManhattanDistance() {
        long reasonableAverageRuntimeManhattanDistance = 2000;
        InstanceManager im = new InstanceManager(instancesPath, builder,
                new InstanceProperties(
                        new MapDimensions(257, 256), -1f, new int[]{5}
                        ));
        MAPF_Instance fullInstance;

        while( (fullInstance = im.getNextInstance()) != null ){
            int numAgents = fullInstance.agents.size(); //is also the number of instances we will test.
            long sumRuntimes = 0;
            for (int i = 0; i < numAgents; i++) {
                OnlineAgent agent = ((OnlineAgent) fullInstance.agents.get(i) );
                MAPF_Instance singleAgentInstance = fullInstance.getSubproblemFor(agent);
                RunParameters_SAAStar runParameters = new RunParameters_SAAStar(0);

                runParameters.agentStartLocation = agent.getPrivateGarage(singleAgentInstance.map.getMapCell(agent.source));

                long timeBefore = System.currentTimeMillis();
                Solution solved = aStar.solve(singleAgentInstance, runParameters);
                sumRuntimes += (System.currentTimeMillis() - timeBefore);

                assertTrue(solved.isValidSolution());
            }
            long averageRuntime = sumRuntimes/numAgents;
            System.out.println("instance: " + fullInstance.name);
            System.out.println("average runtime (ms): " + averageRuntime);

            assert(averageRuntime < reasonableAverageRuntimeManhattanDistance);
        }
    }


    @Test
    void reasonableRuntimeDistanceTable() {
        long reasonableAverageRuntimeManhattanDistance = 200;
        InstanceManager im = new InstanceManager(instancesPath, builder,
                new InstanceProperties(
//                        new MapDimensions(257, 256), -1f, new int[]{5}
                ));
        MAPF_Instance fullInstance;

        while( (fullInstance = im.getNextInstance()) != null ){
            int numAgents = fullInstance.agents.size(); //is also the number of instances we will test.
            long sumRuntimes = 0;
            for (int i = 0; i < numAgents; i++) {
                OnlineAgent agent = ((OnlineAgent) fullInstance.agents.get(i) );
                MAPF_Instance singleAgentInstance = fullInstance.getSubproblemFor(agent);
                RunParameters_SAAStar runParameters = new RunParameters_SAAStar(0, instanceReport);

                runParameters.agentStartLocation = agent.getPrivateGarage(singleAgentInstance.map.getMapCell(agent.source));

                long timeBefore = System.currentTimeMillis();
                runParameters.heuristicFunction = new OnlineDistanceTableAStarHeuristic(new DistanceTableAStarHeuristic(new ArrayList<>(singleAgentInstance.agents), singleAgentInstance.map));
                Solution solved = aStar.solve(singleAgentInstance, runParameters);
                sumRuntimes += (System.currentTimeMillis() - timeBefore);

                assertTrue(solved.isValidSolution());
            }
            long averageRuntime = sumRuntimes/numAgents;
            System.out.println("instance: " + fullInstance.name);
            System.out.println("average runtime (ms): " + averageRuntime);

            assert(averageRuntime < reasonableAverageRuntimeManhattanDistance);
        }
    }
}