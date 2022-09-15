package LifelongMAPF;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.jetbrains.annotations.NotNull;

public class TimeCoordinate {
    public int time;
    public I_Coordinate coordinate;

    public TimeCoordinate(int time, @NotNull I_Coordinate coordinate) {
        this.time = time;
        this.coordinate = coordinate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeCoordinate that = (TimeCoordinate) o;

        if (time != that.time) return false;
        return coordinate.equals(that.coordinate);
    }

    @Override
    public int hashCode() {
        int result = time;
        result = 31 * result + coordinate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(t=" + time +
                ",c=" + coordinate + ")";
    }
}
