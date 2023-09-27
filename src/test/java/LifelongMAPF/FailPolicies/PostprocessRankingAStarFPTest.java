package LifelongMAPF.FailPolicies;

import BasicMAPF.Solvers.I_Solver;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import LifelongMAPF.LifelongTestInstances;
import org.junit.jupiter.api.Test;
class PostprocessRankingAStarFPTest {

    I_Solver simpleWithAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPLookahead1AvoidASFP();
    I_Solver complexWithAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP();

    I_Solver simpleWithWaterfall =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPLookahead1WaterfallPPRASFP();
    I_Solver complexWithWaterfall =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5WaterfallPPRASFP();

    @Test
    void simpleWithAvoid(){
        I_Solver solver = simpleWithAvoid;
        LifelongTestInstances.emptyMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest2(solver);
        LifelongTestInstances.smallMazeDenseValidityTest(solver);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(solver);
    }

    @Test
    void complexWithAvoid(){
        I_Solver solver = complexWithAvoid;
        LifelongTestInstances.emptyMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest2(solver);
        LifelongTestInstances.smallMazeDenseValidityTest(solver);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(solver);
    }

    @Test
    void simpleWithWaterfall(){
        I_Solver solver = simpleWithWaterfall;
        LifelongTestInstances.emptyMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest2(solver);
        LifelongTestInstances.smallMazeDenseValidityTest(solver);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(solver);
    }

    @Test
    void complexWithWaterfall(){
        I_Solver solver = complexWithWaterfall;
        LifelongTestInstances.emptyMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest2(solver);
        LifelongTestInstances.smallMazeDenseValidityTest(solver);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(solver);
    }
}