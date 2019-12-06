package LargeAgents;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import LargeAgents_CBS.Instances.Maps.GraphLocationGroup;
import LargeAgents_CBS.Instances.Maps.GraphMapVertex_LargeAgents;
import LargeAgents_CBS.Instances.Maps.MapFactory_LargeAgents;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphLocationGroupTest {


    private final Enum_MapCellType e = Enum_MapCellType.EMPTY;
    private final Enum_MapCellType w = Enum_MapCellType.WALL;
    private Enum_MapCellType[][] map_3By5 = {
            { e, e, e},
            { e, e, e},
            { e, e, e},
            { e, e, e},
            { e, e, e},
    };
    private I_Map map5 = MapFactory_LargeAgents.newSimple4Connected2D_GraphMap(map_3By5);



    @Test
    public void generate1By1Group(){

        GraphMapVertex_LargeAgents[][] graphMapCells_actual = new GraphMapVertex_LargeAgents[1][1];
        GraphMapVertex_LargeAgents[][] graphMapCells_eastNeighbor = new GraphMapVertex_LargeAgents[1][1];
        GraphMapVertex_LargeAgents[][] graphMapCells_southNeighbor = new GraphMapVertex_LargeAgents[1][1];
        for (int i = 0; i < graphMapCells_actual.length; i++) {
            for (int j = 0; j < graphMapCells_actual[i].length; j++) {
                graphMapCells_actual[i][j] = (GraphMapVertex_LargeAgents) map5.getMapCell(new Coordinate_2D(i, j));
                graphMapCells_eastNeighbor[i][j] = (GraphMapVertex_LargeAgents) map5.getMapCell(new Coordinate_2D(i + 1, j));
                graphMapCells_southNeighbor[i][j] = (GraphMapVertex_LargeAgents) map5.getMapCell(new Coordinate_2D(i, j + 1));
            }
        }


        /* Agent fills map
            [a . . . .]
            [. . . . .]
            [. . . . .]
        */
        GraphLocationGroup locationGroup_actual = new GraphLocationGroup(graphMapCells_actual);
        GraphLocationGroup locationGroup_east = new GraphLocationGroup(graphMapCells_eastNeighbor);
        GraphLocationGroup locationGroup_south = new GraphLocationGroup(graphMapCells_southNeighbor);

        List<I_Location> neighbors = locationGroup_actual.getNeighbors();
        Assert.assertEquals(2,neighbors.size());

        boolean isEastNeighbor = locationGroup_actual.isNeighbor(locationGroup_east);
        boolean isSouthNeighbor = locationGroup_actual.isNeighbor(locationGroup_south);
    }



    @Test
    public void generate3By3Group(){


        GraphMapVertex_LargeAgents[][] graphMapCells_actual = new GraphMapVertex_LargeAgents[3][3];
        GraphMapVertex_LargeAgents[][] graphMapCells_eastNeighbor = new GraphMapVertex_LargeAgents[3][3];
        GraphMapVertex_LargeAgents[][] graphMapCells_westNeighbor = new GraphMapVertex_LargeAgents[3][3];
        for (int i = 0; i < graphMapCells_actual.length; i++) {
            for (int j = 0; j < graphMapCells_actual[i].length; j++) {
                graphMapCells_actual[i][j] = (GraphMapVertex_LargeAgents) map5.getMapCell(new Coordinate_2D(i + 1, j));
                graphMapCells_eastNeighbor[i][j] = (GraphMapVertex_LargeAgents) map5.getMapCell(new Coordinate_2D(i + 2, j));
                graphMapCells_westNeighbor[i][j] = (GraphMapVertex_LargeAgents) map5.getMapCell(new Coordinate_2D(i, j));
            }
        }


        /* Agent fills map
            [. a a a .]
            [. a a a .]
            [. a a a .]
        */
        GraphLocationGroup locationGroup_actual = new GraphLocationGroup(graphMapCells_actual);
        GraphLocationGroup locationGroup_east = new GraphLocationGroup(graphMapCells_eastNeighbor);
        GraphLocationGroup locationGroup_west = new GraphLocationGroup(graphMapCells_westNeighbor);

        List<I_Location> neighbors = locationGroup_actual.getNeighbors();
        Assert.assertEquals(2,neighbors.size());

        boolean isEastNeighbor = locationGroup_actual.isNeighbor(locationGroup_east);
        boolean isWestNeighbor = locationGroup_actual.isNeighbor(locationGroup_west);
    }
}