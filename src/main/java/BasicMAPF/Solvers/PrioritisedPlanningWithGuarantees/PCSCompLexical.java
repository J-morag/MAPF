package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import Environment.Config;

import java.util.Comparator;

public class PCSCompLexical implements Comparator<PCSNode> {

    public static final PCSCompLexical DEFAULT_INSTANCE = new PCSCompLexical();

    @Override
    public int compare(PCSNode o1, PCSNode o2) {
        // todo throw an exception if the orderings are different
        int res;
        for (int i = 0; i < Math.min(o1.MDDs().size(), o2.MDDs().size()); i++) {
            res = Integer.compare(o1.MDDs().get(i).getDepth(), o2.MDDs().get(i).getDepth());
            if (res != 0) {
                return res;
            }
        }
        // todo keep going through all agents, including those that have no MDDs yet, using the heuristic array in their stead.
        res = Integer.compare(o1.MDDs().size(), o2.MDDs().size());
        if (res != 0) {
            return res;
        }
        res = Integer.compare(o1.getSumMddSizes(), o2.getSumMddSizes());
        if (res != 0){
            return res;
        }
        return Integer.compare(o1.uniqueID(), o2.uniqueID());
    }
}
