package LifelongMAPF;

import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.DataTypesAndStructures.Solution;

import static org.junit.jupiter.api.Assertions.*;

public class LifelongTestUtils {

    public static void isFullSolution(Solution solution, int expectedSOC, int expectedMakespan, MAPF_Instance instance){
        isFullSolution(solution, instance);

        assertEquals(expectedSOC, solution.sumIndividualCosts()); // SOC is optimal
        assertEquals(expectedMakespan, solution.makespan()); // makespan is optimal
    }

    public static void isValidFullOrPartialSolution(Solution solution, MAPF_Instance instance){
        assertTrue(solution.isValidSolution(false, false)); //is valid (no conflicts)
        assertTrue(solution.solves(instance, false, false)); // solves (could be partial)
    }

    public static void isFullSolution(Solution solution, MAPF_Instance instance){
        assertTrue(solution.isValidSolution(false, false)); //is valid (no conflicts)
        assertTrue(solution.solves(instance, false, false)); // solves (could be partial)
        // TODO can never guarantee a full solution now - an agent at its last destination will prevent others from ever reaching it (if they have it as one of their destinations)...
        //  handle this somehow... currently using a  very crude alternative of seeing that some progress was made
//        assertTrue(new Solution(solution).solves(instance, true, false)); // solves (is full solution)
        assertTrue(((LifelongSolution)solution).throughputAtT(100) >= instance.agents.size());
        System.out.println("Throughput: " + ((LifelongSolution)solution).throughputAtT(100));
    }

    public static void isPartialSolution(MAPF_Instance instance, Solution solution) {
        assertTrue(solution.isValidSolution(false, false)); //is valid (no conflicts)
        assertTrue(solution.solves(instance, false, false)); // solves (could be partial)
        assertFalse(new Solution(solution).solves(instance, false, false)); // solves (is full solution)
    }
}
