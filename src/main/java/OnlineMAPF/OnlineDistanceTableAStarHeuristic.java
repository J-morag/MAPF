package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Solvers.AStar.AStarHeuristic;
import BasicCBS.Solvers.AStar.DistanceTableAStarHeuristic;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;

import java.util.List;

public class OnlineDistanceTableAStarHeuristic implements AStarHeuristic {

    private DistanceTableAStarHeuristic table;

    public OnlineDistanceTableAStarHeuristic(DistanceTableAStarHeuristic distanceTableAStarHeuristic) {
        this.table = distanceTableAStarHeuristic;
    }

    @Override
    public float getH(SingleAgentAStar_Solver.AStarState state) {
        if(state.getMove().prevLocation instanceof PrivateGarage){
            // getting H for a private garage would result in a NullPointerException
            PrivateGarage garage = ((PrivateGarage)state.getMove().prevLocation);
            // Instead, we want to get the heuristic of the map entry point, and add 1 (for moving into it).
            return 1 + table.getHForAgentAndCurrentLocation(state.getMove().agent, garage.mapEntryPoint);
        }
        else{
            return table.getH(state);
        }
    }
}
