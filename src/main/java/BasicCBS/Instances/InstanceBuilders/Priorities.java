package BasicCBS.Instances.InstanceBuilders;

import java.util.Arrays;

/**
 * Assigns priorities to agents
 */
public class Priorities {

    private final PrioritiesPolicy policy;
    private final int[] priorities;

    public Priorities() {
        this.policy = PrioritiesPolicy.NO_PRIORITIES;
        this.priorities = new int[0];
    }

    public Priorities(PrioritiesPolicy policy, int[] priorities) {
        if (policy == null || priorities == null){
            this.policy = PrioritiesPolicy.NO_PRIORITIES;
            this.priorities = new int[0];
        }
        else{
            this.policy = policy;
            this.priorities = priorities;
            Arrays.sort(this.priorities); // sort ascending
        }
    }

    /**
     * returns the priority that should be assigned to an agent according to its index out of the total number of agents.
     * @param agentIndex the index of the agent out of all agents. should be 0-indexed.
     * @param numAgents the number of agents expected to be in an instance.
     * @return the priority that should be assigned to an agent according to its index out of the total number of agents.
     */
    public int getPriorityForAgent(int agentIndex, int numAgents){
        int numPriorities = priorities.length;
        if (policy == PrioritiesPolicy.ROUND_ROBIN){
            return priorities[agentIndex - (agentIndex % numPriorities) * numPriorities];
        }
        else if (policy == PrioritiesPolicy.HEAVY_FIRST || policy == PrioritiesPolicy.LIGHT_FIRST){
            int agentsPerPriority = Math.floorDiv(numAgents, numPriorities);
            // if the number of agents does not divide evenly between the priorities, then the last priority will get the remainder
            int priorityIndex = Math.min(Math.floorDiv(agentIndex, agentsPerPriority), numPriorities - 1);
            return priorities[policy == PrioritiesPolicy.LIGHT_FIRST ? priorityIndex : numPriorities - 1 - priorityIndex];
        }
        else { // NO_PRIORITIES
            return 1;
        }
    }

    public enum PrioritiesPolicy{
        /**
         * All agents will be assigned priority=1.
         */
        NO_PRIORITIES,
        /**
         * Will try to divide agents evenly between priorities, putting heavier (higher) priorities first.
         */
        HEAVY_FIRST,
        /**
         * Will try to divide agents evenly between priorities, putting lighter (lower) priorities first.
         */
        LIGHT_FIRST,
        /**
         * Will try to divide agents evenly between priorities alternating between them as round robin.
         */
        ROUND_ROBIN
    }

}
