package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class MDDManager {

    public Map<SourceTargetAgent, LinkedList<MDD>> mdds = new HashMap<>();
    public Map<SourceTargetAgent, A_MDDSearcher> searchers = new HashMap<>();
    final private I_MDDSearcherFactory searcherFactory;
    final public SourceTargetAgent keyDummy = new SourceTargetAgent(null, null, null);
    private final Timeout timeout;
    private final SingleAgentGAndH heuristic;
    private int expandedLowLevelNodes;
    private int generatedLowLevelNodes;

    public MDDManager(I_MDDSearcherFactory searcherFactory, Timeout timeout, SingleAgentGAndH heuristic) {
        this.searcherFactory = searcherFactory;
        this.timeout = timeout;
        this.heuristic = heuristic;
    }

    public MDD getMDD(I_Location source, I_Location target, Agent agent, int depth){
        keyDummy.set(source, target, agent);
        LinkedList<MDD> mddsAtIncrementalDepths = mdds.get(keyDummy);
        if(mddsAtIncrementalDepths == null) {
            SourceTargetAgent sourceTargetAgent = new SourceTargetAgent(keyDummy);
            mddsAtIncrementalDepths = new LinkedList<>();
            mdds.put(sourceTargetAgent, mddsAtIncrementalDepths);
        }

        int depthDeltaFromMinMDD = mddsAtIncrementalDepths.isEmpty() ? 0 : depth - mddsAtIncrementalDepths.get(0).getDepth();
        if(depthDeltaFromMinMDD < mddsAtIncrementalDepths.size()) {
            return mddsAtIncrementalDepths.get(depthDeltaFromMinMDD);
        }
        else {
            MDD curr = null;
            if(!searchers.containsKey(keyDummy)) {
                SourceTargetAgent sourceTargetAgent = new SourceTargetAgent(keyDummy);
                searchers.put(sourceTargetAgent, searcherFactory.createSearcher(timeout, source, target, agent, heuristic));
            }
            for (int currDelta = mddsAtIncrementalDepths.size(); currDelta <= depthDeltaFromMinMDD; currDelta++) {
                int currDepth = mddsAtIncrementalDepths.isEmpty() ? heuristic.getHToTargetFromLocation(target.getCoordinate(), source) :
                        mddsAtIncrementalDepths.get(0).getDepth() + currDelta;
                curr = searchers.get(keyDummy).continueSearching(currDepth);
                if(curr == null)
                    return null;
                mddsAtIncrementalDepths.add(curr);
            }
            return curr;
        }
    }

    public MDD getMDDNoReuse(I_Location source, I_Location target, Agent agent, int depth){
        A_MDDSearcher searcher = this.searcherFactory.createSearcher(timeout, source, target, agent, heuristic);
        MDD result = searcher.continueSearching(depth);
        this.expandedLowLevelNodes += searcher.getExpandedNodesNum();
        this.generatedLowLevelNodes += searcher.getGeneratedNodesNum();
        return result;
    }

    public MDD getMinMDDNoReuse(@NotNull I_Location source, @NotNull I_Location target, @NotNull Agent agent, @Nullable I_ConstraintSet constraints){
        A_MDDSearcher searcher = this.searcherFactory.createSearcher(timeout, source, target, agent, heuristic);
        MDD result = searcher.searchToFirstSolution(constraints);
        this.expandedLowLevelNodes += searcher.getExpandedNodesNum();
        this.generatedLowLevelNodes += searcher.getGeneratedNodesNum();
        return result;
    }

    public void clearSearchers(){
        searchers.clear();
    }

    public int getExpandedNodesNum(){
        int sum = 0;
        for (A_MDDSearcher searcher:
             searchers.values()) {
            sum += searcher.getExpandedNodesNum();
        }
        return sum + this.expandedLowLevelNodes;
    }

    public int getGeneratedNodesNum(){
        int sum = 0;
        for (A_MDDSearcher searcher:
                searchers.values()) {
            sum += searcher.getGeneratedNodesNum();
        }
        return sum + this.generatedLowLevelNodes;
    }

    public static class SourceTargetAgent {
        I_Location source;
        I_Location target;
        Agent agent;

        public SourceTargetAgent(I_Location source, I_Location target, Agent agent) {
            this.source = source;
            this.target = target;
            this.agent = agent;
        }

        public SourceTargetAgent(SourceTargetAgent other){
            this.source = other.source;
            this.target = other.target;
            this.agent = other.agent;
        }

        public SourceTargetAgent set(I_Location source, I_Location target, Agent agent){
            this.source = source;
            this.target = target;
            this.agent = agent;
            return this;
        }

        public Agent getAgent() {
            return agent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SourceTargetAgent)) return false;

            SourceTargetAgent that = (SourceTargetAgent) o;

            if (!source.equals(that.source)) return false;
            if (!target.equals(that.target)) return false;
            return agent.equals(that.agent);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + target.hashCode();
            result = 31 * result + agent.hashCode();
            return result;
        }
    }
}
