package LifelongMAPF;

import BasicMAPF.Solvers.I_Solver;

/**
 * Contract indicating compatibility as an offline solver for use in lifelong problems.
 * An implementing solver should not ignore the {@link BasicMAPF.DataTypesAndStructures.RunParameters#problemStartTime} field.
 */
public interface I_LifelongCompatibleSolver extends I_Solver {
    boolean ignoresStayAtSharedSources();
    boolean ignoresStayAtSharedGoals();
    boolean handlesSharedTargets();
}
