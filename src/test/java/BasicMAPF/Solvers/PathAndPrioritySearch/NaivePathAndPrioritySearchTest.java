package BasicMAPF.Solvers.PathAndPrioritySearch;

import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

class NaivePathAndPrioritySearchTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    void comparativeTestPCSVSNaivePaPS(){
        I_Solver baselineSolver = CanonicalSolversFactory.createPCSSolver();
        String nameBaseline = baselineSolver.getName();

        NaivePaPS competitorSolver = CanonicalSolversFactory.createNaivePaPSSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, false, false, competitorSolver,
                nameExperimental, true, true, new int[]{5}, 5, 0);
    }

    @Test
    void comparativeTestNaiveOPSVSNaiveOPSOneOpen(){
        I_Solver baselineSolver = CanonicalSolversFactory.createNaivePaPSSolver();
        String nameBaseline = baselineSolver.getName();

        I_Solver competitorSolver = CanonicalSolversFactory.createNaivePaPSUnifiedOpenSolver();
        String nameExperimental = competitorSolver.getName();

        TestUtils.comparativeTest(baselineSolver, nameBaseline, true, true, competitorSolver,
                nameExperimental, true, true, new int[]{5}, 5, 0);
    }

}