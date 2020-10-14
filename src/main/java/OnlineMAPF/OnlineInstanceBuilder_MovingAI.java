package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.InstanceBuilders.Priorities;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Map;

public class OnlineInstanceBuilder_MovingAI extends InstanceBuilder_MovingAI {

    protected final int INDEX_AGENT_ARRIVAL_TIME = 9;
    private final String onlineFieldsFilenameDelimiter = "#";

    public OnlineInstanceBuilder_MovingAI() {
        super();
    }

    public OnlineInstanceBuilder_MovingAI(Priorities priorities) {
        super(priorities);
    }

    @Override
    protected Agent agentFromStringArray(int id, String[] agentLine, int numAgents) {
        Agent offlineAgent = super.agentFromStringArray(id, agentLine, numAgents);

        return new OnlineAgent(offlineAgent,
                Integer.parseInt(agentLine[INDEX_AGENT_ARRIVAL_TIME]) // add the agent's arrival time
        );

    }

    @Override
    protected MAPF_Instance makeInstance(String instanceName, I_Map graphMap, Agent[] agents, InstanceManager.Moving_AI_Path instancePath) {
        MAPF_Instance offlineInstance = super.makeInstance(instanceName, graphMap, agents, instancePath);
        String scenarioName = instancePath.scenarioPath;
        // trim to the portion where relevant fields are
        String[] splitScenarioName = scenarioName.split(onlineFieldsFilenameDelimiter);
        if(splitScenarioName.length == 1){ // no online descriptors
            return new OnlineMAPF_Instance(offlineInstance, null, null, null);

        }
        scenarioName = splitScenarioName[1];
        // split into fields
        String[] fields = scenarioName.split("_");
        return new OnlineMAPF_Instance(offlineInstance, fields[0], fields[1], fields[2]);
    }
}
