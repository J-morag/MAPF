package LifelongMAPF;

import Environment.IO_Package.IO_Manager;
import LifelongMAPF.LifelongRunManagers.LifelongRunManagerMovingAI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class LifelongRunManagerMovingAITest {

    LifelongRunManagerMovingAI lifelongRunManagerMovingAI;

    @BeforeEach
    void setUp() {
        lifelongRunManagerMovingAI = new LifelongRunManagerMovingAI(IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, "Instances", "MovingAI"}), new int[]{10});
    }

    @Test
    void testRunsWithInstancesFromTestResources() {
        lifelongRunManagerMovingAI.runAllExperiments();
    }
}