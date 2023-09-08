package LifelongMAPF.FailPolicies;

import BasicMAPF.Solvers.I_Solver;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import LifelongMAPF.LifelongTestInstances;
import org.junit.jupiter.api.Test;

class FailPolicyTest {

    I_Solver simpleWithIAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialStayOnceFPLookahead1IntegratedIAvoid();

    I_Solver complexWithIAvoid =
            LifelongSolversFactory.stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5IntegratedIAvoid();

    @Test
    void simpleWithIAvoid(){
        LifelongTestInstances.emptyMapValidityTest1(simpleWithIAvoid);
        LifelongTestInstances.circleMapValidityTest1(simpleWithIAvoid);
        LifelongTestInstances.circleMapValidityTest2(simpleWithIAvoid);
        LifelongTestInstances.smallMazeDenseValidityTest(simpleWithIAvoid);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(simpleWithIAvoid);
    }

    @Test
    void complexWithIAvoid(){
        LifelongTestInstances.emptyMapValidityTest1(complexWithIAvoid);
        LifelongTestInstances.circleMapValidityTest1(complexWithIAvoid);
        LifelongTestInstances.circleMapValidityTest2(complexWithIAvoid);
        LifelongTestInstances.smallMazeDenseValidityTest(complexWithIAvoid);
        LifelongTestInstances.startAdjacentGoAroundValidityTest(complexWithIAvoid);
    }

}