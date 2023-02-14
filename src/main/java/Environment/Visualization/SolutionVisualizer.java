package Environment.Visualization;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_PlanarMap;
import BasicMAPF.Solvers.Solution;

import java.util.ArrayList;
import java.util.List;

public class SolutionVisualizer {

    public static void visualizeSolution(MAPF_Instance instance, Solution solution) {
        if (! (instance.map instanceof I_PlanarMap map)){
            throw new IllegalArgumentException("SolutionVisualizer can only visualize planar maps");
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
                grid[xy[0]][xy[1]] = solution.getPlanFor(agent).getEndTime() > time ? 'a': 'g';
            }
            grids.add(grid);
        }
        GridVisualizer.visualize(grids, 500);
    }

}