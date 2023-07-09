package BasicMAPF.Instances.Maps;

import BasicMAPF.DataTypesAndStructures.Move;
import org.jetbrains.annotations.NotNull;

/**
 * Data type for describing an edge between two locations.
 */
public class Edge {

    public final I_Location from;
    public final I_Location to;

    public Edge(@NotNull I_Location from, @NotNull I_Location to) {
        this.from = from;
        this.to = to;
    }

    public Edge(Move move){
        this(move.prevLocation, move.currLocation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge edge)) return false;

        if (!from.equals(edge.from)) return false;
        return to.equals(edge.to);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "" + from.getCoordinate() +
                "," + to.getCoordinate() +
                '}';
    }
}
