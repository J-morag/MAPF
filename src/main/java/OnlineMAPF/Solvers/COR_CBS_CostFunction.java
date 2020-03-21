package OnlineMAPF.Solvers;

import BasicCBS.Solvers.CBS.CBS_Solver;
import BasicCBS.Solvers.Solution;
import OnlineMAPF.OnlineSolution;

/**
 * A cost function for CBS which includes the cost of rerouting agents.
 */
public class COR_CBS_CostFunction implements CBS_Solver.CBSCostFunction {

    private final int costOfReroute;
    private final Solution exitingSolution;

    public COR_CBS_CostFunction(int costOfReroute, Solution exitingSolution) {
        this.costOfReroute = costOfReroute;
        this.exitingSolution = exitingSolution;
    }

    /**
     * A cost function that combines Sum Of individual Costs with Cost Of Reroute.
     * @param solution a new solution being considered at some time.
     * @param cbs {@inheritDoc}
     * @return the cost of the solution with SOC and COR.
     */
    @Override
    public float solutionCost(Solution solution, CBS_Solver cbs) {
        int costOfReroutes = exitingSolution != null ? OnlineSolution.costOfReroutes(exitingSolution, solution, costOfReroute) : 0;
        return solution.sumIndividualCosts() + costOfReroutes;
    }
}
