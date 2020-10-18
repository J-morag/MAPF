package BasicCBS.Instances.InstanceBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Assigns priorities to agents
 */
public class Priorities {

    private PrioritiesPolicy policy;
    private final int[] priorities;
    private Integer[] prioritiesImbalanced;

    public Priorities() {
        this.priorities = new int[0];
        setPolicy(PrioritiesPolicy.NO_PRIORITIES, this.priorities);
    }

    public Priorities(PrioritiesPolicy policy, int[] priorities) {
        if (policy == null || priorities == null){
            this.priorities = new int[0];
            setPolicy(PrioritiesPolicy.NO_PRIORITIES, this.priorities);
        }
        else{
            this.priorities = priorities;
            Arrays.sort(this.priorities); // sort ascending
            setPolicy(policy, this.priorities);
        }
    }

    public PrioritiesPolicy getPolicy() {
        return policy;
    }

    public int[] getPriorities() {
        return Arrays.copyOf(priorities, priorities.length);
    }

    private void setPolicy(PrioritiesPolicy policy, int[] priorities){
        if (policy == PrioritiesPolicy.FOUR_TO_ONE_ROBIN){
            List<Integer> newPrioritiesImbalanced = new ArrayList<>();
            int duplications = 1;
            for (int priorityIndex = priorities.length - 1 ; priorityIndex >= 0 ; priorityIndex--) {
                int priority = priorities[priorityIndex];
                // starts with last priority, and increases the number of of duplications as we go to towards the first priority
                for (int i = 0; i < duplications; i++) {
                    // insert at tail
                    newPrioritiesImbalanced.add(0, priority);
                }
                duplications *= 4;
            }
            this.prioritiesImbalanced = newPrioritiesImbalanced.toArray(new Integer[0]);
        }

        this.policy = policy;
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
            return priorities[getRoundRobinIndex(agentIndex, numPriorities)];
        }
        else if (policy == PrioritiesPolicy.FOUR_TO_ONE_ROBIN){
            // like round robbin, but from prioritiesImbalanced
            return prioritiesImbalanced[getRoundRobinIndex(agentIndex, prioritiesImbalanced.length)];
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

    private int getRoundRobinIndex(int agentIndex, int numPriorities) {
        return agentIndex - (Math.floorDiv(agentIndex, numPriorities)) * numPriorities;
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
        ROUND_ROBIN,
        /**
         * Like round robin, but for every four of the previous priority, one of the next.
         */
        FOUR_TO_ONE_ROBIN
    }

}
