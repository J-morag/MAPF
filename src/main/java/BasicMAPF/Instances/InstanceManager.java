package BasicMAPF.Instances;

public interface InstanceManager {
    MAPF_Instance getNextInstance();

    void resetIndex();
}
