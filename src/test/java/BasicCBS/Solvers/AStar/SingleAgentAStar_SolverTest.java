package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import Environment.IO_Package.IO_Manager;
import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.*;
import BasicCBS.Solvers.*;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SingleAgentAStar_SolverTest {

    private final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final Enum_MapLocationType w = Enum_MapLocationType.WALL;
    private Enum_MapLocationType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    private I_Map mapCircle = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);

    Enum_MapLocationType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    private I_Map mapEmpty = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);

    Enum_MapLocationType[][] map_2D_withPocket = {
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

    private I_Location location12Circle = mapCircle.getMapLocation(coor12);
    private I_Location location13Circle = mapCircle.getMapLocation(coor13);
    private I_Location location14Circle = mapCircle.getMapLocation(coor14);
    private I_Location location22Circle = mapCircle.getMapLocation(coor22);
    private I_Location location24Circle = mapCircle.getMapLocation(coor24);
    private I_Location location32Circle = mapCircle.getMapLocation(coor32);
    private I_Location location33Circle = mapCircle.getMapLocation(coor33);
    private I_Location location34Circle = mapCircle.getMapLocation(coor34);

    private I_Location location11 = mapCircle.getMapLocation(coor11);
    private I_Location location43 = mapCircle.getMapLocation(coor43);
    private I_Location location53 = mapCircle.getMapLocation(coor53);
    private I_Location location05 = mapCircle.getMapLocation(coor05);

    private I_Location location04 = mapCircle.getMapLocation(coor04);
    private I_Location location00 = mapCircle.getMapLocation(coor00);

    private Agent agent33to12 = new Agent(0, coor33, coor12);
    private Agent agent12to33 = new Agent(1, coor12, coor33);
    private Agent agent53to05 = new Agent(0, coor53, coor05);
    private Agent agent43to11 = new Agent(0, coor43, coor11);
    private Agent agent04to00 = new Agent(0, coor04, coor00);

    InstanceBuilder_BGU builder = new InstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent53to05});
    private MAPF_Instance instanceEmpty2 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent43to11});
    private MAPF_Instance instance1stepSolution = im.getNextInstance();
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent04to00});

    I_Solver aStar = new SingleAgentAStar_Solver();

    @BeforeEach
    void setUp() {

    }

    @Test
    void oneMoveSolution() {
        MAPF_Instance testInstance = instance1stepSolution;
        Solution s = aStar.solve(testInstance, new RunParameters());

        Map<Agent, SingleAgentPlan> plans = new HashMap<>();
        SingleAgentPlan plan = new SingleAgentPlan(testInstance.agents.get(0));
        I_Location location = testInstance.map.getMapLocation(new Coordinate_2D(4,5));
        plan.addMove(new Move(testInstance.agents.get(0), 1, location, location));
        plans.put(testInstance.agents.get(0), plan);
        Solution expected = new Solution(plans);

        assertEquals(s, expected);
    }

    @Test
    void circleOptimality1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        Solution solved = aStar.solve(testInstance, new RunParameters());

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 2, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 3, location22Circle, location12Circle));
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
        Constraint vertexConstraint = new Constraint(null, 1, null, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(vertexConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location33Circle));
        plan.addMove(new Move(agent, 2, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 4, location22Circle, location12Circle));
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
        Constraint vertexConstraint = new Constraint(agent, 1, null, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(vertexConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location33Circle));
        plan.addMove(new Move(agent, 2, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 4, location22Circle, location12Circle));
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
        Constraint swappingConstraint = new Constraint(agent, 1, location33Circle, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(swappingConstraint);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location33Circle));
        plan.addMove(new Move(agent, 2, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 4, location22Circle, location12Circle));
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
        Constraint swappingConstraint1 = new Constraint(null, 1, location33Circle, location32Circle);
        Constraint swappingConstraint2 = new Constraint(null, 2, location33Circle, location32Circle);
        Constraint swappingConstraint3 = new Constraint(null, 3, location33Circle, location32Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(swappingConstraint1);
        constraints.add(swappingConstraint2);
        constraints.add(swappingConstraint3);
        RunParameters parameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
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
        plan.addMove(new Move(agent, 1, location12Circle, location22Circle));
        plan.addMove(new Move(agent, 2, location22Circle, location32Circle));
        plan.addMove(new Move(agent, 3, location32Circle, location33Circle));
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
    void accountsForConstraintAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(10, solved1.getPlanFor(agent).size());
    }

    @Test
    void accountsForMultipleConstraintsAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(15, solved1.getPlanFor(agent).size());
    }

    @Test
    void accountsForMultipleConstraintsAfterReachingGoal2() {
        // now with an expected plan

        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,5, null, location33Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        RunParameters runParameters = new RunParameters(constraints);

        Solution solved = aStar.solve(testInstance, runParameters);

        SingleAgentPlan plan1 = new SingleAgentPlan(agent);
        plan1.addMove(new Move(agent, 1, location12Circle, location22Circle));
        plan1.addMove(new Move(agent, 2, location22Circle, location32Circle));
        plan1.addMove(new Move(agent, 3, location32Circle, location33Circle));
        plan1.addMove(new Move(agent, 4, location33Circle, location33Circle));
        plan1.addMove(new Move(agent, 5, location33Circle, location32Circle));
        plan1.addMove(new Move(agent, 6, location32Circle, location33Circle));
        Solution expected1 = new Solution();
        expected1.putPlan(plan1);

        SingleAgentPlan plan2 = new SingleAgentPlan(agent);
        plan2.addMove(new Move(agent, 1, location12Circle, location22Circle));
        plan2.addMove(new Move(agent, 2, location22Circle, location32Circle));
        plan2.addMove(new Move(agent, 3, location32Circle, location33Circle));
        plan2.addMove(new Move(agent, 4, location33Circle, location33Circle));
        plan2.addMove(new Move(agent, 5, location33Circle, location34Circle));
        plan2.addMove(new Move(agent, 6, location34Circle, location33Circle));
        Solution expected2 = new Solution();
        expected2.putPlan(plan2);

        assertEquals(6, solved.getPlanFor(agent).size());
        assertTrue(expected1.equals(solved) || expected2.equals(solved));
    }

    @Test
    void continuingFromExistingPlan() {
        // modified from circleOptimality1()

        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        SingleAgentPlan existingPlan = new SingleAgentPlan(agent);
        existingPlan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        existingPlan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        Solution existingSolution = new Solution();
        existingSolution.putPlan(existingPlan);

        // give the solver a plan to continue from
        Solution solved = aStar.solve(testInstance, new RunParameters(existingSolution));

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(5, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }
}