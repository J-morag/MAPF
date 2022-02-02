package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures;

import BasicMAPF.Instances.Maps.I_Location;

public class TimeLocation {

    public int time;
    public I_Location location;

    /**
     * This class Wraps up both time and location as one element.
     * Class is used in timeLocation_agents data structure
     * @param time - An int of the time unit in the solution
     * @param location - {@inheritDoc}
     */
    public TimeLocation(int time, I_Location location) {
        this.time = time;
        this.location = location;
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
        int result = time;
        result = 31 * result + location.hashCode();
        return result;
    }
}