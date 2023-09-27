package LifelongMAPF.FailPolicies;

import BasicMAPF.Solvers.I_Solver;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import LifelongMAPF.LifelongTestInstances;
import org.junit.jupiter.api.Test;

class FailPolicyTest {

    I_Solver simpleWithAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPLookahead1IntegratedAvoid();

    I_Solver complexWithAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5IntegratedAvoid();

    @Test
    void simpleWithAvoid(){
        LifelongTestInstances.emptyMapValidityTest1(simpleWithAvoid);
        LifelongTestInstances.circleMapValidityTest1(simpleWithAvoid);
        LifelongTestInstances.circleMapValidityTest2(simpleWithAvoid);
        LifelongTestInstances.smallMazeDenseValidityTest(simpleWithAvoid);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(simpleWithAvoid);
    }

    @Test
    void complexWithAvoid(){
        LifelongTestInstances.emptyMapValidityTest1(complexWithAvoid);
        LifelongTestInstances.circleMapValidityTest1(complexWithAvoid);
        LifelongTestInstances.circleMapValidityTest2(complexWithAvoid);
        LifelongTestInstances.smallMazeDenseValidityTest(complexWithAvoid);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(complexWithAvoid);
    }

}