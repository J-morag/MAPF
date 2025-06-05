package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import Environment.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * AStarMDDBuilder that creates an MDD with only one path
 */
public class OnePathAStarMDDBuilder extends AStarMDDBuilder {

    public OnePathAStarMDDBuilder(@NotNull Timeout timeout, @NotNull I_Location source, @NotNull I_Location target, @NotNull Agent agent, @NotNull SingleAgentGAndH heuristic) {
        super(timeout, source, target, agent, heuristic);
    }

    @Override
    protected void addNodeParents(@NotNull MDDSearchNode node, @NotNull List<MDDSearchNode> parents) {
        // only add if empty, and even then only 1
        if (node.getParents().isEmpty() && !parents.isEmpty()) {
            node.addParent(parents.get(0));
        }
    }

    @Override
    protected void addNodeParent(@NotNull MDDSearchNode node, @NotNull MDDSearchNode parent) {
        // only add if empty
        if (node.getParents().isEmpty()) {
            node.addParent(parent);
        }
    }

    @Override
    protected boolean pausingCondition(int depthOfSolution, MDDSearchNode current, MDDSearchNode goal) {
        // can stop earlier since we don't need to collect all paths
        return super.pausingCondition(depthOfSolution, current, goal) || goal != null;
    }

    @Override
    protected @Nullable MDD MDDFromGoalNode(MDDSearchNode goal) {
        MDD res = super.MDDFromGoalNode(goal);

        if (Config.DEBUG >= 2 && res != null) {
            // verify MDD is one path
            MDDNode current = res.getStart();
            while (current != null) {
                if (current.getNeighbors().size() > 1) {
                    throw new IllegalStateException("MDD is not one path");
                }
                if (current.equals(res.getGoal())) {
                    break;
                }
                current = current.getNeighbors().get(0);
            }
        }

        return res;
    }
}
