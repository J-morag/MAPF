package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.CBS.CBSBuilder;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.*;

import static BasicMAPF.Solvers.AStar.SingleAgentAStar_SolverTest.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestUtils.addRandomConstraints;
import static org.junit.jupiter.api.Assertions.*;

class SingleAgentAStarSIPP_SolverTest {

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

    I_Solver sipp = new SingleAgentAStarSIPP_Solver();

    InstanceReport instanceReport;

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }


    @Test
    void oneMoveSolution() {
        MAPF_Instance testInstance = instance1stepSolution;
        Solution s = sipp.solve(testInstance, new RunParametersBuilder().createRP());

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

        Solution solved = sipp.solve(testInstance, new RunParametersBuilder().createRP());

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

        Solution solved = sipp.solve(testInstance, parameters);

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

        Solution solved = sipp.solve(testInstance, parameters);

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

        Solution solved = sipp.solve(testInstance, parameters);

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

        Solution solved = sipp.solve(testInstance, parameters);

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
        Constraint goalConstraint = new GoalConstraint(null, 1, location22Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(goalConstraint);
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = sipp.solve(testInstance, parameters);

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

        Solution solved = sipp.solve(testInstance, parameters);

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
        Constraint goalConstraint = new GoalConstraint(null, 2, location22Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(goalConstraint);
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = sipp.solve(testInstance, parameters);

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
        Constraint goalConstraint = new GoalConstraint(null, 3, location22Circle);
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(goalConstraint);
        RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = sipp.solve(testInstance, parameters);

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
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();
        MAPF_Instance baseInstance = instanceEmpty1;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(baseInstance.agents, baseInstance.map);

        int seeds = 20;
        for (int seed = 0; seed < seeds; seed++) {
            for (Agent agent : baseInstance.agents) {
                MAPF_Instance testInstance = baseInstance.getSubproblemFor(agent);
                List<I_Location> locations = new ArrayList<>();
                for (int i = 0; i <= 5; i++) {
                    for (int j = 0; j <= 5; j++) {
                        I_Coordinate newCoor = new Coordinate_2D(i, j);
                        I_Location newLocation = testInstance.map.getMapLocation(newCoor);
                        locations.add(newLocation);
                    }
                }
                Random rand = new Random(seed);
                ConstraintSet constraints = new ConstraintSet();
                addRandomConstraints(agent, locations, rand, constraints, 3000, 10);

                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).setAStarGAndH(heuristic).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippSolution = sipp.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippExpandedNodes = sipp.getExpandedNodes();
                int sippGeneratedNodes = sipp.getGeneratedNodes();

                List<Integer> sippPlanCosts = null;
                if (sippSolution != null){
                    List<I_Location> sippPlanLocations = TestUtils.planLocations(sippSolution.getPlanFor(agent));
                    sippPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, sippPlanLocations);
                    System.out.println("SIPP:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippGeneratedNodes);
                }
                else{
                    System.out.println("SIPP Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                List<Integer> aStarPlanCosts = null;
                if (aStarSolution != null){
                    List<I_Location> aStarPlanLocations = TestUtils.planLocations(aStarSolution.getPlanFor(agent));
                    aStarPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, aStarPlanLocations);
                    System.out.println("aStar:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(astarExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(astarGeneratedNodes);
                }
                else{
                    System.out.println("aStar Didn't Solve!!!");
                }


                System.out.println("Costs were:");
                System.out.println(unitCostAndNoHeuristic);

                assertNotNull(aStarSolution);
                assertNotNull(sippSolution);

                int costAStar = 0;
                int costSipp = 0;
                for (int i = 0; i < Math.max(aStarPlanCosts.size(), sippPlanCosts.size()); i++) {
                    if (i < aStarPlanCosts.size()){
                        costAStar += aStarPlanCosts.get(i);
                    }
                    if (i < sippPlanCosts.size()){
                        costSipp += sippPlanCosts.get(i);
                    }
                }
                assertEquals(costAStar, costSipp, "aStar cost " + costAStar + " should be the same as Sipp cost " + costSipp);
                assertTrue(astarExpandedNodes >= sippExpandedNodes, "aStar number of expanded nodes: " + astarExpandedNodes + " not be smaller than Sipp number of expanded nodes: " + sippExpandedNodes);
            }
        }
    }

    @Test
    void largeNumberOfConstraintsWithInfiniteConstraints(){
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();
        MAPF_Instance baseInstance = instanceEmpty1;

        int seeds = 20;
        for (int seed = 0; seed < seeds; seed++) {
            for (Agent agent : baseInstance.agents) {
                MAPF_Instance testInstance = baseInstance.getSubproblemFor(agent);
                List<I_Location> locations = new ArrayList<>();
                for (int i = 0; i <= 5; i++) {
                    for (int j = 0; j <= 5; j++) {
                        I_Coordinate newCoor = new Coordinate_2D(i, j);
                        I_Location newLocation = testInstance.map.getMapLocation(newCoor);
                        locations.add(newLocation);
                    }
                }
                Random rand = new Random(seed);
                ConstraintSet constraints = new ConstraintSet();
                for (int i = 0; i < 5; i++){
                    I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation);
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, 10);
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippSolution = sipp.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippExpandedNodes = sipp.getExpandedNodes();
                int sippGeneratedNodes = sipp.getGeneratedNodes();

                List<Integer> sippPlanCosts = null;
                boolean sippSolved = sippSolution != null;
                if (sippSolved){
                    List<I_Location> sippPlanLocations = TestUtils.planLocations(sippSolution.getPlanFor(agent));
                    sippPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, sippPlanLocations);
                    System.out.println("SIPP:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippGeneratedNodes);
                }
                else{
                    System.out.println("SIPP Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                List<Integer> aStarPlanCosts = null;
                boolean aStarSolved = aStarSolution != null;
                if (aStarSolved){
                    List<I_Location> aStarPlanLocations = TestUtils.planLocations(aStarSolution.getPlanFor(agent));
                    aStarPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, aStarPlanLocations);
                    System.out.println("aStar:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(astarExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(astarGeneratedNodes);
                }
                else{
                    System.out.println("aStar Didn't Solve!!!");
                }


                System.out.println("Costs were:");
                System.out.println(unitCostAndNoHeuristic);

                assertTrue(!aStarSolved || sippSolved, "SIPP should solve if AStar solved");

                if (aStarSolved && sippSolved){
                    int costAStar = 0;
                    int costSipp = 0;
                    for (int i = 0; i < Math.max(aStarPlanCosts.size(), sippPlanCosts.size()); i++) {
                        if (i < aStarPlanCosts.size()){
                            costAStar += aStarPlanCosts.get(i);
                        }
                        if (i < sippPlanCosts.size()){
                            costSipp += sippPlanCosts.get(i);
                        }
                    }
                    assertEquals(costAStar, costSipp, "aStar cost " + costAStar + " should be the same as Sipp cost " + costSipp);
                    assertTrue(astarExpandedNodes >= sippExpandedNodes, "aStar number of expanded nodes: " + astarExpandedNodes + " not be smaller than Sipp number of expanded nodes: " + sippExpandedNodes);
                }
            }
        }
    }

    @Test
    void largeNumberOfConstraintsWithInfiniteConstraintsAndPerfectHeuristic(){
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();
        MAPF_Instance baseInstance = instanceEmpty1;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(baseInstance.agents, baseInstance.map);

        int seeds = 20;
        for (int seed = 0; seed < seeds; seed++) {
            for (Agent agent : baseInstance.agents) {
                MAPF_Instance testInstance = baseInstance.getSubproblemFor(agent);
                List<I_Location> locations = new ArrayList<>();
                for (int i = 0; i <= 5; i++) {
                    for (int j = 0; j <= 5; j++) {
                        I_Coordinate newCoor = new Coordinate_2D(i, j);
                        I_Location newLocation = testInstance.map.getMapLocation(newCoor);
                        locations.add(newLocation);
                    }
                }
                Random rand = new Random(seed);
                ConstraintSet constraints = new ConstraintSet();
                for (int i = 0; i < 5; i++){
                    I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation);
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, 10);
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).setAStarGAndH(heuristic).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippSolution = sipp.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippExpandedNodes = sipp.getExpandedNodes();
                int sippGeneratedNodes = sipp.getGeneratedNodes();

                List<Integer> sippPlanCosts = null;
                boolean sippSolved = sippSolution != null;
                if (sippSolved){
                    List<I_Location> sippPlanLocations = TestUtils.planLocations(sippSolution.getPlanFor(agent));
                    sippPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, sippPlanLocations);
                    System.out.println("SIPP:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippGeneratedNodes);
                }
                else{
                    System.out.println("SIPP Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                List<Integer> aStarPlanCosts = null;
                boolean aStarSolved = aStarSolution != null;
                if (aStarSolved){
                    List<I_Location> aStarPlanLocations = TestUtils.planLocations(aStarSolution.getPlanFor(agent));
                    aStarPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, aStarPlanLocations);
                    System.out.println("aStar:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(astarExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(astarGeneratedNodes);
                }
                else{
                    System.out.println("aStar Didn't Solve!!!");
                }


                System.out.println("Costs were:");
                System.out.println(unitCostAndNoHeuristic);

                assertTrue(!aStarSolved || sippSolved, "SIPP should solve if AStar solved");

                if (aStarSolved && sippSolved){
                    int costAStar = 0;
                    int costSipp = 0;
                    for (int i = 0; i < Math.max(aStarPlanCosts.size(), sippPlanCosts.size()); i++) {
                        if (i < aStarPlanCosts.size()){
                            costAStar += aStarPlanCosts.get(i);
                        }
                        if (i < sippPlanCosts.size()){
                            costSipp += sippPlanCosts.get(i);
                        }
                    }
                    assertEquals(costAStar, costSipp, "aStar cost " + costAStar + " should be the same as Sipp cost " + costSipp);
                    assertTrue(astarExpandedNodes >= sippExpandedNodes, "aStar number of expanded nodes: " + astarExpandedNodes + " not be smaller than Sipp number of expanded nodes: " + sippExpandedNodes);
                }
            }
        }
    }

    @Test
    void largeNumberOfConstraintsWithInfiniteConstraintsBigger(){
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();

        int mapDim = 20;
        Enum_MapLocationType[][] map_matrix = new Enum_MapLocationType[mapDim][mapDim];
        for (int i = 0; i < mapDim; i++) {
            for (int j = 0; j < mapDim; j++) {
                map_matrix[i][j] = Enum_MapLocationType.EMPTY;
            }
        }
        I_Map map = MapFactory.newSimple4Connected2D_GraphMap(map_matrix);
        MAPF_Instance baseInstance = new MAPF_Instance("instanceEmpty" + mapDim + "=" + mapDim, map,
                new Agent[]{agent53to05, agent43to11, agent33to12, agent12to33, agent04to00, agent00to55, agent43to53, agent53to15,
                        new Agent(100, new Coordinate_2D(1,2), new Coordinate_2D(mapDim - 2, mapDim - 3))});
        // perfect heuristic is no better than manhattan distance on empty grid
//        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(baseInstance.agents, baseInstance.map);

        int seeds = 3;
        for (int seed = 0; seed < seeds; seed++) {
            for (Agent agent : baseInstance.agents) {
                MAPF_Instance testInstance = baseInstance.getSubproblemFor(agent);
                List<I_Location> locations = new ArrayList<>();
                for (int i = 0; i < mapDim; i++) {
                    for (int j = 0; j < mapDim; j++) {
                        I_Coordinate newCoor = new Coordinate_2D(i, j);
                        I_Location newLocation = testInstance.map.getMapLocation(newCoor);
                        locations.add(newLocation);
                    }
                }
                Random rand = new Random(seed);
                ConstraintSet constraints = new ConstraintSet();
                for (int i = 0; i < mapDim; i++){
                    I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation);
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, mapDim);
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippSolution = sipp.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippExpandedNodes = sipp.getExpandedNodes();
                int sippGeneratedNodes = sipp.getGeneratedNodes();

                List<Integer> sippPlanCosts = null;
                boolean sippSolved = sippSolution != null;
                if (sippSolved){
                    List<I_Location> sippPlanLocations = TestUtils.planLocations(sippSolution.getPlanFor(agent));
                    sippPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, sippPlanLocations);
                    System.out.println("SIPP:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippGeneratedNodes);
                }
                else{
                    System.out.println("SIPP Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                List<Integer> aStarPlanCosts = null;
                boolean aStarSolved = aStarSolution != null;
                if (aStarSolved){
                    List<I_Location> aStarPlanLocations = TestUtils.planLocations(aStarSolution.getPlanFor(agent));
                    aStarPlanCosts = TestUtils.getPlanCosts(agent, unitCostAndNoHeuristic, aStarPlanLocations);
                    System.out.println("aStar:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(astarExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(astarGeneratedNodes);
                }
                else{
                    System.out.println("aStar Didn't Solve!!!");
                }


                System.out.println("Costs were:");
                System.out.println(unitCostAndNoHeuristic);

                assertTrue(!aStarSolved || sippSolved, "SIPP should solve if AStar solved");

                if (aStarSolved && sippSolved){
                    int costAStar = 0;
                    int costSipp = 0;
                    for (int i = 0; i < Math.max(aStarPlanCosts.size(), sippPlanCosts.size()); i++) {
                        if (i < aStarPlanCosts.size()){
                            costAStar += aStarPlanCosts.get(i);
                        }
                        if (i < sippPlanCosts.size()){
                            costSipp += sippPlanCosts.get(i);
                        }
                    }
                    assertEquals(costAStar, costSipp, "aStar cost " + costAStar + " should be the same as Sipp cost " + costSipp);
                    assertTrue(astarExpandedNodes >= sippExpandedNodes, "aStar number of expanded nodes: " + astarExpandedNodes + " not be smaller than Sipp number of expanded nodes: " + sippExpandedNodes);
                }
            }
        }
    }

    @Test
    void circleOptimalityNorthwestToSoutheast(){
        MAPF_Instance testInstance = instanceCircle2;
        Agent agent = testInstance.agents.get(0);

        Solution solved = sipp.solve(testInstance, new RunParametersBuilder().createRP());

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

        Solution solved1 = sipp.solve(testInstance1, new RunParametersBuilder().createRP());
        Solution solved2 = sipp.solve(testInstance2, new RunParametersBuilder().createRP());

        assertEquals(7, solved1.getPlanFor(agent1).size());
        assertEquals(5, solved2.getPlanFor(agent2).size());
    }

    @Test
    void unsolvableShouldTimeout(){
        MAPF_Instance testInstance = instanceUnsolvable;

        // three second timeout
        RunParameters runParameters = new RunParametersBuilder().setTimeout(1000*3).createRP();
        Solution solved = sipp.solve(testInstance, runParameters);

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

        Solution solved1 = sipp.solve(testInstance, runParameters);

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
        RunParameters runParameters = new RunParametersBuilder().setConstraints(constraints).createRP();

        Solution solved = sipp.solve(testInstance, runParameters);

        SingleAgentPlan plan3 = new SingleAgentPlan(agent);
        plan3.addMove(new Move(agent, 1, location12Circle, location22Circle));
        plan3.addMove(new Move(agent, 2, location22Circle, location32Circle));
        plan3.addMove(new Move(agent, 3, location32Circle, location32Circle));
        plan3.addMove(new Move(agent, 4, location32Circle, location32Circle));
        plan3.addMove(new Move(agent, 5, location32Circle, location32Circle));
        plan3.addMove(new Move(agent, 6, location32Circle, location33Circle));
        Solution expected = new Solution();
        expected.putPlan(plan3);

        assertEquals(6, solved.getPlanFor(agent).size());
        assertTrue(expected.equals(solved));
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

        Solution solved1 = sipp.solve(testInstance, runParameters);

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

        Solution solved1 = sipp.solve(testInstance, runParameters);

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
        Solution solved = sipp.solve(testInstance, new RunParametersBuilder().setExistingSolution(existingSolution).createRP());

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

    // Not supported yet
    @Disabled
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

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).createRP());
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = sipp.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else. So plan length is Manhattan Distance + 1
        assertEquals(8, solved1.getPlanFor(agent).size());
    }

    // Not supported yet
    @Disabled
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

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).createRP());
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = sipp.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // has to visit goal at some point, and then can finish the plan anywhere else,
        // but the surrounding locations also have constraints in the future, so has to take 2 steps
        assertEquals(9, solved1.getPlanFor(agent).size());
    }
    private final SingleAgentGAndH unitCostAndNoHeuristic = new TestUtils.UnitCostAndNoHeuristic();

    @Test
    void optimalVsUCS1(){
        MAPF_Instance testInstance = instanceMaze1;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }

    @Test
    void optimalVsUCS2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCS3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
    }
    @Test
    void optimalVsUCS4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, unitCostAndNoHeuristic);
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
                        compareAStarAndUCS(sipp, new InstanceReport(),
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
                        compareAStarAndUCS(sipp, new InstanceReport(),
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
                        compareAStarAndUCS(sipp, new InstanceReport(), agent, testInstance, new UnitCostsAndManhattanDistance(agent.target));
                    }
                }
            }
        }
    }
    @Test
    void optimalVsUCSWeightedEdges1(){
        MAPF_Instance testInstance = instanceMaze1;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges2(){
        MAPF_Instance testInstance = instanceMaze2;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges3(){
        MAPF_Instance testInstance = instanceMaze3;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
    }
    @Test
    void optimalVsUCSWeightedEdges4(){
        MAPF_Instance testInstance = instanceMaze4;
        Agent agent = testInstance.agents.get(0);
        SingleAgentGAndH randomStableCosts = new RandomButStableCostsFrom1To10AndNoHeuristic((long) (agent.hashCode()));

        compareAStarAndUCS(sipp, instanceReport, agent, testInstance, randomStableCosts);
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
                        compareAStarAndUCS(sipp, new InstanceReport(), agent, testInstance, randomStableCosts);
                    }
                }
            }
        }
    }

    @Test
    void comparativeTest(){
        Metrics.clearAll();
        boolean useAsserts = true;

        I_Solver regularCBS = new CBSBuilder().setUseCorridorReasoning(false).createCBS_Solver();
        String nameBaseline = "regularCBS";
        I_Solver singleAgentSippCBS = new CBSBuilder().setLowLevelSolver(new SingleAgentAStarSIPP_Solver()).setUseCorridorReasoning(false).createCBS_Solver();
        String nameExperimental = "singleAgentSippCBS";
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "ComparativeDiverseTestSet"});
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(),
//                new InstanceProperties(null, -1d, new int[]{5, 10, 15, 20, 25}));
                new InstanceProperties(null, -1d, new int[]{5, 10, 15}));

        // run all instances on both solvers. this code is mostly copied from Environment.Experiment.
        MAPF_Instance instance = null;
        long timeout = 20 /*seconds*/   *1000L;
        int solvedByBaseline = 0;
        int solvedByExperimental = 0;
        int runtimeBaseline = 0;
        int runtimeExperimental = 0;
        while ((instance = instanceManager.getNextInstance()) != null) {
            System.out.println("---------- solving "  + instance.extendedName + " with " + instance.agents.size() + " agents ----------");

            // run baseline (without the improvement)
            //build report
            InstanceReport reportBaseline = Metrics.newInstanceReport();
            reportBaseline.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeTest");
            reportBaseline.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportBaseline.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportBaseline.putStringValue(InstanceReport.StandardFields.solver, "regularCBS");

            RunParameters runParametersBaseline = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportBaseline).createRP();

            //solve
            Solution solutionBaseline = regularCBS.solve(instance, runParametersBaseline);

            // run experimentl (with the improvement)
            //build report
            InstanceReport reportExperimental = Metrics.newInstanceReport();
            reportExperimental.putStringValue(InstanceReport.StandardFields.experimentName, "comparativeTest");
            reportExperimental.putStringValue(InstanceReport.StandardFields.instanceName, instance.name);
            reportExperimental.putIntegerValue(InstanceReport.StandardFields.numAgents, instance.agents.size());
            reportExperimental.putStringValue(InstanceReport.StandardFields.solver, "singleAgentSippCBS");

            RunParameters runParametersExperimental = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(reportExperimental).createRP();

            //solve
            Solution solutionExperimental = singleAgentSippCBS.solve(instance, runParametersExperimental);

            // compare

            boolean baselineSolved = solutionBaseline != null;
            solvedByBaseline += baselineSolved ? 1 : 0;
            boolean experimentalSolved = solutionExperimental != null;
            solvedByExperimental += experimentalSolved ? 1 : 0;
            System.out.println(nameBaseline + " Solved?: " + (baselineSolved ? "yes" : "no") +
                    " ; " + nameExperimental + " solved?: " + (experimentalSolved ? "yes" : "no"));

            if(solutionBaseline != null){
                boolean valid = solutionBaseline.solves(instance);
                System.out.print(nameBaseline + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionExperimental != null){
                boolean valid = solutionExperimental.solves(instance);
                System.out.println(" " + nameExperimental + " Valid?: " + (valid ? "yes" : "no"));
                if (useAsserts) assertTrue(valid);
            }

            if(solutionBaseline != null && solutionExperimental != null){
                int optimalCost = solutionBaseline.sumIndividualCosts();
                int costWeGot = solutionExperimental.sumIndividualCosts();
                boolean optimal = optimalCost==costWeGot;
                System.out.println(nameExperimental + " cost is " + (optimal ? "optimal (" + costWeGot +")" :
                        ("not optimal (" + costWeGot + " instead of " + optimalCost + ")")));
                reportBaseline.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                reportExperimental.putIntegerValue("Cost Delta", costWeGot - optimalCost);
                if (useAsserts) assertEquals(optimalCost, costWeGot);

                // runtimes
                runtimeBaseline += reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                runtimeExperimental += reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS);
                reportBaseline.putIntegerValue("Runtime Delta",
                        reportExperimental.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS)
                                - reportBaseline.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS));
            }
        }

        System.out.println("--- TOTALS: ---");
        System.out.println("timeout for each (seconds): " + (timeout/1000));
        System.out.println(nameBaseline + " solved: " + solvedByBaseline);
        System.out.println(nameExperimental + " solved: " + solvedByExperimental);
        System.out.println("runtime totals (instances where both solved) :");
        System.out.println(nameBaseline + " time: " + runtimeBaseline);
        System.out.println(nameExperimental + " time: " + runtimeExperimental);

        //save results
        DateFormat dateFormat = Metrics.DEFAULT_DATE_FORMAT;
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
        File directory = new File(resultsOutputDir);
        if (! directory.exists()){
            directory.mkdir();
        }
        String updatedPath =  IO_Manager.buildPath(new String[]{ resultsOutputDir,
                "res_ " + this.getClass().getSimpleName() + "_" + new Object(){}.getClass().getEnclosingMethod().getName() +
                        "_" + dateFormat.format(System.currentTimeMillis()) + ".csv"});
        try {
            Metrics.exportCSV(new FileOutputStream(updatedPath),
                    new String[]{
                            InstanceReport.StandardFields.instanceName,
                            InstanceReport.StandardFields.numAgents,
                            InstanceReport.StandardFields.timeoutThresholdMS,
                            InstanceReport.StandardFields.solved,
                            InstanceReport.StandardFields.elapsedTimeMS,
                            "Runtime Delta",
                            InstanceReport.StandardFields.solutionCost,
                            "Cost Delta",
                            InstanceReport.StandardFields.totalLowLevelTimeMS,
                            InstanceReport.StandardFields.generatedNodes,
                            InstanceReport.StandardFields.expandedNodes,
                            InstanceReport.StandardFields.generatedNodesLowLevel,
                            InstanceReport.StandardFields.expandedNodesLowLevel});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}