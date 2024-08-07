package BasicMAPF.Instances.Maps.Coordinates;

import org.jetbrains.annotations.NotNull;

public class CoordinateNamed implements I_Coordinate {
    public final String name;

    public CoordinateNamed(@NotNull String name) {
        this.name = name;
    }

    @Override
    public float distance(I_Coordinate other) {
        return 0;
    }

    @Override
    public int compareTo(@NotNull I_Coordinate o) {
        return 0;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CoordinateNamed that)) return false;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
