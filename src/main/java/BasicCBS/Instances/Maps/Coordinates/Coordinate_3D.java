package BasicCBS.Instances.Maps.Coordinates;

import java.util.Objects;

public class Coordinate_3D extends Coordinate_2D {

    private int x_value;
    private int y_value;
    private int z_value;

    public Coordinate_3D(int x_value, int y_value, int z_value ) {
        super(x_value, y_value);
        this.z_value = z_value;
    }

    public int getX_value() {
        return x_value;
    }

    public void setX_value(int x_value) {
        this.x_value = x_value;
    }

    public int getY_value() {
        return y_value;
    }

    public void setY_value(int y_value) {
        this.y_value = y_value;
    }

    public int getZ_value() {
        return z_value;
    }

    public void setZ_value(int z_value) {
        this.z_value = z_value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate_3D)) return false;

        Coordinate_3D that = (Coordinate_3D) o;

        if (x_value != that.x_value) return false;
        if (y_value != that.y_value) return false;
        return z_value == that.z_value;
    }

    @Override
    public int hashCode() {
        int result = x_value;
        result = 31 * result + y_value;
        result = 31 * result + z_value;
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
        if (!(other instanceof Coordinate_3D)) return -1;
        Coordinate_3D that = (Coordinate_3D) other;
        return (float)Math.sqrt(
                Math.pow((this.y_value-that.y_value), 2) + Math.pow((this.x_value-that.x_value), 2)
                        + Math.pow((this.z_value-that.z_value), 2) );
    }

    /**
     * Returns the manhattan distance to another {@link I_Coordinate coordinate}. Should return 0 iff this.equals(other)
     * return true.  If other is null, or is not of the same runtime type as this, returns -1.
     * @param other a {@link I_Coordinate coordinate}.
     * @return the manhattan distance to another {@link I_Coordinate coordinate}.  If other is null, or is not of the
     * same runtime type as this, returns -1.
     */
    private int manhattanDistance(I_Coordinate other) {
        if (!(other instanceof Coordinate_3D)) return -1;
        Coordinate_3D that = (Coordinate_3D) other;
        return Math.abs(this.y_value-that.y_value) + Math.abs(this.x_value-that.x_value)
                + Math.abs(this.z_value-that.z_value);
    }

    @Override
    public float distance(I_Coordinate other) {
        return manhattanDistance(other);
    }
}
