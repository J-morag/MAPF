package BasicMAPF.Solvers.MultiAgentAStar;

import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MAAStarState implements Comparable<MAAStarState> {
    public static final double EPSILON = 0.01;
    final int time;
    final List<I_Location> locations;
    final float g; // Sum of costs so far
    final float h; // Sum of individual heuristics
    final float f;
    final MAAStarState parent;
    final int id; // For tie-breaking

    public MAAStarState(int time, List<I_Location> locations, float g, float h, MAAStarState parent, int id) {
        this.time = time;
        this.locations = locations;
        this.g = g;
        this.parent = parent;
        this.h = h;
        this.f = this.g + this.h;
        this.id = id;
    }

    @Override
    public int compareTo(@NotNull MAAStarState other) {
        if (this.f + EPSILON < other.f) return -1;
        if (this.f - EPSILON > other.f) return 1;
        // Tie-breaking
        if (this.g - EPSILON > other.g) return -1; // Prefer higher g
        if (this.g + EPSILON < other.g) return 1;
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
}
