package Environment.Visualization;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class VisualizationDemo {

    public static void main(String[] args) {
        Color[][] grid1 = {
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN},
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN},
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN}
        };

        Color[][] grid2 = {
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN},
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN},
                {Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE},
                {Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE}
        };

        Color[][] grid3 = {
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN},
                {Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN, Color.GREEN},
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.WHITE, Color.WHITE, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK},
                {Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE},
                {Color.BLACK, Color.BLACK, Color.BLACK, Color.WHITE, Color.WHITE, Color.WHITE}
        };

        List<Color[][]> grids = Arrays.asList(grid1, grid2, grid3);

        GridVisualizer.visualize(grids, null, 250, "Visualization Demo");
    }

}