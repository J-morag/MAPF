package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.DataTypesAndStructures.MDDs.MDD;

import java.util.Map;

public interface I_MergedMDDCreator {
    /**
     * Create a merged MDD, which is an MDD for the group of agents, created through a cartesian product of their
     * individual MDDs,retaining only the nodes that don't produce conflicts between agents in the group.
     * @param agentMDDs MDD for each agent
     * @return a joint solution of all agents, restricted to each agent moving only on its MDD.
     */
    MergedMDD createMergedMDD(Map<Agent, MDD> agentMDDs, Timeout timout);
    /**
     * Gets the number of nodes expanded during the last time {@link #createMergedMDD(Map, Timeout)} was called.
     * @return the number of nodes expanded during the last time {@link #createMergedMDD(Map, Timeout)} was called.
     */
    int getExpandedLowLevelNodesNum();
    /**
     * Gets the number of nodes generated during the last time {@link #createMergedMDD(Map, Timeout)} was called.
     * @return the number of nodes generated during the last time {@link #createMergedMDD(Map, Timeout)} was called.
     */
    int getGeneratedLowLevelNodesNum();
}