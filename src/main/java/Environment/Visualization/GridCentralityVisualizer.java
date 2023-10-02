package Environment.Visualization;

import BasicMAPF.Instances.Maps.Coordinates.MillimetricCoordinate_2D;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_GridMap;
import BasicMAPF.Instances.Maps.I_Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;
import org.jgrapht.alg.scoring.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Environment.Visualization.GridSolutionVisualizer.paintObstaclesAndFreeCells;
import static Environment.Visualization.MillimetricCoordinatesGraphSolutionVisualizer.*;

public class GridCentralityVisualizer {

    public static void visualizeCentrality(I_GridMap map, Map<I_Location, Double> locationCentrality, String title) {
        Color[][] grid = new Color[map.getWidth()][map.getHeight()];
        paintObstaclesAndFreeCells(map, grid);

        NormalizationValues normalizationValues = getNormalizationValues(locationCentrality);

        for (I_Location location : locationCentrality.keySet()) {
            int[] xy = map.getXY(location);
            Color color = getCentralityColor(locationCentrality, location, normalizationValues.negativeCentralityCorrection(), normalizationValues.maxCentrality());
            grid[xy[0]][xy[1]] = color;
        }

        List<Color[][]> grids = new ArrayList<>();
        grids.add(grid);
        GridVisualizer.visualize(grids, null, 250, title);
    }

    @NotNull
    private static NormalizationValues getNormalizationValues(Map<I_Location, Double> locationCentrality) {
        double minCentrality = Collections.min(locationCentrality.values());
        // may get negative values for some centrality measures
        double negativeCentralityCorrection = minCentrality < 0 ? -minCentrality : 0;
        double maxCentrality = Collections.max(locationCentrality.values()) + negativeCentralityCorrection;
        return new NormalizationValues(negativeCentralityCorrection, maxCentrality);
    }

    private record NormalizationValues(double negativeCentralityCorrection, double maxCentrality) {
    }

    @NotNull
    private static Color getCentralityColor(Map<I_Location, Double> locationCentrality, I_Location location, double negativeCentralityCorrection, double maxCentrality) {
        return new Color(255, 0, 0, (int) (((negativeCentralityCorrection + locationCentrality.get(location)) / maxCentrality) * 255));
    }

    public static void visualizeCentralityMillimetricCoordinate(I_ExplicitMap map, Map<I_Location, Double> locationCentrality, String title) {
        MillimetricCoordinatesGraphSolutionVisualizer.FlattenedMap flattenedMap = flattenedGraph(map);
        Color[][] grid = new Color[flattenedMap.width()][flattenedMap.height()];

        NormalizationValues normalizationValues = getNormalizationValues(locationCentrality);

        paintVertices(flattenedMap, grid);

        for (I_Location location : locationCentrality.keySet()) {
            // paint around this vertex with the centrality value
            Color color = getCentralityColor(locationCentrality, location, normalizationValues.negativeCentralityCorrection(), normalizationValues.maxCentrality());
            if (! (location.getCoordinate() instanceof MillimetricCoordinate_2D mmCoord))
                throw new IllegalArgumentException("MillimetricCoordinatesGraphSolutionVisualizer can only be used with maps that use MillimetricCoordinate_2D coordinates.");
            List<Integer> xy = flattenedMap.mmCoordinatesToVisCoordinates().get(mmCoord);
            paintAroundCoordinate(flattenedMap, grid, new int[]{xy.get(0), xy.get(1)}, color);
        }

        List<Color[][]> grids = new ArrayList<>();
        grids.add(grid);
        GridVisualizer.visualize(grids, null, 250, title);
    }

    public static void computeEigenvectorCentralityAndVisualize(I_ExplicitMap map, @Nullable String mapName){
        if (mapName == null) mapName = "";
        List<VertexScoringAlgorithm<I_Location, Double>> centralieites = new ArrayList<>();
        centralieites.add(new PageRank<>(map.getJGraphTRepresentation()));
        centralieites.add(new EigenvectorCentrality<>(map.getJGraphTRepresentation()));
        centralieites.add(new KatzCentrality<>(map.getJGraphTRepresentation()));
        centralieites.add(new BetweennessCentrality<>(map.getJGraphTRepresentation(), true));
        centralieites.add(new ClosenessCentrality<>(map.getJGraphTRepresentation()));
        for (VertexScoringAlgorithm<I_Location, Double> centrality : centralieites) {
            String title = "%s %s".formatted(mapName, centrality.getClass().getSimpleName());
            System.out.println("calculating " + title +  ". Time now is: "
                    + new Date());
            Map<I_Location, Double> locationCentrality = centrality.getScores();
            System.out.println("calculated " + title + ". Time now is: " + new Date());
            if (map instanceof I_GridMap)
                visualizeCentrality((I_GridMap) map, locationCentrality, title);
            else visualizeCentralityMillimetricCoordinate(map, locationCentrality, title);
        }
    }
}