package Environment.Visualization;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_GridMap;
import BasicMAPF.DataTypesAndStructures.Solution;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GridSolutionVisualizer {

    public static void visualizeSolution(MAPF_Instance instance, Solution solution, String title) throws IllegalArgumentException {
        if (!(instance.map instanceof I_GridMap map)) {
            throw new IllegalArgumentException("SolutionVisualizer can only visualize grid maps");
        }

        List<Color[][]> grids = new ArrayList<>();
        List<Integer> finishedGoals = new ArrayList<>();
        Set<Agent> sumFinishedAgents = new HashSet<>();

        // Determine the range of Agent IDs
        int minId = instance.agents.stream().mapToInt(agent -> agent.iD).min().orElse(0);
        int maxId = instance.agents.stream().mapToInt(agent -> agent.iD).max().orElse(0);
        int idRange = Math.max(1, maxId - minId); // Prevent division by zero

        // Map Agent IDs to colors (warmest to coldest)
        Map<Integer, Color> agentColorMap = new HashMap<>();
        for (Agent agent : instance.agents) {
            float normalizedId = (float) (agent.iD - minId) / idRange; // Normalize ID to [0, 1]
            float hue = 0.67f * normalizedId; // Map to hue range [0 (red), 0.67 (blue)]
            agentColorMap.put(agent.iD, Color.getHSBColor(hue, 0.8f, 0.8f)); // Vivid colors
        }

        // Iterate until and including the final time step
        for (int time = 0; time <= solution.endTime(); time++) {
            Color[][] grid = new Color[map.getWidth()][map.getHeight()];
            paintObstaclesAndFreeCells(map, grid);

            for (Agent agent : instance.agents) {
                int[] xy = map.getXY(solution.getAgentLocation(agent, time));
                if (map.isObstacle(xy)) {
                    throw new IllegalArgumentException(String.format("Agent %s is on an obstacle", agent));
                }
                boolean atGoal = solution.getPlanFor(agent).getEndTime() <= time;
                if (atGoal) {
                    // Get the agent's original color
                    Color originalColor = agentColorMap.get(agent.iD);

                    // Convert RGB to HSB
                    float[] hsbValues = Color.RGBtoHSB(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), null);

                    // Reduce saturation by 50% (or clamp to minimum 0.1 to avoid fully greyscale)
                    float dimmedSaturation = Math.max(0.1f, hsbValues[1] * 0.5f);

                    // Create a new color with lowered saturation
                    grid[xy[0]][xy[1]] = Color.getHSBColor(hsbValues[0], dimmedSaturation, hsbValues[2]);
                    sumFinishedAgents.add(agent);
                } else {
                    grid[xy[0]][xy[1]] = agentColorMap.get(agent.iD); // Assign color based on ID
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