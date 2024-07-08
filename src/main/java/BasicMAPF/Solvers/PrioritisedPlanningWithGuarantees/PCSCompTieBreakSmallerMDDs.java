package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import java.util.Comparator;

public class PCSCompTieBreakSmallerMDDs implements Comparator<PCSNode> {
    @Override
    public int compare(PCSNode o1, PCSNode o2) {
        int res = Integer.compare(o1.getF(), o2.getF());
        if (res != 0){
            return res;
        }
        res = Integer.compare(o2.g(), o1.g()); // reversed to prefer higher g
        if (res != 0){
            return res;
        }
        res = Integer.compare(o1.getSumMddSizes(), o2.getSumMddSizes());
        if (res != 0){
            return res;
        }
        return Integer.compare(o1.uniqueID(), o2.uniqueID());
    }
}
