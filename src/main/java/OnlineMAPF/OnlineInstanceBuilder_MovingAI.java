package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;

public class OnlineInstanceBuilder_MovingAI extends InstanceBuilder_MovingAI {

    protected final int INDEX_AGENT_ARRIVAL_TIME = 9;

    @Override
    protected Agent agentFromStringArray(int id, String[] agentLine) {
        Agent offlineAgent = super.agentFromStringArray(id, agentLine);

        return new OnlineAgent(offlineAgent,
                Integer.parseInt(agentLine[INDEX_AGENT_ARRIVAL_TIME]) // add the agent's arrival time
        );

    }

}
