package GraphMapPackage;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import LargeAgents_CBS.Instances.Maps.Enum_direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for the creation of instances of all classes that implement {@link I_Map}.
 * Factory design pattern.
 */
public class MapFactory {

    /**
     * Generates a new 4-connected {@link GraphMap} from a square, 2D grid.
     *
     * Simple - Only 2 {@link Enum_MapCellType cell types} exist, {@link Enum_MapCellType#EMPTY} and
     * {@link Enum_MapCellType#WALL}. {@link Enum_MapCellType#EMPTY} cells are passable, and can only connect to other
     * {@link Enum_MapCellType#EMPTY} cells. {@link Enum_MapCellType#WALL} cells are impassable, and can not connect to
     * any other cell, so they will not be generated.
     * @param rectangle_2D_Map A rectangle grid representing a map, containing only {@link Enum_MapCellType#EMPTY} and
     *                      {@link Enum_MapCellType#WALL}. The length of its first dimension should correspond to the
     *                         original map's x dimension.
     * @return a new 4-connected {@link GraphMap}.
     */
    public static GraphMap newSimple4Connected2D_GraphMap(Enum_MapCellType[][] rectangle_2D_Map){
        int xAxis_length = rectangle_2D_Map.length;
        int yAxis_length = rectangle_2D_Map[0].length;
        GraphMapVertex[][] cells = new GraphMapVertex[xAxis_length][yAxis_length]; //rectangle map
        //generate all cells
        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {
                if(rectangle_2D_Map[xIndex][yIndex] == Enum_MapCellType.EMPTY){
                    cells[xIndex][yIndex] = new GraphMapVertex(rectangle_2D_Map[xIndex][yIndex], new Coordinate_2D(xIndex, yIndex));
                }
            }
        }
        HashMap<I_Coordinate, GraphMapVertex> allCells = new HashMap<>(); //to be used for GraphMap constructor
        //connect cells to their neighbors (4-connected)
        ArrayList<GraphMapVertex> neighbors = new ArrayList<>(4);
        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {
                GraphMapVertex currentCell = cells[xIndex][yIndex];
                if(cells[xIndex][yIndex] != null){
                    neighbors.clear();
                    //look for WEST neighbor
                    if(xIndex-1 >= 0 && cells[xIndex-1][yIndex] != null){neighbors.add(cells[xIndex-1][yIndex]);}
                    //look for EAST neighbor
                    if(xIndex+1 < xAxis_length && cells[xIndex+1][yIndex] != null){neighbors.add(cells[xIndex+1][yIndex]);}
                    //look for NORTH neighbor
                    if(yIndex-1 >= 0 && cells[xIndex][yIndex-1] != null){neighbors.add(cells[xIndex][yIndex-1]);}
                    //look for SOUTH neighbor
                    if(yIndex+1 < yAxis_length && cells[xIndex][yIndex+1] != null){neighbors.add(cells[xIndex][yIndex+1]);}
                    // set cell neighbors
                    currentCell.setNeighbors(neighbors.toArray(new GraphMapVertex[0]));
                    // add to allCells
                    allCells.put(currentCell.coordinate, currentCell);
                }
            }
        }
        return new GraphMap(allCells);
    }



    public static GraphMap newSimple4Connected2D_GraphMap_LargeAgents(Enum_MapCellType[][] rectangle_2D_Map) {
        int xAxis_length = rectangle_2D_Map.length;
        int yAxis_length = rectangle_2D_Map[0].length;
        GraphMapVertex_LargeAgents[][] cells = new GraphMapVertex_LargeAgents[xAxis_length][yAxis_length]; //rectangle map
        //generate all cells
        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {
                if (rectangle_2D_Map[xIndex][yIndex] == Enum_MapCellType.EMPTY) {
                    cells[xIndex][yIndex] = new GraphMapVertex_LargeAgents(rectangle_2D_Map[xIndex][yIndex], new Coordinate_2D(xIndex, yIndex));
                }
            }
        }
        HashMap<I_Coordinate, GraphMapVertex_LargeAgents> allCells = new HashMap<>(); //to be used for GraphMap constructor
        //connect cells to their neighbors (4-connected)
        Map<I_Location, Enum_direction> neighbors = new HashMap<>(4);
        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {
                GraphMapVertex_LargeAgents currentCell = cells[xIndex][yIndex];
                if (cells[xIndex][yIndex] != null) {
                    neighbors.clear();
                    //look for WEST neighbor
                    if (xIndex - 1 >= 0 && cells[xIndex - 1][yIndex] != null) {
                        addGraphMapCellToMap(neighbors, cells[xIndex - 1][yIndex], Enum_direction.WEST);
                    }
                    //look for EAST neighbor
                    if (xIndex + 1 < xAxis_length && cells[xIndex + 1][yIndex] != null) {
                        addGraphMapCellToMap(neighbors, cells[xIndex + 1][yIndex], Enum_direction.EAST);
                    }
                    //look for NORTH neighbor
                    if (yIndex - 1 >= 0 && cells[xIndex][yIndex - 1] != null) {
                        addGraphMapCellToMap(neighbors, cells[xIndex][yIndex - 1], Enum_direction.NORTH);
                    }
                    //look for SOUTH neighbor
                    if (yIndex + 1 < yAxis_length && cells[xIndex][yIndex + 1] != null) {
                        addGraphMapCellToMap(neighbors, cells[xIndex][yIndex + 1], Enum_direction.SOUTH);
                    }

                    // Done - return Map with directions
                    // set cell neighbors
                    currentCell.setNeighbors(neighbors);

                    // add to allCells
                    allCells.put(currentCell.coordinate, currentCell);
                }
            }
        }
        return new GraphMap(allCells);
    }


    private static void addGraphMapCellToMap(Map<I_Location, Enum_direction> map, GraphMapVertex_LargeAgents cell, Enum_direction direction) {
        map.put(cell, direction);
    }





    /* nicetohave - 8 connected 2D map
    public static GraphMap newSimple8Connected2D_GraphMap(Enum_MapCellType[][] map_2D){
        return null;
    }
    */

    /* nicetohave - 3D map
    public static GraphMap newSimple6Connected3D_GraphMap(Enum_MapCellType[][] map_2D){
        return null;
    }
    */

}
