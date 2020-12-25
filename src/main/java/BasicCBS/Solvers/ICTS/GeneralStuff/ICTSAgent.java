package BasicCBS.Solvers.ICTS.GeneralStuff;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Solvers.ICTS.LowLevel.A_LowLevelSearcher;

import java.util.HashMap;
import java.util.Map;

public class ICTSAgent extends Agent {
    private Map<Integer, MDD> mdds;
    private A_LowLevelSearcher searcher;

    public ICTSAgent(Agent other){
        this(other.iD, other.source, other.target);
    }

    public ICTSAgent(int iD, I_Coordinate source, I_Coordinate target) {
        super(iD, source, target);
        mdds = new HashMap<Integer, MDD>();
    }

    public void setSearcher(A_LowLevelSearcher searcher){
        this.searcher = searcher;
    }

    public MDD getMDD(int depth){
        if(!mdds.containsKey(depth))
        {
            MDD curr = searcher.continueSearching(depth);
            if(curr == null)
                return null;
            mdds.put(depth, curr);
        }
        return mdds.get(depth);
    }

    public int getExpandedNodesNum(){
        return searcher.getExpandedNodesNum();
    }

    public int getGeneratedNodesNum(){
        return searcher.getGeneratedNodesNum();
    }
}
