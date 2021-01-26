package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.GeneralStuff.I_MergedMDDFactory;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.HighLevel.ICT_NodeComparator;
import BasicCBS.Solvers.ICTS.LowLevel.I_LowLevelSearcherFactory;
import OnlineMAPF.OnlineICTSDistanceTableHeuristic;

import java.util.Map;

/**
 * currently can't solve online problems as offline, since it does not consider arrival times.
 */
public class OnlineCompatibleICTS extends ICTS_Solver {

    /**
     * Custom locations to start the agents at.
     */
    private Map<Agent, I_Location> customStartLocations;
    /**
     * A start time to use for all agents instead of their arrival times.
     */
    private int customStartTime = -1;

    public OnlineCompatibleICTS(ICT_NodeComparator comparator, I_LowLevelSearcherFactory searcherFactory, I_MergedMDDFactory mergedMDDFactory, Boolean usePairWiseGoalTest, Map<Agent, I_Location> customStartLocations, int customStartTime) {
        // has to be online merged MDD factory, so that it will use OnlineSolution and ignore target conflicts (after time of arriving at goal)
        super(comparator, searcherFactory, new onlineDFS_ID_MergedMDDFactory(), usePairWiseGoalTest);
        super.searcherFactory.setDefaultDisappearAtGoal(true);
        this.customStartLocations = customStartLocations;
        this.customStartTime = customStartTime;
    }

    public OnlineCompatibleICTS(Map<Agent, I_Location> customStartLocations, int customStartTime) {
        this(null, null, null, null, customStartLocations, customStartTime);
    }

    @Override
    protected I_Location getSource(Agent agent) {
        if (this.customStartLocations.containsKey(agent)){
            return customStartLocations.get(agent);
        }
        else {
            return super.getSource(agent);
        }
    }

    @Override
    protected void getHeuristic(MAPF_Instance instance) {
        heuristicICTS = new OnlineICTSDistanceTableHeuristic(instance.agents, instance.map);
    }

    @Override
    public String name() {
        return "ICTS(online_restart)";
    }
}
