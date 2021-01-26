package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.GeneralStuff.AgentsGroup;
import BasicCBS.Solvers.ICTS.GeneralStuff.DFS_ID_MergedMDDFactory;
import BasicCBS.Solvers.ICTS.GeneralStuff.MDD;
import BasicCBS.Solvers.ICTS.GeneralStuff.MergedMDDNode;
import BasicCBS.Solvers.Solution;
import OnlineMAPF.OnlineSolution;

import java.util.Map;
import java.util.Set;

public class onlineDFS_ID_MergedMDDFactory extends DFS_ID_MergedMDDFactory {

    @Override
    protected AgentsGroup getAgentsGroup(Set<Agent> agents, Solution solution) {
        return super.getAgentsGroup(agents, new OnlineSolution(solution));
    }

    @Override
    protected Solution getPossibleSolution(Map<Agent, MDD> agentMDDs, Agent agent) {
        return new OnlineSolution(super.getPossibleSolution(agentMDDs, agent));
    }

    @Override
    protected MergedMDDNode getMergedMDDNode(int mddNodeDepth) {
        return new MergedMDDNode(mddNodeDepth, true);
    }

    @Override
    protected Solution getSolution(AgentsGroup ag) {
        return new OnlineSolution(ag.getSolution());
    }

    @Override
    protected Solution getSolution() {
        return new OnlineSolution(new Solution());
    }

    @Override
    protected Solution getSolution(Solution solution) {
        return solution == null ? null : new OnlineSolution(solution);
    }
}
