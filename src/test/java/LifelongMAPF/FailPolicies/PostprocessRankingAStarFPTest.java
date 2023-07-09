package LifelongMAPF.FailPolicies;

import BasicMAPF.Solvers.I_Solver;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import LifelongMAPF.LifelongTestInstances;
import org.junit.jupiter.api.Test;
class PostprocessRankingAStarFPTest {

    I_Solver simpleWithIAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPLookahead1IAvoidASFP();
    I_Solver complexWithIAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5IAvoid1ASFP();

    I_Solver simpleWithWaterfall =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPLookahead1WaterfallPPRASFP();
    I_Solver complexWithWaterfall =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead5WaterfallPPRASFP();

    @Test
    void simpleWithIAvoid(){
        I_Solver solver = simpleWithIAvoid;
        LifelongTestInstances.emptyMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest1(solver);
        LifelongTestInstances.circleMapValidityTest2(solver);
        LifelongTestInstances.smallMazeDenseValidityTest(solver);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(solver);
    }

    @Test
    void complexWithIAvoid(){
        I_Solver solver = complexWithIAvoid;
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