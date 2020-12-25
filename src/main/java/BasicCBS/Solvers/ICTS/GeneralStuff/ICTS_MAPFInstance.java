package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Map;

import java.util.ArrayList;
import java.util.List;

public class ICTS_MAPFInstance extends MAPF_Instance {
    public ICTS_MAPFInstance(String name, I_Map map, Agent[] agents) {
        super(name, map, agents);
    }

    public static ICTS_MAPFInstance Copy(MAPF_Instance other){
        List<Agent> agents = new ArrayList<>();
        for(Agent agent : other.agents){
            Agent ictsAgent = new ICTSAgent(agent);
            agents.add(ictsAgent);
        }
        return new ICTS_MAPFInstance(other.name, other.map, agents.toArray(Agent[]::new));
    }
}
