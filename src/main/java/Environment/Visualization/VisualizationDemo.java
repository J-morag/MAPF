package Environment.Visualization;

import java.util.Arrays;
import java.util.List;

public class VisualizationDemo {

    public static void main(String[] args) {
        char[][] grid1 = {
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'C', 'C', 'C', 'C', 'C', 'C'},
                {'C', 'C', 'C', 'C', 'C', 'C'},
                {'C', 'C', 'C', 'C', 'C', 'C'}
        };

        char[][] grid2 = {
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'C', 'C', 'C', 'C', 'C', 'C'},
                {'C', 'C', 'C', 'C', 'C', 'C'},
                {'B', 'B', 'B', 'A', 'A', 'A'},
                {'B', 'B', 'B', 'A', 'A', 'A'}
        };

        char[][] grid3 = {
                {'C', 'C', 'C', 'C', 'C', 'C'},
                {'C', 'C', 'C', 'C', 'C', 'C'},
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'A', 'A', 'A', 'B', 'B', 'B'},
                {'B', 'B', 'B', 'A', 'A', 'A'},
                {'B', 'B', 'B', 'A', 'A', 'A'}
        };

        List<char[][]> grids = Arrays.asList(grid1, grid2, grid3);

        GridVisualizer.visualize(grids, 250);
    }

}