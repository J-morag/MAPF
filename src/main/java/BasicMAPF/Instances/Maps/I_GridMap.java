package BasicMAPF.Instances.Maps;

public interface I_GridMap extends I_ExplicitMap{

        int getWidth();
        int getHeight();

        boolean isObstacle(int x, int y);
        boolean isObstacle(int[] xy);

        boolean isFree(int x, int y);
        boolean isFree(int[] xy);

        boolean isOnMap(int x, int y);
        boolean isOnMap(int[] xy);

        int[] getXY(I_Location location);

}
