package BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures;

import BasicCBS.Instances.Agent;

import java.util.Objects;

public class AgentAtGoal{
    public final Agent agent;
    public final int time;

    /**
     *
     * @param agent - {@inheritDoc}
     * @param time - An int of the time unit in the solution
     */
    public AgentAtGoal(Agent agent, int time) {
        this.agent = agent;
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentAtGoal)) return false;

        AgentAtGoal that = (AgentAtGoal) o;

        if (time != that.time) return false;
        return agent.equals(that.agent);
    }

    @Override
    public int hashCode() {
        int result = agent.hashCode();
        result = 31 * result + time;
        return result;
    }
}
