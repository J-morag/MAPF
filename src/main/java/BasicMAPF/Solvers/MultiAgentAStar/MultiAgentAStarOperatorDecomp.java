package BasicMAPF.Solvers.MultiAgentAStar;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Multi-Agent A* with operator decomposition.
 * This solver uses the operator decomposition - expands (generates moves of) one agent at a time,
 * rather than generate all move combinations at once when expanding.
 * Based on Standley, Trevor. "Finding optimal solutions to cooperative pathfinding problems." Proceedings of the AAAI conference on artificial intelligence. Vol. 24. No. 1. 2010.
 */
public class MultiAgentAStarOperatorDecomp extends MultiAgentAStar {

    public MultiAgentAStarOperatorDecomp(@Nullable Comparator<MAAStarState> openListComparator) {
        super(openListComparator);
        this.name = CanonicalSolversFactory.MAASTAR_OD_NAME;
    }

    public MultiAgentAStarOperatorDecomp() {
    }

    @Override
    protected @NotNull MAAStarState getRootState(List<I_Location> startLocations, float[] initialHArr) {
        return new MAAStarODState(problemStartTime, startLocations, new Move[startLocations.size()], new float[this.agents.size()] /*zeros*/, initialHArr, null, generatedNodes++, -1);
    }

    @Override
    protected int getNextTime(MAAStarState parentState, boolean afterLastConstraint) {
        int nextAgentIndex = getNextAgentIndex(parentState);
        if (nextAgentIndex == 0){
            return super.getNextTime(parentState, afterLastConstraint);
        }
        else {
            return parentState.time;
        }
    }

    @Override
    protected boolean expandHelper(MAAStarState parentState, int time) {
        int nextAgentIndex = getNextAgentIndex(parentState);
        I_Location currentLocation = parentState.locations.get(nextAgentIndex);
        List<I_Location> possibleLocationsToMoveTo = new ArrayList<>(currentLocation.outgoingEdges());
        possibleLocationsToMoveTo.add(currentLocation); // include staying in place
        for (I_Location neighbor: possibleLocationsToMoveTo){
            List<I_Location> nextStateLocationsVector = new ArrayList<>(parentState.locations);
            nextStateLocationsVector.set(nextAgentIndex, neighbor);
            expandHelper2(parentState, time, nextStateLocationsVector);
        }
        // not very necessary to check timeout here, since we only expand one agent at a time, but doing it to be consistent with super
        return !checkTimeout();
    }

    @Override
    protected void calculateStepCostsHelper(MAAStarState parentState, Move[] moves, float[] gArr) {
        // Only have cost for the next agent. The rest have already been calculated or will be calculated in later expansions.
        int nextAgentIndex = getNextAgentIndex(parentState);
        updateIndexAgentMoveCost(parentState, moves[nextAgentIndex], nextAgentIndex, gArr);
    }

    @Override
    protected int getCostOfSkippedWaitAtTargetActions(MAAStarState parentState, Move move, int i, Agent agent) {
        // when iterating back through parents to count wait actions, don't count the cost of intermediate states where other agents moved

        int costOfSkippedWaitAtTargetActions = 0;
        // If the agent steps out of its target location, walk back through parents and add the costs of all the
        // wait-at-target actions that we got for free
        if (move.prevLocation.getCoordinate().equals(agent.target) && !move.currLocation.getCoordinate().equals(agent.target)) {
            int intermediateStatesUntilNextRelevantState = agents.size() - 1;
            boolean done = false;
            MAAStarState currentParent = parentState.parent;
            MAAStarState currentState = parentState;
            while (currentParent != null && !done) {
                if (intermediateStatesUntilNextRelevantState > 0) {
                    // skip intermediate states
                    intermediateStatesUntilNextRelevantState--;
                    currentState = currentParent;
                    currentParent = currentParent.parent;
                    continue;
                }

                intermediateStatesUntilNextRelevantState = agents.size() - 1; // reset to the next agent index

                if (currentParent.locations.get(i).getCoordinate().equals(agent.target)) {
                    // The agent was at the target location in the previous state, so it was waiting there
                    costOfSkippedWaitAtTargetActions += singleAgentGAndH.cost(currentState.moves[i]);
                    currentState = currentParent;
                    currentParent = currentParent.parent;
                } else {
                    done = true; // before started waiting at the target location
                }
            }
        }
        return costOfSkippedWaitAtTargetActions;
    }

