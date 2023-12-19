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
     * Searches for the MDD (all paths) of a wanted depth
     * Continuing the search from the last "checkpoint" - meaning all the open and closed lists are already saved in the searcher.
     * @param depthOfSolution - The depth of the wanted solutions. Must be greater than the previous depth.
     * @return the goal state, which can easily be transferred to an MDD
     */
    public abstract MDD continueSearching(int depthOfSolution);

    /**
     * Searches for the first (minimal depth) MDD that can be found under the given constraints
     * @throws IllegalStateException if this searcher is called after the searcher has already been used
     * @param constraints - the constraints that the MDD should satisfy
     * @return the minimal-depth MDD that satisfies the constraints
     */
    public abstract MDD searchToFirstSolution(@Nullable ConstraintSet constraints);
}
