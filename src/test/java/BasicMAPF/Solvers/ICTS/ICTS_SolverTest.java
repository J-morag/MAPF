package BasicMAPF.Solvers.ICTS;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.MapDimensions;
import BasicMAPF.Solvers.CBS.CBSBuilder;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.TestUtils;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.Metrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Instances.*;
import static org.junit.jupiter.api.Assertions.*;

class ICTS_SolverTest {

    InstanceBuilder_BGU builder = new InstanceBuilder_BGU();
    InstanceManager im = new InstanceManager(IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,"Instances"}),
            new InstanceBuilder_BGU(), new InstanceProperties(new MapDimensions(new int[]{6,6}),0f,new int[]{1}));

    I_Solver ictsSolver = new ICTS_Solver();

    @BeforeEach
    void setUp() {

    }

    void validate(Solution solution, int numAgents, int optimalSOC, int optimalMakespan, MAPF_Instance instance){
        assertTrue(solution.isValidSolution()); //is valid (no conflicts)
        assertTrue(solution.solves(instance));

        assertEquals(numAgents, solution.size()); // solution includes all agents
        assertEquals(optimalSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(optimalMakespan, solution.makespan()); // makespan is optimal
    }

    @Test
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 7, solved.sumIndividualCosts(),solved.makespan(), testInstance); //need to find actual optimal costs
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 2, 8, 5, testInstance);

    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 2, 8, 5, testInstance);
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParametersBuilder().setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        System.out.println(solved);
        validate(solved, 2, 6, 4, testInstance);
    }

    @Test
    void unsolvableBecauseOfConflictsShouldTimeout() {
        MAPF_Instance testInstance = instanceUnsolvable;
        InstanceReport instanceReport = Metrics.newInstanceReport();
        Solution solved = ictsSolver.solve(testInstance, new RunParametersBuilder().setTimeout(2L*1000).setInstanceReport(instanceReport).createRP());
        Metrics.removeReport(instanceReport);

        assertNull(solved);
    }

    @Test
    void TestingBenchmark(){
        TestUtils.TestingBenchmark(ictsSolver, 10, true, false);
    }

    @Test
    void comparativeTest(){
        I_Solver cbs = new CBSBuilder().createCBS_Solver();
        String nameBaseline = "cbs";
        I_Solver icts = new ICTS_Solver();
        String nameExperimental = "ICTS";

        TestUtils.comparativeTest(cbs, nameBaseline, true, icts, nameExperimental,
                true, new int[]{10}, 10, 0);
    }

}