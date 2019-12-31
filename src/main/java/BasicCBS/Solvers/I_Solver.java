package BasicCBS.Solvers;

import BasicCBS.Instances.MAPF_Instance;

/**
 * A class that implements {@link I_Solver} is called a solver.
 *
 * A solver is a class that solves {@link MAPF_Instance problem instances}. These are most commonly multi agent problems.
 * Single agent solvers will also implement this interface, but will only consider the first agent in the instance.
 *
 * BasicCBS.Solvers should not keep a persistent internal state. This means that while the control flow is in {@link #solve(MAPF_Instance, RunParameters) solve},
 * the solver can keep information in internal fields, but before solve returns, the solver should clear all internal
 * fields. This is to keep successive runs of the same solver independent from each other.
 *
 * In the event that it is useful for a solver to keep its state after a run (after solve is called and returns), then
 * such behaviour should be explicitly evoked, and well documented.
 * e.g. a boolean passed to the constructor of the class, where one value indicates keeping a persistent state, and the
 * other indicates clearing the solver's state before returning.
 */
public interface I_Solver {
    /**
     * Solve a {@link MAPF_Instance problem instance}.
     * @param instance a problem instance to solve.
     * @param parameters parameters that expand upon the problem instance or change the solver's behaviour for this specific
     *                   run.
     * @return a {@link Solution} to the given problem, or null if a timeout occurred before it was solved.
     */
    Solution solve(MAPF_Instance instance, RunParameters parameters);

    /**
     * returns a uniquely identifying name for the solver.
     * @return a uniquely identifying name for the solver.
     */
    String name();
}
