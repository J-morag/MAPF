package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.TestConstants.Maps;
import BasicMAPF.TestUtils;
import Environment.Metrics.InstanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

import static BasicMAPF.TestConstants.Coordiantes.*;

class PathAndPrioritySearchTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    void comparativeTestNaivePaPSUnifiedOpenVsPaPS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createNaivePaPSUnifiedOpenSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createPaPSSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, true, true, new int[]{5}, 5, 0);
    }

    @Test
    void comparativeTestPrPrVsPaPS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPPSIPPRRAnytimeSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createPaPSSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, true, true, new int[]{5}, 5, 0);
    }

    @Test
    void comparativeTestPCSVsPaPS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPCSSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createPaPSSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, true, true, new int[]{5}, 20, 0);
    }

    @Test
    void comparativeTestDuplicateDetection(){
        I_Solver baselineSolver = new PaPSBuilder().setUseDuplicateDetection(false).createPaPS();
        String nameBaseline = "PaPS-noClosedList";

        I_Solver competitorSolver = new PaPSBuilder().setUseDuplicateDetection(true).createPaPS();
        String nameExperimental = "PaPS-ClosedList";
        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, true, true, new int[]{5}, 20, 1);
    }

    @Test
    void testDuplicateDetection1(){
        I_Solver solver = new PaPSBuilder().setUseDuplicateDetection(true).createPaPS();
        MAPF_Instance instance = new MAPF_Instance("empty map independent agents", Maps.mapEmpty, new Agent[]{
                new Agent(0, coor00, coor11), new Agent(1, coor22, coor33)
        });
        InstanceReport report = new InstanceReport();
        Solution solution = solver.solve(instance, new RunParametersBuilder().setInstanceReport(report).createRP());
        assertNotNull(solution);
        System.out.println(solution);
        assertEquals(4, solution.sumIndividualCosts());
        int numSkippedClosedNodes = report.getIntegerValue(PathAndPrioritySearch.CLOSED_DUPLICATE_NODES_SKIPPED_STR);
        int numSkippedOpenNodes= report.getIntegerValue(PathAndPrioritySearch.OPEN_DUPLICATE_NODES_SKIPPED_STR);
        System.out.println("numSkippedClosedNodes: " + numSkippedClosedNodes);
        System.out.println("numSkippedOpenNodes: " + numSkippedOpenNodes);
        int numGeneratedNodes = report.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        int numExpandedNodes = report.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        System.out.println("generated nodes: " + numGeneratedNodes);
        System.out.println("expanded nodes: " + numExpandedNodes);
        assertEquals(4, numGeneratedNodes);
        assertEquals(2, numExpandedNodes); // once for adding each agent
        assertEquals(0, numSkippedClosedNodes);
        assertEquals(0, numSkippedOpenNodes);
    }

    @Test
    void testDuplicateDetection2(){
        I_Solver solver = new PaPSBuilder().setUseDuplicateDetection(true).createPaPS();
        MAPF_Instance instance = new MAPF_Instance("empty map independent agents", Maps.mapEmpty, new Agent[]{
                new Agent(0, coor00, coor11), new Agent(1, coor22, coor33), new Agent(2, coor44, coor55),
        });
        InstanceReport report = new InstanceReport();
        Solution solution = solver.solve(instance, new RunParametersBuilder().setInstanceReport(report).createRP());
        assertNotNull(solution);
        System.out.println(solution);
        assertEquals(6, solution.sumIndividualCosts());
        int numSkippedClosedNodes = report.getIntegerValue(PathAndPrioritySearch.CLOSED_DUPLICATE_NODES_SKIPPED_STR);
        int numSkippedOpenNodes= report.getIntegerValue(PathAndPrioritySearch.OPEN_DUPLICATE_NODES_SKIPPED_STR);
        System.out.println("numSkippedClosedNodes: " + numSkippedClosedNodes);
        System.out.println("numSkippedOpenNodes: " + numSkippedOpenNodes);
        int numGeneratedNodes = report.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        int numExpandedNodes = report.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        System.out.println("generated nodes: " + numGeneratedNodes);
        System.out.println("expanded nodes: " + numExpandedNodes);
        assertEquals(7, numGeneratedNodes);
        assertEquals(3, numExpandedNodes); // once for adding each agent
        assertEquals(0, numSkippedClosedNodes);
        assertEquals(0, numSkippedOpenNodes);
    }

    @Test
    void testDuplicateDetection3(){
        I_Solver solver = new PaPSBuilder().setUseDuplicateDetection(true).createPaPS();
        MAPF_Instance instance = new MAPF_Instance("empty map independent agents", Maps.mapEmpty, new Agent[]{
                new Agent(0, coor00, coor11), new Agent(1, coor22, coor33), new Agent(2, coor44, coor55),
                new Agent(3, coor05, coor14)
        });
        InstanceReport report = new InstanceReport();
        Solution solution = solver.solve(instance, new RunParametersBuilder().setInstanceReport(report).createRP());
        assertNotNull(solution);
        System.out.println(solution);
        assertEquals(8, solution.sumIndividualCosts());
        int numSkippedClosedNodes = report.getIntegerValue(PathAndPrioritySearch.CLOSED_DUPLICATE_NODES_SKIPPED_STR);
        int numSkippedOpenNodes = report.getIntegerValue(PathAndPrioritySearch.OPEN_DUPLICATE_NODES_SKIPPED_STR);
        System.out.println("numSkippedClosedNodes: " + numSkippedClosedNodes);
        System.out.println("numSkippedOpenNodes: " + numSkippedOpenNodes);
        int numGeneratedNodes = report.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        int numExpandedNodes = report.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        System.out.println("generated nodes: " + numGeneratedNodes);
        System.out.println("expanded nodes: " + numExpandedNodes);
        assertEquals(11, numGeneratedNodes);
        assertEquals(4, numExpandedNodes); // once for adding each agent
        assertEquals(0, numSkippedClosedNodes);
        assertEquals(0, numSkippedOpenNodes);
    }

    @Test
    void testDuplicateDetectionIndependentPairs(){
        I_Solver solver = new PaPSBuilder().setUseDuplicateDetection(true).createPaPS();
        MAPF_Instance instance = new MAPF_Instance("empty map independent agents", Maps.mapEmpty, new Agent[]{
                new Agent(0, coor00, coor22), new Agent(1, coor33, coor00), new Agent(2, coor35, coor55),
                new Agent(3, coor55, coor45)
        });
        InstanceReport report = new InstanceReport();
        Solution solution = solver.solve(instance, new RunParametersBuilder().setInstanceReport(report).createRP());
        assertNotNull(solution);
        System.out.println(solution);
        assertEquals(15, solution.sumIndividualCosts());
        int numSkippedClosedNodes = report.getIntegerValue(PathAndPrioritySearch.CLOSED_DUPLICATE_NODES_SKIPPED_STR);
        int numSkippedOpenNodes = report.getIntegerValue(PathAndPrioritySearch.OPEN_DUPLICATE_NODES_SKIPPED_STR);
        System.out.println("numSkippedClosedNodes: " + numSkippedClosedNodes);
        System.out.println("numSkippedOpenNodes: " + numSkippedOpenNodes);
        int numGeneratedNodes = report.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        int numExpandedNodes = report.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
        System.out.println("generated nodes: " + numGeneratedNodes);
        System.out.println("expanded nodes: " + numExpandedNodes);
        assertTrue(numSkippedClosedNodes > 0 || numSkippedOpenNodes > 0);
    }

    @Test
    void comparativeTestPaPSVsPFCS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPaPSSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createPFCSSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, false, false, new int[]{5, 7, 10, 12}, 10, 0);
    }

    @Test
    void comparativeTestPCSVsPFCS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPCSSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createPFCSSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{5, 7, 10, 12}, 10, 0);
    }

    @Test
    void comparativeTestPrioritisedPlanningVsPP_byUsingPaPS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPPSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createPP_byUsingPaPS();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, false, false, new int[]{10, 15, 20}, 10, 0);
    }

}