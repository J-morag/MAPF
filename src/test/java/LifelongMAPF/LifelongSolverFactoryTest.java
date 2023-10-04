package LifelongMAPF;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Solvers.I_Solver;
import Environment.IO_Package.IO_Manager;
import LifelongMAPF.LifelongRunManagers.LifelongGenericRunManager;
import LifelongMAPF.LifelongRunManagers.LifelongSolversFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Essentially a regression test for lifelong solvers. Initializes all solvers in the factory and runs them on all instances in the test resources.
 * Will only fail if an exception is thrown while running any of the solvers or an invalid solution is returned.
 */
class LifelongSolverFactoryTest {

    @Test
    void testLifelongSolverFactorySolversWithInstancesFromTestResources() {
        String resultsOutputDir = IO_Manager.buildPath(new String[]{   System.getProperty("user.home"), "MAPF_Tests"});
        String instancesDir = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, "Instances", "MovingAI"});
        LifelongGenericRunManager lifelongRunManager = new LifelongGenericRunManager(instancesDir, new int[]{100},
                new InstanceBuilder_MovingAI(true), "LifelongSolverFactoryTest", false,
                "warehouse-10-20-10-2-1-even-1.scen", resultsOutputDir, null,
                null, null, 200L, 33);

        Class<?> lfsClass = LifelongSolversFactory.class;
        Method[] solverBuilders = lfsClass.getDeclaredMethods();
        Arrays.sort(solverBuilders, Comparator.comparing(Method::getName));
        System.out.println("Found " + solverBuilders.length + " solver builders");
        List<I_Solver> solvers = new LinkedList<>();
        for (int i = 0; i < solverBuilders.length; i++) {
            Method method = solverBuilders[i];
            Object res;
            try {
                res = method.invoke(null);
                System.out.println(method.getName() + "(number " + i + " of " + solverBuilders.length + ")");
                if (res instanceof I_Solver) {
                    solvers.add((I_Solver) res);
                }
            } catch (Exception e) {
                fail("Failed to invoke method " + method.getName() + " in LifelongSolversFactory \n" + Arrays.toString(e.getStackTrace()));
            }
        }
        lifelongRunManager.overrideSolvers(solvers);
        boolean allSolutionsValid = lifelongRunManager.runAllExperiments();
        assertTrue(allSolutionsValid);
    }
}