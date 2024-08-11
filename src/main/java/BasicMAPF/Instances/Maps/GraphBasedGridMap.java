package BasicMAPF.Instances.Maps;

import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import Environment.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GraphBasedGridMap extends GraphMap implements I_GridMap {

    public final int height;
    public final int width;
    public final GraphMapVertex[][] locationsGrid;

    GraphBasedGridMap(GraphMapVertex[][] locationsGrid, Map<I_Coordinate, GraphMapVertex> allGraphVertices) {
        this(locationsGrid, allGraphVertices, null);
    }

    GraphBasedGridMap(@NotNull GraphMapVertex[][] locationsGrid, Map<I_Coordinate, GraphMapVertex> allGraphVertices, Boolean isStronglyConnected) {
        super(allGraphVertices, isStronglyConnected);
        if (locationsGrid.length == 0 || locationsGrid[0].length == 0 )
            throw new IllegalArgumentException("Grid must have at least one row and one column.");
        this.width = locationsGrid.length;
        this.height = locationsGrid[0].length;
        if (Config.DEBUG >= 2) {
            verifyGridAndGraphAreEqual(locationsGrid, allGraphVertices);
            // verify all coordinates are 2D
            for (GraphMapVertex[] row : locationsGrid) {
                for (GraphMapVertex location : row) {
                    if (location != null && !(location.getCoordinate() instanceof Coordinate_2D))
                        throw new IllegalArgumentException("Location must be 2D.");
                }
            }
        }
        this.locationsGrid = locationsGrid;
    }

    /**
     * Verifies that the grid and graph representations of the map are consistent.
     * @param locationsGrid the grid representation of the map.
     * @param allGraphVertices the graph representation of the map.
     */
    private void verifyGridAndGraphAreEqual(I_Location[][] locationsGrid, Map<I_Coordinate, GraphMapVertex> allGraphVertices) {
        Set<I_Coordinate> graphCoordinates = allGraphVertices.keySet();
        Set<I_Coordinate> gridCoordinates = new HashSet<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                I_Location location = locationsGrid[x][y];
                if (location == null)
                    continue;
                else {
                    gridCoordinates.add(location.getCoordinate());
                }
                GraphMapVertex vertex = allGraphVertices.get(location.getCoordinate());
                List<I_Location> neighbours = vertex.outgoingEdges();
                if (x > 0) {
                    I_Location leftNeighbour = locationsGrid[x - 1][y];
                    if (leftNeighbour != null && ! neighbours.contains(leftNeighbour)) {
                        throw new IllegalArgumentException(String.format("Graph and grid neighbours do not match. Grid location %s is not a neighbour of graph location %s", leftNeighbour, location));
                    }
                }
                if (x < width - 1) {
                    I_Location rightNeighbour = locationsGrid[x + 1][y];
                    if (rightNeighbour != null && ! neighbours.contains(rightNeighbour)) {
                        throw new IllegalArgumentException(String.format("Graph and grid neighbours do not match. Grid location %s is not a neighbour of graph location %s", rightNeighbour, location));
                    }
                }
                if (y > 0) {
                    I_Location topNeighbour = locationsGrid[x][y - 1];
                    if (topNeighbour != null && ! neighbours.contains(topNeighbour)) {
                        throw new IllegalArgumentException(String.format("Graph and grid neighbours do not match. Grid location %s is not a neighbour of graph location %s", topNeighbour, location));
                    }
                }
                if (y < height - 1) {
                    I_Location bottomNeighbour = locationsGrid[x][y + 1];
                    if (bottomNeighbour != null && ! neighbours.contains(bottomNeighbour)) {
                        throw new IllegalArgumentException(String.format("Graph and grid neighbours do not match. Grid location %s is not a neighbour of graph location %s", bottomNeighbour, location));
                    }
                }
            }
        }
        if (! graphCoordinates.equals(gridCoordinates)) {
            throw new IllegalArgumentException("Graph and grid coordinates do not match.");
        }
    }

    @Override
    public @Nullable GraphMapVertex getMapLocation(@NotNull I_Coordinate coordinate) {
        return locationsGrid[((Coordinate_2D)coordinate).x_value][((Coordinate_2D)coordinate).y_value];
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public boolean isObstacle(int x, int y) {
        return locationsGrid[x][y] == null;
    }

    @Override
    public boolean isObstacle(int[] xy) {
        return locationsGrid[xy[0]][xy[1]] == null;
    }

    @Override
    public boolean isFree(int x, int y) {
        return ! isObstacle(x, y);
    }

    @Override
    public boolean isFree(int[] xy) {
        return ! isObstacle(xy);
    }

    @Override
    public boolean isOnMap(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Override
    public boolean isOnMap(int[] xy) {
        return isOnMap(xy[0], xy[1]);
    }

    @Override
    public int[] getXY(I_Location location) {
        if (! (location.getCoordinate() instanceof Coordinate_2D coordinate))
            throw new IllegalArgumentException("Location must be 2D.");
        return new int[]{coordinate.x_value, coordinate.y_value};
    }
}