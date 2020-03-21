package OnlineMAPF.Solvers;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineConstraintSet;
import OnlineMAPF.OnlineSolution;

import java.util.List;
import java.util.Objects;

/**
 * A single agent AStar implementation for online agents.
 */
public class OnlineAStar extends SingleAgentAStar_Solver {

    private int costOfReroute = 0;
    private Solution previousSolution;
    private SingleAgentPlan previousPlan;

    public OnlineAStar(int costOfReroute, Solution previousSolution) {
        super(false);
        this.costOfReroute = costOfReroute;
        this.previousSolution = Objects.requireNonNullElseGet(previousSolution, Solution::new);
    }

    public OnlineAStar() {
        this(0, null);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);
        this.previousPlan = previousSolution.getPlanFor(super.agent);
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

    @Override
    protected AStarState newState(Move move, AStarState prevState, int g) {
        return new OnlineAStarState(move, (OnlineAStarState)prevState, g);
    }


    public class OnlineAStarState extends AStarState {

        /**
         * Indicates if this or a previous state had a reroute already.
         */
        boolean hadReroutes;

        public OnlineAStarState(Move move, OnlineAStarState prevState, int g) {
            super(move, prevState,
                    /*
                    Add the cost of reroute to g if this is the first occurrence of a reroute on this route.
                    If some parent was already a reroute, then the whole route is a reroute and the cost is only added once.
                     */
                    g + (( previousPlan != null
                               && (prevState == null || !prevState.hadReroutes)
                               && OnlineSolution.isAReroute(previousPlan, move)) ?
                               OnlineAStar.this.costOfReroute : 0) );
            this.hadReroutes = previousPlan != null &&
                    ((prevState != null && prevState.hadReroutes) || OnlineSolution.isAReroute(previousPlan, move));
        }
    }

}
