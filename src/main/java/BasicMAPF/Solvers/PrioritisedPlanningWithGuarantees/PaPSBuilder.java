package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.MDDs.I_MDDSearcherFactory;
import BasicMAPF.DataTypesAndStructures.I_OpenList;

import java.util.Comparator;

public class PaPSBuilder {
    private I_OpenList<PaPSNode> openList;
    private Comparator<PaPSNode> nodeComparator;
    private I_MDDSearcherFactory mddSearcherFactory;
    private Boolean useSimpleMDDCache;
    private Integer MDDCacheDepthDeltaMax;
    private I_PaPSHeuristic PCSHeuristic;
    private PaPSRootGenerator rootGenerator;
    private Boolean useDuplicateDetection;
    private Boolean noAgentsSplit;

    public PaPSBuilder setOpenList(I_OpenList<PaPSNode> openList) {
        this.openList = openList;
        return this;
    }

    public PaPSBuilder setNodeComparator(Comparator<PaPSNode> nodeComparator) {
        this.nodeComparator = nodeComparator;
        return this;
    }

    public PaPSBuilder setMddSearcherFactory(I_MDDSearcherFactory mddSearcherFactory) {
        this.mddSearcherFactory = mddSearcherFactory;
        return this;
    }

    public PaPSBuilder setUseSimpleMDDCache(boolean useSimpleMDDCache) {
        this.useSimpleMDDCache = useSimpleMDDCache;
        return this;
    }

    public PaPSBuilder setMDDCacheDepthDeltaMax(int MDDCacheDepthDeltaMax) {
        this.MDDCacheDepthDeltaMax = MDDCacheDepthDeltaMax;
        return this;
    }

    public PaPSBuilder setPaPSHeuristic(I_PaPSHeuristic PCSHeuristic) {
        this.PCSHeuristic = PCSHeuristic;
        return this;
    }

    public PaPSBuilder setRootGenerator(PaPSRootGenerator rootGenerator) {
        this.rootGenerator = rootGenerator;
        return this;
    }

    public PaPSBuilder setUseDuplicateDetection(Boolean useDuplicateDetection) {
        this.useDuplicateDetection = useDuplicateDetection;
        return this;
    }

    /**
     * If set to false, will not split on agents, resulting in PCS-like behavior
     */
    public PaPSBuilder setNoAgentsSplit(Boolean noAgentsSplit) {
        this.noAgentsSplit = noAgentsSplit;
        return this;
    }

    public PathAndPrioritySearch createPaPS() {
        return new PathAndPrioritySearch(openList, nodeComparator, mddSearcherFactory, useSimpleMDDCache, MDDCacheDepthDeltaMax, PCSHeuristic, rootGenerator, useDuplicateDetection, noAgentsSplit);
    }
}