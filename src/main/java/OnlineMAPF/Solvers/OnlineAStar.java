package OnlineMAPF.Solvers;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.RunParameters;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineConstraintSet;

import java.util.List;

public class OnlineAStar extends SingleAgentAStar_Solver {

    public OnlineAStar() {
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

    @Override
    protected void updateExistingPlanWithFoundPlan(List<Move> moves) {
        // if there was an existing plan before solving, then we started from its last move, and don't want to duplicate it.
        if(super.existingPlan.size() > 0) {moves.remove(0);}
        // if there is only one move, then it may be just a stay move at goal, and therefore redundant.
        if(! (moves.size() == 1 && isStayMove(moves.get(0)))){
            existingPlan.addMoves(moves);
        }
    }

    private boolean isStayMove(Move move){
        return move.prevLocation.equals(move.currLocation);
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
