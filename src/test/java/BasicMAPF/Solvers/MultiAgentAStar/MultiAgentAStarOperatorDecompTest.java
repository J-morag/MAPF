package BasicMAPF.Solvers.MultiAgentAStar;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.CBS.CBS_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.TestUtils;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static BasicMAPF.TestConstants.Agents.agent04to00;
import static BasicMAPF.TestConstants.Coordinates.*;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Instances.instanceSmallMaze;
import static BasicMAPF.TestConstants.Maps.*;
import static org.junit.jupiter.api.Assertions.*;

class MultiAgentAStarOperatorDecompTest {

    MultiAgentAStar MAAStarOD = CanonicalSolversFactory.createMultiAgentAStarOperatorDecompSolver();
    MultiAgentAStar MAAStarOD_Lexical = CanonicalSolversFactory.createMultiAgentAStarOperatorDecompLexicalSolver();
    private final CBS_Solver cbsSolver = CanonicalSolversFactory.createCBSSolver(); // Used as a baseline for optimal solutions
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
    void singleAgentTrivialCase() {
        // Test with single agent already at target
        Agent agent = new Agent(0, coor33, coor33);
        MAPF_Instance testInstance = new MAPF_Instance("singleAgentAtTarget", mapCircle, new Agent[]{agent});

        RunParameters params = new RunParametersBuilder().setTimeout(5000).createRP();
        Solution solution = MAAStarOD.solve(testInstance, params);

        assertNotNull(solution, "Solution should exist for trivial case");
        assertTrue(solution.solves(testInstance));
        assertEquals(0, solution.sumIndividualCosts());
    }

    @Test
    void twoAgentsNoConflict() {
        // Test with two agents that don't conflict
        Agent agent1 = new Agent(0, coor33, coor32);
        Agent agent2 = new Agent(1, coor22, coor24);
        MAPF_Instance testInstance = new MAPF_Instance("twoAgentsNoConflict", mapCircle,
                new Agent[]{agent1, agent2});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());

        assertNotNull(solution, "Solution should exist for non-conflicting agents");
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");

        SingleAgentPlan plan1 = solution.getPlanFor(agent1);
        SingleAgentPlan plan2 = solution.getPlanFor(agent2);

        System.out.println("Agent1 Plan: " + plan1);
        System.out.println("Agent2 Plan: " + plan2);

