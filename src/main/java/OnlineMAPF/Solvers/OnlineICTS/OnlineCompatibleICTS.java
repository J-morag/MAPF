package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.MDDs.AStarFactory;
import BasicCBS.Solvers.ICTS.MergedMDDs.I_MergedMDDCreator;
import BasicCBS.Solvers.ICTS.MergedMDDs.I_MergedMDDSolver;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.HighLevel.ICT_NodeComparator;
import BasicCBS.Solvers.ICTS.MDDs.I_MDDSearcherFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * currently can't solve online problems as offline, since it does not consider arrival times.
 * Makes {@link ICTS_Solver ICTS} compatible with online MAPf (agents disappearing at goal, appearing in private garage).
 */
public class OnlineCompatibleICTS extends ICTS_Solver {

    /**
     * Custom locations to start the agents at.
     */
    protected Map<Agent, I_Location> customStartLocations;
    /**
     * A start time to use for all agents instead of their arrival times.
     */
    private int customStartTime = -1;

    public OnlineCompatibleICTS(ICT_NodeComparator comparator, I_MDDSearcherFactory searcherFactory, I_MergedMDDSolver mergedMDDSolver,
                                PruningStrategy pruningStrategy, I_MergedMDDCreator mergedMDDCreator,
                                Map<Agent, I_Location> customStartLocations, int customStartTime) {
        // has to be online merged MDD factory, so that it will use OnlineSolution and ignore target conflicts (after time of arriving at goal)
        super(comparator, searcherFactory, Objects.requireNonNullElse(mergedMDDSolver, new Online_ID_MergedMDDSolver(new OnlineDFS_MergedMDDSpaceSolver())),
                pruningStrategy, Objects.requireNonNullElseGet(mergedMDDCreator, OnlineBFS_MergedMDDCreator::new));
        if (mergedMDDSolver != null && ! (mergedMDDSolver instanceof Online_ID_MergedMDDSolver))
            throw new IllegalArgumentException("Must use an online MergedMDDSolver.");
        if (mergedMDDCreator != null && ! (mergedMDDCreator instanceof OnlineBFS_MergedMDDCreator))
            throw new IllegalArgumentException("Must use an online MergedMDDCreator.");
        // set searcher factory to online
        super.searcherFactory.setDefaultDisappearAtGoal(true);
        this.customStartLocations = Objects.requireNonNullElseGet(customStartLocations, HashMap::new);
        this.customStartTime = customStartTime;
    }

    public OnlineCompatibleICTS(Map<Agent, I_Location> customStartLocations, int customStartTime) {
        this(null, new AStarFactory(true), new Online_ID_MergedMDDSolver(new OnlineDFS_MergedMDDSpaceSolver()),
                null, new OnlineBFS_MergedMDDCreator(), customStartLocations, customStartTime);
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
