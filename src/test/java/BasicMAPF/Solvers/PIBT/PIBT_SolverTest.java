package BasicMAPF.Solvers.PIBT;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PIBT_SolverTest {

    private final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to00});
    private final MAPF_Instance instanceEmptyHarder = new MAPF_Instance("instanceEmpty", mapEmpty, new Agent[]
            {agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, agent00to10, agent55to34, agent34to32, agent31to14, agent40to02});
    private final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    private final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent12to33, agent33to12});
    private final MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{agent00to10, agent10to00});
    private final MAPF_Instance instanceUnsolvableBecauseOrderWithInfiniteWait = new MAPF_Instance("instanceUnsolvableWithInfiniteWait", mapWithPocket, new Agent[]{agent43to53, agent55to34});
    private final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});


    I_Solver PIBT_Solver = new PIBT_Solver();

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
    void emptyMapValidityTest1() {
        MAPF_Instance testInstance = instanceEmpty1;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParameters(instanceReport));

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void emptyMapHarderValidityTest1() {
        MAPF_Instance testInstance = instanceEmptyHarder;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParameters(instanceReport));

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest1() {
        MAPF_Instance testInstance = instanceCircle1;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParameters(instanceReport));

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void circleMapValidityTest2() {
        MAPF_Instance testInstance = instanceCircle2;
        Solution solved = PIBT_Solver.solve(testInstance, new RunParameters(instanceReport));

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
    }

    @Test
    void startAdjacentGoAroundValidityTest() {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = PIBT_Solver.solve(testInstance, new RunParameters(instanceReport));
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        assertTrue(solved.solves(testInstance));
        assertEquals(6, solved.sumIndividualCosts());
        assertEquals(4, solved.makespan());
    }

//    @Test
//    void unsolvable() {
//        MAPF_Instance testInstance = instanceUnsolvable;
//        Solution solved = PIBT_Solver.solve(testInstance, new RunParameters(instanceReport));
//
//        assertNull(solved);
//    }
}
