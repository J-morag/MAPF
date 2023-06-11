package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ICTS.HighLevel.ICTS_Solver;
import BasicMAPF.Solvers.ICTS.MDDs.MDD;
import BasicMAPF.DataTypesAndStructures.Solution;

import java.util.Map;

public interface I_MergedMDDSolver {
    /**
     * Looks for a joint solution of all agents, restricted to each agent moving only on its MDD.
     * @param agentMDDs MDD for each agent
     * @param highLevelSolver The high level solver (for timeout check)
     * @return a joint solution of all agents, restricted to each agent moving only on its MDD.
     */
    Solution findJointSolution(Map<Agent, MDD> agentMDDs, ICTS_Solver highLevelSolver);
    /**
     * Gets the number of nodes expanded during the last time {@link #findJointSolution(Map, ICTS_Solver)} was called.
     * @return the number of nodes expanded during the last time {@link #findJointSolution(Map, ICTS_Solver)} was called.
     */
    int getExpandedLowLevelNodesNum();
    /**
     * Gets the number of nodes generated during the last time {@link #findJointSolution(Map, ICTS_Solver)} was called.
     * @return the number of nodes generated during the last time {@link #findJointSolution(Map, ICTS_Solver)} was called.
     */
    int getGeneratedLowLevelNodesNum();
}