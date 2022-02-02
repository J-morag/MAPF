package BasicMAPF.Maps;

import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapFactoryTest {

    private final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final Enum_MapLocationType w = Enum_MapLocationType.WALL;

    @BeforeEach
    void setUp() {

    }

    /*  = Tests on Sample 2D Maps =  */

    private Enum_MapLocationType[][] map_2D_1Location_middle = {
            {w, w, w, w, w, w},
            {w, e, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_1Location_middle(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_1Location_middle);
        checkGraphMapProperties(map,1, new Coordinate_2D[]{asCoordinate2D(1,1)} );

    }

    private Enum_MapLocationType[][] map_2D_1Location_fringe = {
            {w, w, w, w, w, w},
            {e, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_1Location_fringe(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_1Location_fringe);
        checkGraphMapProperties(map,1, new Coordinate_2D[]{asCoordinate2D(1,0)} );
    }

    private Enum_MapLocationType[][] map_2D_2Locations_middle = {
            {w, w, w, w, w, w},
            {w, w, w, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_2Locations_middle(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_2Locations_middle);
        checkGraphMapProperties(map,2, new Coordinate_2D[]{asCoordinate2D(1,3),
                asCoordinate2D(1,4)} );
        assertAreNeighbors(map, asCoordinate2D(1,3), asCoordinate2D(1,4));
    }

    private Enum_MapLocationType[][] map_2D_2Locations_fringe = {
            {w, w, w, w, e, w},
            {w, w, w, w, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_2Locations_fringe(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_2Locations_fringe);
        checkGraphMapProperties(map,2, new Coordinate_2D[]{asCoordinate2D(0,4),
                asCoordinate2D(1,4)} );
        assertAreNeighbors(map, asCoordinate2D(0,4), asCoordinate2D(1,4));
    }

    private Enum_MapLocationType[][] map_2D_2Locations_diagonal = {
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, e, w, w, w},
            {w, w, w, e, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_2Locations_diagonal(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_2Locations_diagonal);
        checkGraphMapProperties(map,2, new Coordinate_2D[]{asCoordinate2D(3,2),
                asCoordinate2D(4,3)} );

        assertNotNeighbors(map, asCoordinate2D(3,2), asCoordinate2D(4,3));
    }

    private Enum_MapLocationType[][] map_2D_3Locations_line = {
            {w, w, w, e, w, w},
            {w, w, w, e, w, w},
            {w, w, w, e, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_3Locations_line(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_3Locations_line);
        checkGraphMapProperties(map,3, new Coordinate_2D[]{asCoordinate2D(0, 3),
                asCoordinate2D(1,3), asCoordinate2D(2,3)} );

        assertAreNeighbors(map, asCoordinate2D(0,3), asCoordinate2D(1,3));
        assertAreNeighbors(map, asCoordinate2D(1,3), asCoordinate2D(2,3));

        assertNotNeighbors(map, asCoordinate2D(0, 3), asCoordinate2D(2,3));
    }

    private Enum_MapLocationType[][] map_2D_4locations_clump = {
            {w, w, w, w, w, w},
            {w, w, w, e, e, w},
            {w, w, w, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_4locations_clump(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_4locations_clump);
        checkGraphMapProperties(map,4, new Coordinate_2D[]{asCoordinate2D(1, 3),
                asCoordinate2D(2, 3), asCoordinate2D(1,4), asCoordinate2D(2,4)} );

        assertAreNeighbors(map, asCoordinate2D(1,3), asCoordinate2D(2,3));
        assertAreNeighbors(map, asCoordinate2D(2,3), asCoordinate2D(2,4));
        assertAreNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(2,4));
        assertAreNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(1,3));
    }

    private Enum_MapLocationType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_circle(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);
        Coordinate_2D[] expectedCoordinaes = new Coordinate_2D[]{asCoordinate2D(1,2),
                asCoordinate2D(1,3), asCoordinate2D(1,4),
                asCoordinate2D(2,2), asCoordinate2D(2,4),
                asCoordinate2D(3,2), asCoordinate2D(3,3),
                asCoordinate2D(3,4)};
        checkGraphMapProperties(map,8, expectedCoordinaes );

        assertAreNeighbors(map, asCoordinate2D(1,2), asCoordinate2D(1,3));
        assertAreNeighbors(map, asCoordinate2D(1,3), asCoordinate2D(1,4));
        assertAreNeighbors(map, asCoordinate2D(2,2), asCoordinate2D(1,2));
        assertAreNeighbors(map, asCoordinate2D(3,2), asCoordinate2D(3,3));
        assertAreNeighbors(map, asCoordinate2D(3,3), asCoordinate2D(3,4));
        assertAreNeighbors(map, asCoordinate2D(2,4), asCoordinate2D(3,4));

        assertNotNeighbors(map, asCoordinate2D(2,2), asCoordinate2D(2,4));
    }

    private Enum_MapLocationType[][] map_2D_corners = {
            {e, w, w, w, w, e},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {e, w, w, w, w, e},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_corners(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_corners);
        checkGraphMapProperties(map,4, new Coordinate_2D[]{asCoordinate2D(0,0),
                asCoordinate2D(5,0), asCoordinate2D(0,5), asCoordinate2D(5,5)} );
    }

    private Enum_MapLocationType[][] map_2D_disjointGroups = {
            {w, w, w, w, w, w},
            {w, w, w, w, e, w},
            {w, w, w, w, w, w},
            {w, w, e, e, w, w},
            {w, w, w, e, w, e},
            {w, w, w, w, w, e},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_disjointGroups(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_disjointGroups);
        checkGraphMapProperties(map,6, new Coordinate_2D[]{asCoordinate2D(1,4),
                asCoordinate2D(3,2), asCoordinate2D(3,3), asCoordinate2D(4,3),
                asCoordinate2D(4,5), asCoordinate2D(5,5)} );

        assertAreNeighbors(map, asCoordinate2D(3,2), asCoordinate2D(3,3));
        assertAreNeighbors(map, asCoordinate2D(3,3), asCoordinate2D(4,3));

        assertAreNeighbors(map, asCoordinate2D(4,5), asCoordinate2D(5,5));

        assertNotNeighbors(map, asCoordinate2D(3,2), asCoordinate2D(4,5));
        assertNotNeighbors(map, asCoordinate2D(3,2), asCoordinate2D(5,5));
        assertNotNeighbors(map, asCoordinate2D(3,3), asCoordinate2D(4,5));
        assertNotNeighbors(map, asCoordinate2D(3,3), asCoordinate2D(5,5));
        assertNotNeighbors(map, asCoordinate2D(4,3), asCoordinate2D(4,5));
        assertNotNeighbors(map, asCoordinate2D(4,3), asCoordinate2D(5,5));

        assertNotNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(3,2));
        assertNotNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(3,3));
        assertNotNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(4,3));
        assertNotNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(4,5));
        assertNotNeighbors(map, asCoordinate2D(1,4), asCoordinate2D(5,5));
    }

    private Enum_MapLocationType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_empty(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);
        List<Coordinate_2D> expectedCoordinates = new ArrayList<>(6*6);
        for (int x = 0; x < map_2D_empty.length; x++) {
            for (int y = 0; y < map_2D_empty[0].length; y++) {
                expectedCoordinates.add(asCoordinate2D(x, y));
            }
        }
        checkGraphMapProperties(map,6*6, expectedCoordinates.toArray(Coordinate_2D[]::new));
        assertAreNeighbors(map, asCoordinate2D(0,0), asCoordinate2D(0,1));
        assertAreNeighbors(map, asCoordinate2D(4,5), asCoordinate2D(5,5));
        assertAreNeighbors(map, asCoordinate2D(2,1), asCoordinate2D(3,1));
    }

    private Enum_MapLocationType[][] map_2D_allWalls = {
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    @Test
    void test_newSimple4Connected2D_GraphMap_map_2D_allWalls(){
        GraphMap map = MapFactory.newSimple4Connected2D_GraphMap(map_2D_allWalls);
        checkGraphMapProperties(map,0, new Coordinate_2D[]{});
    }


    /*  = Utility functions =  */

    static void checkGraphMapProperties(GraphMap map, int numLocations, Coordinate_2D[] containsCoordinates){
        assertEquals(numLocations, map.getNumMapLocations());
        for (Coordinate_2D coor:
            containsCoordinates){
            assertTrue(map.isValidCoordinate(coor));
        }
    }

    static void assertAreNeighbors(I_Map map, I_Coordinate coor1, I_Coordinate coor2){
        I_Location location1 = map.getMapLocation(coor1);
        I_Location location2 = map.getMapLocation(coor2);
        assertTrue(location1.isNeighbor(location2) && location2.isNeighbor(location1));
    }

    static void assertNotNeighbors(I_Map map, I_Coordinate coor1, I_Coordinate coor2){
        I_Location location1 = map.getMapLocation(coor1);
        I_Location location2 = map.getMapLocation(coor2);
        assertFalse(location1.isNeighbor(location2) && location2.isNeighbor(location1));
    }

    static Coordinate_2D asCoordinate2D(int iIndex, int jIndex){
        return new Coordinate_2D(iIndex, jIndex);
    }

}