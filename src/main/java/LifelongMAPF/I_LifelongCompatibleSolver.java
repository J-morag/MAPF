package LifelongMAPF;

import BasicMAPF.Solvers.I_Solver;

/**
 * Contract indicating compatibility as an offline solver for use in lifelong problems.
 * An implementing solver should not ignore the {@link BasicMAPF.Solvers.RunParameters#problemStartTime} field.
 */
public interface I_LifelongCompatibleSolver extends I_Solver {
    boolean sharedSources();
    boolean sharedGoals();
}
