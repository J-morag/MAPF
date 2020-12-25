package BasicCBS.Solvers.ICTS.HighLevel;

import java.util.Comparator;

abstract public class ICT_NodeComparator implements Comparator<ICT_Node> {
    @Override
    public int compare(ICT_Node first, ICT_Node second) {
        return Integer.compare(costFunction(first), costFunction(second));
    }

    protected abstract int costFunction(ICT_Node node);
}
