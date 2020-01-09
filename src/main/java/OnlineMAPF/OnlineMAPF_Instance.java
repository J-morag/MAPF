package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Map;

public class OnlineMAPF_Instance extends MAPF_Instance {

    private static final String DEFAULT_VALUE = "unknown";

    public final String agentSelection;
    public final String arrivalDistribution;
    public final String arrivalRate;

    public OnlineMAPF_Instance(String name, I_Map map, Agent[] agents, String agentSelection, String arrivalDistribution, String arrivalRate) {
        super(name, map, agents);
        this.agentSelection = agentSelection != null ? agentSelection : DEFAULT_VALUE;
        this.arrivalDistribution = arrivalDistribution != null ? arrivalDistribution : DEFAULT_VALUE;
        this.arrivalRate = arrivalRate != null ? arrivalRate : DEFAULT_VALUE;
    }

    public OnlineMAPF_Instance(MAPF_Instance offlineInstance, String agentSelection, String arrivalDistribution, String arrivalRate) {
        this(offlineInstance.name, offlineInstance.map, offlineInstance.agents.toArray(Agent[]::new), agentSelection,
                arrivalDistribution, arrivalRate);
    }
}
