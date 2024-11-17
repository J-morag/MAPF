package TransientMAPF;

import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import Environment.Visualization.GridCentralityVisualizer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class SeparatingVerticesFinderTest {

    private final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final Enum_MapLocationType w = Enum_MapLocationType.WALL;

    private final Enum_MapLocationType[][] mapWithTShapedObstacle = {
            {e, e, e, e, e},
            {e, w, w, w, e},
            {e, e, w, e, e},
            {e, e, w, e, e},
            {e, e, w, e, e}
    };
    private final I_ExplicitMap exampleWithTShapedObstacle = MapFactory.newSimple4Connected2D_GraphMap(mapWithTShapedObstacle);

    private final Enum_MapLocationType[][] mapWithDiagonalObstacle = {
            {e, e, e, e, e},
            {e, w, e, e, e},
            {e, e, w, e, e},
            {e, e, e, w, e},
            {e, e, e, e, w}
    };
    private final I_ExplicitMap exampleWithDiagonalObstacle = MapFactory.newSimple4Connected2D_GraphMap(mapWithDiagonalObstacle);

    private final Enum_MapLocationType[][] mapWithArbitraryObstacles = {
            {e, e, e, e, e},
            {w, w, e, w, w},
            {e, e, e, e, e},
            {e, w, e, w, w},
            {e, e, e, e, e}
    };
    private final I_ExplicitMap exampleWithArbitraryObstacles = MapFactory.newSimple4Connected2D_GraphMap(mapWithArbitraryObstacles);

    private final Enum_MapLocationType[][] mapWithObstaclesInTheCenter = {
            {e, e, e, e, e},
            {e, w, e, w, e},
            {e, e, w, e, e},
            {e, w, e, w, e},
            {e, e, e, e, e}
    };
    private final I_ExplicitMap exampleWithObstaclesInTheCenter = MapFactory.newSimple4Connected2D_GraphMap(mapWithObstaclesInTheCenter);

    @Test
    public void testSVMapWithTShapedObstacle() {
        Set<I_Coordinate> SVsCoordinates = new HashSet<>(Arrays.asList(coor00, coor01, coor02, coor03, coor04, coor10, coor14, coor20, coor24));
        Set<I_Location> separatingVertices = SeparatingVerticesFinder.findSeparatingVertices(exampleWithTShapedObstacle);
        assertEquals(SVsCoordinates.size(), separatingVertices.size());
        for (I_Location location : separatingVertices) {
            assertTrue(SVsCoordinates.contains(location.getCoordinate()));
        }
        printMapWithSeparatingVertices((I_GridMap) exampleWithTShapedObstacle, separatingVertices);
    }

    @Test
    public void testSVMapWithDiagonalObstacle() {
        Set<I_Coordinate> SVsCoordinates = new HashSet<>(Arrays.asList(coor00, coor01, coor02, coor10, coor20, coor24, coor42));
        Set<I_Location> separatingVertices = SeparatingVerticesFinder.findSeparatingVertices(exampleWithDiagonalObstacle);
        assertEquals(SVsCoordinates.size(), separatingVertices.size());
        for (I_Location location : separatingVertices) {
            assertTrue(SVsCoordinates.contains(location.getCoordinate()));
        }
        printMapWithSeparatingVertices((I_GridMap) exampleWithDiagonalObstacle, separatingVertices);
    }

    @Test
    public void testSVMapWithArbitraryObstacles() {
        Set<I_Coordinate> SVsCoordinates = new HashSet<>(Arrays.asList(coor01, coor02, coor03, coor12, coor22, coor23, coor42, coor43));
        Set<I_Location> separatingVertices = SeparatingVerticesFinder.findSeparatingVertices(exampleWithArbitraryObstacles);
        assertEquals(SVsCoordinates.size(), separatingVertices.size());
        for (I_Location location : separatingVertices) {
            assertTrue(SVsCoordinates.contains(location.getCoordinate()));
        }
        printMapWithSeparatingVertices((I_GridMap) exampleWithArbitraryObstacles, separatingVertices);
    }

    @Test
    public void testSVMapWithObstaclesInTheCenter() {
        Set<I_Coordinate> SVsCoordinates = new HashSet<>(Arrays.asList(coor02, coor42, coor20, coor24));
        Set<I_Location> separatingVertices = SeparatingVerticesFinder.findSeparatingVertices(exampleWithObstaclesInTheCenter);
        assertEquals(SVsCoordinates.size(), separatingVertices.size());
        for (I_Location location : separatingVertices) {
            assertTrue(SVsCoordinates.contains(location.getCoordinate()));
        }
        printMapWithSeparatingVertices((I_GridMap) exampleWithObstaclesInTheCenter, separatingVertices);
    }

    public static void printMapWithSeparatingVertices(I_GridMap map, Set<I_Location> separatingVertices) {
        int width = map.getWidth();
        int height = map.getHeight();

        // Create a grid to store the visualization characters
        char[][] visualGrid = new char[width][height];
        // Fill the grid with initial representation
        GraphMapVertex[][] locationsGrid = ((GraphBasedGridMap) map).locationsGrid;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                GraphMapVertex vertex = locationsGrid[x][y];
                if (vertex == null) {
                    visualGrid[x][y] = '#';  // Obstacle
                } else if (separatingVertices.contains(vertex)) {
                    visualGrid[x][y] = 'S';  // Separating vertex
                } else {
                    visualGrid[x][y] = 'O';  // Non-separating vertex
                }
            }
        }

        // Print the grid
        System.out.println("Visualizing the grid with separating vertices:");
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                System.out.print(visualGrid[x][y] + " ");
            }
            System.out.println();
        }
    }
}
