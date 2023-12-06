package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class A_MDDSearcher {
    protected int expandedNodesNum;
    protected int generatedNodesNum;
    protected Timeout timeout;
    protected I_Location source;
    protected I_Location target;
    protected Agent agent;

    public A_MDDSearcher(@NotNull Timeout timeout, @NotNull I_Location source, @NotNull I_Location target, @NotNull Agent agent) {
        expandedNodesNum = 0;
        generatedNodesNum = 0;
        this.timeout = timeout;
        this.source = source;
        this.target = target;
        this.agent = agent;
    }

    public int getExpandedNodesNum() {
        return expandedNodesNum;
    }

    public int getGeneratedNodesNum() {
        return generatedNodesNum;
    }

    protected I_Location getSource(){
        return source;
    }

    protected I_Location getTarget(){
        return target;
    }

    /**
     * Searches for all the solutions in a wanted depth
     * Continuing the search from the last "checkpoint" that means that all of the open list and closed list is already saved in the searcher.
     * @param depthOfSolution - the depth of the wanted solutions
     * @return the goal state, which can easily be transferred to an MDD
     */
    public abstract MDD continueSearching(int depthOfSolution);

    public abstract MDD searchToFirstSolution(@Nullable ConstraintSet constraints);
}
