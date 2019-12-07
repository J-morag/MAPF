package LargeAgents;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import GraphMapPackage.MapFactory;
import LargeAgents_CBS.Instances.Maps.Enum_direction;
import LargeAgents_CBS.Instances.Maps.GraphLocationGroup;
import GraphMapPackage.GraphMapVertex_LargeAgents;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    private I_Map iMap3By5 = MapFactory.newSimple4Connected2D_GraphMap_LargeAgents(map_3By5);


    Enum_MapCellType[][] map_twoWalls = {
            {e, e, e, e},
            {e, e, e, e},
            {e, w, e, e},
            {e, w, e, e},
    };

    private I_Map iMap_twoWalls = MapFactory.newSimple4Connected2D_GraphMap_LargeAgents(map_twoWalls);



    @Test
    public void checkComparator(){

        GraphMapVertex_LargeAgents[][] cells = new GraphMapVertex_LargeAgents[2][2];
        cells[0][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,0));
        cells[0][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,1));
        cells[1][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,0));
        cells[1][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,1));

        GraphLocationGroup group_0_0_1_1 = new GraphLocationGroup(cells);

        GraphMapVertex_LargeAgents expectedCellWithMinNeighbors = cells[0][0];
        GraphMapVertex_LargeAgents actualCellWithMinNeighbors = (GraphMapVertex_LargeAgents) group_0_0_1_1.getCellWithMinimumNeighbors();
        Assert.assertEquals(expectedCellWithMinNeighbors, actualCellWithMinNeighbors);
    }


    @Test
    public void generate1By1Group(){

        GraphMapVertex_LargeAgents[][] graphMapCells_actual = new GraphMapVertex_LargeAgents[1][1];
        GraphMapVertex_LargeAgents[][] graphMapCells_eastNeighbor = new GraphMapVertex_LargeAgents[1][1];
        GraphMapVertex_LargeAgents[][] graphMapCells_southNeighbor = new GraphMapVertex_LargeAgents[1][1];
        for (int i = 0; i < graphMapCells_actual.length; i++) {
            for (int j = 0; j < graphMapCells_actual[i].length; j++) {
                graphMapCells_actual[i][j] = (GraphMapVertex_LargeAgents) iMap3By5.getMapCell(new Coordinate_2D(i, j));
                graphMapCells_eastNeighbor[i][j] = (GraphMapVertex_LargeAgents) iMap3By5.getMapCell(new Coordinate_2D(i + 1, j));
                graphMapCells_southNeighbor[i][j] = (GraphMapVertex_LargeAgents) iMap3By5.getMapCell(new Coordinate_2D(i, j + 1));
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

        Assert.assertTrue(locationGroup_actual.isNeighbor(locationGroup_east));
        Assert.assertTrue( locationGroup_actual.isNeighbor(locationGroup_south));
    }



    @Test
    public void generate3By3Group(){


        GraphMapVertex_LargeAgents[][] graphMapCells_actual = new GraphMapVertex_LargeAgents[3][3];
        GraphMapVertex_LargeAgents[][] graphMapCells_eastNeighbor = new GraphMapVertex_LargeAgents[3][3];
        GraphMapVertex_LargeAgents[][] graphMapCells_westNeighbor = new GraphMapVertex_LargeAgents[3][3];
        for (int i = 0; i < graphMapCells_actual.length; i++) {
            for (int j = 0; j < graphMapCells_actual[i].length; j++) {
                graphMapCells_actual[i][j] = (GraphMapVertex_LargeAgents) iMap3By5.getMapCell(new Coordinate_2D(i + 1, j));
                graphMapCells_eastNeighbor[i][j] = (GraphMapVertex_LargeAgents) iMap3By5.getMapCell(new Coordinate_2D(i + 2, j));
                graphMapCells_westNeighbor[i][j] = (GraphMapVertex_LargeAgents) iMap3By5.getMapCell(new Coordinate_2D(i, j));
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

        Assert.assertTrue(locationGroup_actual.isNeighbor(locationGroup_east));
        Assert.assertTrue(locationGroup_actual.isNeighbor(locationGroup_west));
    }


    @Test
    public void checkForOneNeighbor(){


        // Check for one neighbor
        // GG - Group, N1 - Neighbor1, N2 - Neighbor2
        //       X0  X1  X2  X3         X0  X1  X2  X3
        //    Y0{GG, GG, EE, EE}     Y0{EE, EE, EE, EE}
        //    Y1{GG, GG, WW, WW}     Y1{N1, N1, WW, WW}
        //    Y2{EE, EE, EE, EE}     Y2{N1, N1, EE, EE}
        //    Y3{EE, EE, EE, EE}     Y3{EE, EE, EE, EE}

        GraphMapVertex_LargeAgents[][] expectedNeighbor_Cells_0_1_1_2 = new GraphMapVertex_LargeAgents[2][2];
        expectedNeighbor_Cells_0_1_1_2[0][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,1));
        expectedNeighbor_Cells_0_1_1_2[0][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,2));
        expectedNeighbor_Cells_0_1_1_2[1][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,1));
        expectedNeighbor_Cells_0_1_1_2[1][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,2));

        GraphLocationGroup expected_neighbor_0_1_1_2 = new GraphLocationGroup(expectedNeighbor_Cells_0_1_1_2);

        GraphMapVertex_LargeAgents[][] cells = new GraphMapVertex_LargeAgents[2][2];
        cells[0][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,0));
        cells[0][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,1));
        cells[1][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,0));
        cells[1][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,1));

        GraphLocationGroup group_0_0_1_1 = new GraphLocationGroup(cells);

        List<I_Location> actual_neighborList_0_0_1_1 = group_0_0_1_1.getNeighbors();
        Assert.assertTrue(actual_neighborList_0_0_1_1.contains(expected_neighbor_0_1_1_2));

        // check neighbor 0_1_1_2
        Enum_direction actualSouth = group_0_0_1_1.getNeighborDirection(expected_neighbor_0_1_1_2);
        Assert.assertTrue(actual_neighborList_0_0_1_1.contains(expected_neighbor_0_1_1_2));
        Assert.assertEquals(Enum_direction.SOUTH, actualSouth);

    }


    @Test
    public void checkForTwoNeighborsAndDirections(){

        // Check for two neighbors
        // GG - Group, N1 - Neighbor1, N2 - Neighbor2
        //       X0  X1  X2  X3         X0  X1  X2  X3       X0  X1  X2  X3
        //    Y0{EE, EE, EE, EE}     Y0{N1, N1, EE, EE}   Y0{EE, EE, EE, EE}
        //    Y1{GG, GG, WW, WW}     Y1{N1, N1, WW, WW}   Y1{EE, EE, WW, WW}
        //    Y2{GG, GG, EE, EE}     Y2{EE, EE, EE, EE}   Y2{N2, N2, EE, EE}
        //    Y3{EE, EE, EE, EE}     Y3{EE, EE, EE, EE}   Y3{N2, N2, EE, EE}

        GraphMapVertex_LargeAgents[][] cells_0_1_1_2 = new GraphMapVertex_LargeAgents[2][2];
        cells_0_1_1_2[0][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,1));
        cells_0_1_1_2[0][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,2));
        cells_0_1_1_2[1][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,1));
        cells_0_1_1_2[1][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,2));

        GraphLocationGroup group_0_1_1_2 = new GraphLocationGroup(cells_0_1_1_2);

        GraphMapVertex_LargeAgents[][] expectedNeighbor_Cells_0_0_1_1 = new GraphMapVertex_LargeAgents[2][2];
        expectedNeighbor_Cells_0_0_1_1[0][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,0));
        expectedNeighbor_Cells_0_0_1_1[0][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,1));
        expectedNeighbor_Cells_0_0_1_1[1][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,0));
        expectedNeighbor_Cells_0_0_1_1[1][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,1));

        GraphLocationGroup expected_neighbor_0_0_1_1 = new GraphLocationGroup(expectedNeighbor_Cells_0_0_1_1);

        GraphMapVertex_LargeAgents[][] expectedNeighbor_Cells_0_2_1_3 = new GraphMapVertex_LargeAgents[2][2];
        expectedNeighbor_Cells_0_2_1_3[0][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,2));
        expectedNeighbor_Cells_0_2_1_3[0][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(0,3));
        expectedNeighbor_Cells_0_2_1_3[1][0] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,2));
        expectedNeighbor_Cells_0_2_1_3[1][1] = (GraphMapVertex_LargeAgents) iMap_twoWalls.getMapCell(new Coordinate_2D(1,3));

        GraphLocationGroup expected_neighbor_0_2_1_3 = new GraphLocationGroup(expectedNeighbor_Cells_0_2_1_3);

        List<I_Location> actual_neighborList_0_1_1_2 = group_0_1_1_2.getNeighbors();

        // check neighbor 0_0_1_1 from North
        Enum_direction actualNorth = group_0_1_1_2.getNeighborDirection(expected_neighbor_0_0_1_1);
        Assert.assertTrue(actual_neighborList_0_1_1_2.contains(expected_neighbor_0_0_1_1));
        Assert.assertEquals(Enum_direction.NORTH, actualNorth);

        // check neighbor 0_2_1_3 from South
        Enum_direction actualSouth = group_0_1_1_2.getNeighborDirection(expected_neighbor_0_2_1_3);
        Assert.assertTrue(actual_neighborList_0_1_1_2.contains(expected_neighbor_0_2_1_3));
        Assert.assertEquals(Enum_direction.SOUTH, actualSouth);
    }




}