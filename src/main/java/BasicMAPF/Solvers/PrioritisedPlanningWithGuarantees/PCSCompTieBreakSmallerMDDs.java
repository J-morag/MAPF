package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.DataTypesAndStructures.BucketingComparator;

public class PCSCompTieBreakSmallerMDDs implements BucketingComparator<PCSNode> {

    public static final PCSCompTieBreakSmallerMDDs defaultInstance = new PCSCompTieBreakSmallerMDDs();

    @Override
    public int compare(PCSNode o1, PCSNode o2) {
        int res = bucketCompare(o1, o2); // f
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

    @Override
    public int getBucket(PCSNode pcsNode) {
        return pcsNode.getF();
    }
}
