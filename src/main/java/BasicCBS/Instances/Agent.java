package BasicCBS.Instances;

import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;

import java.util.Objects;

public class Agent {

    public final int iD;
    public final I_Coordinate source;
    public final I_Coordinate target;




    public Agent(int iD, I_Coordinate source, I_Coordinate target) {
        this.iD = iD;
        this.source = source;
        this.target = target;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent)) return false;

        Agent agent = (Agent) o;

        if (iD != agent.iD) return false;
        if (!source.equals(agent.source)) return false;
        return target.equals(agent.target);
    }

    @Override
    public int hashCode() {
        int result = iD;
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "iD=" + iD +
                '}';
    }
}
