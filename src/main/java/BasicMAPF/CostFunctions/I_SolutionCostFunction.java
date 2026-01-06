package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Config;
import Environment.Metrics.InstanceReport;

public interface I_SolutionCostFunction {

    int solutionCost(Solution solution);

    String name();

    static void addCommonCostsToReport(Solution solution, InstanceReport report){
        report.putIntegerValue(SumOfCosts.NAME, SumOfCosts.instance.solutionCost(solution));
        report.putIntegerValue(Makespan.NAME, Makespan.instance.solutionCost(solution));
        report.putIntegerValue(SumServiceTimes.NAME, SumServiceTimes.instance.solutionCost(solution));
        report.putIntegerValue(MakespanServiceTime.NAME, MakespanServiceTime.instance.solutionCost(solution));
        if (Config.Misc.RECORD_SOLUTION_AGENT_COSTS_STRING){
            report.putStringValue(PathCosts.NAME, PathCosts.instance.getPathCostsString(solution));
            report.putStringValue(PathDelays.NAME, PathDelays.instance.getPathDelaysString(solution, report));
        }
    }

}
