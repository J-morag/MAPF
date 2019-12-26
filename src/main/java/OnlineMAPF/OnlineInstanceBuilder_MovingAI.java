package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Map;

public class OnlineInstanceBuilder_MovingAI extends InstanceBuilder_MovingAI {

    protected final int INDEX_AGENT_ARRIVAL_TIME = 9;

    @Override
    protected Agent agentFromStringArray(int id, String[] agentLine) {
        Agent offlineAgent = super.agentFromStringArray(id, agentLine);

        return new OnlineAgent(offlineAgent,
                Integer.parseInt(agentLine[INDEX_AGENT_ARRIVAL_TIME]) // add the agent's arrival time
        );

    }

    @Override
    protected MAPF_Instance makeInstance(String instanceName, I_Map graphMap, Agent[] agents, InstanceManager.InstancePath instancePath) {
        InstanceManager.Moving_AI_Path moving_ai_path = (InstanceManager.Moving_AI_Path)instancePath;
        String scenarioName = moving_ai_path.scenarioPath;
        // trim to the portion where relevant field are
        scenarioName = scenarioName.split(".scen")[0];
        scenarioName = scenarioName.split(".map")[1];
        // split into fields
        String[] fields = scenarioName.split("_");
        return new OnlineMAPF_Instance(instanceName, graphMap, agents, fields[1], fields[2], fields[3]);
    }
}
