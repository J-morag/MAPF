package BasicCBS.Solvers.ICTS.MergedMDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicCBS.Solvers.ICTS.MDDs.MDD;

import java.util.Map;

public interface I_MergedMDDCreator {
    /**
     * Create a merged MDD, which is an MDD for the group of agents, created through a cartesian product of their
     * individual MDDs,retaining only the nodes that don't produce conflicts between agents in the group.
     * @param agentMDDs MDD for each agent
     * @param highLevelSolver The high level solver (for timeout check)
     * @return a joint solution of all agents, restricted to each agent moving only on its MDD.
     */
    MergedMDD createMergedMDD(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver);
    /**
     * Gets the number of nodes expanded during the last time {@link #createMergedMDD(Map, ICTS_Solver)} was called.
     * @return the number of nodes expanded during the last time {@link #createMergedMDD(Map, ICTS_Solver)} was called.
     */
    int getExpandedLowLevelNodesNum();
    /**
     * Gets the number of nodes generated during the last time {@link #createMergedMDD(Map, ICTS_Solver)} was called.
     * @return the number of nodes generated during the last time {@link #createMergedMDD(Map, ICTS_Solver)} was called.
     */
    int getGeneratedLowLevelNodesNum();
}