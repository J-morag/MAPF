package BasicMAPF.Solvers.MultiAgentAStar;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MAAStarODState extends MAAStarState {
    final int agentExpandedHereIndex;

    final int cachedHashCode;

    public MAAStarODState(int time, @NotNull List<I_Location> locations, Move[] moves, float[] gArr, float[] hArr, @Nullable MAAStarState parent, int id, int agentExpandedHereIndex) {
        super(time, locations, moves, gArr, hArr, parent, id);
        this.agentExpandedHereIndex = agentExpandedHereIndex;

        // Calculate hash code eagerly
        int result = super.hashCode();

        // add the moves of agents that have already moved at this point in the partial expansion
        // mandatory, because moves we've committed to affect the moves that subsequent agents can make, making this state unique
        for (int i = 0; i <= agentExpandedHereIndex; i++) {
            result = 31 * result + moves[i].hashCode();
        }

        result = 31 * result + agentExpandedHereIndex;
        this.cachedHashCode = result;
    }

    @Override
    protected void validateMoves(int time, @NotNull List<I_Location> locations, Move[] moves) {
        if (moves[agentExpandedHereIndex] != null) {
            if (time != moves[agentExpandedHereIndex].timeNow && !(time == 0 && moves[agentExpandedHereIndex].timeNow == 1)) {
                throw new IllegalArgumentException("Move time must match the current time of the state, except for time 0, " +
                        "which can have moves with time 1 (patched to create valid move objects). Got time " + time + " but move has time " + moves[agentExpandedHereIndex].timeNow);
            }
            if (!moves[agentExpandedHereIndex].currLocation.equals(locations.get(agentExpandedHereIndex))) {
                throw new IllegalArgumentException("All moves must match the current locations of agents");
            }
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof MAAStarODState that)) return false;

        // Quick check using cached hash codes - if they differ, objects cannot be equal
        if (this.cachedHashCode != that.cachedHashCode) return false;

        if (!super.equals(o)) return false;

        // add the moves of agents that have already moved at this point in the partial expansion
        // mandatory, because moves we've committed to affect the moves that subsequent agents can make, making this state unique
        for (int i = 0; i <= that.agentExpandedHereIndex; i++) {
            if (!this.moves[i].equals(that.moves[i])) {
                return false;
            }
        }

        return agentExpandedHereIndex == that.agentExpandedHereIndex;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }
}
