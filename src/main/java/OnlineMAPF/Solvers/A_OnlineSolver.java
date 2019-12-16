package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import OnlineMAPF.OnlineAgent;

import java.util.*;

public abstract class A_OnlineSolver {

    /**
     * A custom arrival time to give {@link Agent offline agents} that this solver attempts to solve for.
     * Defaults to {@link OnlineAgent#DEFAULT_ARRIVAL_TIME}.
     */
    private static final int ARRIVAL_TIME_FOR_OFFLINE_AGENTS = OnlineAgent.DEFAULT_ARRIVAL_TIME;

    public static Map<Integer, List<OnlineAgent>> getAgentsByTime(List<? extends Agent> agents) {
        Map<Integer, List<OnlineAgent>> result = new HashMap<>();
        ArrayList<OnlineAgent> onlineAgents = offlineToOnlineAgents(agents);

        //sort by time
        onlineAgents.sort(Comparator.comparing(OnlineAgent::getArrivalTime));

        //group by time
        for (int i = 0; i < onlineAgents.size();) {
            int currentTime = onlineAgents.get(i).arrivalTime;
            //find range with same arrival time
            int j = i;
            while(j < onlineAgents.size() && onlineAgents.get(j).arrivalTime == currentTime){
                j++;
            }
            //so the range we found is [i,j)

            result.put(currentTime, onlineAgents.subList(i, j /*end index is non-inclusive*/ ));

            i=j; //next group
        }

        return result;
    }


    /**
     * Cast agents to online agents. If they are regular Agents, create new OnlineAgents out of them with the default arrival time.
     * @param agents
     */
    private static ArrayList<OnlineAgent> offlineToOnlineAgents(List<? extends Agent> agents) {
        ArrayList<OnlineAgent> onlineAgents = new ArrayList<>(agents.size());
        for (Agent a :
                agents) {
            onlineAgents.add(a instanceof OnlineAgent ? (OnlineAgent)a : new OnlineAgent(a, ARRIVAL_TIME_FOR_OFFLINE_AGENTS));
        }
        return onlineAgents;
    }
}
