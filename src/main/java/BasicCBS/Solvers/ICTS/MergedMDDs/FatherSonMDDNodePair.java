package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Solvers.ICTS.MDDs.MDDNode;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FatherSonMDDNodePair that = (FatherSonMDDNodePair) o;
        return father.equals(that.father) &&
                son.equals(that.son);
    }

    @Override
    public int hashCode() {
        return Objects.hash(father, son);
    }

    public boolean colliding(FatherSonMDDNodePair other){
        return sonCollision(other) || edgeCollision(other);
    }

    private boolean edgeCollision(FatherSonMDDNodePair other) {
        return this.son.sameLocation(other.father) && this.father.sameLocation(other.son);
    }

    private boolean sonCollision(FatherSonMDDNodePair other) {
        return this.son.sameLocation(other.son);
    }
}
