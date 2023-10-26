package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Agents.agent21to00;
import static BasicMAPF.TestConstants.Maps.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LaCAM_SolverTest {

    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});

    private final MAPF_Instance exampleInstance = new MAPF_Instance("exampleInstance", mapTwoWallsSmall, new Agent[]{agent00to02, agent02to00});
    private final MAPF_Instance instanceEmptyEasy = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent04to00});


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
    }


}
