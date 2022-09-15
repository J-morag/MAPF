package BasicMAPF.Instances;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.jetbrains.annotations.NotNull;

public class Agent implements Comparable<Agent>{

    public final int iD;
    public final I_Coordinate source;
    public final I_Coordinate target;
    public final int priority;

    public Agent(int iD, I_Coordinate source, I_Coordinate target) {
        this.iD = iD;
        this.source = source;
        this.target = target;
        this.priority = 1;
    }

    public Agent(int iD, I_Coordinate source, I_Coordinate target, int priority) {
        this.iD = iD;
        this.source = source;
        this.target = target;
        this.priority = priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent)) return false;

        Agent agent = (Agent) o;

        return iD == agent.iD;
    }

    @Override
    public int hashCode() {
        return iD;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "iD=" + iD +
                ", source=" + source +
                ", target=" + target +
                '}';
    }

    @Override
    public int compareTo(@NotNull Agent o) {
        return Integer.compare(this.iD, o.iD);
    }
}
