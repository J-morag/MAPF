package LargeAgents_CBS.Instances.Maps;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.GraphMapVertex;
import BasicCBS.Instances.Maps.I_Location;

import java.util.HashMap;
import java.util.Map;

public class MapFactory_LargeAgents {

    public static GraphMap newSimple4Connected2D_GraphMap(Enum_MapCellType[][] rectangle_2D_Map) {
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
}
