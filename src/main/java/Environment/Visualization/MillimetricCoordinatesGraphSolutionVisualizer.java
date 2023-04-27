package Environment.Visualization;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.MillimetricCoordinate_2D;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.Solution;

import java.util.*;

public class MillimetricCoordinatesGraphSolutionVisualizer {

    private static final int MM_RESOLUTION = 70;
    private static final boolean paintOverVertices = true;

    public static void visualizeSolution(MAPF_Instance instance, Solution solution, String title) throws IllegalArgumentException {
        if (!(instance.map instanceof I_ExplicitMap map)) {
            throw new IllegalArgumentException("GridLikeGraphSolutionVisualizer can only visualize explicit maps");
        }

        int width = 0;
        int height = 0;
        Set<List<Integer>> coordinatesOnGraph = new HashSet<>();
        for (I_Location location : map.getAllLocations() ) {
            if (!(location.getCoordinate() instanceof MillimetricCoordinate_2D)) {
                throw new IllegalArgumentException("GridLikeGraphSolutionVisualizer can only be used with maps that use MillimetricCoordinate_2D coordinates.");
            }
            int x = ((MillimetricCoordinate_2D) location.getCoordinate()).x_value / MM_RESOLUTION;
            int y = ((MillimetricCoordinate_2D) location.getCoordinate()).y_value / MM_RESOLUTION;
            coordinatesOnGraph.add(Arrays.asList(x, y));
            width = Math.max(width, x);
            height = Math.max(height, y);
        }

        List<char[][]> grids = new ArrayList<>();
        List<Integer> finishedGoals = new ArrayList<>();
        Set<Agent> sumFinishedAgents = new HashSet<>();
        for (int time = 0; time < solution.endTime(); time++) {
            char[][] grid = new char[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (coordinatesOnGraph.contains(Arrays.asList(x, y))) {
                        grid[x][y] = 'o';
                    } else {
                        grid[x][y] = 'f';
                    }
                }
            }
            for (Agent agent : instance.agents) {
                MillimetricCoordinate_2D mmCoord = (MillimetricCoordinate_2D) solution.getAgentLocation(agent, time).getCoordinate();
                int[] xy = new int[]{mmCoord.x_value / MM_RESOLUTION, mmCoord.y_value / MM_RESOLUTION};
                boolean atGoal = solution.getPlanFor(agent).getEndTime() <= time;
                char agentPaintColor = 'a';
                if (atGoal) {
                    agentPaintColor = 'g';
                    sumFinishedAgents.add(agent);
                }
                // paint the agent with some volume
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        if (xy[0] + i >= 0 && xy[0] + i < width && xy[1] + j >= 0 && xy[1] + j < height) {
//                            if (grid[xy[0] + i][xy[1] + j] == 'a' || grid[xy[0] + i][xy[1] + j] == 'g'){ // TODO uncomment when scenarios are fixed to avoid shared source and goal and verify that coordinates spacing is sufficient
//                                throw new IllegalArgumentException("Agent " + agent + " is overlapping with another agent at time " + time + " at location " + solution.getAgentLocation(agent, time) + " (x=" + xy[0] + ", y=" + xy[1] + ")" + "solution: " + solution);
//                            }
                            if (paintOverVertices || grid[xy[0] + i][xy[1] + j] == 'f') {
                                grid[xy[0] + i][xy[1] + j] = agentPaintColor;
                            }
                        }
                    }
                }
            }
            grids.add(grid);
            finishedGoals.add(sumFinishedAgents.size());
        }
        GridVisualizer.visualize(grids, finishedGoals, 250, title);
    }

}
