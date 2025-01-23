package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.MDDs.I_MDDSearcherFactory;
import BasicMAPF.DataTypesAndStructures.I_OpenList;

import java.util.Comparator;

public class PCSBuilder {
    private I_OpenList<PCSNode> openList;
    private Comparator<PCSNode> nodeComparator;
    private I_MDDSearcherFactory mddSearcherFactory;
    private Boolean useSimpleMDDCache;
    private Integer MDDCacheDepthDeltaMax;
    private Boolean usePartialGeneration;
    private I_PCSHeuristic PCSHeuristic;

    public PCSBuilder setOpenList(I_OpenList<PCSNode> openList) {
        this.openList = openList;
        return this;
    }

    public PCSBuilder setNodeComparator(Comparator<PCSNode> nodeComparator) {
        this.nodeComparator = nodeComparator;
        return this;
    }

    public PCSBuilder setMddSearcherFactory(I_MDDSearcherFactory mddSearcherFactory) {
        this.mddSearcherFactory = mddSearcherFactory;
        return this;
    }

    public PCSBuilder setUseSimpleMDDCache(boolean useSimpleMDDCache) {
        this.useSimpleMDDCache = useSimpleMDDCache;
        return this;
    }

    public PCSBuilder setUsePartialGeneration(boolean usePartialGeneration) {
        this.usePartialGeneration = usePartialGeneration;
        return this;
    }

    public PCSBuilder setMDDCacheDepthDeltaMax(int MDDCacheDepthDeltaMax) {
        this.MDDCacheDepthDeltaMax = MDDCacheDepthDeltaMax;
        return this;
    }

    public PCSBuilder setPCSHeuristic(I_PCSHeuristic PCSHeuristic) {
        this.PCSHeuristic = PCSHeuristic;
        return this;
    }

    public PriorityConstrainedSearch createPCS() {
        return new PriorityConstrainedSearch(openList, nodeComparator, mddSearcherFactory, useSimpleMDDCache, MDDCacheDepthDeltaMax, usePartialGeneration, PCSHeuristic);
    }
}