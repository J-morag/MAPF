package BasicMAPF.Instances.Maps.Coordinates;

/**
 * A data type which represents a coordinate in two-dimensional space.
 */
public class Coordinate_2D implements I_Coordinate {

    public final int x_value;
    public final int y_value;


    public Coordinate_2D(int x_value, int y_value) {
        this.x_value = x_value;
        this.y_value = y_value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate_2D)) return false;

        Coordinate_2D that = (Coordinate_2D) o;

        if (x_value != that.x_value) return false;
        return y_value == that.y_value;
    }

    @Override
    public int hashCode() {
        int result = x_value;
        result = 31 * result + y_value;
        return result;
    }

    @Override
    public String toString() {
        return "(" + this.x_value + "," + this.y_value + ")";
    }

    /**
     * Returns the euclidean distance to another {@link I_Coordinate coordinate}. Should return 0 iff this.equals(other)
     * return true.  If other is null, or is not of the same runtime type as this, returns -1.
     * @param other a {@link I_Coordinate coordinate}.
     * @return the euclidean distance to another {@link I_Coordinate coordinate}. If other is null, or is not of the
     * same runtime type as this, returns -1.
     */
    private float euclideanDistance(I_Coordinate other) {
        if (!(other instanceof Coordinate_2D)) return -1;
        Coordinate_2D that = (Coordinate_2D) other;
        return (float)Math.sqrt(
                Math.pow((this.y_value-that.y_value), 2) + Math.pow((this.x_value-that.x_value), 2) );
    }

    /**
     * Returns the manhattan distance to another {@link I_Coordinate coordinate}. Should return 0 iff this.equals(other)
     * return true.  If other is null, or is not of the same runtime type as this, returns -1.
     * @param other a {@link I_Coordinate coordinate}.
     * @return the manhattan distance to another {@link I_Coordinate coordinate}.  If other is null, or is not of the
     * same runtime type as this, returns -1.
     */
    private int manhattanDistance(I_Coordinate other) {
        if (!(other instanceof Coordinate_2D)) return -1;
        Coordinate_2D that = (Coordinate_2D) other;
        return Math.abs(this.y_value-that.y_value) + Math.abs(this.x_value-that.x_value);
    }

    @Override
    public float distance(I_Coordinate other) {
        return manhattanDistance(other);
    }
}
