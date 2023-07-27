package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.Move;

public class TimeLocation implements I_ConstraintGroupingKey {
    public final I_Location location;
    public final int time;

    public TimeLocation(Constraint constraint) {
        this.location = constraint.location;
        this.time = constraint.time;
    }

    public TimeLocation(Move move){
        this.location = move.currLocation;
        this.time = move.timeNow;
    }

    public TimeLocation(TimeLocation toCopy){
        this.location = toCopy.location;
        this.time = toCopy.time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeLocation)) return false;

        TimeLocation that = (TimeLocation) o;

        if (time != that.time) return false;
        return location.equals(that.location);

    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + time;
        return result;
    }

    @Override
    public boolean relevantInTheFuture(Move finalMove) {
        return this.time > finalMove.timeNow && this.location.equals(finalMove.currLocation);
    }

    @Override
    public int getTime() {
        return this.time;
    }

    @Override
    public I_Location getLocation() {
        return this.location;
    }
}
