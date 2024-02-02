package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.MDDs.I_MDDSearcherFactory;
import BasicMAPF.DataTypesAndStructures.I_OpenList;

import java.util.Comparator;

public class PCSBuilder {
    private I_OpenList<PCSNode> openList;
    private Comparator<? super PCSNode> nodeComparator;
    private I_MDDSearcherFactory mddSearcherFactory;

    public PCSBuilder setOpenList(I_OpenList<PCSNode> openList) {
        this.openList = openList;
        return this;
    }

    public PCSBuilder setNodeComparator(Comparator<? super PCSNode> nodeComparator) {
        this.nodeComparator = nodeComparator;
        return this;
    }

    public PCSBuilder setMddSearcherFactory(I_MDDSearcherFactory mddSearcherFactory) {
        this.mddSearcherFactory = mddSearcherFactory;
        return this;
    }

    public PriorityConstrainedSearch createPriorityConstrainedSearch() {
        return new PriorityConstrainedSearch(openList, nodeComparator, mddSearcherFactory);
    }
}