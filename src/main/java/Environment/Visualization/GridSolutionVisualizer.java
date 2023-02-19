package Environment.Visualization;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_GridMap;
import BasicMAPF.Solvers.Solution;
import LifelongMAPF.LifelongSolution;

import java.util.ArrayList;
import java.util.List;

public class GridSolutionVisualizer {

    public static void visualizeSolution(MAPF_Instance instance, Solution solution) throws IllegalArgumentException {
        if (! (instance.map instanceof I_GridMap map)){
            throw new IllegalArgumentException("SolutionVisualizer can only visualize grid maps");
        }

        List<char[][]> grids = new ArrayList<>();
        for (int time = 0; time < solution.endTime(); time++) {
            char[][] grid = new char[map.getWidth()][map.getHeight()];
            for (int y = 0; y < map.getHeight(); y++) {
                for (int x = 0; x < map.getWidth(); x++) {
                    if (map.isObstacle(x, y)) {
                        grid[x][y] = 'o';
                    }else if (map.isFree(x, y)) {
                        grid[x][y] = 'f';
                    }
                }
            }
            for (Agent agent : instance.agents) {
                int[] xy = map.getXY(solution.getAgentLocation(agent, time));
                if (map.isObstacle(xy)) {
                    throw new IllegalArgumentException(String.format("Agent %s is on an obstacle", agent));
                }
                grid[xy[0]][xy[1]] = time > 0 && solution.getPlanFor(agent).getEndTime() <= time ? 'g': 'a';
                if (solution instanceof LifelongSolution lifelongSolution){
                    if (lifelongSolution.agentAchievedAWaypointAtTime(agent, time)){
                        grid[xy[0]][xy[1]] = 'g';
                    }
                }
            }
            grids.add(grid);
        }
        GridVisualizer.visualize(grids, 250);
    }

}