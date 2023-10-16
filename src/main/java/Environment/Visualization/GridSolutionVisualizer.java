package Environment.Visualization;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_GridMap;
import BasicMAPF.DataTypesAndStructures.Solution;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GridSolutionVisualizer {

    public static void visualizeSolution(MAPF_Instance instance, Solution solution, String title) throws IllegalArgumentException {
        if (!(instance.map instanceof I_GridMap map)) {
            throw new IllegalArgumentException("SolutionVisualizer can only visualize grid maps");
        }

        List<Color[][]> grids = new ArrayList<>();
        List<Integer> finishedGoals = new ArrayList<>();
        Set<Agent> sumFinishedAgents = new HashSet<>();
        for (int time = 0; time < solution.endTime(); time++) {
            Color[][] grid = new Color[map.getWidth()][map.getHeight()];
            paintObstaclesAndFreeCells(map, grid);
            for (Agent agent : instance.agents) {
                int[] xy = map.getXY(solution.getAgentLocation(agent, time));
                if (map.isObstacle(xy)) {
                    throw new IllegalArgumentException(String.format("Agent %s is on an obstacle", agent));
                }
                boolean atGoal = solution.getPlanFor(agent).getEndTime() <= time;
                if (atGoal) {
                    grid[xy[0]][xy[1]] = Color.GREEN;
                    sumFinishedAgents.add(agent);
                } else {
                    grid[xy[0]][xy[1]] = Color.PINK;
                }
            }
            grids.add(grid);
            finishedGoals.add(sumFinishedAgents.size());
        }
        GridVisualizer.visualize(grids, finishedGoals, 250, title);
    }

    static void paintObstaclesAndFreeCells(I_GridMap map, Color[][] grid) {
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.isObstacle(x, y)) {
                    grid[x][y] = Color.BLACK;
                } else if (map.isFree(x, y)) {
                    grid[x][y] = Color.WHITE;
                }
            }
        }
    }
}