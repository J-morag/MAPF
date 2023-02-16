package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures;

import BasicMAPF.Instances.Agent;

public class AgentAtGoal{

    public Agent agent;
    public int time;

    /**
     *
     * @param agent - {@inheritDoc}
     * @param time - An int of the time unit in the solution
     */
    public AgentAtGoal(Agent agent, int time) {
        this.agent = agent;
        this.time = time;
    }

    public AgentAtGoal setTo(Agent agent, int time){
        this.agent = agent;
        this.time = time;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgentAtGoal that)) return false;

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
