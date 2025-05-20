package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

public class ConflictsCount implements I_SolutionCostFunction{

    public static final String NAME = "ConflictsCount";
    public boolean sharedGoals;
    public boolean sharedSources;

    public ConflictsCount(boolean sharedGoals, boolean sharedSources) {
        this.sharedGoals = sharedGoals;
        this.sharedSources = sharedSources;
    }
    @Override
    public int solutionCost(Solution solution) {
        return solution.countConflicts(this.sharedGoals, this.sharedGoals);
    }

    @Override
    public String name() {  return NAME;  }
}
