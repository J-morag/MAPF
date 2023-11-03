package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import Environment.IO_Package.IO_Manager;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Map;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Agents.agent21to00;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestUtils.readResultsCSV;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;

public class LaCAM_SolverTest {

    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});

    private final MAPF_Instance exampleInstance = new MAPF_Instance("exampleInstance", mapTwoWallsSmall, new Agent[]{agent00to02, agent02to00});
    private final MAPF_Instance instanceEmptyEasy = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent04to00});
    private final MAPF_Instance instanceEmptyHarder = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, agent00to10, agent55to34, agent34to32, agent31to14, agent40to02});

    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceAgentsInterruptsEachOther = new MAPF_Instance("instanceAgentsInterruptsEachOther", mapWithPocket, new Agent[]{agent43to53, agent55to34});
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});
    private final MAPF_Instance instanceAgentsNeedsToSwapLocations = new MAPF_Instance("instanceAgentsNeedsToSwapLocations", mapWithPocket, new Agent[]{agent55to34, agent54to55});

    I_Solver LaCAM_Solver = new LaCAM_Solver();

    long timeout = 10*1000;

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
    void exampleTest() {
        MAPF_Instance testInstance = exampleInstance;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(8, solved.sumIndividualCosts());
        assertEquals(4, solved.makespan());
    }

    @Test
    void emptyMapEasyNoConflictsTest() {
        MAPF_Instance testInstance = instanceEmptyEasy;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(8, solved.sumIndividualCosts());
        assertEquals(4, solved.makespan());
    }

    @Test
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(35, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
    }

    @Test
    void emptyMapHarderValidityTest1() {
        MAPF_Instance testInstance = instanceEmptyHarder;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(14, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(14, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
    }

    @Test
    void instanceAgentsInterruptsEachOtherTest() {
        MAPF_Instance testInstance = instanceAgentsInterruptsEachOther;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(14, solved.sumIndividualCosts());
        assertEquals(7, solved.makespan());
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(8, solved.sumIndividualCosts());
        assertEquals(4, solved.makespan());
    }

    @Test
    void agentsNeedToSwapTest() {
        MAPF_Instance testInstance = instanceAgentsNeedsToSwapLocations;
        Solution solved = LaCAM_Solver.solve(testInstance, new RunParametersBuilder().setTimeout(timeout).setInstanceReport(instanceReport).createRP());
        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
//        assertEquals(8, solved.sumIndividualCosts());
//        assertEquals(4, solved.makespan());
    }
}
