package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.Solvers.I_Solver;
import TransientMAPF.TransientMAPFSettings;

import java.util.List;

public class LNSBuilder {
    private I_SolutionCostFunction solutionCostFunction = null;
    private List<I_DestroyHeuristic> destroyHeuristics = null;
    private Boolean sharedGoals = null;
    private Boolean sharedSources = null;
    private Double reactionFactor = null;
    private Integer neighborhoodSize = null;
    private I_Solver initialSolver;
    private I_Solver iterationsSolver;
    private TransientMAPFSettings transientMAPFSettings = null;
    private boolean LNS2;
    private Integer RHCR_Horizon;

    public LNSBuilder setSolutionCostFunction(I_SolutionCostFunction solutionCostFunction) {
        this.solutionCostFunction = solutionCostFunction;
        return this;
    }

    public LNSBuilder setDestroyHeuristics(List<I_DestroyHeuristic> destroyHeuristics) {
        this.destroyHeuristics = destroyHeuristics;
        return this;
    }

    public LNSBuilder setSharedGoals(Boolean sharedGoals) {
        this.sharedGoals = sharedGoals;
        return this;
    }

    public LNSBuilder setSharedSources(Boolean sharedSources) {
        this.sharedSources = sharedSources;
        return this;
    }

    public LNSBuilder setReactionFactor(Double reactionFactor) {
        this.reactionFactor = reactionFactor;
        return this;
    }

    public LNSBuilder setNeighborhoodSize(Integer neighborhoodSize) {
        this.neighborhoodSize = neighborhoodSize;
        return this;
    }

    public LNSBuilder setTransientMAPFBehaviour(TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = transientMAPFSettings;
        return this;
    }

    public LNSBuilder setInitialSolver(I_Solver initialSolver) {
        this.initialSolver = initialSolver;
        return this;
    }

    public LNSBuilder setIterationsSolver(I_Solver iterationsSolver) {
        this.iterationsSolver = iterationsSolver;
        return this;
    }

    public LNSBuilder setLNS2(Boolean LNS2) {
        this.LNS2 = LNS2;
        return this;
    }

    public LNSBuilder setRHCR_Horizon(Integer RHCR_Horizon) {
        this.RHCR_Horizon = RHCR_Horizon;
        return this;
    }

    public LargeNeighborhoodSearch_Solver createLNS() {
        return new LargeNeighborhoodSearch_Solver(solutionCostFunction, destroyHeuristics, sharedGoals, sharedSources, reactionFactor, neighborhoodSize, initialSolver, iterationsSolver, transientMAPFSettings, LNS2, RHCR_Horizon);
    }
}