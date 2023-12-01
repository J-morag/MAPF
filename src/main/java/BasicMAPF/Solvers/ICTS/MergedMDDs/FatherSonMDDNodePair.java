package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.DataTypesAndStructures.MDDs.MDDNode;

public class FatherSonMDDNodePair{
    private MDDNode father;
    private MDDNode son;

    public FatherSonMDDNodePair(MDDNode father, MDDNode son) {
        this.father = father;
        this.son = son;
    }

    public MDDNode getFather() {
        return father;
    }

    public MDDNode getSon() {
        return son;
    }

    public void set(MDDNode father, MDDNode son) {
        this.father = father;
        this.son = son;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FatherSonMDDNodePair)) return false;

        FatherSonMDDNodePair that = (FatherSonMDDNodePair) o;

        if (!father.equals(that.father)) return false;
        return son.equals(that.son);
    }

    @Override
    public int hashCode() {
        int result = father.hashCode();
        result = 31 * result + son.hashCode();
        return result;
    }

    public boolean colliding(FatherSonMDDNodePair other, boolean disappearAtGoal){
        return sonCollision(other, disappearAtGoal) || edgeCollision(other, disappearAtGoal);
    }

    private boolean edgeCollision(FatherSonMDDNodePair other, boolean disappearAtGoal) {
        if (disappearAtGoal && (isStayAtGoal(this) || isStayAtGoal(other))){
            return false;
        }
        else{
            return this.son.sameLocation(other.father) && this.father.sameLocation(other.son);
        }
    }

    private boolean sonCollision(FatherSonMDDNodePair other, boolean disappearAtGoal) {
        if (disappearAtGoal && (isStayAtGoal(this) || isStayAtGoal(other))){
            return false;
        }
        else{
            return this.son.sameLocation(other.son);
        }
    }

    private boolean isStayAtGoal(FatherSonMDDNodePair other) {
        return other.father.equals(other.son);
    }
}
