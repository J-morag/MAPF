package LifelongMAPF;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Metrics.InstanceReport;
import Environment.Metrics.S_Metrics;

import static LifelongMAPF.LifelongTestConstants.*;
import static LifelongMAPF.LifelongTestConstants.instanceStartAdjacentGoAround;
import static LifelongMAPF.LifelongTestUtils.isFullSolution;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LifelongTestInstances {

    public static void emptyMapValidityTest1(I_Solver solver) {
        MAPF_Instance testInstance = instanceEmpty1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    public static void circleMapValidityTest1(I_Solver solver) {
        MAPF_Instance testInstance = instanceCircle1;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);

    }

    public static void circleMapValidityTest2(I_Solver solver) {
        MAPF_Instance testInstance = instanceCircle2;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);

        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    public static void smallMazeDenseValidityTest(I_Solver solver) {
        MAPF_Instance testInstance = instanceSmallMazeDense;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

    public static void startAdjacentGoAroundValidityTest(I_Solver solver) {
        MAPF_Instance testInstance = instanceStartAdjacentGoAround;
        InstanceReport instanceReport = S_Metrics.newInstanceReport();
        Solution solved = solver.solve(testInstance, new RunParametersBuilder().setTimeout(DEFAULT_TIMEOUT).setInstanceReport(instanceReport).createRP());
        S_Metrics.removeReport(instanceReport);
        assertNotNull(solved);
        System.out.println(solved.readableToString());
        isFullSolution(solved, testInstance);
    }

}
