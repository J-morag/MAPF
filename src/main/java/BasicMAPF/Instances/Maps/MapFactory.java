package BasicMAPF.Instances.Maps;

import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Responsible for the creation of instances of all classes that implement {@link I_Map}.
 * Factory design pattern.
 */
public class MapFactory {

    /**
     * Generates a new 4-connected {@link GraphMap} from a square, 2D grid.
     * Simple - Only 2 {@link Enum_MapLocationType location types} exist, {@link Enum_MapLocationType#EMPTY} and
     * {@link Enum_MapLocationType#WALL}. {@link Enum_MapLocationType#EMPTY} locations are passable, and can only connect to other
     * {@link Enum_MapLocationType#EMPTY} locations. {@link Enum_MapLocationType#WALL} locations are impassable, and can not connect to
     * any other location, so they will not be generated.
     * @param rectangle_2D_Map A rectangle grid representing a map, containing only {@link Enum_MapLocationType#EMPTY} and
     *                      {@link Enum_MapLocationType#WALL}. The length of its first dimension should correspond to the
     *                         original map's x dimension.
     * @param isOneConnectedComponent if the grid is one connected component.
     * @return a new 4-connected {@link GraphMap}.
     */
    public static GraphBasedGridMap newSimple4Connected2D_GraphMap(Enum_MapLocationType[][] rectangle_2D_Map, boolean isOneConnectedComponent){
        int xAxis_length = rectangle_2D_Map.length;
        int yAxis_length = rectangle_2D_Map[0].length;
        GraphMapVertex[][] locations = new GraphMapVertex[xAxis_length][yAxis_length]; //rectangle map
        int serialID = 0;
        //generate all locations
        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {
                if(rectangle_2D_Map[xIndex][yIndex] == Enum_MapLocationType.EMPTY){
                    locations[xIndex][yIndex] = new GraphMapVertex(rectangle_2D_Map[xIndex][yIndex], new Coordinate_2D(xIndex, yIndex), serialID++);
                }
            }
        }
        HashMap<I_Coordinate, GraphMapVertex> allLocations = new HashMap<>(); //to be used for GraphMap constructor
        //connect locations to their neighbors (4-connected)
        ArrayList<GraphMapVertex> neighbors = new ArrayList<>(4);
        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {
                GraphMapVertex currentLocation = locations[xIndex][yIndex];
                if(locations[xIndex][yIndex] != null){
                    neighbors.clear();
                    //look for WEST neighbor
                    if(xIndex-1 >= 0 && locations[xIndex-1][yIndex] != null){neighbors.add(locations[xIndex-1][yIndex]);}
                    //look for EAST neighbor
                    if(xIndex+1 < xAxis_length && locations[xIndex+1][yIndex] != null){neighbors.add(locations[xIndex+1][yIndex]);}
                    //look for NORTH neighbor
                    if(yIndex-1 >= 0 && locations[xIndex][yIndex-1] != null){neighbors.add(locations[xIndex][yIndex-1]);}
                    //look for SOUTH neighbor
                    if(yIndex+1 < yAxis_length && locations[xIndex][yIndex+1] != null){neighbors.add(locations[xIndex][yIndex+1]);}
                    // set location neighbors
                    currentLocation.setNeighbors(neighbors.toArray(new GraphMapVertex[0]));
                    // add to allLocations
                    allLocations.put(currentLocation.coordinate, currentLocation);
                }
            }
        }
        return new GraphBasedGridMap(locations, allLocations, isOneConnectedComponent);
    }

    /**
     * Generates a new 4-connected {@link GraphMap} from a square, 2D grid.
     * Simple - Only 2 {@link Enum_MapLocationType location types} exist, {@link Enum_MapLocationType#EMPTY} and
     * {@link Enum_MapLocationType#WALL}. {@link Enum_MapLocationType#EMPTY} locations are passable, and can only connect to other
     * {@link Enum_MapLocationType#EMPTY} locations. {@link Enum_MapLocationType#WALL} locations are impassable, and can not connect to
     * any other location, so they will not be generated.
     * @param rectangle_2D_Map A rectangle grid representing a map, containing only {@link Enum_MapLocationType#EMPTY} and
     *                      {@link Enum_MapLocationType#WALL}. The length of its first dimension should correspond to the
     *                         original map's x dimension.
     * @return a new 4-connected {@link GraphMap}.
     */
    public static GraphBasedGridMap newSimple4Connected2D_GraphMap(Enum_MapLocationType[][] rectangle_2D_Map){
        return newSimple4Connected2D_GraphMap(rectangle_2D_Map, true);
    }

    /* nicetohave - 8 connected 2D map
    public static GraphMap newSimple8Connected2D_GraphMap(Enum_MapLocationType[][] map_2D){
        return null;
    }
    */

    /* nicetohave - 3D map
    public static GraphMap newSimple6Connected3D_GraphMap(Enum_MapLocationType[][] map_2D){
        return null;
    }
    */

    /**
     * Create a {@link GraphMap} with any arbitrary shape or dimensionality.
     *
     * @param coordinatesAdjacencyLists maps from every vertex to a list of (directed) edges coming out of it.
     * @param coordinatesEdgeWeights    maps from every vertex to a list of edges weights of its edges.
     * @param coordinatesLocationType  maps from every vertex to its location type.
     * @param coordinatesLocationSubtypes maps from every vertex to its location subtypes.
     * @return a {@link GraphMap}.
     */
    public static GraphMap newArbitraryGraphMap(Map<? extends I_Coordinate, ? extends List<? extends I_Coordinate>> coordinatesAdjacencyLists,
                                                Map<? extends I_Coordinate, List<Integer>> coordinatesEdgeWeights,
                                                Map<? extends I_Coordinate, Enum_MapLocationType> coordinatesLocationType,
                                                boolean isStronglyConnected,
                                                @Nullable Map<? extends I_Coordinate, List<String>> coordinatesLocationSubtypes){
        HashMap<I_Coordinate, GraphMapVertex> allLocations = new HashMap<>(coordinatesAdjacencyLists.size());
        MutableInt serialID = new MutableInt(0);

        for (I_Coordinate coordinateCurrentVertex: coordinatesAdjacencyLists.keySet()){
            GraphMapVertex currentVertex = getOrGenerateVertex(allLocations, coordinateCurrentVertex, coordinatesLocationType, coordinatesLocationSubtypes, serialID);

            List<? extends I_Coordinate> coordinateNeighbors = coordinatesAdjacencyLists.get(coordinateCurrentVertex);
            List<Integer> edgeWeights = coordinatesEdgeWeights.get(coordinateCurrentVertex);

            GraphMapVertex[] neighbors = new GraphMapVertex[coordinateNeighbors.size()];
            for (int i = 0; i < neighbors.length; i++) {
                I_Coordinate neighborCoordinate = coordinateNeighbors.get(i);
                GraphMapVertex neighbor = getOrGenerateVertex(allLocations, neighborCoordinate, coordinatesLocationType, coordinatesLocationSubtypes, serialID);
                neighbors[i] = neighbor;
            }

            currentVertex.setNeighbors(neighbors, edgeWeights.toArray(Integer[]::new));
        }

        return new GraphMap(allLocations, isStronglyConnected);
    }

    private static GraphMapVertex getOrGenerateVertex(Map<I_Coordinate, GraphMapVertex> allLocations, I_Coordinate coordinate,
                                                      Map<? extends I_Coordinate, Enum_MapLocationType> coordinatesLocationType,
                                                      @Nullable Map<? extends I_Coordinate, List<String>> coordinatesLocationSubtypes,
                                                      @NotNull MutableInt serialID){
        GraphMapVertex vertex = allLocations.get(coordinate);
        if (vertex == null) {
            vertex = new GraphMapVertex(coordinatesLocationType != null ? coordinatesLocationType.get(coordinate) : Enum_MapLocationType.EMPTY,
                    coordinate,
                    coordinatesLocationSubtypes != null ? coordinatesLocationSubtypes.get(coordinate) : null,
                    serialID.getAndIncrement());
            allLocations.put(coordinate, vertex);
        }
        return vertex;
    }

}
