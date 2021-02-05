package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Instances.Agent;
import BasicCBS.Solvers.ICTS.MDDs.MDD;
import BasicCBS.Solvers.ICTS.MergedMDDs.*;
import BasicCBS.Solvers.Solution;
import OnlineMAPF.OnlineSolution;

import java.util.Map;
import java.util.Set;

public class Online_ID_MergedMDDSolver extends IndependenceDetection_MergedMDDSolver {

    protected Online_ID_MergedMDDSolver(I_MergedMDDSolver delegatedMergedMDDSolver) {
        super(delegatedMergedMDDSolver);
        if (! (delegatedMergedMDDSolver instanceof OnlineDFS_MergedMDDSpaceSolver
                || delegatedMergedMDDSolver instanceof OnlineBFS_MergedMDDCreator) )
            throw new IllegalArgumentException("Must use an online MergedMDDSolver.");
    }

    public Online_ID_MergedMDDSolver() {
        super(new OnlineDFS_MergedMDDSpaceSolver());
    }

    @Override
    protected AgentsGroup getAgentsGroup(Set<Agent> agents, Solution solution) {
        return super.getAgentsGroup(agents, new OnlineSolution(solution));
    }

    @Override
    protected Solution getPossibleSolution(Map<Agent, MDD> agentMDDs, Agent agent) {
        return new OnlineSolution(super.getPossibleSolution(agentMDDs, agent));
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
