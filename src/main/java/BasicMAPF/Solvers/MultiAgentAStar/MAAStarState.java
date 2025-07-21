package BasicMAPF.Solvers.MultiAgentAStar;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Maps.I_Location;
import Environment.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MAAStarState implements Comparable<MAAStarState> {
    final int time;
    final List<I_Location> locations;
    final Move[] moves;
    final float[] gArr; // Cost so far for each agent
    final float[] hArr; // Heuristic for each agent
    final float g; // Sum of costs so far
    final float h; // Sum of individual heuristics
    final float f;
    final MAAStarState parent;
    final int id; // For tie-breaking
    final int depth; // Depth in the search tree

    public MAAStarState(int time, @NotNull List<I_Location> locations, Move[] moves, float[] gArr, float[] hArr, @Nullable MAAStarState parent, int id) {
        if (Config.DEBUG >= 2) {
            validateMoves(time, locations, moves);
        }
        this.time = time;
        this.locations = locations;
        this.moves = moves;
        this.gArr = gArr;
        this.hArr = hArr;
        if (gArr.length != hArr.length || gArr.length != locations.size()) {
            throw new IllegalArgumentException("gArr, hArr, and locations must have the same length");
        }

        float g = 0;
        float h = 0;
        for (int i = 0; i < gArr.length; i++) {
            if (gArr[i] < 0 || hArr[i] < 0) {
                throw new IllegalArgumentException("Costs and heuristics must be non-negative");
            }
            g += gArr[i];
            h += hArr[i];
        }
        this.g = g;
        this.h = h;
        this.f = this.g + this.h;

        this.parent = parent;
        this.id = id;

        this.depth = parent == null ? 0 : parent.depth + 1;
    }

    protected void validateMoves(int time, @NotNull List<I_Location> locations, Move[] moves) {
        for (int i = 0; i < moves.length; i++) {
            if (moves[i] != null) {
                if (time != moves[i].timeNow && ! (time == 0 && moves[i].timeNow == 1) ) {
                    throw new IllegalArgumentException("All moves must have the same time as the state except for time 0, which can have moves with time 1 (patched to create valid move objects). Got time " + time + " but move " + i + " has time " + moves[i].timeNow);
                }
                if (! moves[i].currLocation.equals(locations.get(i))) {
                    throw new IllegalArgumentException("All moves must match the current locations of agents");
                }
            }
        }
    }

    @Override
    public int compareTo(@NotNull MAAStarState other) {
        if (this.f + Config.Misc.FLOAT_EPSILON < other.f) return -1;
        if (this.f - Config.Misc.FLOAT_EPSILON > other.f) return 1;
        // Tie-breaking
        if (this.g - Config.Misc.FLOAT_EPSILON > other.g) return -1; // Prefer higher g
        if (this.g + Config.Misc.FLOAT_EPSILON < other.g) return 1;
        return Integer.compare(this.id, other.id); // Final deterministic tie-breaker
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MAAStarState that = (MAAStarState) o;
        // A state is defined by the locations of all agents at a specific time
        return time == that.time && locations.equals(that.locations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, locations);
    }

    public double getG() {
        return this.g;
    }

    public double getF() {
        return this.f;
    }
}
