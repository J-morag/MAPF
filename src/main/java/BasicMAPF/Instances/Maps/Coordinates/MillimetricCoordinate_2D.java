package BasicMAPF.Instances.Maps.Coordinates;

public class MillimetricCoordinate_2D extends Coordinate_2D {

    public static final float STICKER_DISTANCE_UNIT_MM = 490;

    public MillimetricCoordinate_2D(int x_value, int y_value) {
        super(x_value, y_value);
    }

    @Override
    public float distance(I_Coordinate other) {
        if (other == null || getClass() != other.getClass()) return -1;
        Coordinate_2D that = (Coordinate_2D) other;
        return Math.round((float)(Math.abs(this.y_value-that.y_value))/STICKER_DISTANCE_UNIT_MM) + Math.round((float)(Math.abs(this.x_value-that.x_value))/STICKER_DISTANCE_UNIT_MM);
    }
}
