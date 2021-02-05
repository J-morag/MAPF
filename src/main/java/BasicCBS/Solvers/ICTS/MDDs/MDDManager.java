package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICTS_Solver;

import java.util.HashMap;
import java.util.Map;

public class MDDManager {

    final private Map<SourceTargetPair, Map<Integer, MDD>> mdds = new HashMap<>();
    final private Map<SourceTargetPair, A_MDDSearcher> searchers = new HashMap<>();
    final private I_MDDSearcherFactory searcherFactory;
    final private SourceTargetPair keyDummy = new SourceTargetPair(null, null);
    private ICTS_Solver highLevelSolver;
    private DistanceTableAStarHeuristicICTS heuristic; //todo replace?
    private int expandedLowLevelNodes;
    private int generatedLowLevelNodes;

    public MDDManager(I_MDDSearcherFactory searcherFactory, ICTS_Solver highLevelSolver, DistanceTableAStarHeuristicICTS heuristic) {
        this.searcherFactory = searcherFactory;
        this.highLevelSolver = highLevelSolver;
        this.heuristic = heuristic;
    }

    public MDD getMDD(I_Location source, I_Location target, Agent agent, int depth){
        keyDummy.set(source, target);
        if(!mdds.containsKey(keyDummy)) {
            SourceTargetPair sourceTargetPair = new SourceTargetPair(keyDummy);
            mdds.put(sourceTargetPair, new HashMap<>());
        }
        if(!searchers.containsKey(keyDummy)) {
            SourceTargetPair sourceTargetPair = new SourceTargetPair(keyDummy);
            searchers.put(sourceTargetPair, searcherFactory.createSearcher(this.highLevelSolver, source, target, agent, heuristic));
        }

        if(mdds.get(keyDummy).containsKey(depth)) {
            return mdds.get(keyDummy).get(depth);
        }
        else{
            MDD curr = searchers.get(keyDummy).continueSearching(depth);
            if(curr == null)
                return null;
            mdds.get(keyDummy).put(depth, curr);
            return curr;
        }
    }

    public MDD getMDDNoReuse(I_Location source, I_Location target, Agent agent, int depth){
        A_MDDSearcher searcher = this.searcherFactory.createSearcher(highLevelSolver, source, target, agent, heuristic);
        MDD result = searcher.continueSearching(depth);
        this.expandedLowLevelNodes += searcher.getExpandedNodesNum();
        this.generatedLowLevelNodes += searcher.getGeneratedNodesNum();
        return result;
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

    private class SourceTargetPair {
        I_Location source;
        I_Location target;

        public SourceTargetPair(I_Location source, I_Location target) {
            this.source = source;
            this.target = target;
        }

        public SourceTargetPair(SourceTargetPair other){
            this.source = other.source;
            this.target = other.target;
        }

        public void set(I_Location source, I_Location target){
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SourceTargetPair that = (SourceTargetPair) o;

            if (!source.equals(that.source)) return false;
            return target.equals(that.target);
        }

        @Override
        public int hashCode() {
            int result = source.hashCode();
            result = 31 * result + target.hashCode();
            return result;
        }
    }
}
