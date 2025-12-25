package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
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
import TransientMAPF.TransientMAPFSettings;
import org.junit.jupiter.api.*;

import java.util.*;

import static BasicMAPF.TestConstants.Coordinates.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestUtils.addRandomConstraints;
import static org.junit.jupiter.api.Assertions.*;

class SingleAgentAStarSIPPS_SolverTest {

    // TODO - expand this test class

    private I_Location location12Circle = mapCircle.getMapLocation(coor12);
    private I_Location location13Circle = mapCircle.getMapLocation(coor13);
    private I_Location location14Circle = mapCircle.getMapLocation(coor14);
    private I_Location location22Circle = mapCircle.getMapLocation(coor22);
    private I_Location location24Circle = mapCircle.getMapLocation(coor24);
    private I_Location location32Circle = mapCircle.getMapLocation(coor32);
    private I_Location location33Circle = mapCircle.getMapLocation(coor33);
    private I_Location location34Circle = mapCircle.getMapLocation(coor34);
    private final I_Location location12 = mapCircle.getMapLocation(coor12);
    private final I_Location location22 = mapCircle.getMapLocation(coor22);
    private final I_Location location32 = mapCircle.getMapLocation(coor32);

    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    private MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent53to05});
    private MAPF_Instance instance1stepSolution = im.getNextInstance();
    private MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12});
    I_Solver sipps = new SingleAgentAStarSIPPS_Solver();

    InstanceReport instanceReport;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @BeforeEach
    void setUp() {
        instanceReport = Metrics.newInstanceReport();
    }

    @AfterEach
    void tearDown() {
        Metrics.removeReport(instanceReport);
    }

    @Test
    void testSafeIntervalsWithSoftConstraints() {
        // Create a Conflict Avoidance Table (CAT)
        RemovableConflictAvoidanceTableWithContestedGoals cat = new RemovableConflictAvoidanceTableWithContestedGoals();

        // Adding soft constraints (conflicting agent) at different times
        Agent agentToAvoid = new Agent(200, coor32, coor12);
        SingleAgentPlan conflictingPlan = new SingleAgentPlan(agentToAvoid);

        // Adding conflicts:
        conflictingPlan.addMove(new Move(agentToAvoid, 1, location32, location32));
        conflictingPlan.addMove(new Move(agentToAvoid, 2, location32, location22));
        conflictingPlan.addMove(new Move(agentToAvoid, 3, location22, location12));
        cat.addPlan(conflictingPlan);

        // Convert CAT into safe intervals
        Map<I_Location, List<TimeInterval>> safeSoftIntervals = cat.conflictAvoidanceTableToSafeTimeIntervals();

        // Location 12: goal location
        // [0,2], [3,inf]
        List<TimeInterval> safeIntervals12 = safeSoftIntervals.get(location12);
        assertNotNull(safeIntervals12);
        assertEquals(2, safeIntervals12.size());
        assertEquals(0, safeIntervals12.get(0).start());
        assertEquals(2, safeIntervals12.get(0).end());
        assertEquals(3, safeIntervals12.get(1).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals12.get(1).end());

        // Location 22
        // [0,1], [2,2], [3,inf]
        List<TimeInterval> safeIntervals22 = safeSoftIntervals.get(location22);
        assertNotNull(safeIntervals22);
        assertEquals(3, safeIntervals22.size());
        assertEquals(0, safeIntervals22.get(0).start());
        assertEquals(1, safeIntervals22.get(0).end());
        assertEquals(2, safeIntervals22.get(1).start());
        assertEquals(2, safeIntervals22.get(1).end());
        assertEquals(3, safeIntervals22.get(2).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals22.get(2).end());

        // Location 32
        // [0,0], [1,1], [2,inf)
        List<TimeInterval> safeIntervals32 = safeSoftIntervals.get(location32);
        assertNotNull(safeIntervals32);
        assertEquals(3, safeIntervals32.size());
        assertEquals(0, safeIntervals32.get(0).start());
        assertEquals(0, safeIntervals32.get(0).end());
        assertEquals(1, safeIntervals32.get(1).start());
        assertEquals(1, safeIntervals32.get(1).end());
        assertEquals(2, safeIntervals32.get(2).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals32.get(2).end());
    }

    @Test
    void testSafeIntervalsWithBothHardAndSoftConstraints() {
        // Create a Conflict Avoidance Table (CAT)
        RemovableConflictAvoidanceTableWithContestedGoals cat = new RemovableConflictAvoidanceTableWithContestedGoals();

        // Adding soft constraints (conflicting agent) at different times
        Agent agentToAvoid = new Agent(200, coor32, coor12);
        SingleAgentPlan conflictingPlan = new SingleAgentPlan(agentToAvoid);

        // Adding conflicts:
        conflictingPlan.addMove(new Move(agentToAvoid, 1, location32, location32));
        conflictingPlan.addMove(new Move(agentToAvoid, 2, location32, location22));
        conflictingPlan.addMove(new Move(agentToAvoid, 3, location22, location12));
        cat.addPlan(conflictingPlan);

        // Convert CAT into safe intervals
        SingleAgentAStarSIPPS_Solver sipps = new SingleAgentAStarSIPPS_Solver();
        Map<I_Location, List<TimeInterval>> safeSoftIntervals = cat.conflictAvoidanceTableToSafeTimeIntervals();

        // **Adding Hard Constraints**
        ConstraintSet hardConstraints = new ConstraintSet();
        hardConstraints.add(new Constraint(null, 1, null, location32));
        hardConstraints.add(new Constraint(null, 2, null, location22));
        hardConstraints.add(new Constraint(null, 4, null, location12));

        // Convert hard constraints into safe intervals
        Map<I_Location, List<TimeInterval>> safeHardIntervals = hardConstraints.vertexConstraintsToSortedSafeTimeIntervals(null, null);

        // Combine the safe intervals (soft + hard)
        Map<I_Location, List<TimeInterval>> combinedSafeIntervals = sipps.combineSafeIntervals(safeHardIntervals, safeSoftIntervals);

        // Location 12: goal location
        // [0,2], [3,3], [5,inf]
        List<TimeInterval> safeIntervals12 = combinedSafeIntervals.get(location12);
        assertNotNull(safeIntervals12);
        assertEquals(3, safeIntervals12.size());
        assertEquals(0, safeIntervals12.get(0).start());
        assertEquals(2, safeIntervals12.get(0).end());
        assertEquals(3, safeIntervals12.get(1).start());
        assertEquals(3, safeIntervals12.get(1).end());
        assertEquals(5, safeIntervals12.get(2).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals12.get(2).end());

        // Location 22
        // [0,1], [3,inf]
        List<TimeInterval> safeIntervals22 = combinedSafeIntervals.get(location22);
        assertNotNull(safeIntervals22);
        assertEquals(2, safeIntervals22.size());
        assertEquals(0, safeIntervals22.get(0).start());
        assertEquals(1, safeIntervals22.get(0).end());
        assertEquals(3, safeIntervals22.get(1).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals22.get(1).end());

        // Location 32
        // [0,0), [2,inf]
        List<TimeInterval> safeIntervals32 = combinedSafeIntervals.get(location32);
        assertNotNull(safeIntervals32);
        assertEquals(2, safeIntervals32.size());
        assertEquals(0, safeIntervals32.get(0).start());
        assertEquals(0, safeIntervals32.get(0).end());
        assertEquals(2, safeIntervals32.get(1).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals32.get(1).end());
    }

    @Test
    void testSafeIntervalsWithBothHardAndSoftConstraints2() {
        // Create a Conflict Avoidance Table (CAT) with soft constraints
        RemovableConflictAvoidanceTableWithContestedGoals cat = new RemovableConflictAvoidanceTableWithContestedGoals();

        // Adding soft constraints (conflicting agent) at different times
        Agent agentToAvoid = new Agent(200, coor12, coor34);
        SingleAgentPlan conflictingPlan = new SingleAgentPlan(agentToAvoid);

        // The agent moves from location 1,2 → 2,2 → 3,2 → 3,3 → 3,4
        conflictingPlan.addMove(new Move(agentToAvoid, 1, location12Circle, location22Circle));
        conflictingPlan.addMove(new Move(agentToAvoid, 2, location22Circle, location32Circle));
        conflictingPlan.addMove(new Move(agentToAvoid, 3, location32Circle, location33Circle));
        conflictingPlan.addMove(new Move(agentToAvoid, 4, location33Circle, location34Circle));
        cat.addPlan(conflictingPlan);

        // Hard Constraints (explicit vertex constraints)
        ConstraintSet hardConstraints = new ConstraintSet();
        hardConstraints.add(new Constraint(null, 2, null, location22Circle));
        hardConstraints.add(new Constraint(null, 4, null, location33Circle));
        hardConstraints.add(new Constraint(null, 10, null, location34Circle));
        hardConstraints.add(new Constraint(null, 3, null, location33Circle));
        hardConstraints.add(new Constraint(null, 3, null, location12Circle));

        // Convert soft constraints into safe intervals
        SingleAgentAStarSIPPS_Solver sipps = new SingleAgentAStarSIPPS_Solver();
        Map<I_Location, List<TimeInterval>> safeSoftIntervals = cat.conflictAvoidanceTableToSafeTimeIntervals();

        // Convert hard constraints into safe intervals
        Map<I_Location, List<TimeInterval>> safeHardIntervals = hardConstraints.vertexConstraintsToSortedSafeTimeIntervals(null, null);

        // Combine soft and hard safe intervals
        Map<I_Location, List<TimeInterval>> combinedSafeIntervals = sipps.combineSafeIntervals(safeHardIntervals, safeSoftIntervals);

        // Location 12
        // [0,0], [1,1], [2,2], [4,inf]
        List<TimeInterval> safeIntervals12 = combinedSafeIntervals.get(location12Circle);
        assertNotNull(safeIntervals12);
        assertEquals(4, safeIntervals12.size());
        assertEquals(0, safeIntervals12.get(0).start());
        assertEquals(0, safeIntervals12.get(0).end());
        assertEquals(1, safeIntervals12.get(1).start());
        assertEquals(1, safeIntervals12.get(1).end());
        assertEquals(2, safeIntervals12.get(2).start());
        assertEquals(2, safeIntervals12.get(2).end());
        assertEquals(4, safeIntervals12.get(3).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals12.get(3).end());

        // Location 22
        // [0,0], [1,1], [3,inf]
        List<TimeInterval> safeIntervals22 = combinedSafeIntervals.get(location22Circle);
        assertNotNull(safeIntervals22);
        assertEquals(3, safeIntervals22.size());
        assertEquals(0, safeIntervals22.get(0).start());
        assertEquals(0, safeIntervals22.get(0).end());
        assertEquals(1, safeIntervals22.get(1).start());
        assertEquals(1, safeIntervals22.get(1).end());
        assertEquals(3, safeIntervals22.get(2).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals22.get(2).end());

        // Location 32
        // [0,1], [2,2], [3,inf]
        List<TimeInterval> safeIntervals32 = combinedSafeIntervals.get(location32Circle);
        assertNotNull(safeIntervals32);
        assertEquals(3, safeIntervals32.size());
        assertEquals(0, safeIntervals32.get(0).start());
        assertEquals(1, safeIntervals32.get(0).end());
        assertEquals(2, safeIntervals32.get(1).start());
        assertEquals(2, safeIntervals32.get(1).end());
        assertEquals(3, safeIntervals32.get(2).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals32.get(2).end());

        // Location 33
        // [0,2], [5,inf]
        List<TimeInterval> safeIntervals33 = combinedSafeIntervals.get(location33Circle);
        assertNotNull(safeIntervals33);
        assertEquals(2, safeIntervals33.size());
        assertEquals(0, safeIntervals33.get(0).start());
        assertEquals(2, safeIntervals33.get(0).end());
        assertEquals(5, safeIntervals33.get(1).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals33.get(1).end());

        // Location 34: goal location
        // [0,3], [4,9], [11, inf]
        List<TimeInterval> safeIntervals34 = combinedSafeIntervals.get(location34Circle);
        assertNotNull(safeIntervals34);
        assertEquals(3, safeIntervals34.size());
        assertEquals(0, safeIntervals34.get(0).start());
        assertEquals(3, safeIntervals34.get(0).end());
        assertEquals(4, safeIntervals34.get(1).start());
        assertEquals(9, safeIntervals34.get(1).end());
        assertEquals(11, safeIntervals34.get(2).start());
        assertEquals(Integer.MAX_VALUE, safeIntervals34.get(2).end());
    }

    @Test
    void circleOptimalityOtherDirectionBecauseOfCAT() {
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        // Create a Conflict Avoidance Table (CAT)
        RemovableConflictAvoidanceTableWithContestedGoals cat = new RemovableConflictAvoidanceTableWithContestedGoals();

        // Add a conflicting plan to the CAT that blocks the optimal path (going through location32Circle)
        Agent blockingAgent = new Agent(200, coor12, coor22);
        SingleAgentPlan blockingPlan = new SingleAgentPlan(blockingAgent);
        blockingPlan.addMove(new Move(blockingAgent, 1, location12Circle, location22Circle));

        cat.addPlan(blockingPlan);

        // Create run parameters with the CAT
        RunParameters parameters = new RunParametersBuilder().setConflictAvoidanceTable(cat).createRP();

        // Solve the instance with SIPPS
        Solution solved = sipps.solve(testInstance, parameters);
        System.out.println(solved);

        // Expected plan: The agent takes a **different** direction due to the conflict
        SingleAgentPlan expectedPlan = new SingleAgentPlan(agent);
        expectedPlan.addMove(new Move(agent, 1, location33Circle, location34Circle));
        expectedPlan.addMove(new Move(agent, 2, location34Circle, location24Circle));
        expectedPlan.addMove(new Move(agent, 3, location24Circle, location14Circle));
        expectedPlan.addMove(new Move(agent, 4, location14Circle, location13Circle));
        expectedPlan.addMove(new Move(agent, 5, location13Circle, location12Circle));

        Solution expected = new Solution();
        expected.putPlan(expectedPlan);

        // Validate solution
        assertEquals(5, solved.getPlanFor(agent).size());
        assertEquals(expected, solved);
    }

    @Test
    void largeNumberOfConstraintsWithInfiniteConstraintsAndPerfectHeuristic(){
        SingleAgentAStarSIPPS_Solver sipps = new SingleAgentAStarSIPPS_Solver();
        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();
        MAPF_Instance baseInstance = instanceEmpty1;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(baseInstance.agents, baseInstance.map);

        int seeds = 10;
        for (int seed = 0; seed < seeds; seed++) {
            if (seed != 2) continue;
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
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation, new Agent(1000, coor43,  coor34)); // arbitrary agent not in instance
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, 10);
                I_ConflictAvoidanceTable cat = new RemovableConflictAvoidanceTableWithContestedGoals();
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).setConflictAvoidanceTable(cat).setAStarGAndH(heuristic).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippsSolution = sipps.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippsExpandedNodes = sipps.getExpandedNodes();
                int sippsGeneratedNodes = sipps.getGeneratedNodes();

                boolean sippsSolved = sippsSolution != null;
                if (sippsSolved){
                    System.out.println("SIPPS:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippsExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippsGeneratedNodes);
                }
                else{
                    System.out.println("SIPPS Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                boolean aStarSolved = aStarSolution != null;
                if (aStarSolved){
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
                assertTrue(!aStarSolved || sippsSolved, "SIPPS should solve if AStar solved");
            }
        }
    }

    @Test
    void largeNumberOfConstraintsWithInfiniteConstraintsBigger(){
        SingleAgentAStarSIPPS_Solver sipps = new SingleAgentAStarSIPPS_Solver();
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

        int seeds = 2;
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
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation, new Agent(1000, coor43,  coor34)); // arbitrary agent not in instance
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, mapDim);
                I_ConflictAvoidanceTable cat = new RemovableConflictAvoidanceTableWithContestedGoals();
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).setConflictAvoidanceTable(cat).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippsSolution = sipps.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippsExpandedNodes = sipps.getExpandedNodes();
                int sippsGeneratedNodes = sipps.getGeneratedNodes();

                boolean sippsSolved = sippsSolution != null;
                if (sippsSolved){
                    System.out.println("SIPPS:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippsExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippsGeneratedNodes);
                }
                else{
                    System.out.println("SIPP Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                boolean aStarSolved = aStarSolution != null;
                if (aStarSolved){
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
                assertTrue(!aStarSolved || sippsSolved, "SIPPS should solve if AStar solved");
            }
        }
    }

    private final SingleAgentGAndH unitCostAndNoHeuristic = new TestUtils.UnitCostAndNoHeuristic();

    @Test
    void oneMoveSolution() {
        MAPF_Instance testInstance = instance1stepSolution;
        Solution s = sipps.solve(testInstance, new RunParametersBuilder().createRP());

        Map<Agent, SingleAgentPlan> plans = new HashMap<>();
        SingleAgentPlan plan = new SingleAgentPlan(testInstance.agents.get(0));
        I_Location location = testInstance.map.getMapLocation(new Coordinate_2D(4,5));
        plan.addMove(new Move(testInstance.agents.get(0), 1, location, location));
        plans.put(testInstance.agents.get(0), plan);
        Solution expected = new Solution(plans);

        assertEquals(s, expected);
    }

    @Test
    void circleOptimalityWaitingBecauseOfConstraint1(){
        MAPF_Instance testInstance = instanceCircle1;
        Agent agent = testInstance.agents.get(0);

        RemovableConflictAvoidanceTableWithContestedGoals cat = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent blockingAgent1 = new Agent(999, coor34, coor34);
        Agent blockingAgent2 = new Agent(998, coor32, coor13);
        SingleAgentPlan blockingPlan1 = new SingleAgentPlan(blockingAgent1);
        SingleAgentPlan blockingPlan2 = new SingleAgentPlan(blockingAgent2);
        blockingPlan1.addMove(new Move(blockingAgent1, 1, location34Circle, location34Circle));

        blockingPlan2.addMove(new Move(blockingAgent2, 1, location32Circle, location32Circle));
        blockingPlan2.addMove(new Move(blockingAgent2, 2, location32Circle, location22Circle));
        blockingPlan2.addMove(new Move(blockingAgent2, 3, location22Circle, location12Circle));
        blockingPlan2.addMove(new Move(blockingAgent2, 4, location12Circle, location13Circle));

        cat.addPlan(blockingPlan1);
        cat.addPlan(blockingPlan2);

        RunParameters parameters = new RunParametersBuilder().setConflictAvoidanceTable(cat).createRP();
        Solution solved = sipps.solve(testInstance, parameters);
        System.out.println(solved);

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
    void testTransientIdentifiesSourceEqualsTargetSoVisitedAtTime0() {
        SingleAgentAStarSIPPS_Solver sippst = new SingleAgentAStarSIPPS_Solver(TransientMAPFSettings.defaultTransientMAPF);
        I_Coordinate coor = coor14;
        MAPF_Instance testInstance = new MAPF_Instance("Single agent source equals target", instanceEmpty1.map, new Agent[]{new Agent(0, coor, coor)});
        Agent agent = new Agent(0, coor, coor); // source equals target

        // constraint on target at time 1
        Constraint constraintAtTimeAfterReachingGoal1 = new Constraint(agent,1, null, instanceEmpty1.map.getMapLocation(coor));
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(constraintAtTimeAfterReachingGoal1);

        RunParameters_SAAStar runParameters = new RunParameters_SAAStar(new RunParametersBuilder().setConstraints(constraints).setInstanceReport(new InstanceReport()).setAStarGAndH(new ServiceTimeGAndH(new UnitCostsAndManhattanDistance(agent.target))).createRP());
        runParameters.goalCondition = new VisitedTargetAStarGoalCondition();

        Solution solved1 = sippst.solve(testInstance, runParameters);
        System.out.println(solved1.getPlanFor(agent));

        // visited at time 0, so plan has size 1, and contributes 0 cost to SST
        assertEquals(1, solved1.getPlanFor(agent).size());
        assertEquals(coor, solved1.getPlanFor(agent).getFirstMove().prevLocation.getCoordinate());
        assertEquals(0, solved1.sumServiceTimes());
        // blocked at time 1, so cannot stay in place
        assertNotEquals(coor, solved1.getPlanFor(agent).getFirstMove().currLocation.getCoordinate());
    }
}