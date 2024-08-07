package BasicMAPF.Solvers.CBS;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.DataTypesAndStructures.I_OpenList;
import BasicMAPF.Solvers.I_Solver;
import TransientMAPF.TransientMAPFSettings;

import java.util.Comparator;

public class CBSBuilder {
    private I_Solver lowLevelSolver = null;
    private I_OpenList<CBS_Solver.CBS_Node> openList = null;
    private CBS_Solver.OpenListManagementMode openListManagementMode = null;
    private I_SolutionCostFunction costFunction = null;
    private Comparator<? super CBS_Solver.CBS_Node> cbsNodeComparator = null;
    private Boolean useCorridorReasoning = null;
    private Boolean sharedGoals = null;
    private Boolean sharedSources = null;
    private TransientMAPFSettings transientMAPFSettings = null;

    public CBSBuilder setLowLevelSolver(I_Solver lowLevelSolver) {
        this.lowLevelSolver = lowLevelSolver;
        return this;
    }

    public CBSBuilder setOpenList(I_OpenList<CBS_Solver.CBS_Node> openList) {
        this.openList = openList;
        return this;
    }

    public CBSBuilder setOpenListManagementMode(CBS_Solver.OpenListManagementMode openListManagementMode) {
        this.openListManagementMode = openListManagementMode;
        return this;
    }

    public CBSBuilder setCostFunction(I_SolutionCostFunction costFunction) {
        this.costFunction = costFunction;
        return this;
    }

    public CBSBuilder setCbsNodeComparator(Comparator<? super CBS_Solver.CBS_Node> cbsNodeComparator) {
        this.cbsNodeComparator = cbsNodeComparator;
        return this;
    }

    public CBSBuilder setUseCorridorReasoning(Boolean useCorridorReasoning) {
        this.useCorridorReasoning = useCorridorReasoning;
        return this;
    }

    public CBSBuilder setSharedGoals(Boolean sharedGoals) {
        this.sharedGoals = sharedGoals;
        return this;
    }

    public CBSBuilder setSharedSources(Boolean sharedSources) {
        this.sharedSources = sharedSources;
        return this;
    }

    public CBSBuilder setTransientMAPFSettings(TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = transientMAPFSettings;
        return this;
    }

    public CBS_Solver createCBS_Solver() {
        return new CBS_Solver(lowLevelSolver, openList, openListManagementMode, costFunction, cbsNodeComparator, useCorridorReasoning, sharedGoals, sharedSources, transientMAPFSettings);
    }
}