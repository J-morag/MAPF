package BasicCBS.Solvers.ICTS.MergedMDDs;

import java.util.*;

/**
 * Implements a BFS Factory for Merged MDD.
 * We don't need a closed set because MDDs are DAGS. (no cycles...)
 */
public class BreadthFirstSearch_MergedMDDCreator extends SearchBased_MergedMDDCreator {

    private Queue<MergedMDDNode> openList;
    /**
     * We implement a closed list only for being able to say that we have an error, and when we realize that the MDD is not a DAG
     */
    private Set<MergedMDDNode> closedList;
    private boolean debug = false;

    @Override
    protected void initializeSearch() {
        super.initializeSearch();
        openList = createNewOpenList();
        if (debug) closedList = createNewClosedList();
    }

    @Override
    protected boolean isOpenEmpty() {
        return openList.isEmpty();
    }

    @Override
    protected void addToClosed(MergedMDDNode node) {
        if (debug) closedList.add(node);
    }

    @Override
    protected void addToOpen(MergedMDDNode node) {
        MergedMDDNode inOpen = contentOfOpen.get(node);
        if (inOpen != null) {
            inOpen.addParents(node.getParents());
            for (MergedMDDNode parent : node.getParents()) {
                parent.fixNeighbor(inOpen);
            }
        } else if (debug && closedList.contains(node)) {
            try {
                throw new Exception("The MDD supposed to be DAG, but we now found a cyclic path");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            openList.add(node);
            contentOfOpen.put(node, node);
        }
    }

    @Override
    protected MergedMDDNode pollFromOpen() {
        MergedMDDNode next = openList.poll();
        contentOfOpen.remove(next);
        return next;
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        openList = null;
        closedList = null;
    }

    /**
     * Can override this method to create your own search based Factory
     *
     * @return new closed list
     */
    protected Set<MergedMDDNode> createNewClosedList() {
        return new HashSet<>();
    }

    /**
     * Can override this method to create your own search based Factory
     *
     * @return new open list
     */
    protected Queue<MergedMDDNode> createNewOpenList() {
        return new ArrayDeque<>();
    }

    @Override
    protected int openSize() {
        return this.openList.size();
    }
}
