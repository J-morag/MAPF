package OnlineMAPF.Solvers;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.RunParameters;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineConstraintSet;

public class OnlineSingleAgentAStar_Solver extends SingleAgentAStar_Solver {

    public OnlineSingleAgentAStar_Solver() {
        super(false);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        if (! (super.agent instanceof OnlineAgent) ) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " is an online solver and accepts only Online Agents.");
        }

        if(runParameters.constraints != null){
            if (! (runParameters.constraints instanceof OnlineConstraintSet) ) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " is an online solver and accepts only Online Constraint Sets.");
            }
            super.constraints = runParameters.constraints;
        }
        else{
            super.constraints = new OnlineConstraintSet();
        }
    }

//
//    /**
//     * Adds a possible stay move at the garage.
//     */
//    @Override
//    protected void fillOpenWithRoots() {
//        super.fillOpenWithRoots();
//        I_Location garage = ((OnlineAgent) super.agent).getPrivateGarage(super.map.getMapCell(super.agent.source));
//        if(! (garage.equals(super.agentStartLocation))){
//            // can stay at the garage instead of coming into the map. if the agent wants to come into the map on its first
//            // move, it won't start in the garage, but will instead start at the location on the map, and it can move from there.
//            Move stayAtGarage = new Move(super.agent, super.problemStartTime + 1, garage, garage);
//            openList.add(new AStarState(stayAtGarage, null, 1));
//            super.generatedNodes++;
//        }
//
//    }
}
