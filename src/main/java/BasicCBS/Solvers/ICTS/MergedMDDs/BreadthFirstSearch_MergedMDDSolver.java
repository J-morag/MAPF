package BasicCBS.Solvers.ICTS.MergedMDDs;

import java.util.*;

/**
 * Implements a BFS Factory for Merged MDD.
 * We don't need a closed set because MDDs are DAGS. (no cycles...)
 */
public class BreadthFirstSearch_MergedMDDSolver extends SearchBased_MergedMDDSolver {

    private Map<MergedMDDNode, MergedMDDNode> contentOfOpen;
    private Queue<MergedMDDNode> openList;
    /**
     * We implement a closed list only for being able to say that we have an error, and when we realize that the MDD is not a DAG
     */
    private Set<MergedMDDNode> closedList;

    @Override
    protected void initializeSearch() {
        openList = createNewOpenList();
        contentOfOpen = new HashMap<>();
        closedList = createNewClosedList();
    }

    @Override
    protected boolean isOpenEmpty() {
        return openList.isEmpty();
    }

    @Override
    protected void addToClosed(MergedMDDNode node) {
        closedList.add(node);
    }

    @Override
    protected void addToOpen(MergedMDDNode node) {
        if (contentOfOpen.containsKey(node)) {
            MergedMDDNode inOpen = contentOfOpen.get(node);
            inOpen.addParents(node.getParents());
            for (MergedMDDNode parent : node.getParents()) {
                parent.fixNeighbor(inOpen);
            }
        } else if (closedList.contains(node)) {
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
}