    @Override
    protected Move[] getMovesToNextLocations(MAAStarState parentState, List<I_Location> nextLocations, int time) {
        if (time == 0){
            // handles the case where there are no constraints at all, giving all states time 0
            time = 1; // to generate valid moves
        }
        Move[] moves = Arrays.copyOf(parentState.moves, parentState.moves.length);
        int nextAgentIndex = getNextAgentIndex(parentState);
        moves[nextAgentIndex] = new Move(agents.get(nextAgentIndex), time, parentState.locations.get(nextAgentIndex), nextLocations.get(nextAgentIndex));
        return moves;
    }

    @Override
    protected float[] calculateHeuristicArray(List<I_Location> locations,  @Nullable MAAStarState parentState) {
        if (parentState == null){
            return super.calculateHeuristicArray(locations, null);
        }
        int nextAgentIndex = getNextAgentIndex(parentState);
        float[] hArr = Arrays.copyOf(parentState.hArr, parentState.hArr.length);
        hArr[nextAgentIndex] = singleAgentGAndH.getHToTargetFromLocation(agents.get(nextAgentIndex).target, locations.get(nextAgentIndex));
        return hArr;
    }

    @Override
    protected @NotNull MAAStarState getMaaStarState(MAAStarState parentState, List<I_Location> nextLocations, Move[] moves, int time, float[] gArr, float[] hArr) {
        return new MAAStarODState(time, nextLocations, moves, gArr, hArr, parentState, generatedNodes++, getNextAgentIndex(parentState));
    }

    private int getNextAgentIndex(MAAStarState parentState){
        if (parentState instanceof MAAStarODState parentODState){
            return parentODState.agentExpandedHereIndex == parentODState.locations.size() - 1 ? 0 : parentODState.agentExpandedHereIndex + 1;
        }
        else throw new IllegalArgumentException("Parent state must be of type MAAStarODState");
    }

    private int getCurrentAgentIndex(MAAStarState currentState){
        if (currentState instanceof MAAStarODState currentODState){
            return currentODState.agentExpandedHereIndex;
        }
        else throw new IllegalArgumentException("Parent state must be of type MAAStarODState");
    }

    @Override
    protected boolean isValidJointMove(Move[] moves, MAAStarState parentState) {
        // only need to validate the next agent's move against previous agents' moves
        int nextAgentIndex = getNextAgentIndex(parentState);

        Move nextAgentMove = moves[nextAgentIndex];
        // check for conflicts with external constraints
        if (externalConstraints != null) {
            if (externalConstraints.rejects(nextAgentMove)){
                return false;
            }
        }

        // check for conflicts with existing moves (agents with a lower index that have thus already decided their move)
        for (int existingMoveAgentIdx = 0; existingMoveAgentIdx < nextAgentIndex; existingMoveAgentIdx++) {
            Move existingMove = moves[existingMoveAgentIdx];
            // check for internal conflicts
            if (A_Conflict.haveConflicts(existingMove, nextAgentMove)) {
                return false;
            }
        }

        return true;

    }

    @Override
    protected boolean isGoal(MAAStarState currentState) {
        int currentAgentIndex = getCurrentAgentIndex(currentState);
        return (currentAgentIndex == -1 || currentAgentIndex == agents.size() - 1) && super.isGoal(currentState);
    }

    @Override
    protected @NotNull ArrayList<MAAStarState> getSolutionStatesSequence(MAAStarState goalState) {
        List<MAAStarState> decomposedStates = super.getSolutionStatesSequence(goalState);
        ArrayList<MAAStarState> completeStates = new ArrayList<>();
        // the root is the same
        completeStates.add(decomposedStates.get(0));
        // skip intermediate states (decomposed states)
        for (int i = agents.size(); i < decomposedStates.size(); i += agents.size()) {
            completeStates.add(decomposedStates.get(i));
        }
        return completeStates;
    }
}
