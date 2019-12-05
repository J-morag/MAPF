package BasicCBS.Instances.Maps.Coordinates;

import java.util.Objects;

public class Coordinate_3D implements I_Coordinate {

    private int x_value;
    private int y_value;
    private int z_value;



    public Coordinate_3D(int x_value, int y_value, int z_value ) {
        this.x_value = x_value;
        this.y_value = y_value;
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
        return getX_value() == that.getX_value() &&
                getY_value() == that.getY_value() &&
                getZ_value() == that.getZ_value();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX_value(), getY_value(), getZ_value());
    }


    @Override
    public String toString() {
        return "Coordinate_3D{" +
                "x_value=" + x_value +
                ", y_value=" + y_value +
                ", Z_value=" + z_value +
                '}';
    }

    @Override
    public float distance(I_Coordinate other) {
        return 0;
    }
}
