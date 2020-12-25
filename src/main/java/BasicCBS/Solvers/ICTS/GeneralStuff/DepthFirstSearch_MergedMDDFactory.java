package BasicCBS.Solvers.ICTS.GeneralStuff;

import java.util.*;

public class DepthFirstSearch_MergedMDDFactory extends SearchBased_MergedMDDFactory {
    private Map<MergedMDDNode, MergedMDDNode> contentOfOpen;
    private Stack<MergedMDDNode> openList;
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
        } else if (!closedList.contains(node)) {
            openList.push(node);
            contentOfOpen.put(node, node);
        }
    }

    @Override
    protected MergedMDDNode pollFromOpen() {
        MergedMDDNode next = openList.pop();
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
    protected Stack<MergedMDDNode> createNewOpenList() {
        return new Stack<>();
    }
}
