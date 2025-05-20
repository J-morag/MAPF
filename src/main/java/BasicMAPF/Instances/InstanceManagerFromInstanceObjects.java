package BasicMAPF.Instances;

import java.util.List;

public class InstanceManagerFromInstanceObjects implements InstanceManager {
    private final List<MAPF_Instance> instances;
    private int currentInstanceIndex = 0;

    public InstanceManagerFromInstanceObjects(List<MAPF_Instance> instances) {
        this.instances = instances;
    }
    public InstanceManagerFromInstanceObjects(MAPF_Instance... instances) {
        this(List.of(instances));
    }

    @Override
    public MAPF_Instance getNextInstance() {
        if (currentInstanceIndex >= instances.size()) {
            return null; // No more instances available
        }
        return instances.get(currentInstanceIndex++);
    }

    @Override
    public void resetIndex() {
        currentInstanceIndex = 0;
    }
}
