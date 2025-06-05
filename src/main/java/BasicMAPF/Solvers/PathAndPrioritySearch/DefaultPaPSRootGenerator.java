package BasicMAPF.Solvers.PathAndPrioritySearch;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import Environment.Config;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultPaPSRootGenerator implements PaPSRootGenerator {
    @Override
    public void generateRoot(MAPF_Instance instance, RunParameters runParameters, PathAndPrioritySearch pcs, Timeout timeout) {
        pcs.generateRoot(getPriorityOrderedAgents(instance, runParameters));
    }

    private Agent @NotNull [] getPriorityOrderedAgents(@NotNull MAPF_Instance instance,RunParameters runParameters) {
        Agent[] priorityOrderedAgents = runParameters.priorityOrder != null ? runParameters.priorityOrder :
                instance.agents.toArray(new Agent[0]);
        if (priorityOrderedAgents.length != instance.agents.size()){
            throw new IllegalArgumentException("Priority order array must cover exactly the same set of agents that exist in the instance");
        }
        if (Config.DEBUG >= 2 &&  ! Set.of(priorityOrderedAgents).equals(new HashSet<>(instance.agents))){
            throw new IllegalArgumentException("Priority order array must cover exactly the same set of agents that exist in the instance: \n" +
                    Arrays.toString(priorityOrderedAgents) + "\nvs\n" + instance.agents);
        }
        return priorityOrderedAgents;
    }
}
