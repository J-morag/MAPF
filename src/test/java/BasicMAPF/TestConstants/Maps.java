package BasicMAPF.TestConstants;

import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Instances.Maps.MapFactory;

public class Maps {
    private final static Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final static Enum_MapLocationType w = Enum_MapLocationType.WALL;
    public static final  Enum_MapLocationType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    public static final I_Map mapCircle = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);

    public static final Enum_MapLocationType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    public static final I_Map mapEmpty = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);

    public static final Enum_MapLocationType[][] map_2D_withPocket = {
            {e, w, e, w, e, w},
            {e, w, e, e, e, e},
            {w, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, e, w, e, e, e},
    };
    public static final I_Map mapWithPocket = MapFactory.newSimple4Connected2D_GraphMap(map_2D_withPocket);

    public static final Enum_MapLocationType[][] map_2D_smallMaze = {
            {e, e, e, w, e, w},
            {e, w, e, e, e, e},
            {e, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, w, w, e, e, e},
    };
    public static final I_Map mapSmallMaze = MapFactory.newSimple4Connected2D_GraphMap(map_2D_smallMaze);

    public static final Enum_MapLocationType[][] map_2D_H = {
            { e, w, w, e},
            { e, e, e, e},
            { e, w, w, e},
    };
    public static final I_Map mapH = MapFactory.newSimple4Connected2D_GraphMap(map_2D_H);

    public static final Enum_MapLocationType[][] twoLocationMap = new Enum_MapLocationType[][]{{e,e}};
    public static final I_Map mapTwoLocations = MapFactory.newSimple4Connected2D_GraphMap(twoLocationMap);

    public static final Enum_MapLocationType[][] map_2D_H_long = {
            {e, w, w, e},
            {e, w, w, e},
            {e, e, e, e},
            {e, w, w, e},
            {e, w, w, e},
    };
    public static final I_Map mapHLong = MapFactory.newSimple4Connected2D_GraphMap(map_2D_H_long);
}
