package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableAStarHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.SingleTargetCoordinateGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedAGoalAtSomePointInPlanGoalCondition;
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
import Environment.Metrics.S_Metrics;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
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

    I_Solver aStar = new SingleAgentAStar_Solver();

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
    void accountsForConstraintAfterReachingGoal2() {
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
        RunParameters runParameters = new RunParameters(constraints);

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
        RunParameters runParameters = new RunParameters(constraints);

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

    @Test
    void findsPIBTStylePlanUnderConstraintsUsingPIBTStyleGoalCondition() {
        MAPF_Instance testInstance = instanceEmpty1;
        Agent agent = testInstance.agents.get(0);
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,9, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal2 = new Constraint(agent,13, null, instanceEmpty1.map.getMapLocation(coor05));
        Constraint constraintAtTimeAfterReachingGoal3 = new Constraint(agent,14, null, instanceEmpty1.map.getMapLocation(coor05));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);
        constraints.add(constraintAtTimeAfterReachingGoal2);
        constraints.add(constraintAtTimeAfterReachingGoal3);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParameters(constraints, new InstanceReport()));
        runParameters.goalCondition = new VisitedAGoalAtSomePointInPlanGoalCondition(new SingleTargetCoordinateGoalCondition(agent.target));

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
    }

    @Test
    void findsPIBTStylePlanUnderConstraintsAlsoAroundGoalUsingPIBTStyleGoalCondition() {
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

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParameters(constraints, new InstanceReport()));
        runParameters.goalCondition = new VisitedAGoalAtSomePointInPlanGoalCondition(new SingleTargetCoordinateGoalCondition(agent.target));

        Solution solved1 = aStar.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else,
        // but the surrounding locations also have constraints in the future, so has to take 2 steps
        assertEquals(9, solved1.getPlanFor(agent).size());
    }

    private class UnitCostAndNoHeuristic implements AStarGAndH {
        @Override
        public float getH(SingleAgentAStar_Solver.AStarState state) {
            return 0;
        }

        @Override
        public int cost(Move move) {
            return AStarGAndH.super.cost(move);
        }

        @Override
        public boolean isConsistent() {
            return true;
        }

        @Override
        public String toString() {
            return "All edges = 1";
        }
    }

    private final AStarGAndH unitCostAndNoHeuristic = new UnitCostAndNoHeuristic();

    private static List<I_Location> planLocations(SingleAgentPlan planFromAStar) {
        List<I_Location> aStarPlanLocations = new ArrayList<>();
        for (Move move :
                planFromAStar) {
            if (move.timeNow == 1) {
                aStarPlanLocations.add(move.prevLocation);
            }
            aStarPlanLocations.add(move.currLocation);
        }
        return aStarPlanLocations;
    }

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
                        DistanceTableAStarHeuristic distanceTableAStarHeuristic = new DistanceTableAStarHeuristic(testInstance.agents, testInstance.map);
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

    private static class RandomButStableCostsFrom1To10AndNoHeuristic implements AStarGAndH{
        Map<Edge, Integer> randomButStableCosts = new HashMap<>();
        Random rand;

        private RandomButStableCostsFrom1To10AndNoHeuristic(Long seed) {
            seed = Objects.requireNonNullElse(seed, 42L);
            rand = new Random(seed);
        }

        @Override
        public float getH(SingleAgentAStar_Solver.AStarState state) {
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
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(aStar, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);
        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

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
                        AStarGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));
                        compareAStarAndUCS(aStar, new InstanceReport(), agent, testInstance, randomStableCosts);
                    }
                }
            }
        }
    }

    private void compareAStarAndUCS(I_Solver aStar, InstanceReport instanceReport, Agent agent, MAPF_Instance testInstance, AStarGAndH costFunction) {
        RunParameters aStarRunParameters = new RunParameters_SAAStar(instanceReport, costFunction);

        String identifier = testInstance.name + " " + agent.source + " to " + agent.target;
        System.out.println("\n" + identifier);

        Solution aStarSolution = aStar.solve(testInstance, aStarRunParameters);
        List<Integer> aSTsarPlanCosts = null;
        if (aStarSolution != null){
            List<I_Location> aStarPlanLocations = planLocations(aStarSolution.getPlanFor(agent));
            aSTsarPlanCosts = getCosts(agent, costFunction, aStarPlanLocations);
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
            UCSPlanCosts = getCosts(agent, costFunction, UCSPlanLocations);
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

    @NotNull
    private static List<Integer> getCosts(Agent agent, AStarGAndH costFunction, List<I_Location> UCSPlanLocations) {
        List<Integer> UCSPlanCosts = new ArrayList<>();
        UCSPlanCosts.add(0);
        I_Location prev = null;
        for (I_Location curr :
                UCSPlanLocations) {
            if (prev != null){
                UCSPlanCosts.add(costFunction.cost(new Move(agent, 1, prev, curr)));
            }
            prev = curr;
        }
        return UCSPlanCosts;
    }
}