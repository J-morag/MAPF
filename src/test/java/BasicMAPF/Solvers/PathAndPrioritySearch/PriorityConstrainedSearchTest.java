package BasicMAPF.Solvers.PathAndPrioritySearch;

import BasicMAPF.DataTypesAndStructures.*;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.TestUtils;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.junit.jupiter.api.*;

import static BasicMAPF.TestConstants.Agents.agent43to53;
import static BasicMAPF.TestConstants.Agents.agent55to34;
import static BasicMAPF.TestConstants.Coordiantes.coor12;
import static BasicMAPF.TestConstants.Coordiantes.coor33;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Maps.mapCircle;
import static BasicMAPF.TestConstants.Maps.mapWithPocket;
import static org.junit.jupiter.api.Assertions.*;

class PriorityConstrainedSearchTest {

    private final MAPF_Instance instanceUnsolvableBecauseOrderWithInfiniteWait = new MAPF_Instance("instanceUnsolvableWithInfiniteWait", mapWithPocket, new Agent[]{agent43to53, agent55to34});

    I_Solver PCSSolver = CanonicalSolversFactory.createPCSSolver();
    I_Solver PCSLexicalSolver = CanonicalSolversFactory.createPCSLexicalSolver();

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
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        assertTrue(solved.solves(testInstance));
        assertEquals(10, solved.sumIndividualCosts());
        assertEquals(8, solved.makespan());
    }

    @Test
    void failsBeforeTimeoutWhenUnsolvableOrder() {
        MAPF_Instance testInstance = instanceUnsolvableBecauseOrderWithInfiniteWait;
        InstanceReport instanceReport = new InstanceReport();
        long timeout = 10*1000;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());

        // shouldn't time out
        assertFalse(instanceReport.getIntegerValue(InstanceReport.StandardFields.elapsedTimeMS) > timeout);
        // should return "no solution" (is a complete algorithm)
        assertNull(solved);
    }

    @Test
    void unsolvable() {
        MAPF_Instance testInstance = instanceUnsolvable;
        Solution solved = PCSSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertNull(solved);
    }


    @Test
    void differentOrders() {
        MAPF_Instance testInstance = instanceCircle1;
        I_Solver solver = PCSSolver;

        Agent agent0 = new Agent(0, coor33, coor12);
        Agent agent1 = new Agent(1, coor12, coor33);

        MAPF_Instance agent0prioritisedInstance = new MAPF_Instance("agent0prioritised", mapCircle, new Agent[]{agent0, agent1});
        Solution agent0prioritisedSolution = solver.solve(agent0prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        MAPF_Instance agent1prioritisedInstance = new MAPF_Instance("agent1prioritised", mapCircle, new Agent[]{agent1, agent0});
        Solution agent1prioritisedSolution = solver.solve(agent1prioritisedInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());

        assertTrue(agent0prioritisedSolution.solves(testInstance));
        assertTrue(agent1prioritisedSolution.solves(testInstance));

        assertEquals(3, agent0prioritisedSolution.getPlanFor(agent0).size());
        assertEquals(3, agent1prioritisedSolution.getPlanFor(agent1).size());
    }

    // TODO add PBS paper examples

    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(PCSSolver, 5 * Config.TESTS_SCOPE, false, false);
    }

    @Test
    void comparativeTestVsPP(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPPSIPPSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = PCSSolver;
        String nameExperimental = competitorSolver.getName();
        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, true, true, new int[]{10 * Config.TESTS_SCOPE}, 3, 4);
    }

    @Test
    void comparativeTestVsPPRStar(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPPRStarAnytimeSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = PCSSolver;
        String nameExperimental = competitorSolver.getName();
        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, true, true, new int[]{10 * Config.TESTS_SCOPE}, 3, 4);
    }

    @Test
    void comparativeTestHeuristics(){
        I_Solver baselineSolver = new PaPSBuilder().setNoAgentsSplit(true).setPaPSHeuristic(new PaPSHeuristicDefault()).createPaPS();
        String nameBaseline = "PCSDefault";

        I_Solver competitorSolver = new PaPSBuilder().setNoAgentsSplit(true).setPaPSHeuristic(new PaPSHeuristicSIPP()).createPaPS();
        String nameExperimental = "PCSSIPP";
        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, true, true, new int[]{10 * Config.TESTS_SCOPE}, 3, 1);
    }

    @Test
    void comparativeTestDuplicateDetection(){
        I_Solver baselineSolver = new PaPSBuilder().setNoAgentsSplit(true).setUseDuplicateDetection(false).createPaPS();
        String nameBaseline = "PCS-noClosedList";

        I_Solver competitorSolver = new PaPSBuilder().setNoAgentsSplit(true).setUseDuplicateDetection(true).createPaPS();
        String nameExperimental = "PCS-ClosedList";
        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, true, true, new int[]{10}, 10, 1);
    }

    @Test
    void lexicalOptimalityDiverseInstancesTest() {
        int[] agentCounts = Config.TESTS_SCOPE >= 3 ? new int[]{10,15,20,25,30} :
                Config.TESTS_SCOPE == 2 ? new int[]{20, 25} : new int[]{20};
        TestUtils.verifyLexicalOptimalityOnDiverseInstances(PCSSolver, PCSLexicalSolver, agentCounts, 3000 * Config.TESTS_SCOPE);
    }
}