        // optimal
        assertEquals(1, plan1.size(), "Agent1 should have a plan of size 1");
        assertEquals(4, plan2.size(), "Agent2 should have a plan of size 4");
    }

    @Test
    void twoAgentsNoConflictEmptyMap() {
        // Test with two agents on an empty map that don't conflict
        Agent agent1 = new Agent(0, coor00, coor05);
        Agent agent2 = new Agent(1, coor50, coor55);
        MAPF_Instance testInstance = new MAPF_Instance("twoAgentsNoConflictEmpty", mapEmpty,
                new Agent[]{agent1, agent2});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());
        assertNotNull(solution, "Solution should exist for non-conflicting agents");
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");

        SingleAgentPlan plan1 = solution.getPlanFor(agent1);
        SingleAgentPlan plan2 = solution.getPlanFor(agent2);

        System.out.println("Agent1 Plan: " + plan1);
        System.out.println("Agent2 Plan: " + plan2);

        // optimal
        assertEquals(5, plan1.size());
        assertEquals(5, plan2.size());
    }

    @Test
    void twoAgentsSimpleConflict() {
        // Test with two agents that would conflict if planned independently
        // Agent1: (1,2) -> (1,3), Agent2: (1,3) -> (1,2) - swapping positions
        Agent agent1 = new Agent(0, coor12, coor13);
        Agent agent2 = new Agent(1, coor13, coor12);
        MAPF_Instance testInstance = new MAPF_Instance("twoAgentsSwapping", mapCircle,
                new Agent[]{agent1, agent2});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());

        assertNotNull(solution, "Solution should exist for two agents swapping");
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(8, solution.sumIndividualCosts(), "Should find optimal cost of 8 for both agents");
    }

    @Test
    void emptyMapCase() {
        // Test on empty map where agents can move freely
        Agent agent1 = new Agent(0, coor53, coor05);
        Agent agent2 = new Agent(1, coor43, coor11);
        MAPF_Instance testInstance = new MAPF_Instance("emptyMapTwoAgents", mapEmpty,
                new Agent[]{agent1, agent2});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());
        assertNotNull(solution, "Solution should exist for two agents on empty map");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(12, solution.sumIndividualCosts(), "Should find optimal cost");
    }

    @Test
    void instanceEmpty1Test() {
        MAPF_Instance testInstance = instanceEmpty1;

        RunParameters params = new RunParametersBuilder().setTimeout(3000).createRP();
        Solution solution = MAAStarOD.solve(testInstance, params);
        assertNotNull(solution, "Solution should exist for complex instance");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(cbsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(3000).createRP()).sumIndividualCosts(), solution.sumIndividualCosts());
    }

    @Test
    void instanceCircle1Test() {
        MAPF_Instance testInstance = instanceCircle1;

        RunParameters params = new RunParametersBuilder().setTimeout(3000).createRP();
        Solution solution = MAAStarOD.solve(testInstance, params);
        assertNotNull(solution, "Solution should exist for circle instance");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(cbsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(3000).createRP()).sumIndividualCosts(), solution.sumIndividualCosts(),
                MAAStarOD.getName() + " should find optimal cost for " + testInstance.extendedName);
    }

    @Test
    void instanceCircle2Test() {
        MAPF_Instance testInstance = instanceCircle2;

        RunParameters params = new RunParametersBuilder().setTimeout(3000).createRP();
        Solution solution = MAAStarOD.solve(testInstance, params);
        assertNotNull(solution, "Solution should exist for circle instance");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(cbsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(3000).createRP()).sumIndividualCosts(), solution.sumIndividualCosts(),
                MAAStarOD.getName() + " should find optimal cost for " + testInstance.extendedName);
    }

    @Test
    void instanceEmptyEasyTest() {
        MAPF_Instance testInstance = instanceEmptyEasy;

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());
        assertNotNull(solution, "Solution should exist for easy empty instance");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(cbsSolver.solve(testInstance, new RunParametersBuilder().createRP()).sumIndividualCosts(), solution.sumIndividualCosts(),
                MAAStarOD.getName() + " should find optimal cost for " + testInstance.extendedName);
    }

    @Test
    void threeAgentsNoConflict() {
        Agent agent1 = new Agent(0, coor00, coor05);
        Agent agent2 = new Agent(1, coor50, coor55);
        Agent agent3 = new Agent(2, coor22, coor24);
        MAPF_Instance testInstance = new MAPF_Instance("threeAgentsNoConflict", mapEmpty,
                new Agent[]{agent1, agent2, agent3});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());
        assertNotNull(solution, "Solution should exist for three agents on empty map");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(12, solution.sumIndividualCosts(), "Should find optimal cost for three agents on empty map");
    }

    @Test
    void singleAgentOptimalityTest() {
        // Test single agent case and verify optimality
        Agent agent = new Agent(0, coor00, coor22);
        MAPF_Instance testInstance = new MAPF_Instance("singleAgentOptimal", mapEmpty, new Agent[]{agent});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());
        assertNotNull(solution, "Solution should exist for single agent case");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(4, solution.sumIndividualCosts(), "Should find optimal cost of 4 for single agent");
    }

    @Test
    void twoAgentsSequentialTargets() {
        // Test where one agent needs to follow the another
        Agent agent1 = new Agent(0, coor12, coor32);
        Agent agent2 = new Agent(1, coor22, coor24);
        MAPF_Instance testInstance = new MAPF_Instance("twoAgentsSequential", mapCircle,
                new Agent[]{agent1, agent2});

        Solution solution = MAAStarOD.solve(testInstance, new RunParametersBuilder().createRP());
        assertNotNull(solution, "Solution should exist for two agents with sequential targets");
        System.out.println("Solution: " + solution);
        assertTrue(solution.solves(testInstance), "Solution should solve the instance");
        assertEquals(6, solution.sumIndividualCosts(), "Should find optimal cost");
    }


    /**
     * A test with three agents that must coordinate to reach their goals.
     */
    @Test
    void threeAgentInteraction() {
        MAPF_Instance instance = new MAPF_Instance("threeAgentInteraction", mapEmpty, new Agent[]{
                new Agent(0, coor00, coor02),
                new Agent(1, coor01, coor21),
                new Agent(2, coor22, coor20)
        });

        // Get the optimal solution cost from CBS
        Solution cbsSolution = cbsSolver.solve(instance, new RunParametersBuilder().createRP());
        int optimalCost = cbsSolution.sumIndividualCosts();

        Solution maSolution = MAAStarOD.solve(instance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        assertNotNull(maSolution, "MA-A* should find a solution for three agents");
        assertTrue(maSolution.solves(instance), "MA-A* solution should solve the instance");
        assertEquals(optimalCost, maSolution.sumIndividualCosts(), "MA-A* should find the optimal cost for three agents");
        System.out.println("MA-A* solution: " + maSolution);
        System.out.println("Optimal cost from CBS: " + optimalCost);
        assertEquals(cbsSolution.sumIndividualCosts(), maSolution.sumIndividualCosts(),
                "MA-A* should find the same cost as CBS for three agents interaction");
    }

    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(MAAStarOD, 3 * Config.TESTS_SCOPE, true, false);
    }

    @Test
    void comparativeDiverseTestVSCBS() {
        I_Solver baselineSolver = cbsSolver;
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = MAAStarOD;
        String nameExperimental = competitorSolver.getName();

        int[] agentCounts = Config.TESTS_SCOPE >= 3 ? new int[]{5, 10, 15, 20, 25} :
                Config.TESTS_SCOPE == 2 ? new int[]{10, 15} : new int[]{15};
        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, true, true, agentCounts, 4 * Config.TESTS_SCOPE, 0);
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        Solution solved = MAAStarOD.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        assertNotNull(solved, "Solution should exist for start adjacent go around instance");
        System.out.println("Solution: " + solved);
        assertTrue(solved.solves(testInstance), "Solution should solve the instance");
        assertEquals(6, solved.sumIndividualCosts(), "Should find optimal cost of 6 for start adjacent go around instance");
    }

    @Test
    void unsolvableInstance() {
        // Test with unsolvable instance
        MAPF_Instance testInstance = instanceUnsolvable;

        int timeout = 10000;
        InstanceReport instanceReport = new InstanceReport();
        RunParameters params = new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP();
        Solution solution = MAAStarOD.solve(testInstance, params);

        // Should return null for unsolvable instances without timing out
        assertNull(solution, "Should return null for unsolvable instance");
        assertTrue(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) < timeout, "Should not time out for unsolvable instance");
    }

    @Test
    void narrowCorridorTestUnsolvableShouldFailBeforeTimeout() {
        MAPF_Instance testInstance = new MAPF_Instance("Narrow corridor test", mapNarrowCorridor, new Agent[]{
                new Agent(1, coor00, coor03),
                new Agent(2, coor01, coor02)
        });

        Solution solved = MAAStarOD.solve(testInstance, new RunParametersBuilder()
                .setTimeout(10000)
                .setInstanceReport(instanceReport)
                .createRP());

        assertNull(solved, "Solution should not exist for narrow corridor test - should be unsolvable");
        assertTrue(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) < 5000,
                "Should not time out for unsolvable narrow corridor test");
    }

    @Test
    void unsolvableBecauseConstraintsShouldFailBeforeTimeout1() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 1, testInstance.map.getMapLocation(coor14)));
        int timeout = 5000;
        Solution solved = MAAStarOD.solve(testInstance, new RunParametersBuilder().setConstraints(constraintSet).setInstanceReport(instanceReport).setTimeout(timeout).createRP());
        Metrics.removeReport(instanceReport);

        assertNull(solved);
        assertTrue(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) < timeout,
                "Should not time out for unsolvable instance with constraints");
    }

    @Test
    void unsolvableBecauseConstraintsShouldFailBeforeTimeout2() {
        MAPF_Instance testInstance = instanceSmallMaze;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor04)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor14)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor13)));
        constraintSet.add(new Constraint(agent04to00, 2, testInstance.map.getMapLocation(coor15)));
        int timeout = 5000;
        Solution solved = MAAStarOD.solve(testInstance, new RunParametersBuilder().setConstraints(constraintSet).setInstanceReport(instanceReport).setTimeout(timeout).createRP());
        Metrics.removeReport(instanceReport);

        assertNull(solved);
        assertTrue(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) < timeout,
                "Should not time out for unsolvable instance with constraints");
    }

    @Test
    void narrowCorridorWithRoomTest() {
        MAPF_Instance testInstance = new MAPF_Instance("Narrow corridor with room", mapNarrowCorridorWithRoom, new Agent[]{
                new Agent(1, coor10, coor13),
                new Agent(2, coor11, coor12)
        });

        Solution solved = MAAStarOD.solve(testInstance, new RunParametersBuilder()
                .setTimeout(5000)
                .setInstanceReport(instanceReport)
                .createRP());

        assertNotNull(solved, "Solution should exist for narrow corridor with room test");
        System.out.println("Solution: " + solved);
        assertTrue(solved.solves(testInstance), "Solution should solve the instance");
        assertEquals(18, solved.sumIndividualCosts(), "Should find optimal cost");
    }

    @Test
    void compareWithCBSOnSmallMaze() {
        MAPF_Instance testInstance = instanceSmallMaze;

        // First solve with CBS to get optimal baseline
        Solution cbsSolution = cbsSolver.solve(testInstance,
                new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        assertNotNull(cbsSolution, "CBS should be able to solve this instance");

        // Now solve with Multi-Agent A*
        Solution maSolution = MAAStarOD.solve(testInstance,
                new RunParametersBuilder().setInstanceReport(instanceReport).setTimeout(10000).createRP());

        if (maSolution != null) {
            assertTrue(maSolution.isValidSolution(), "Solution should be valid");
            assertTrue(maSolution.solves(testInstance), "Solution should solve the instance");
            assertEquals(cbsSolution.sumIndividualCosts(), maSolution.sumIndividualCosts(),
                    "MA* should find optimal solution");
        }
    }

    @Test
    void lexicalOptimalityWithConstraintsTest() {
        // Test lexical optimality with external constraints
        // Does not fully verify lexical optimality, so false positives (passing tests) are possible
        MAPF_Instance instance = new MAPF_Instance("lexicalWithConstraintsTest", mapEmpty, new Agent[]{
                new Agent(0, coor00, coor22),
                new Agent(1, coor10, coor32)
        });

        // Add constraints that force agent 0 to take a specific path
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(instance.agents.get(0), 1, instance.map.getMapLocation(coor01)));

        // Solve with constraints
        Solution socSolution = MAAStarOD.solve(instance,
                new RunParametersBuilder().setConstraints(constraintSet).createRP());
        Solution lexicalSolution = MAAStarOD_Lexical.solve(instance,
                new RunParametersBuilder().setConstraints(constraintSet).createRP());

        assertNotNull(socSolution, "SOC solution with constraints should exist");
        assertNotNull(lexicalSolution, "Lexical solution with constraints should exist");

        System.out.println("SOC Solution with constraints: " + socSolution);
        System.out.println("Lexical Solution with constraints: " + lexicalSolution);

        // Check if first agent with different cost has lower cost in lexical solution
        for (Agent agent : instance.agents) {
            int socCost = socSolution.getPlanFor(agent).getCost();
            int lexicalCost = lexicalSolution.getPlanFor(agent).getCost();

            if (socCost != lexicalCost) {
                assertTrue(lexicalCost <= socCost,
                        "First agent with cost difference (agent " + agent.iD + ") should have lower cost in lexical solution with constraints");
                System.out.println("Found first cost difference at agent " + agent.iD + ": Lexical = " +
                        lexicalCost + ", SOC = " + socCost);
                break;
            }
        }
    }

    @Test
    void lexicalOptimalityDiverseInstancesTest() {
        int[] agentCounts = Config.TESTS_SCOPE >= 3 ? new int[]{3,4,5,6} :
                Config.TESTS_SCOPE == 2 ? new int[]{4, 5} : new int[]{5};
        TestUtils.verifyLexicalOptimalityOnDiverseInstances(MAAStarOD, MAAStarOD_Lexical, agentCounts, 3000 * Config.TESTS_SCOPE);
    }

    @Test
    void lexicalOptimalityVSPCS_Lexical() {
        int[] agentCounts = Config.TESTS_SCOPE >= 3 ? new int[]{3,4,5,6} :
                Config.TESTS_SCOPE == 2 ? new int[]{4, 5} : new int[]{5};
        TestUtils.compareLexicalSolvers(CanonicalSolversFactory.createPCSLexicalSolver(), MAAStarOD_Lexical, agentCounts, 3000 * Config.TESTS_SCOPE);
    }

}