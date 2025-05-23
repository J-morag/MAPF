package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.GoalConstraint;
import BasicMAPF.TestUtils;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Solvers.*;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;

import java.util.*;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestUtils.unitCostAndNoHeuristic;
import static org.junit.jupiter.api.Assertions.*;

class SingleAgentAStar_SolverTest {

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

    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent53to05});
    private MAPF_Instance instanceEmpty2 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent43to11});
    private MAPF_Instance instance1stepSolution = im.getNextInstance();
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12});
    private MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33});
    private MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent04to00});
    private MAPF_Instance instanceMaze1 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent04to40});
    private MAPF_Instance instanceMaze2 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent00to55});
    private MAPF_Instance instanceMaze3 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent43to53});
    private MAPF_Instance instanceMaze4 = new MAPF_Instance("instanceMaze", mapSmallMaze, new Agent[]{agent53to15});

    I_Solver aStar = CanonicalSolversFactory.createAStarSolver();

    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }


    @Test
    void oneMoveSolution() {
        MAPF_Instance testInstance = instance1stepSolution;
        Solution s = aStar.solve(testInstance, new RunParametersBuilder().createRP());

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

        Solution solved = aStar.solve(testInstance, new RunParametersBuilder().createRP());

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
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

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
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

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
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

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
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

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
    void circleOptimalityOtherDirectionBecauseOfGoalConstraint1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint goalConstraint = new GoalConstraint(null, 1, location22Circle, new Agent(1000, coor34, coor34));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(goalConstraint);
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalityOtherDirectionBecauseOfGoalConstraint1UsingPlan(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Agent constrainingAgent = new Agent(agent.iD+1, coor12, coor22);
        SingleAgentPlan constrainingPlan = new SingleAgentPlan(constrainingAgent, List.of(new Move(constrainingAgent, 1, location12Circle, location22Circle)));
        ConstraintSet constraints = new ConstraintSet();
        constraints.addAll(constraints.allConstraintsForPlan(constrainingPlan));
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalityOtherDirectionBecauseOfGoalConstraint2(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint goalConstraint = new GoalConstraint(null, 2, location22Circle, new Agent(1000, coor34, coor34));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(goalConstraint);
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        plan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        plan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        plan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        plan.addMove(new Move(agent, 5, location13Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(expected, solved);
    }

    @Test
    void circleOptimalitySameDirectionDespiteLateGoalConstraint(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        //constraint
        Constraint goalConstraint = new GoalConstraint(null, 3, location22Circle, new Agent(1000, coor34, coor34));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(goalConstraint);
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = aStar.solve(testInstance, parameters);

        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMove(new Move(agent, 1, location33Circle, location32Circle));
        plan.addMove(new Move(agent, 2, location32Circle, location22Circle));
        plan.addMove(new Move(agent, 3, location22Circle, location12Circle));
        Solution expected = new Solution();
        expected.putPlan(plan);

        assertEquals(expected, solved);
    }

    @Test
    void largeNumberOfConstraints(){
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        List<I_Location> locations = new ArrayList<>();
        for (int i = 0; i <= 5; i++) {
            for (int j = 0; j <= 5; j++) {
                I_Coordinate newCoor = new Coordinate_2D(i, j);
                I_Location newLocation = instanceEmpty1.map.getMapLocation(newCoor);
                locations.add(newLocation);
            }
        }
        Random rand = new Random();
        rand.setSeed(10);
        ConstraintSet constraints = new ConstraintSet();
        Set<I_Location> checkDuplicates = new HashSet<>();
        for (int t = 1; t <= 3000; t++) {
            for (int j = 0; j < 10; j++) {
                I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                if (checkDuplicates.contains(randomLocation)){
                    j--;
                    continue;
                }
                checkDuplicates.add(randomLocation);
                Constraint constraint = new Constraint(agent, t, null, randomLocation);
                constraints.add(constraint);
            }
            checkDuplicates = new HashSet<>();
        }
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = aStar.solve(testInstance, parameters);
        assertNotNull(solved);
    }

    @Test
    void circleOptimalityNorthwestToSoutheast(){
        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Solution solved = aStar.solve(testInstance, new RunParametersBuilder().createRP());

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

        Solution solved1 = aStar.solve(testInstance1, new RunParametersBuilder().createRP());
        Solution solved2 = aStar.solve(testInstance2, new RunParametersBuilder().createRP());

        assertEquals(7, solved1.getPlanFor(agent1).size());
        assertEquals(5, solved2.getPlanFor(agent2).size());
    }

    @Test
    void unsolvableShouldTimeout(){
        MAPF_Instance testInstance = instanceUnsolvable;

        // three second timeout
        RunParameters runParameters = new RunParametersBuilder().setTimeout(1000 * 3).createRP();
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
        RunParameters runParameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(10, solved1.getPlanFor(agent).size());
    }

    @Test
    void accountsForConstraintAfterReachingGoal2() {
        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,5, null, location33Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        RunParameters runParameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        SingleAgentPlan solved = aStar.solve(testInstance, runParameters).getPlanFor(agent);

        System.out.println("found: " + solved);
        assertEquals(6, solved.getCost());
        assertEquals(solved.getFirstMove(), new Move(agent, 1, location12Circle, location22Circle));
        assertTrue(solved.getLastMove().equals(new Move(agent, 6, location34Circle, location33Circle)) || solved.getLastMove().equals(new Move(agent, 6, location32Circle, location33Circle)));
        assertNotEquals(solved.moveAt(5).currLocation, location33Circle);
    }

    @Test
    void accountsForConstraintInFarFutureAfterReachingGoal() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,90, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,200, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        for (int t = 0; t < 200 /*agents*/ * 200 /*timesteps* * 2 /*constraints*/; t++) {
            constraints.add(new Constraint(agent,t, null, instanceEmpty1.map.getMapLocation(coor15)));
        }
        RunParameters runParameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(201, solved1.getPlanFor(agent).size());
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
        RunParameters runParameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved1 = aStar.solve(testInstance, runParameters);

        //was made longer because it has to come back to goal after avoiding the constraint
        assertEquals(15, solved1.getPlanFor(agent).size());
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
        Solution solved = aStar.solve(testInstance, new RunParametersBuilder().setExistingSolution(existingSolution).createRP());

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

    /* Test TMAPF support */

    @Test
    void findsTMAPFPlanUnderConstraintsUsingTMAPFGoalCondition() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
    }

    @Test
    void findsTMAPFPlanUnderConstraintsAlsoAroundGoalUsingTMAPFGoalCondition() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoalAroundGoal1 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor15));
        Constraint constraintAtTimeAfterReachingGoalAroundGoal2 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor04));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);
        constraints.add(constraintAtTimeAfterReachingGoalAroundGoal1);
        constraints.add(constraintAtTimeAfterReachingGoalAroundGoal2);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else,
        // but the surrounding locations also have constraints in the future, so has to take 2 steps
        assertEquals(9, solved1.getPlanFor(agent).size());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetVertexConflict1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor15))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor04, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetVertexConflict2() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor15, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetEdgeConflict1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor15, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }


    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetEdgeConflict2() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // Has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor04, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictOtherAgentArrivesFirst1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor04, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictOtherAgentArrivesFirst2() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor15, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictThisAgentArrivesFirst1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor02)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor02), instanceEmpty1.map.getMapLocation(coor03)),
                new Move(otherAgent, 9, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 10, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor15, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictThisAgentArrivesFirst2() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor25)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor25), instanceEmpty1.map.getMapLocation(coor35)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor35), instanceEmpty1.map.getMapLocation(coor25)),
                new Move(otherAgent, 9, instanceEmpty1.map.getMapLocation(coor25), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 10, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor04, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictSimultaneousArrival1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor25)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor25), instanceEmpty1.map.getMapLocation(coor35)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor35), instanceEmpty1.map.getMapLocation(coor25)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor25), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 9, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor25))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor04, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictSimultaneousArrival2() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor02)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor02), instanceEmpty1.map.getMapLocation(coor03)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor15, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictSimultaneousArrivalAndStay1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor25)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor25), instanceEmpty1.map.getMapLocation(coor35)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor35), instanceEmpty1.map.getMapLocation(coor25)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor25), instanceEmpty1.map.getMapLocation(coor15))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor04, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    @Test
    void testHandlesTMAPFWithTieBreakingForLessConflictsAtChosenTargetTargetConflictSimultaneousArrivalAndStay2() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);

        // hard constraint on target immediately after the earliest possible arrival time
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,8, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        // soft constraints on locations around the target
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent otherAgent = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(otherAgent);
        planToAvoid.addMoves(List.of(
                new Move(otherAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(otherAgent, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(otherAgent, 5, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03)),
                new Move(otherAgent, 6, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor02)),
                new Move(otherAgent, 7, instanceEmpty1.map.getMapLocation(coor02), instanceEmpty1.map.getMapLocation(coor03)),
                new Move(otherAgent, 8, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor04))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.conflictAvoidanceTable = conflictAvoidanceTable;
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));
        System.out.println(planToAvoid);

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
        // should prefer to avoid the conflict with the other agent
        assertEquals(coor15, solved1.getPlanFor(agent).moveAt(8).currLocation.getCoordinate());
    }

    /* Test against other search implementations */

    @Test
    void optimalVsUCS1(){
        MAPF_Instance testInstance = instanceMaze1;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }

    @Test
    void optimalVsUCS2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCS3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCS4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCSDynamic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentMapsWithNames;
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        compareAStarAndUCS(aStar, new InstanceReport(),
                                agent, testInstance, unitCostAndNoHeuristic);
                    }
                }
            }
        }
    }
    @Test
    void optimalVsUCSDynamicWithDistanceTableHeuristic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentMapsWithNames;
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        DistanceTableSingleAgentHeuristic distanceTableAStarHeuristic = new DistanceTableSingleAgentHeuristic(testInstance.agents, testInstance.map);
                        compareAStarAndUCS(aStar, new InstanceReport(),
                                agent, testInstance, distanceTableAStarHeuristic);
                    }
                }
            }
        }
    }
    @Test
    void optimalVsUCSDDynamicWithManhattanDistanceHeuristic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentGridMapsWithNames; // grid maps only!
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        compareAStarAndUCS(aStar, new InstanceReport(), agent, testInstance, new UnitCostsAndManhattanDistance(agent.target));
                    }
                }
            }
        }
    }

    public static class RandomButStableCostsFrom1To10AndNoHeuristic implements SingleAgentGAndH {
        Map<Edge, Integer> randomButStableCosts = new HashMap<>();
        Random rand;

        public RandomButStableCostsFrom1To10AndNoHeuristic(Long seed) {
            seed = Objects.requireNonNullElse(seed, 42L);
            rand = new Random(seed);
        }

        @Override
        public float getH(SingleAgentAStar_Solver.@NotNull AStarState state) {
            return 0;
        }

        @Override
        public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
            return 0;
        }

        @Override
        public int cost(Move move) {
            Edge edge = new Edge(move);
            return randomButStableCosts.computeIfAbsent(edge, e -> rand.nextInt(10) + 1);
        }

        @Override
        public boolean isConsistent() {
            return true;
        }

        @Override
        public String toString() {
//            SortedMap<Edge, Integer> sortedMap = new TreeMap<>(Comparator.comparingInt(e -> randomButStableCosts.get(e)));
//            sortedMap.putAll(randomButStableCosts);
            return randomButStableCosts.toString();
        }
    }

    @Test
    void optimalVsUCSWeightedEdges1(){
        MAPF_Instance testInstance = instanceMaze1;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdgesDynamic(){
        Map<I_ExplicitMap, String> maps = singleStronglyConnectedComponentMapsWithNames;
        for (I_ExplicitMap testMap :
                maps.keySet()) {
            for (I_Location source :
                    testMap.getAllLocations()) {
                for (I_Location target :
                        testMap.getAllLocations()) {
                    if ( ! source.equals(target)){
                        Agent agent = new Agent(0, source.getCoordinate(), target.getCoordinate());
                        MAPF_Instance testInstance = new MAPF_Instance(
                                maps.get(testMap) + " " + agent, testMap, new Agent[]{agent});
                        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));
                        compareAStarAndUCS(aStar, new InstanceReport(), agent, testInstance, randomStableCosts);
                    }
                }
            }
        }
    }

    public static void compareAStarAndUCS(I_Solver aStar, InstanceReport instanceReport, Agent agent, MAPF_Instance testInstance, SingleAgentGAndH costFunction) {
        RunParameters runParameters = new RunParametersBuilder().setInstanceReport(instanceReport).setAStarGAndH(costFunction).createRP();

        String identifier = testInstance.name + " " + agent.source + " to " + agent.target;
        System.out.println("\n" + identifier);

        Solution aStarSolution = aStar.solve(testInstance, runParameters);
        List<Integer> aSTsarPlanCosts = null;
        if (aStarSolution != null){
            List<I_Location> aStarPlanLocations = TestUtils.planLocations(aStarSolution.getPlanFor(agent));
            aSTsarPlanCosts = TestUtils.getPlanCosts(agent, costFunction, aStarPlanLocations);
            System.out.println("AStar:");
            System.out.println(aStarPlanLocations);
            System.out.println(aSTsarPlanCosts);
        }
        else{
            System.out.println("AStar Didn't Solve!!!");
        }

        List<I_Location> UCSPlanLocations = NoStateTimeSearches.uniformCostSearch(testInstance.map.getMapLocation(agent.target),
                testInstance.map.getMapLocation(agent.source), costFunction, agent);
        List<Integer> UCSPlanCosts = null;
        if (UCSPlanLocations != null){
            UCSPlanCosts = TestUtils.getPlanCosts(agent, costFunction, UCSPlanLocations);
            System.out.println("UCS:");
            System.out.println(UCSPlanLocations);
            System.out.println(UCSPlanCosts);
        }
        else{
            System.out.println("UCS Didn't Solve!!!");
        }


        System.out.println("Costs were:");
        System.out.println(costFunction);

        assertNotNull(aStarSolution);
        assertNotNull(UCSPlanLocations);

        int costAStar = 0;
        int costUCS = 0;
        for (int i = 0; i < Math.max(aSTsarPlanCosts.size(), UCSPlanCosts.size()); i++) {
            if (i < aSTsarPlanCosts.size()){
                costAStar += aSTsarPlanCosts.get(i);
            }
            if (i < UCSPlanCosts.size()){
                costUCS += UCSPlanCosts.get(i);
            }
        }
        assertEquals(costAStar, costUCS);
    }

}