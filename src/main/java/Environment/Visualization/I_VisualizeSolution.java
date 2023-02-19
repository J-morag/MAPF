package Environment.Visualization;

import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.Solution;

public interface I_VisualizeSolution {
    void visualizeSolution(MAPF_Instance instance, Solution solution, String title) throws IllegalArgumentException;
}
