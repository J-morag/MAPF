package Environment.Visualization;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.MillimetricCoordinate_2D;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.DataTypesAndStructures.Solution;
import LifelongMAPF.LifelongSolution;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Environment.Visualization.GridSolutionVisualizer.isAtLastLocationInPlan;

public class MillimetricCoordinatesGraphSolutionVisualizer {
    // TODO DRY this class and GridSolutionVisualizer

    private static final int MM_RESOLUTION = 70;
    static final boolean PAINT_OVER_VERTICES = true;

    public static void visualizeSolution(MAPF_Instance instance, Solution solution, String title) throws IllegalArgumentException {
        if (!(instance.map instanceof I_ExplicitMap map)) {
            throw new IllegalArgumentException("MillimetricCoordinatesGraphSolutionVisualizer can only visualize explicit maps");
        }

        FlattenedMap flattenedGraph = flattenedGraph(map);

        List<Color[][]> grids = new ArrayList<>();
        List<Integer> finishedGoals = new ArrayList<>();
        Set<Agent> sumFinishedAgents = new HashSet<>();
        for (int time = 0; time < solution.endTime(); time++) {
            Color[][] grid = new Color[flattenedGraph.width()][flattenedGraph.height()];
            paintVertices(flattenedGraph, grid);
            paintAgents(instance, solution, time, sumFinishedAgents, flattenedGraph, grid);
            grids.add(grid);
            finishedGoals.add(sumFinishedAgents.size());
        }
        GridVisualizer.visualize(grids, finishedGoals, 250, title);
    }

    @NotNull
    static MillimetricCoordinatesGraphSolutionVisualizer.FlattenedMap flattenedGraph(I_ExplicitMap map) {
        int width = 0;
        int height = 0;
        Map<MillimetricCoordinate_2D, List<Integer>> mmCoordinatesToVisCoordinates = new HashMap<>();
        for (I_Location location : map.getAllLocations() ) {
            if (!(location.getCoordinate() instanceof MillimetricCoordinate_2D mmCoord)) {
                throw new IllegalArgumentException("MillimetricCoordinatesGraphSolutionVisualizer can only be used with maps that use MillimetricCoordinate_2D coordinates.");
            }
            int x = mmCoord.x_value / MM_RESOLUTION;
            int y = mmCoord.y_value / MM_RESOLUTION;
            mmCoordinatesToVisCoordinates.put(mmCoord, Arrays.asList(x, y));
            width = Math.max(width, x);
            height = Math.max(height, y);
        }
        return new FlattenedMap(width, height, mmCoordinatesToVisCoordinates);
    }

    static void paintVertices(FlattenedMap flattenedMap, Color[][] grid) {
        Set<List<Integer>> verticesCoordinatesOnViz = new HashSet<>(flattenedMap.mmCoordinatesToVisCoordinates().values());
        for (int y = 0; y < flattenedMap.height(); y++) {
            for (int x = 0; x < flattenedMap.width(); x++) {
                if (verticesCoordinatesOnViz.contains(Arrays.asList(x, y))) {
                    grid[x][y] = Color.BLACK;
                } else {
                    grid[x][y] = Color.WHITE;
                }
            }
        }
    }

    private static void paintAgents(MAPF_Instance instance, Solution solution, int time, Set<Agent> sumFinishedAgents, FlattenedMap flattenedMap, Color[][] grid) {
        for (Agent agent : instance.agents) {
            MillimetricCoordinate_2D mmCoord = (MillimetricCoordinate_2D) solution.getAgentLocation(agent, time).getCoordinate();
            int[] xy = new int[]{mmCoord.x_value / MM_RESOLUTION, mmCoord.y_value / MM_RESOLUTION};
            Color agentPaintColor = Color.PINK;
            if (solution instanceof LifelongSolution lifelongSolution){
                if (GridSolutionVisualizer.isAchievedWaypoint(time, agent, lifelongSolution)){
                    agentPaintColor = Color.GREEN;
                    sumFinishedWaypoints++;
                } else if (isAtLastLocationInPlan(solution, time, agent)) {
                    agentPaintColor = Color.RED;
                }
            }
            else {
                if (isAtLastLocationInPlan(solution, time, agent)) {
                    agentPaintColor = Color.GREEN;
                    sumFinishedAgents.add(agent);
                }
            }
            // paint the agent with some volume
            paintAroundCoordinate(flattenedMap, grid, xy, agentPaintColor);
        }
    }

    static void paintAroundCoordinate(FlattenedMap flattenedMap, Color[][] grid, int[] xy, Color color) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (xy[0] + i >= 0 && xy[0] + i < flattenedMap.width() && xy[1] + j >= 0 && xy[1] + j < flattenedMap.height()) {
                    if (PAINT_OVER_VERTICES || grid[xy[0] + i][xy[1] + j] == Color.WHITE) {
                        grid[xy[0] + i][xy[1] + j] = color;
                    }
                }
            }
        }
    }

    record FlattenedMap(int width, int height, Map<MillimetricCoordinate_2D, List<Integer>> mmCoordinatesToVisCoordinates) {
    }

}
